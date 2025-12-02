package mips;

import llvmir.value.Value;
import llvmir.value.instructions.Br;
import llvmir.value.instructions.Load;
import llvmir.value.instructions.Ret;
import llvmir.value.instructions.Store;
import llvmir.value.instructions.Alloca;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Function;
import llvmir.value.structure.Instruction;
import llvmir.value.structure.Module;
import llvmir.value.structure.Param;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LivenessAnalyzer {
    private Map<BasicBlock, Set<Value>> useMap = new HashMap<>();
    private Map<BasicBlock, Set<Value>> defMap = new HashMap<>();
    private Map<BasicBlock, Set<Value>> inMap = new HashMap<>();
    private Map<BasicBlock, Set<Value>> outMap = new HashMap<>();

    public void analyze(Module module) {
        for (Value function : module.getFunctions()) {
            analyzeFunction((Function) function);
        }
        analyzeFunction(module.getMainFunction());
    }

    private void analyzeFunction(Function function) {
        buildCFG(function);
        computeDefUse(function);
        computeInOut(function);
    }

    private void buildCFG(Function function) {
        for (BasicBlock bb : function.getBasicBlocks()) {
            bb.getPredecessors().clear();
            bb.getSuccessors().clear();
        }

        for (BasicBlock bb : function.getBasicBlocks()) {
            if (bb.getInstructions().isEmpty()) continue;
            Instruction lastInst = bb.getInstructions().get(bb.getInstructions().size() - 1);
            if (lastInst instanceof Br) {
                Br br = (Br) lastInst;
                if (br.getOperands().size() == 1) {
                    // Unconditional branch
                    BasicBlock target = (BasicBlock) br.getOperands().get(0);
                    bb.getSuccessors().add(target);
                    target.getPredecessors().add(bb);
                } else {
                    // Conditional branch
                    // Br(trueBlock, falseBlock, cond)
                    BasicBlock trueTarget = (BasicBlock) br.getOperands().get(0);
                    BasicBlock falseTarget = (BasicBlock) br.getOperands().get(1);
                    bb.getSuccessors().add(trueTarget);
                    trueTarget.getPredecessors().add(bb);
                    bb.getSuccessors().add(falseTarget);
                    falseTarget.getPredecessors().add(bb);
                }
            }
            // Ret instruction has no successors
        }
    }

    private void computeDefUse(Function function) {
        for (BasicBlock bb : function.getBasicBlocks()) {
            Set<Value> use = new HashSet<>();
            Set<Value> def = new HashSet<>();

            for (Instruction inst : bb.getInstructions()) {
                if (inst instanceof Store) {
                    Value ptr = inst.getOperands().get(1);
                    Value val = inst.getOperands().get(0);
                    
                    if ((val instanceof Instruction || val instanceof Param) && !def.contains(val)) {
                        use.add(val);
                    }
                    
                    if (ptr instanceof Alloca && !(((Alloca)ptr).getAllocType().isIntArrayType() || ((Alloca)ptr).getAllocType().isCharArrayType())) {
                        def.add(ptr);
                    } else {
                        if ((ptr instanceof Instruction || ptr instanceof Param) && !def.contains(ptr)) {
                            use.add(ptr);
                        }
                    }
                } else if (inst instanceof Load) {
                    Value ptr = inst.getOperands().get(0);
                    if (ptr instanceof Alloca && !(((Alloca)ptr).getAllocType().isIntArrayType() || ((Alloca)ptr).getAllocType().isCharArrayType())) {
                        if (!def.contains(ptr)) {
                            use.add(ptr);
                        }
                    } else {
                        if ((ptr instanceof Instruction || ptr instanceof Param) && !def.contains(ptr)) {
                            use.add(ptr);
                        }
                    }
                } else {
                    // Check operands (Uses)
                    for (Value operand : inst.getOperands()) {
                        if (operand instanceof Instruction || operand instanceof Param) {
                            if (!def.contains(operand)) {
                                use.add(operand);
                            }
                        }
                    }

                    // Check definition (Def)
                    // The instruction itself is a definition if it produces a value (not void)
                    if (inst.getType() != null && !inst.getType().isVoidType()) {
                        def.add(inst);
                    }
                }
            }

            useMap.put(bb, use);
            defMap.put(bb, def);
            inMap.put(bb, new HashSet<>());
            outMap.put(bb, new HashSet<>());
        }
    }

    private void computeInOut(Function function) {
        boolean changed = true;
        while (changed) {
            changed = false;
            // Traverse in reverse order (optional but faster convergence)
            for (int i = function.getBasicBlocks().size() - 1; i >= 0; i--) {
                BasicBlock bb = function.getBasicBlocks().get(i);
                
                // Out[B] = U (In[S]) for S in successors
                Set<Value> newOut = new HashSet<>();
                for (BasicBlock succ : bb.getSuccessors()) {
                    newOut.addAll(inMap.get(succ));
                }

                // In[B] = Use[B] U (Out[B] - Def[B])
                Set<Value> newIn = new HashSet<>(newOut);
                newIn.removeAll(defMap.get(bb));
                newIn.addAll(useMap.get(bb));

                if (!newIn.equals(inMap.get(bb)) || !newOut.equals(outMap.get(bb))) {
                    inMap.put(bb, newIn);
                    outMap.put(bb, newOut);
                    changed = true;
                }
            }
        }
    }

    public Set<Value> getIn(BasicBlock bb) {
        return inMap.getOrDefault(bb, new HashSet<>());
    }

    public Set<Value> getOut(BasicBlock bb) {
        return outMap.getOrDefault(bb, new HashSet<>());
    }
}
