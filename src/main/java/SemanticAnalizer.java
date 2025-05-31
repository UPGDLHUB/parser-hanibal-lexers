import java.util.*;
/**
 * SemanticAnalizer performs:
 *  1) Scoping & “no shadowing” checks (checkVariable/lookupVariable).
 *  2) Type checking for expressions via a typeStack + a typeCube.
 *  3) Method registration (registerMethod) + signature lookup (findMethod).
 */
public class SemanticAnalizer {
    private int errorCount;

    // Step 1: scoping
    //   Maps each identifier → Vector of SymbolTableItem entries (could be multiple overloads).
    private Hashtable<String, Vector<SymbolTableItem>> symbolTable;
    private Stack<String> scopeStack;

    // Step 2: type checking
    private String[][][] typeCube;
    private Stack<String> typeStack;
    private static final int TYPES = 9;      // int, float, boolean, char, string, binary, octal, hexadecimal, void
    private static final int OPERATORS = 15; // +, -, *, /, %, =, &&, <, >, <=, >=, ==, !=, !

    // Constants for types
    public static final int TYPE_INT         = 0;
    public static final int TYPE_FLOAT       = 1;
    public static final int TYPE_BOOLEAN     = 2;
    public static final int TYPE_CHAR        = 3;
    public static final int TYPE_STRING      = 4;
    public static final int TYPE_BINARY      = 5;
    public static final int TYPE_OCTAL       = 6;
    public static final int TYPE_HEXADECIMAL = 7;
    public static final int TYPE_VOID        = 8;

    // Constants for operators
    public static final int OP_PLUS       = 0;
    public static final int OP_MINUS      = 1;
    public static final int OP_MULT       = 2;
    public static final int OP_DIV        = 3;
    public static final int OP_MOD        = 4;
    public static final int OP_ASSIGN     = 5;
    public static final int OP_AND        = 6;
    public static final int OP_OR         = 7;
    public static final int OP_LESS       = 8;
    public static final int OP_GREATER    = 9;
    public static final int OP_LESSEQ     = 10;
    public static final int OP_GREATEREQ  = 11;
    public static final int OP_EQUAL      = 12;
    public static final int OP_NOTEQUAL   = 13;
    public static final int OP_NOT        = 14;

    public SemanticAnalizer() {
        this.symbolTable = new Hashtable<>();
        this.scopeStack  = new Stack<>();
        this.errorCount  = 0;
        scopeStack.push("global");
        this.typeStack   = new Stack<>();
        InitTypeCube();
    }

    public int getErrorCount() {
        return errorCount;
    }

    public String currentScope() {
        return scopeStack.peek();
    }

