package mips;

import llvmir.value.Value;
import llvmir.value.instructions.Alloca;
import llvmir.value.instructions.BinaryOp;
import llvmir.value.instructions.Br;
import llvmir.value.instructions.Call;
import llvmir.value.instructions.GetElementPtr;
import llvmir.value.instructions.Icmp;
import llvmir.value.instructions.Load;
import llvmir.value.instructions.Ret;
import llvmir.value.instructions.Store;
import llvmir.value.instructions.Zext;
import llvmir.value.instructions.Trunc;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Function;
import llvmir.value.structure.Instruction;
import llvmir.value.structure.Module;
import llvmir.value.structure.Param;
import mips.structure.MipsFunc;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.RegManager;
import mips.value.StackSlot;

import java.util.*;

public class GraphColoringAllocator {
    private final LivenessAnalyzer livenessAnalyzer;
    private final GenerationContext context;
    
    private Map<Value, Set<Value>> adjList = new HashMap<>();
    private Map<Value, Integer> degree = new HashMap<>();
    private Set<Value> nodes = new HashSet<>(); // Only non-precolored nodes to be colored
    private Stack<Value> selectStack = new Stack<>();
    private Set<Value> spilledNodes = new HashSet<>();
    
    // K = 16: $t0-$t7 (8) + $s0-$s7 (8)
    private static final int K = 16;
    private List<PhyReg> allocatableRegs = new ArrayList<>();

    public GraphColoringAllocator(GenerationContext context) {
        this.context = context;
        this.livenessAnalyzer = new LivenessAnalyzer();
        initializeRegs();
    }

    private void initializeRegs() {
        allocatableRegs.addAll(RegManager.ALLOCATABLE_REGS);
    }

    public void allocateRegisters(Module module) {
        livenessAnalyzer.analyze(module);
        
        for (Value function : module.getFunctions()) {
            allocateForFunction((Function) function);
        }
        allocateForFunction(module.getMainFunction());
    }

    private void allocateForFunction(Function function) {
        String funcName = function.getName().substring(1);
        MipsFunc mipsFunc = context.getMipsModule().getFunction(funcName);
        if (funcName.equals("main")) mipsFunc = context.getMipsModule().main;
        
        // Reset state
        adjList.clear();
        degree.clear();
        nodes.clear();
        selectStack.clear();
        spilledNodes.clear();
        preColoredInterference.clear();
        
        buildGraph(function);
        simplifyAndSpill();
        assignColors(mipsFunc);
    }

    private boolean isScalar(Alloca alloca) {
        return !(alloca.getAllocType().isIntArrayType() || alloca.getAllocType().isCharArrayType());
    }

    private void buildGraph(Function function) {
        // 1. Identify nodes (Instructions that produce values)
        for (BasicBlock bb : function.getBasicBlocks()) {
            for (Instruction inst : bb.getInstructions()) {
                if (inst.getType() != null && !inst.getType().isVoidType()) {
                    if (inst instanceof Alloca) {
                        if (isScalar((Alloca)inst)) {
                            nodes.add(inst);
                            adjList.put(inst, new HashSet<>());
                            degree.put(inst, 0);
                        }
                    } else {
                        nodes.add(inst);
                        adjList.put(inst, new HashSet<>());
                        degree.put(inst, 0);
                    }
                }
            }
        }
        
        // 2. Build edges based on liveness
        for (BasicBlock bb : function.getBasicBlocks()) {
            Set<Value> live = new HashSet<>(livenessAnalyzer.getOut(bb));
            
            List<Instruction> insts = bb.getInstructions();
            for (int i = insts.size() - 1; i >= 0; i--) {
                Instruction inst = insts.get(i);
                
                if (inst instanceof Store) {
                    Value ptr = inst.getOperands().get(1);
                    if (ptr instanceof Alloca && isScalar((Alloca)ptr)) {
                        for (Value liveVar : live) {
                            if (liveVar != ptr) {
                                addEdge(ptr, liveVar);
                            }
                        }
                        live.remove(ptr);
                        
                        Value val = inst.getOperands().get(0);
                        if (val instanceof Instruction || val instanceof Param) {
                            live.add(val);
                        }
                        continue;
                    }
                }
                
                if (inst instanceof Load) {
                    Value ptr = inst.getOperands().get(0);
                    if (ptr instanceof Alloca && isScalar((Alloca)ptr)) {
                        live.add(ptr);
                    }
                }
                
                if (inst.getType() != null && !inst.getType().isVoidType()) {
                    if (!(inst instanceof Alloca)) {
                        // Def interferes with LiveOut
                        for (Value liveVar : live) {
                            if (liveVar != inst) {
                                addEdge(inst, liveVar);
                            }
                        }
                        // Def is not live before definition
                        live.remove(inst);
                    }
                }
                
                // Handle Call interference: Live variables across call interfere with Caller-Saved Regs
                if (inst instanceof Call) {
                    for (Value liveVar : live) {
                        for (PhyReg callerSaved : RegManager.CALLER_SAVED_REGS) {
                            addInterferenceWithReg(liveVar, callerSaved);
                        }
                    }
                }
                
                // Uses become live
                for (Value operand : inst.getOperands()) {
                    if (operand instanceof Instruction || operand instanceof Param) {
                        live.add(operand);
                    }
                }
            }
        }
    }

