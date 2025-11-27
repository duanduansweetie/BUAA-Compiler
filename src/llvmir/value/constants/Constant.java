package llvmir.value.constants;
import llvmir.value.Value;

import llvmir.type.Type;
public class Constant extends Value{
    public Constant(String name,Type type) {
        super( name,type);
    }
}
