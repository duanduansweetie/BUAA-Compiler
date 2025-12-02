package mips;

import java.util.ArrayList;
import java.util.List;
import llvmir.value.Value;
import llvmir.value.instructions.*;
import mips.instructions.*;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.RegManager;
import mips.value.StackSlot;

public class InstructionTranslator {
    private final GenerationContext context;
    private final RegisterAllocator allocator;

    public InstructionTranslator(GenerationContext context, RegisterAllocator allocator) {
        this.context = context;
        this.allocator = allocator;
    }

    public void translate(Value instruction) {
        if (instruction instanceof Alloca) {
            translateAlloca((Alloca) instruction);
        } else if (instruction instanceof BinaryOp) {
            translateBinaryOp((BinaryOp) instruction);
        } else if (instruction instanceof Br) {
            translateBr((Br) instruction);
        } else if (instruction instanceof Call) {
            translateCall((Call) instruction);
        } else if (instruction instanceof GetElementPtr) {
            translateGetElementPtr((GetElementPtr) instruction);
        } else if (instruction instanceof Icmp) {
            translateIcmp((Icmp) instruction);
        } else if (instruction instanceof Load) {
            translateLoad((Load) instruction);
        } else if (instruction instanceof Ret) {
            translateRet((Ret) instruction);
        } else if (instruction instanceof Store) {
            translateStore((Store) instruction);
        } else if (instruction instanceof Zext) {
            translateZext((Zext) instruction);
        } else if (instruction instanceof Trunc) {
            translateTrunc((Trunc) instruction);
        }
    }

    private void translateAlloca(Alloca alloca) {
        if (alloca.getRegister() == null) {
            alloca.setRegister(new StackSlot(context.getStackOffset()));
            context.addStackOffset(alloca.getAllocType().getSize());
        }
    }

    private void translateBinaryOp(BinaryOp binaryOp) {
        MipsOperand lhs = allocator.getOperand(binaryOp.getOperands().get(0));
        MipsOperand rhs = allocator.getOperand(binaryOp.getOperands().get(1));
        PhyReg target = allocator.allocateReg(binaryOp);
        context.getCurrBlock().addInstruction(new MipsALU(target, lhs, rhs, binaryOp.getOp()));
    }

    private void translateBr(Br br) {
        if (br.getOperands().size() == 1) {
            context.getCurrBlock().addInstruction(new MipsJump(context.getCurrFunc().getName() + "_" + br.getOperands().get(0).getName()));
        } else {
            MipsOperand cond = allocator.getOperand(br.getOperands().get(2));
            context.getCurrBlock().addInstruction(
                    new MipsBranch("==", cond,
                            PhyReg.ZERO, context.getCurrFunc().getName() + "_" + br.getOperands().get(1).getName(),
                            context.getCurrFunc().getName() + "_" + br.getOperands().get(0).getName()));
        }
    }

    private void translateCall(Call call) {
        if (!call.getFirstOperand().getType().isVoidType()) {
            allocator.allocateReg(call);
        }

        List<MipsOperand> args = new ArrayList<>();
        if (call.getOperands().size() > 1) {
            for (int i = 1; i < call.getOperands().size(); i++) {
                args.add(allocator.getOperand(call.getOperands().get(i)));
            }
        }

        String funcName = call.getOperands().get(0).getName().substring(1);
        int offset = 0;
        if (isLibraryFunction(funcName)) {
            offset = -1;
        } else {
            offset = context.getMipsModule().getFunction(funcName).getStackSize();
        }

        if (call.getOperands().size() == 1) {
            context.getCurrBlock().addInstruction(new MipsCall(funcName, offset, context.getCurrFunc()));
        } else {
            context.getCurrBlock().addInstruction(new MipsCall(funcName, args, offset, context.getCurrFunc()));
        }

        if (!call.getFirstOperand().getType().isVoidType()) {
            context.getCurrBlock().addInstruction(new MipsMove(call.getRegister(), PhyReg.V0));
        }
    }

    private boolean isLibraryFunction(String name) {
        return name.equals("putint") || name.equals("putch") || name.equals("getint")
                || name.equals("getchar") || name.equals("putstr");
    }