    private void addInterferenceWithReg(Value u, PhyReg reg) {
        if (nodes.contains(u)) {
            // We can't add PhyReg to adjList directly if it expects Value keys.
            // But we can check if u is assigned to 'reg' during coloring.
            // Actually, the standard way is to have pre-colored nodes in the graph.
            // Since we don't have Value wrappers for all PhyRegs, we can just
            // add 'reg' to the 'forbidden' set of 'u'?
            // Or, we can treat this as: u cannot be assigned 'reg'.
            // Let's add a map of forbidden registers for each node?
            // Or better: add 'reg' to adjList? But adjList is Map<Value, Set<Value>>.
            // We need to change adjList to Map<Value, Set<Object>> or similar? No.
            
            // Hack: We can't easily add PhyReg to the graph structure defined as Set<Value>.
            // Alternative: When selecting colors, check if the color is caller-saved and if the node interferes with caller-saved.
            // But we need to know WHICH instruction interferes with WHICH register?
            // No, if it interferes with ALL caller-saved, we can just mark it.
            
            // Let's add a set of "interferingRegs" to each node?
            // No, let's just add a "forceCalleeSaved" flag?
            // If a variable is live across a call, it MUST NOT be in a caller-saved register.
            // So we can just restrict its valid colors.
            
            // Let's add a map: Map<Value, Set<PhyReg>> preColoredInterference
            if (!preColoredInterference.containsKey(u)) {
                preColoredInterference.put(u, new HashSet<>());
            }
            preColoredInterference.get(u).add(reg);
            degree.put(u, degree.get(u) + 1);
        }
    }

    private Map<Value, Set<PhyReg>> preColoredInterference = new HashMap<>();

    private void addEdge(Value u, Value v) {
        // We only care about edges connected to nodes we want to color.
        // u and v can be: Instruction (node), Param (pre-colored or stack), or other Value.
        
        boolean uIsNode = nodes.contains(u);
        boolean vIsNode = nodes.contains(v);
        
        if (uIsNode && vIsNode) {
            if (!adjList.get(u).contains(v)) {
                adjList.get(u).add(v);
                adjList.get(v).add(u);
                degree.put(u, degree.get(u) + 1);
                degree.put(v, degree.get(v) + 1);
            }
        } else if (uIsNode && isPreColored(v)) {
            // u interferes with pre-colored v
            if (!adjList.get(u).contains(v)) {
                adjList.get(u).add(v);
                degree.put(u, degree.get(u) + 1); // Optional: increasing degree for pre-colored interference
            }
        } else if (vIsNode && isPreColored(u)) {
            if (!adjList.get(v).contains(u)) {
                adjList.get(v).add(u);
                degree.put(v, degree.get(v) + 1);
            }
        }
    }
    
