import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class FirstSets {

    // Map of rule names to their FIRST sets
    public static final Map<String, Set<String>> FIRST_MAP = createFirstMap();

    private static Map<String, Set<String>> createFirstMap() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("program", program());
        map.put("method", method());
        map.put("body", body());
        map.put("simpleStmt", simpleStmt());
        map.put("controlStmt", controlStmt());
        map.put("variable", variable());
        map.put("assignment", assignment());
        map.put("callMethod", callMethod());
        map.put("return", _return());
        map.put("print", _print());
        map.put("if", _if());
        map.put("while", _while());
        map.put("doWhile", doWhile());
        map.put("for", _for());
        map.put("switch", _switch());
        map.put("params", params());
        map.put("paramValues", paramValues());
        map.put("expression", expression());
        map.put("x", x());
        map.put("y", y());
        map.put("R", R());
        map.put("E", E());
        map.put("A", A());
        map.put("B", B());
        map.put("C", C());
        map.put("type", _type());
        return map;
    }

    public static Set<String> program() {
        return Set.of("class");
    }

    public static Set<String> method() {
        return _type();
    }

    public static Set<String> body() {
        Set<String> set = new HashSet<>();
        set.addAll(simpleStmt());
        set.addAll(controlStmt());
        set.add(";");
        return set;
    }

    public static Set<String> simpleStmt() {
        Set<String> set = new HashSet<>();
        set.addAll(variable());
        set.addAll(assignment());
        set.addAll(callMethod());
        set.addAll(_return());
        set.addAll(_print());
        return set;
    }

    public static Set<String> controlStmt() {
        Set<String> set = new HashSet<>();
        set.addAll(_if());
        set.addAll(_while());
        set.addAll(doWhile());
        set.addAll(_for());
        set.addAll(_switch());
        set.add("break");
        set.add("continue");
        return set;
    }

    public static Set<String> variable() {
        return _type();
    }

    public static Set<String> assignment() {
        return Set.of("ID");
    }

    public static Set<String> callMethod() {
        return Set.of("ID");
    }

    public static Set<String> _return() {
        return Set.of("return");
    }

    public static Set<String> _print() {
        return Set.of("print");
    }

    public static Set<String> _if() {
        return Set.of("else", "if");
    }

    public static Set<String> _while() {
        return Set.of("while");
    }

    public static Set<String> doWhile() {
        return Set.of("do");
    }

    public static Set<String> _for() {
        return Set.of("for");
    }

    public static Set<String> _switch() {
        return Set.of("switch");
    }

    public static Set<String> params() {
        return _type();
    }

    public static Set<String> paramValues() {
        return expression();
    }

    public static Set<String> expression() {
        return x();
    }

    public static Set<String> x() {
        return y();
    }

    public static Set<String> y() {
        Set<String> set = new HashSet<>();
        set.addAll(R());
        set.add("!");
        return set;
    }

    public static Set<String> R() {
        return E();
    }

    public static Set<String> E() {
        return A();
    }

    public static Set<String> A() {
        return B();
    }

    public static Set<String> B() {
        Set<String> set = new HashSet<>();
        set.add("-");
        set.addAll(C());
        return set;
    }

    public static Set<String> C() {
        Set<String> set = new HashSet<>();
        set.addAll(_type());
        set.addAll(Set.of(
                "int", "INTEGER", "OCTAL", "hexadecimal", "binary",
                "true", "false", "string", "char", "FLOAT",
                "ID", "(", ")"));
        return set;
    }

    public static Set<String> _type() {
        return Set.of("int", "INTEGER", "float", "boolean", "char", "string", "void");
    }
}
