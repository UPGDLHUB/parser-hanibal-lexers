import java.util.ArrayList;
import java.util.List;

/**
 * A single entry in the symbol table.  Can represent either:
 *  • a variable (isMethod == false), in which case:
 *      – type = the variable’s type (e.g. "int", "boolean", etc.)
 *      – value = default‐value string (e.g. "0", "false", "\"\"", etc.)
 *      – paramTypes == null
 *  • a method (isMethod == true), in which case:
 *      – type = the method’s return type (e.g. "int", "void", etc.)
 *      – paramTypes = a List<String> of parameter types in order
 *      – value = unused (null)
 */
public class SymbolTableItem {
    // For variables: the variable’s type.
    // For methods: the return type.
    private String type;

    // A scope string (e.g. “function” or “methodName@returnType”).
    private String scope;

    // For a variable: holds its default‐value string (like "0", "false", "\"\"", etc.).
    // For a method: unused (null).
    private String value;

    // If isMethod == true, this List holds exactly the method’s declared
    // parameter types in order (e.g. ["int","boolean","String"]).
    // If isMethod == false, paramTypes should be null.
    private List<String> paramTypes;

    // True if this entry represents a method; false if it’s a plain variable.
    private boolean isMethod;

    // ------------------------------------------------------------
    // Constructor for a VARIABLE entry.
    //
    //   type:  the variable’s declared type (e.g. "int", "boolean", etc.)
    //   scope: the scope in which the variable lives (e.g. "foo@int")
    //   value: a string for a default value (e.g. "0", "false", "\"\"", etc.)
    // ------------------------------------------------------------
    public SymbolTableItem(String type, String scope, String value) {
        this.type       = type;
        this.scope      = scope;
        this.value      = value;
        this.paramTypes = null;
        this.isMethod   = false;
    }

    // ------------------------------------------------------------
    // Overloaded constructor for a METHOD entry.
    //
    //   returnType: the method’s return type (e.g. "int", "void", etc.)
    //   scope:      a unique scope string for the method (e.g. "foo@int")
    //   paramTypes: a List<String> of parameter types in order (e.g. ["int","boolean","String"])
    // ------------------------------------------------------------
    public SymbolTableItem(String returnType, String scope, List<String> paramTypes) {
        this.type       = returnType;            // the return type of the method
        this.scope      = scope;
        this.value      = null;                  // methods do not use 'value'
        this.paramTypes = new ArrayList<>(paramTypes);
        this.isMethod   = true;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    /** Returns true if this SymbolTableItem represents a method (not a variable). */
    public boolean isMethod() {
        return isMethod;
    }

    /**
     * If isMethod() == true, returns the list of parameter types.
     * Otherwise returns null.
     */
    public List<String> getParamTypes() {
        return paramTypes;
    }
}
