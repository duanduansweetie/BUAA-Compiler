package mips.structure;

import llvmir.value.Value;
import llvmir.value.constants.Constant;
import llvmir.value.constants.ConstantArray;
import llvmir.value.constants.ConstantInt;
import llvmir.value.constants.ConstantString;

public class MipsGV {
    String name;
    Constant value;

    public MipsGV(String name, Value value) {
        this.name = name;
        this.value = (Constant) value;
    }

    @Override
    public String toString() {
        if (value instanceof ConstantString) {
            ConstantString cs = (ConstantString) value;
            if (name.startsWith(".str.")) {
                return ".align 2\n" + name + ": " + cs.toMips();
            } else {
                return name + ": " + cs.toMipsForInt();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":");
        if (value instanceof ConstantInt) {
            sb.append("\n\t.word ").append(((ConstantInt) value).getValue());
        }
        else if (value instanceof ConstantArray) {
            ConstantArray array = (ConstantArray) value;

            if (array.getValues().isEmpty()) {
                sb.append("\n\t.space ").append(array.getType().getSize());
            } 
            else {
                sb.append("\n\t.word ");
                for (int i = 0; i < array.getValues().size(); i++) {
                    Constant item = array.getValues().get(i);
                    if (item instanceof ConstantInt) {
                        sb.append(((ConstantInt) item).getValue());
                    } else {
                        sb.append("0"); 
                    }
                    
                    if (i < array.getValues().size() - 1) {
                        sb.append(", ");
                    }
                }
                
                int totalSize = array.getType().getSize();
                // 已初始化的字节数 = 元素个数 * 4
                int initSize = array.getValues().size() * 4;
                
                if (initSize < totalSize) {
                    sb.append("\n\t.space ").append(totalSize - initSize);
                }
            }
        }
        else {
            sb.append("\n\t.word ").append(value.toMips());
        }

        return sb.toString();
    }
}