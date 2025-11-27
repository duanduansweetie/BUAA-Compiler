package llvmir.value;
import java.util.ArrayList;
import java.util.List;
import llvmir.type.Type;
public class Value {
    protected String name;
    protected Type type;
    private List<User> users;
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

}
