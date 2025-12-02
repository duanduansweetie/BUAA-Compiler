package mips.structure;
import java.util.ArrayList;
import java.util.List;
public class MipsBlock {
    public String name;
    public List<MipsInstr> instructions = new ArrayList<>();
    public List<MipsBlock> successors = new ArrayList<>(); // 后继基本块
    public List<MipsBlock> predecessors = new ArrayList<>(); // 前驱基本块
    public MipsBlock(String name) {
        this.name = name;
    }
    public void addInstruction(MipsInstr instruction) {
        instructions.add(instruction);
    }

    public void addSuccessor(MipsBlock block) {
        successors.add(block);
    }

    public void addPredecessor(MipsBlock block) {
        predecessors.add(block);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n" + name + ":\n");
        for (MipsInstr instr : instructions) {
            sb.append(instr + "\n");
        }
        return sb.toString();
    }
}
