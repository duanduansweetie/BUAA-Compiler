package llvmir.value.constants;
import llvmir.type.IntArrayType;
import java.util.ArrayList;
import java.util.List;
public class ConstantArray extends Constant{
    private List<ConstantInt> values = new ArrayList<>();
    private int length;
    public List<ConstantInt> getValues() {
        return values;
    }
    public ConstantArray(List<Integer> values,int length){
      super("ConstantArray",new IntArrayType(length));
      if(values!=null){
          for(Integer val:values){
              this.values.add(new ConstantInt(val));
          }
      }
      this.length=length;
    }

    public boolean isNull() {
        return values == null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(values.size()==0||values==null){
            sb.append("zeroinitializer");
            return sb.toString();
        }
        sb.append("[");
        for(int i=0;i<values.size();i++){
            sb.append(values.get(i).getType());
            sb.append(values.get(i));
            if(i!=values.size()-1){
                sb.append(", ");
            }
        }
        for(int i=values.size();i<length;i++){
            sb.append(", i32 0");
        }
        sb.append("]");
        return sb.toString();
    }
}
