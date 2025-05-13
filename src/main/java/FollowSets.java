import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

public class FollowSets {

    // Map of rule names to their FOLLOW sets
    public static final Map<String, Set<String>> FOLLOW_MAP = createFollowMap();

    private static Map<String, Set<String>> createFollowMap() {
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
        map.put("type", type());
        return map;
    }

    public static Set<String> program() {
        // PROGRAM ::= 'class' id '{' METHOD* '}'
        return Set.of("$");
    }

    public static Set<String> method() {
        // After METHOD inside PROGRAM: METHOD* can be another METHOD or '}'
        return Set.of("int", "INTEGER", "float", "boolean", "char", "string", "void", "}");
    }

    public static Set<String> body() {
        // BODY* in METHOD: another BODY or '}'
        Set<String> set = new HashSet<>();
        // FIRST(BODY)
        set.addAll(Set.of(";","int", "INTEGER","float","boolean","char","string","void","ID","return","print","if","while","do","for","switch","break","continue"));
        set.add("}");
        return set;
    }

    public static Set<String> simpleStmt() {
        // simpleStmt appears in BODY
        return body();
    }

    public static Set<String> controlStmt() {
        // controlStmt appears in BODY
        return body();
    }

    public static Set<String> variable() {
        // VARIABLE ::= TYPE id ... ; and in FOR
        return Set.of(";", ")");
    }

    public static Set<String> assignment() {
        // ASSIGNMENT ends with ';' or ')' in FOR
        return Set.of(";", ")");
    }

    public static Set<String> callMethod() {
        // CALL_METHOD ::= id '(' ... ')' ';'
        return Set.of(";");
    }

    public static Set<String> _return() {
        // RETURN ends with ';', then follows BODY
        return body();
    }

    public static Set<String> _print() {
        // PRINT ends with ';', then follows BODY
        return body();
    }

    public static Set<String> _if() {
        // IF is a CONTROL_STMT
        return body();
    }

    public static Set<String> _while() {
        return body();
    }

    public static Set<String> doWhile() {
        return body();
    }

    public static Set<String> _for() {
        return body();
    }

    public static Set<String> _switch() {
        return body();
    }

    public static Set<String> params() {
        // PARAMS ::= ... ')' follows
        return Set.of(")");
    }

    public static Set<String> paramValues() {
        // PARAM_VALUES ::= ... ')' follows
        return Set.of(")");
    }

    public static Set<String> expression() {
        // After EXPRESSION in various contexts
        return Set.of(")", ";", ",", ":", "}");
    }

    public static Set<String> x() {
        Set<String> set = new HashSet<>();
        set.addAll(Set.of("|","||"));
        set.addAll(expression());
        return set;
    }

    public static Set<String> y() {
        Set<String> set = new HashSet<>();
        set.addAll(Set.of("&","&&"));
        set.addAll(x());
        return set;
    }

    public static Set<String> R() {
        Set<String> set = new HashSet<>();
        set.addAll(Set.of("!=","==",">","<"));
        set.addAll(y());
        return set;
    }

    public static Set<String> E() {
        Set<String> set = new HashSet<>();
        set.addAll(Set.of("+","-"));
        set.addAll(R());
        return set;
    }

    public static Set<String> A() {
        Set<String> set = new HashSet<>();
        set.addAll(Set.of("*","/"));
        set.addAll(E());
        return set;
    }

    public static Set<String> B() {
        // B ::= '-'? C
        Set<String> set = new HashSet<>();
        set.addAll(A());
        return set;
    }

    public static Set<String> C() {
        // C ::= ... then follows B
        return A();
    }

    public static Set<String> type() {
        // TYPE ::= 'int' | ... always followed by id
        return Set.of("ID");
    }
}