    public void enterScope(String scopeName) {
        scopeStack.push(scopeName);
    }

    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
        }
    }

    /**
     * Called at declaration sites for variables.
     * If 'id' already exists in the same scope → error.
     * If 'id' exists in any parent scope → error (no shadowing).
     * Otherwise insert (id → SymbolTableItem(type, currentScope(), value)) into symbolTable.
     */
    public void checkVariable(String id, String type, String value) {
        // A. Search the id in the symbol table in current scope
        if (existsInCurrentScope(id, type)) {
            reportError("Variable '" + id + "' already exists in scope '" + currentScope() + "'");
        } else {
            // B. If not exist, check parent scopes for same name+type
            List<String> scopes = new ArrayList<>(scopeStack);
            for (int i = scopes.size() - 2; i >= 0; i--) {
                if (existsInSelectedScope(scopes.get(i), id, type)) {
                    reportError("Variable '" + id + "' already exists in parent scope '"
                            + scopes.get(i) + "'. Cannot redeclare in nested scope '"
                            + currentScope() + "'");
                    break;
                }
            }

            // If no initial value provided, assign a language‐default
            if (value == null || value.isEmpty()) {
                value = getDefaultValue(type);
            }

            SymbolTableItem symbol = new SymbolTableItem(type, currentScope(), value);
            if (symbolTable.containsKey(id)) {
                symbolTable.get(id).add(symbol);
            } else {
                Vector<SymbolTableItem> v = new Vector<>();
                v.add(symbol);
                symbolTable.put(id, v);
            }

            System.out.println("Added variable: " + id
                    + " of type " + type
                    + " in scope " + currentScope()
                    + " with value " + value);
        }
    }

    /** Returns true if (id,type) exists exactly in currentScope(). */
    private boolean existsInCurrentScope(String id, String type) {
        if (!symbolTable.containsKey(id)) {
            return false;
        }
        for (SymbolTableItem symbol : symbolTable.get(id)) {
            if (symbol.getScope().equals(currentScope()) && symbol.getType().equals(type)
                    && !symbol.isMethod()) {
                return true;
            }
        }
        return false;
    }

    /** Returns true if (id,type) exists in the given 'scope'. */
    private boolean existsInSelectedScope(String scope, String id, String type) {
        if (!symbolTable.containsKey(id)) {
            return false;
        }
        for (SymbolTableItem symbol : symbolTable.get(id)) {
            if (symbol.getScope().equals(scope) && symbol.getType().equals(type) && !symbol.isMethod()) {
                return true;
            }
        }
        return false;
    }

    private String getDefaultValue(String type) {
        return switch (type.toLowerCase()) {
            case "int"     -> "0";
            case "float"   -> "0.0f";
            case "boolean" -> "false";
            case "char"    -> "''";
            case "string"  -> "\"\"";
            default        -> "";
        };
    }

    /** Print all entries in the symbol table (for debugging). */
    public void printSymbolTable() {
        System.out.println("\n=== SYMBOL TABLE ===");
        for (String id : symbolTable.keySet()) {
            for (SymbolTableItem item : symbolTable.get(id)) {
                System.out.printf("Name: %s,\tType: %s,\tScope: %s,\tValue: %s%s\n",
                        id, item.getType(), item.getScope(),
                        item.getValue(),
                        item.isMethod() ? ", METHOD" : "");
                if (item.isMethod()) {
                    System.out.printf("    → Param types: %s\n", item.getParamTypes());
                }
            }
        }
    }

    // ----------------------------------------------------------------
    // VARIABLE lookup (for usage, not declaration)
    // ----------------------------------------------------------------

    /**
     * Called when encountering an identifier in usage context (e.g. assignment LHS or method call).
     * Returns true if 'id' is declared in any enclosing scope; false otherwise.
     */
    public boolean lookupVariable(String id) {
        if (!symbolTable.containsKey(id)) {
            return false;
        }
        List<String> scopes = new ArrayList<>(scopeStack);
        for (int i = scopes.size() - 1; i >= 0; i--) {
            String s = scopes.get(i);
            for (SymbolTableItem item : symbolTable.get(id)) {
                // If there is any SymbolTableItem (variable or method) in that scope, return true
                if (item.getScope().equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the type of 'id' as declared in the nearest enclosing scope.
     * If 'id' is not found, returns null.
     */
    public String getDeclaredType(String id) {
        if (!symbolTable.containsKey(id)) {
            return null;
        }
        List<String> scopes = new ArrayList<>(scopeStack);
        for (int i = scopes.size() - 1; i >= 0; i--) {
            String s = scopes.get(i);
            for (SymbolTableItem item : symbolTable.get(id)) {
                if (item.getScope().equals(s) && !item.isMethod()) {
                    // If it’s a variable entry, return its type.
                    return item.getType();
                }
            }
        }
        return null;
    }

    /**
     * Reports a semantic error message (increments errorCount).
     * This is a public alias to the private error(...) method.
     */
    public void reportError(String message) {
        error(message);
    }

    /**
     * When the parser finishes parsing an expression, it can push the computed type
     * onto typeStack. Calling this returns and pops the topmost type.
     * If no type was pushed, returns a default (“int”) to avoid NPEs.
     */
    public String getLastExpressionType() {
        if (!typeStack.isEmpty()) {
            return typeStack.pop();
        }
        // Fallback if parser hasn’t computed a type: assume “int”
        return "int";
    }

    /**
     * If the parser wants to push literal/variable/method-call types, call this.
     * (e.g. semanticAnalizer.pushExpressionType("boolean");)
     */
    public void pushExpressionType(String type) {
        typeStack.push(type);
    }

    // ----------------------------------------------------------------
    // MISSING/ADDED METHODS FOR METHOD SUPPORT & EXPRESSION STACK
    // ----------------------------------------------------------------

    /**
     * Registers a newly-declared method into the symbol table.
     *
     * @param methodName the method’s identifier (e.g. "foo")
     * @param returnType the method’s declared return type (e.g. "int")
     * @param paramTypes a List<String> of parameter types in order (e.g. ["int","boolean","String"])
     */
    public void registerMethod(String methodName, String returnType, List<String> paramTypes) {
        // Because we already did enterScope("function"), currentScope() is "function".
        String methodScope = currentScope();
        SymbolTableItem methodEntry = new SymbolTableItem(returnType, methodScope, paramTypes);

        if (symbolTable.containsKey(methodName)) {
            symbolTable.get(methodName).add(methodEntry);
        } else {
            Vector<SymbolTableItem> v = new Vector<>();
            v.add(methodEntry);
            symbolTable.put(methodName, v);
        }
    }


    /**
     * Returns the current size of the expression‐type stack.
     * Used by the parser to track how many types were pushed during argument parsing.
     */
    public int expressionStackSize() {
        return typeStack.size();
    }

    /**
     * Finds and returns the first SymbolTableItem under key=id where isMethod()==true.
     * Returns null if no such method entry exists.
     */
    public SymbolTableItem findMethod(String id) {
        if (!symbolTable.containsKey(id)) {
            return null;
        }
        for (SymbolTableItem item : symbolTable.get(id)) {
            if (item.isMethod()) {
                return item;
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // PRIVATE helper to report errors
    // ----------------------------------------------------------------
    private void error(String message) {
        System.err.println("Semantic error: " + message);
        errorCount++;
    }

    // ----------------------------------------------------------------
    // TYPE‐CUBE INITIALIZATION (unchanged from original)
    // ----------------------------------------------------------------
    private void InitTypeCube() {
        typeCube = new String[OPERATORS][TYPES][TYPES];
        for (int i = 0; i < OPERATORS; i++)
            for (int j = 0; j < TYPES; j++)
                for (int k = 0; k < TYPES; k++)
                    typeCube[i][j][k] = "ERROR";

        // ---------- Addition Operator ----------
        typeCube[OP_PLUS][TYPE_INT][TYPE_INT]         = "int";
        typeCube[OP_PLUS][TYPE_INT][TYPE_FLOAT]       = "float";
        typeCube[OP_PLUS][TYPE_INT][TYPE_STRING]      = "string";
        typeCube[OP_PLUS][TYPE_INT][TYPE_BINARY]      = "int";
        typeCube[OP_PLUS][TYPE_INT][TYPE_OCTAL]       = "int";
        typeCube[OP_PLUS][TYPE_INT][TYPE_HEXADECIMAL] = "int";

        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_INT]         = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_FLOAT]       = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_STRING]      = "string";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_BINARY]      = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_OCTAL]       = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_HEXADECIMAL] = "float";

        typeCube[OP_PLUS][TYPE_BOOLEAN][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_CHAR][TYPE_STRING]    = "string";

        typeCube[OP_PLUS][TYPE_STRING][TYPE_INT]         = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_FLOAT]       = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_BOOLEAN]     = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_CHAR]        = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_STRING]      = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_BINARY]      = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_OCTAL]       = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_HEXADECIMAL] = "string";

        typeCube[OP_PLUS][TYPE_BINARY][TYPE_INT]         = "int";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_FLOAT]       = "float";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_STRING]      = "string";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_BINARY]      = "int";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_OCTAL]       = "int";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_HEXADECIMAL] = "int";

        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_INT]         = "int";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_FLOAT]       = "float";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_STRING]      = "string";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_BINARY]      = "int";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_OCTAL]       = "int";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_HEXADECIMAL] = "int";

        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_INT]         = "int";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_FLOAT]       = "float";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_STRING]      = "string";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_BINARY]      = "int";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_OCTAL]       = "int";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "int";

        // ... (rest of typeCube initialization identical to your original code) ...
        // For brevity, I have omitted the full subtraction, multiplication, division, etc.
        // You can copy‐paste everything from your previous version of InitTypeCube() here.
    }

    private int typeIndexOf(String type) {
        return switch (type.toLowerCase()) {
            case "int"         -> TYPE_INT;
            case "float"       -> TYPE_FLOAT;
            case "boolean"     -> TYPE_BOOLEAN;
            case "char"        -> TYPE_CHAR;
            case "string"      -> TYPE_STRING;
            case "binary"      -> TYPE_BINARY;
            case "octal"       -> TYPE_OCTAL;
            case "hexadecimal" -> TYPE_HEXADECIMAL;
            case "void"        -> TYPE_VOID;
            default             -> -1;
        };
    }

    private int operatorIndexOf(String operator) {
        return switch (operator) {
            case "+"  -> OP_PLUS;
            case "-"  -> OP_MINUS;
            case "*"  -> OP_MULT;
            case "/"  -> OP_DIV;
            case "%"  -> OP_MOD;
            case "="  -> OP_ASSIGN;
            case "&&", "&" -> OP_AND;
            case "||", "|" -> OP_OR;
            case "<"  -> OP_LESS;
            case ">"  -> OP_GREATER;
            case "<=" -> OP_LESSEQ;
            case ">=" -> OP_GREATEREQ;
            case "==" -> OP_EQUAL;
            case "!=" -> OP_NOTEQUAL;
            case "!"  -> OP_NOT;
            default   -> -1;
        };
    }
}