    private void translateGetElementPtr(GetElementPtr gep) {
        PhyReg target = allocator.allocateReg(gep);
        MipsOperand base = allocator.getOperand(gep.getOperands().get(0));
        MipsOperand index = allocator.getOperand(gep.getOperands().get(1));

        context.getCurrBlock().addInstruction(new MipsMem(target, base, MipsMem.MemOp.LA));

        MipsOperand offsetVal = index;
        if (index instanceof PhyReg) {
            PhyReg temp = RegManager.getTempRegister(context.getCurrFunc());
            context.getCurrBlock().addInstruction(new MipsALU(temp, index, new MipsImm(2), "<<"));
            offsetVal = temp;
        } else if (index instanceof MipsImm) {
            offsetVal = new MipsImm(((MipsImm) index).getValue() * 4);
        }

        if (!((offsetVal instanceof MipsImm) && ((MipsImm) offsetVal).getValue() == 0)) {
            context.getCurrBlock().addInstruction(new MipsALU(target, target, offsetVal, "+"));
        }

        if (offsetVal instanceof PhyReg && offsetVal != index) {
            RegManager.releaseTempRegister((PhyReg) offsetVal, context.getCurrFunc());
        }
    }

    private void translateIcmp(Icmp icmp) {
        String op = icmp.getOp();
        MipsOperand lhs = allocator.getOperand(icmp.getOperands().get(0));
        MipsOperand rhs = allocator.getOperand(icmp.getOperands().get(1));
        PhyReg target = allocator.allocateReg(icmp);
        context.getCurrBlock().addInstruction(new MipsALU(target, lhs, rhs, op));
    }

    private void translateLoad(Load load) {
        MipsOperand addr = allocator.getOperand(load.getOperands().get(0));
        PhyReg target = allocator.allocateReg(load);
        context.getCurrBlock().addInstruction(new MipsMem(target, addr, MipsMem.MemOp.LW));
    }

    private void translateRet(Ret ret) {
        if (ret.getOperands().size() == 0 || ret.getOperands().get(0) == null) {
            context.getCurrBlock().addInstruction(new MipsReturn(context.getCurrFunc().getStackSize()));
        } else {
            MipsOperand val = allocator.getOperand(ret.getOperands().get(0));
            context.getCurrBlock().addInstruction(new MipsReturn(val, context.isMain(), context.getCurrFunc().getStackSize()));
        }
    }

    private void translateStore(Store store) {
        MipsOperand data = allocator.getOperand(store.getOperands().get(0));
        MipsOperand addr = allocator.getOperand(store.getOperands().get(1));

        MipsOperand finalData = data;
        if (data instanceof MipsImm) {
            PhyReg li = RegManager.getTempRegister(context.getCurrFunc());
            context.getCurrBlock().addInstruction(new MipsMem(li, data, MipsMem.MemOp.LI));
            finalData = li;
        }

        context.getCurrBlock().addInstruction(new MipsMem(finalData, addr, MipsMem.MemOp.SW));

        if (finalData != data && finalData instanceof PhyReg) {
            RegManager.releaseTempRegister((PhyReg) finalData, context.getCurrFunc());
        }
    }

    private void translateZext(Zext zext) {
        MipsOperand src = allocator.getOperand(zext.getFirstOperand());
        PhyReg target = allocator.allocateReg(zext);

        if (src instanceof MipsImm) {
            PhyReg li = RegManager.getTempRegister(context.getCurrFunc());
            context.getCurrBlock().addInstruction(new MipsMem(li, src, MipsMem.MemOp.LI));
            context.getCurrBlock().addInstruction(new MipsMove(target, li));
            RegManager.releaseTempRegister(li, context.getCurrFunc());
        } else {
            context.getCurrBlock().addInstruction(new MipsMove(target, src));
        }
    }

    private void translateTrunc(Trunc trunc) {
        MipsOperand src = allocator.getOperand(trunc.getFirstOperand());

        if (src instanceof MipsImm) {
            int value = ((MipsImm) src).getValue();
            trunc.setRegister(new MipsImm(value & 0x7F));
        } else {
            PhyReg target = allocator.allocateReg(trunc);
            context.getCurrBlock().addInstruction(new MipsALU(target, src, new MipsImm(0x7F), "&"));
        }
    }
}
