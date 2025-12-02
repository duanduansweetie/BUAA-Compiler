package mips;

import llvmir.value.Value;
import llvmir.value.instructions.Alloca;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Function;
import llvmir.value.structure.Module;
import mips.structure.MipsFunc;
import mips.structure.MipsModule;

public class StackAnalyzer {
    public void analyze(Module module, MipsModule mipsModule) {
        for (Value function : module.getFunctions()) {
            calculateStackSize((Function) function, mipsModule);
        }
        calculateStackSize(module.getMainFunction(), mipsModule);
    }

    private void calculateStackSize(Function function, MipsModule mipsModule) {
        int size = 4 + 18 * 4; // RA + Saved Registers
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Value instruction : basicBlock.getInstructions()) {
                if (instruction instanceof Alloca) {
                    size += ((Alloca) instruction).getAllocType().getSize();
                }
            }
        }
        String funcName = function.getName().substring(1);
        MipsFunc mipsFunc = new MipsFunc(funcName);
        mipsFunc.setStackSize(size);
        if (funcName.equals("main"))
            mipsModule.main = mipsFunc;
        else
            mipsModule.functions.add(mipsFunc);
    }
}