    private boolean isPreColored(Value v) {
        // PhyReg is not a Value, so we can't check instanceof PhyReg directly if v is strictly Value
        // But wait, addEdge takes Value u, Value v.
        // If we pass PhyReg to addEdge, it must be a Value?
        // PhyReg implements MipsOperand, not extends Value.
        // Ah, the problem is that addEdge expects Value, but we are passing PhyReg from RegManager.
        // We need to wrap PhyReg in a Value or change addEdge signature.
        // Or, we can just check if v.getRegister() is PhyReg.
        // But for the CallerSavedRegs loop, we are passing PhyReg directly.
        // We need a way to represent PhyReg as a node in the graph.
        // Actually, we don't need to add PhyReg to the graph as a node.
        // We just need to record that 'u' interferes with 'PhyReg'.
        return v.getRegister() instanceof PhyReg;
    }

    private void simplifyAndSpill() {
        // Working set of nodes to simplify
        Set<Value> activeNodes = new HashSet<>(nodes);
        
        while (!activeNodes.isEmpty()) {
            // Simplify: find node with degree < K
            Value nodeToSimplify = null;
            for (Value node : activeNodes) {
                if (degree.get(node) < K) {
                    nodeToSimplify = node;
                    break;
                }
            }
            
            if (nodeToSimplify != null) {
                selectStack.push(nodeToSimplify);
                activeNodes.remove(nodeToSimplify);
                removeNode(nodeToSimplify, activeNodes);
            } else {
                // Spill: pick node with max degree
                Value nodeToSpill = null;
                int maxDegree = -1;
                for (Value node : activeNodes) {
                    if (degree.get(node) > maxDegree) {
                        maxDegree = degree.get(node);
                        nodeToSpill = node;
                    }
                }
                
                if (nodeToSpill != null) {
                    // Optimistic coloring: push to stack anyway
                    selectStack.push(nodeToSpill);
                    activeNodes.remove(nodeToSpill);
                    removeNode(nodeToSpill, activeNodes);
                }
            }
        }
    }

    private void removeNode(Value node, Set<Value> activeNodes) {
        for (Value neighbor : adjList.get(node)) {
            if (activeNodes.contains(neighbor)) {
                degree.put(neighbor, degree.get(neighbor) - 1);
            }
        }
        // Note: We don't need to decrement degree for preColoredInterference neighbors
        // because they are not in activeNodes and we don't simplify them.
    }

    private void assignColors(MipsFunc mipsFunc) {
        while (!selectStack.isEmpty()) {
            Value node = selectStack.pop();
            Set<PhyReg> usedColors = new HashSet<>();
            
            for (Value neighbor : adjList.get(node)) {
                // Check neighbors
                // Neighbor could be:
                // 1. Pre-colored (Param)
                // 2. Already colored node (popped before this one)
                // 3. Uncolored node (still in stack? No, we process in reverse pop order)
                //    Wait, if neighbor is in stack, it's not colored yet.
                //    If neighbor was popped BEFORE this node, it is colored.
                
                MipsOperand reg = neighbor.getRegister();
                if (reg instanceof PhyReg) {
                    usedColors.add((PhyReg) reg);
                }
            }
            
            // Check pre-colored interference (e.g. Call clobbers)
            if (preColoredInterference.containsKey(node)) {
                usedColors.addAll(preColoredInterference.get(node));
            }
            
            PhyReg assigned = null;
            for (PhyReg reg : allocatableRegs) {
                if (!usedColors.contains(reg)) {
                    assigned = reg;
                    break;
                }
            }
            
            if (assigned != null) {
                node.setRegister(assigned);
                mipsFunc.addRegister(assigned);
            } else {
                // Actual spill
                spilledNodes.add(node);
                int offset = mipsFunc.getStackSize();
                node.setRegister(new StackSlot(offset));
                mipsFunc.setStackSize(offset + 4);
            }
        }
    }
}
