package llvmir.value;

import java.util.ArrayList;
import java.util.List;
import llvmir.type.Type;
import mips.structure.MipsFunc;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.RegManager;

public class Value {
    protected String name;
    protected Type type;
    private List<User> users;
    // 一个value应该有一个属于自己的寄存器
    protected MipsOperand register;
    // 寄存器也应该有一个指针指向该value
    protected MipsOperand point;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        users = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public MipsOperand getRegister() {
        return register;
    }

    public MipsOperand getPoint() {
        return point;
    }

    public void setRegister(MipsOperand register) {
        this.register = register;
    }

    public void setPoint(MipsOperand point) {
        this.point = point;
    }

    public void clearRegister(MipsFunc func) {
        if (register != null && register instanceof PhyReg
                && (((PhyReg) register).getType() == PhyReg.RegType.TEMP)) {
            RegManager.releaseTempRegister((PhyReg) register, func);
            register = null;
        }
    }

    public void addUser(User user) {
        users.add(user);
    }

    public String getPrintName() {
        if (name == null) {
            return "null";
        }
        if (name.startsWith("@") || name.startsWith("%")) {
            return name;
        }
        if (name.length() > 0 && (Character.isDigit(name.charAt(0)) || name.startsWith("-"))) {
            return name;
        }
        return "%" + name;
    }
}
