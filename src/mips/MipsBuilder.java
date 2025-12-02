package mips;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import llvmir.value.Value;
import llvmir.value.instructions.Alloca;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Function;
import llvmir.value.structure.GlobalVariable;
import llvmir.value.structure.Module;
import mips.structure.MipsBlock;
import mips.structure.MipsGV;
import mips.structure.MipsModule;
import mips.value.GlobalLabel;
import mips.value.RegManager;
import mips.value.StackSlot;

public class MipsBuilder {
    private BufferedWriter writer;
    private final MipsModule mipsModule;
    private final Module module;
    private final GenerationContext context;
    private final StackAnalyzer stackAnalyzer;
    private final RegisterAllocator registerAllocator;
    private final InstructionTranslator instructionTranslator;
    private final GraphColoringAllocator graphColoringAllocator;

    public MipsBuilder(MipsModule mipsModule, Module module) {
        this.mipsModule = mipsModule;
        this.module = module;
        this.context = new GenerationContext(mipsModule);
        this.stackAnalyzer = new StackAnalyzer();
        this.registerAllocator = new RegisterAllocator(context);
        this.instructionTranslator = new InstructionTranslator(context, registerAllocator);
        this.graphColoringAllocator = new GraphColoringAllocator(context);
    }

    public void buildMipsModule() {
        for (Value gv : module.getGlobalVariables()) {
            mipsModule.globalVariables.add(
                    new MipsGV(gv.getName().substring(1),
                            ((GlobalVariable) gv).getFirstOperand()));
            gv.setRegister(new GlobalLabel(gv.getName().substring(1)));
        }
        stackAnalyzer.analyze(module, mipsModule);
        
        graphColoringAllocator.allocateRegisters(module);

        for (Value function : module.getFunctions()) {
            buildMipsFunction((Function) function);
        }
        context.setMain(true);
        buildMipsFunction(module.getMainFunction());
    }

    private void buildMipsFunction(Function function) {
        String funcName = function.getName().substring(1);
        if (funcName.equals("main")) {
            context.setCurrFunc(mipsModule.main);
        } else {
            context.setCurrFunc(mipsModule.getFunction(funcName));
        }
        allocateArguments(function);
        
        // Pre-process Alloca instructions to assign stack slots
        for (BasicBlock bb : function.getBasicBlocks()) {
            for (Value inst : bb.getInstructions()) {
                if (inst instanceof Alloca) {
                    instructionTranslator.translate(inst);
                }
            }
        }

        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            context.getCurrFunc().blocks.add(
                    buildMipsBasicBlock(basicBlock)
            );
        }

        RegManager.releaseArgRegisters();
        RegManager.releaseTempRegisters(context.getCurrFunc());
        context.setStackOffset(0);
        context.setCurrFunc(null);
    }

    private void allocateArguments(Function function) {
        int argumentRegNum = RegManager.getArgRegCount();
        for (int i = 0; i < function.getParams().size(); i++) {
            if (i < argumentRegNum) {
                function.getParams().get(i).setRegister(RegManager.getArgRegister());
            } else {
                function.getParams().get(i).setRegister(new StackSlot(4 * i));
            }
        }
    }

    private MipsBlock buildMipsBasicBlock(BasicBlock bb) {
        MipsBlock mipsBlock = new MipsBlock(bb.getParent().getName().substring(1) + "_" + bb.getName());
        context.setCurrBlock(mipsBlock);

        for (int i = 0; i < bb.getInstructions().size(); i++) {
            Value instruction = bb.getInstructions().get(i);
            
            instructionTranslator.translate(instruction);
            // registerAllocator.tryToReleaseRegister(instruction, bb, i);
            
            if (isJumpInstruction(instruction)) {
                break;
            }
        }

        RegManager.releaseTempRegisters(context.getCurrFunc());
        context.setCurrBlock(null);
        return mipsBlock;
    }

    private boolean isJumpInstruction(Value instruction) {
        return instruction instanceof llvmir.value.instructions.Br || 
               instruction instanceof llvmir.value.instructions.Ret;
    }

    public void initBuffer(String outputFile) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    public void closeBuffer() throws IOException {
        writer.close();
    }

    public void showMips() throws IOException {
        buildMipsModule();
        writer.write(mipsModule.toString());
    }
}
