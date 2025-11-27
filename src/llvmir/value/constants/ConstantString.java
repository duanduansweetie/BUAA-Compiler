package llvmir.value.constants;
import llvmir.type.CharArrayType;
public class ConstantString extends Constant{
    private String valueStr;
    private int length;

    // 设置字符和输出的映射，7-12、34、39、92、0号字符是转义字符
    // 7: \a 8: \b 9: \t 10: \n 11: \v 12: \f 34: \" 39: \' 92: \\
    public ConstantString(String valueStr,int length){
        super("ConstantString", new CharArrayType(length));
        // 去掉首尾的引号
        this.valueStr = valueStr;
        this.length = length;
    }
    public String getValueStr(){
        return valueStr;
    }
    public int getLength(){
        return length;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(valueStr==null||valueStr.length()==0){
            return " zeroinitializer";
        }
        sb.append(" c\"");
        for(int i=0;i<length;i++){
            if(i<valueStr.length()){
                char c=valueStr.charAt(i);
                if(c=='\n'){
                    sb.append("\\0A");
                }
                else if(c=='\0'){
                    sb.append("\\00");
                }
               else if(c<32||c>126){
                    sb.append(String.format("\\%02X", (int) c));
                }
               else{
                   sb.append(c);
                }
            }
            else{
                sb.append("\\00");

            }
        }
        sb.append("\"");
        return sb.toString();
    }





}
