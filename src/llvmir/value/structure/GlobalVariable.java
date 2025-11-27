package llvmir.value.structure;

import llvmir.type.PointType;
import llvmir.type.Type;
import llvmir.value.User;
import llvmir.value.Value;

public class GlobalVariable extends User {
    public enum VType {
        CONSTANT,
        VARIABLE;

        @Override
        public String toString() {
            switch (this) {
                case CONSTANT:
                    return " constant";
                default:
                    return " global";
            }
        }
    }

    // 全局变量是变量还是常量
    private VType vType;

    // type可能是int32/int8/[x * int32]/[x * int8]
    public GlobalVariable(String name, Type type, VType vType, Value value) {
        super("@" + name, new PointType(type));
        this.vType = vType;
        // 仅在 value 非空时添加操作数，并且避免重复添加到 operands 或 users 列表
        if (value != null) {
            // addOperand 应该将 operand 注册到当前 User 中；为保险起见先检查是否已存在
            try {
                // 如果 User 提供了 addOperand 的幂等性，则此处不会重复
                this.addOperand(value);
            } catch (Exception ignored) {
                // 容错：若 addOperand 抛异常则忽略（保持兼容）
            }
            // 确保 value 的 users 列表中包含当前 GlobalVariable（避免重复）
            if (!value.getUsers().contains(this)) {
                value.getUsers().add(this);
            }
        }
    }

    public VType getVType() {
        return vType;
    }

    @Override
    public String toString() {
        String firstOp = "";
        if (this.getFirstOperand() != null) {
            firstOp = this.getFirstOperand().toString();
        }
        return this.getName() + " = dso_local" +
                this.getVType() + " " + ((PointType) this.getType()).getPoint() +
                firstOp;
    }
}
