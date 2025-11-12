package symbol;

public abstract class Symbol {
    private final String name;//符号名
    private final SymbolType type;//符号类型
    private final int scopelevel;//符号所在层次
    private final int scopenum;//符号所在层次编号

    public Symbol(String name, SymbolType type, int level, int num) {
        this.name = name;
        this.type = type;
        this.scopelevel = level;
        this.scopenum = num;
    }
    public String getName() {
        return name;
    }
    public SymbolType getType() {
        return type;
    }
    public int getLevel() {
        return scopelevel;
    }
    public int getNum() {
        return scopenum;
    }
    protected abstract String getTypeName();
}
