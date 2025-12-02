package mips;

import mips.structure.MipsBlock;
import mips.structure.MipsFunc;
import mips.structure.MipsModule;

public class GenerationContext {
    private MipsFunc currFunc;
    private MipsBlock currBlock;
    private MipsModule mipsModule;
    private boolean isMain;
    private int stackOffset;

    public GenerationContext(MipsModule mipsModule) {
        this.mipsModule = mipsModule;
    }

    public MipsFunc getCurrFunc() {
        return currFunc;
    }

    public void setCurrFunc(MipsFunc currFunc) {
        this.currFunc = currFunc;
    }

    public MipsBlock getCurrBlock() {
        return currBlock;
    }

    public void setCurrBlock(MipsBlock currBlock) {
        this.currBlock = currBlock;
    }

    public MipsModule getMipsModule() {
        return mipsModule;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }
    
    public void addStackOffset(int offset) {
        this.stackOffset += offset;
    }
}
