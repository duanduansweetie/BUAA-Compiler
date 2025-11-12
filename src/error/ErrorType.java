package error;
public enum ErrorType{
    A("非法符号","a"),
    B("名字重定义","b"),
    C("未定义的名字","c"),
    D("函数参数个数不匹配","d"),
    E("函数参数类型不匹配","e"),
    F("无返回值的函数存在不匹配的return语句","f"),
    G("有返回值的函数缺少return语句","g"),
    H("不能改变常量的值","h"),
    I("缺少分号","i"),
    J("缺少右小括号')'","j"),
    K("缺少右中括号']'","k"),
    L("printf中格式字符串与表达式个数不匹配","l"),
    M("在非循环块中使用break和continue语句","m");

    private final String value;
    private final String category;
    ErrorType(String value, String category){
        this.value = value;
        this.category = category;
    }
    @Override
    public String toString(){
        return category;
    }
    public String getNameString() {
        return value;
    }
}