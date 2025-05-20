import java.util.*;

public class SemanticAnalizer {
    private int errorCount;

    // Step 1, scoping
    private Hashtable<String, Vector<SymbolTableItem>> symbolTable;
    private Stack<String> scopeStack;

    // Step 2, type checking
    private String[][][] typeCube;
    private Stack<String> typeStack;
    private static final int TYPES = 9; // int, float, boolean, char, string, binary, octal, hexadecimal, void
    private static final int OPERATORS = 15; // +, -, *, /, %, =, &&, <, >, <=, >=, ==, !=, !

    // Constants for types
    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = 1;
    public static final int TYPE_BOOLEAN = 2;
    public static final int TYPE_CHAR = 3;
    public static final int TYPE_STRING = 4;
    public static final int TYPE_BINARY = 5;
    public static final int TYPE_OCTAL = 6;
    public static final int TYPE_HEXADECIMAL = 7;
    public static final int TYPE_VOID = 8;

    // Define constants for operators
    public static final int OP_PLUS = 0;
    public static final int OP_MINUS = 1;
    public static final int OP_MULT = 2;
    public static final int OP_DIV = 3;
    public static final int OP_MOD = 4;
    public static final int OP_ASSIGN = 5;
    public static final int OP_AND = 6;
    public static final int OP_OR = 7;
    public static final int OP_LESS = 8;
    public static final int OP_GREATER = 9;
    public static final int OP_LESSEQ = 10;
    public static final int OP_GREATEREQ = 11;
    public static final int OP_EQUAL = 12;
    public static final int OP_NOTEQUAL = 13;
    public static final int OP_NOT = 14;

    public SemanticAnalizer() {
        this.symbolTable = new Hashtable<>();
        this.scopeStack = new Stack<>();
        this.errorCount = 0;
        scopeStack.push("global");
        this.typeStack = new Stack<>();
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

    public void checkVariable(String id, String type, String value) {
        // A. Search the id in the symbol table in current scope
        if (existsInCurrentScope(id, type)) {
            // C. Variable already exists - report error
            error("Variable '" + id + "' already exists in scope '" + currentScope() + "'");
        } else {
            //B. If not exist, add it with the provided value or default value if it's not in parent scope
            List<String> scopes = new ArrayList<>(scopeStack);
            for (int i = scopes.size() - 2; i > 1; i--) {
                if (existsInSelectedScope(scopes.get(i), id, type))
                    error("Variable '" + id + "' already exists in parent scope '" + scopes.get(i) + "'. Cannot redeclare in nested scope '" + currentScope() + "'");
            }

            if (value == null || value.isEmpty())
                value = getDefaultValue(type);

            SymbolTableItem symbol = new SymbolTableItem(type, currentScope(), value);

            if (symbolTable.containsKey(id)) {
                symbolTable.get(id).add(symbol);
            } else {
                Vector<SymbolTableItem> v = new Vector<>();
                v.add(symbol);
                symbolTable.put(id, v);
            }
            System.out.println("Added variable: " + id + " of type " + type + " in scope " + currentScope() + " with value " + value);
        }
    }

    private boolean existsInCurrentScope(String id, String type) {
        if (!symbolTable.containsKey(id)) { return false; }

        for (SymbolTableItem symbol : symbolTable.get(id)) {
            if (symbol.getScope().equals(currentScope()) && symbol.getType().equals(type)) { return true; }
        }

        return false;
    }

    private boolean existsInSelectedScope(String scope, String id, String type) {
        if (!symbolTable.containsKey(id)) { return false; }

        for (SymbolTableItem symbol : symbolTable.get(id)) {
            if (symbol.getScope().equals(scope) && symbol.getType().equals(type)) { return true; }
        }

        return false;
    }

    private String getDefaultValue(String type) {
        return switch (type.toLowerCase()) {
            case "int" -> "0";
            case "float" -> "0.0f";
            case "boolean" -> "false";
            case "char" -> "''";
            case "string" -> "\"\"";
            default -> "";
        };
    }

    private void error(String message) {
        System.err.println("Semantic error: " + message);
        errorCount++;
    }

    public void printSymbolTable() {
        System.out.println("\n=== SYMBOL TABLE ===");
        for (String id : symbolTable.keySet()) {
            for (SymbolTableItem item : symbolTable.get(id)) {
                System.out.printf("Name: %s,\tType: %s,\tScope: %s,\tValue: %s\n",
                        id, item.getType(), item.getScope(), item.getValue());
            }
        }
    }

    private void InitTypeCube() {
        typeCube = new String[OPERATORS][TYPES][TYPES];
        for (int i = 0; i < OPERATORS; i++)
            for (int j = 0; j < TYPES; j++)
                for (int k = 0; k < TYPES; k++)
                    typeCube[i][j][k] = "ERROR";

        // Addition Operator
        // int + TYPE
        typeCube[OP_PLUS][TYPE_INT][TYPE_INT] = "int";
        typeCube[OP_PLUS][TYPE_INT][TYPE_FLOAT] = "float";
        typeCube[OP_PLUS][TYPE_INT][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_INT][TYPE_BINARY] = "int";
        typeCube[OP_PLUS][TYPE_INT][TYPE_OCTAL] = "int";
        typeCube[OP_PLUS][TYPE_INT][TYPE_HEXADECIMAL] = "int";

        // float + TYPE
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_INT] = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_FLOAT] = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_BINARY] = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_OCTAL] = "float";
        typeCube[OP_PLUS][TYPE_FLOAT][TYPE_HEXADECIMAL] = "float";

        // boolean + TYPE
        typeCube[OP_PLUS][TYPE_BOOLEAN][TYPE_STRING] = "string";

        // char + TYPE
        typeCube[OP_PLUS][TYPE_CHAR][TYPE_STRING] = "string";

        // string + TYPE (string + anything = string)
        typeCube[OP_PLUS][TYPE_STRING][TYPE_INT] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_FLOAT] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_BOOLEAN] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_CHAR] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_BINARY] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_OCTAL] = "string";
        typeCube[OP_PLUS][TYPE_STRING][TYPE_HEXADECIMAL] = "string";

        // binary + TYPE
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_INT] = "int";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_FLOAT] = "float";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_BINARY] = "int";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_OCTAL] = "int";
        typeCube[OP_PLUS][TYPE_BINARY][TYPE_HEXADECIMAL] = "int";

        // octal + TYPE
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_INT] = "int";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_FLOAT] = "float";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_BINARY] = "int";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_OCTAL] = "int";
        typeCube[OP_PLUS][TYPE_OCTAL][TYPE_HEXADECIMAL] = "int";

        // hexadecimal + TYPE
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_INT] = "int";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_FLOAT] = "float";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_STRING] = "string";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_BINARY] = "int";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_OCTAL] = "int";
        typeCube[OP_PLUS][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "int";

        //Subtraction Operator
        // int - TYPE
        typeCube[OP_MINUS][TYPE_INT][TYPE_INT] = "int";
        typeCube[OP_MINUS][TYPE_INT][TYPE_FLOAT] = "float";
        typeCube[OP_MINUS][TYPE_INT][TYPE_BINARY] = "int";
        typeCube[OP_MINUS][TYPE_INT][TYPE_OCTAL] = "int";
        typeCube[OP_MINUS][TYPE_INT][TYPE_HEXADECIMAL] = "int";

        // float - TYPE
        typeCube[OP_MINUS][TYPE_FLOAT][TYPE_INT] = "float";
        typeCube[OP_MINUS][TYPE_FLOAT][TYPE_FLOAT] = "float";
        typeCube[OP_MINUS][TYPE_FLOAT][TYPE_BINARY] = "float";
        typeCube[OP_MINUS][TYPE_FLOAT][TYPE_OCTAL] = "float";
        typeCube[OP_MINUS][TYPE_FLOAT][TYPE_HEXADECIMAL] = "float";

        // binary - TYPE
        typeCube[OP_MINUS][TYPE_BINARY][TYPE_INT] = "int";
        typeCube[OP_MINUS][TYPE_BINARY][TYPE_FLOAT] = "float";
        typeCube[OP_MINUS][TYPE_BINARY][TYPE_BINARY] = "int";
        typeCube[OP_MINUS][TYPE_BINARY][TYPE_OCTAL] = "int";
        typeCube[OP_MINUS][TYPE_BINARY][TYPE_HEXADECIMAL] = "int";

        // octal - TYPE
        typeCube[OP_MINUS][TYPE_OCTAL][TYPE_INT] = "int";
        typeCube[OP_MINUS][TYPE_OCTAL][TYPE_FLOAT] = "float";
        typeCube[OP_MINUS][TYPE_OCTAL][TYPE_BINARY] = "int";
        typeCube[OP_MINUS][TYPE_OCTAL][TYPE_OCTAL] = "int";
        typeCube[OP_MINUS][TYPE_OCTAL][TYPE_HEXADECIMAL] = "int";

        // hexadecimal - TYPE
        typeCube[OP_MINUS][TYPE_HEXADECIMAL][TYPE_INT] = "int";
        typeCube[OP_MINUS][TYPE_HEXADECIMAL][TYPE_FLOAT] = "float";
        typeCube[OP_MINUS][TYPE_HEXADECIMAL][TYPE_BINARY] = "int";
        typeCube[OP_MINUS][TYPE_HEXADECIMAL][TYPE_OCTAL] = "int";
        typeCube[OP_MINUS][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "int";

        // Multiplication Operator
        // int * TYPE
        typeCube[OP_MULT][TYPE_INT][TYPE_INT] = "int";
        typeCube[OP_MULT][TYPE_INT][TYPE_FLOAT] = "float";
        typeCube[OP_MULT][TYPE_INT][TYPE_BINARY] = "int";
        typeCube[OP_MULT][TYPE_INT][TYPE_OCTAL] = "int";
        typeCube[OP_MULT][TYPE_INT][TYPE_HEXADECIMAL] = "int";

        // float * TYPE
        typeCube[OP_MULT][TYPE_FLOAT][TYPE_INT] = "float";
        typeCube[OP_MULT][TYPE_FLOAT][TYPE_FLOAT] = "float";
        typeCube[OP_MULT][TYPE_FLOAT][TYPE_BINARY] = "float";
        typeCube[OP_MULT][TYPE_FLOAT][TYPE_OCTAL] = "float";
        typeCube[OP_MULT][TYPE_FLOAT][TYPE_HEXADECIMAL] = "float";

        // binary * TYPE
        typeCube[OP_MULT][TYPE_BINARY][TYPE_INT] = "int";
        typeCube[OP_MULT][TYPE_BINARY][TYPE_FLOAT] = "float";
        typeCube[OP_MULT][TYPE_BINARY][TYPE_BINARY] = "int";
        typeCube[OP_MULT][TYPE_BINARY][TYPE_OCTAL] = "int";
        typeCube[OP_MULT][TYPE_BINARY][TYPE_HEXADECIMAL] = "int";

        // octal * TYPE
        typeCube[OP_MULT][TYPE_OCTAL][TYPE_INT] = "int";
        typeCube[OP_MULT][TYPE_OCTAL][TYPE_FLOAT] = "float";
        typeCube[OP_MULT][TYPE_OCTAL][TYPE_BINARY] = "int";
        typeCube[OP_MULT][TYPE_OCTAL][TYPE_OCTAL] = "int";
        typeCube[OP_MULT][TYPE_OCTAL][TYPE_HEXADECIMAL] = "int";

        // hexadecimal * TYPE
        typeCube[OP_MULT][TYPE_HEXADECIMAL][TYPE_INT] = "int";
        typeCube[OP_MULT][TYPE_HEXADECIMAL][TYPE_FLOAT] = "float";
        typeCube[OP_MULT][TYPE_HEXADECIMAL][TYPE_BINARY] = "int";
        typeCube[OP_MULT][TYPE_HEXADECIMAL][TYPE_OCTAL] = "int";
        typeCube[OP_MULT][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "int";

        // Division Operator
        // int / TYPE
        typeCube[OP_DIV][TYPE_INT][TYPE_INT] = "float";
        typeCube[OP_DIV][TYPE_INT][TYPE_FLOAT] = "float";
        typeCube[OP_DIV][TYPE_INT][TYPE_BINARY] = "float";
        typeCube[OP_DIV][TYPE_INT][TYPE_OCTAL] = "float";
        typeCube[OP_DIV][TYPE_INT][TYPE_HEXADECIMAL] = "float";

        // float / TYPE
        typeCube[OP_DIV][TYPE_FLOAT][TYPE_INT] = "float";
        typeCube[OP_DIV][TYPE_FLOAT][TYPE_FLOAT] = "float";
        typeCube[OP_DIV][TYPE_FLOAT][TYPE_BINARY] = "float";
        typeCube[OP_DIV][TYPE_FLOAT][TYPE_OCTAL] = "float";
        typeCube[OP_DIV][TYPE_FLOAT][TYPE_HEXADECIMAL] = "float";

        // binary / TYPE
        typeCube[OP_DIV][TYPE_BINARY][TYPE_INT] = "float";
        typeCube[OP_DIV][TYPE_BINARY][TYPE_FLOAT] = "float";
        typeCube[OP_DIV][TYPE_BINARY][TYPE_BINARY] = "float";
        typeCube[OP_DIV][TYPE_BINARY][TYPE_OCTAL] = "float";
        typeCube[OP_DIV][TYPE_BINARY][TYPE_HEXADECIMAL] = "float";

        // octal / TYPE
        typeCube[OP_DIV][TYPE_OCTAL][TYPE_INT] = "float";
        typeCube[OP_DIV][TYPE_OCTAL][TYPE_FLOAT] = "float";
        typeCube[OP_DIV][TYPE_OCTAL][TYPE_BINARY] = "float";
        typeCube[OP_DIV][TYPE_OCTAL][TYPE_OCTAL] = "float";
        typeCube[OP_DIV][TYPE_OCTAL][TYPE_HEXADECIMAL] = "float";

        // hexadecimal / TYPE
        typeCube[OP_DIV][TYPE_HEXADECIMAL][TYPE_INT] = "float";
        typeCube[OP_DIV][TYPE_HEXADECIMAL][TYPE_FLOAT] = "float";
        typeCube[OP_DIV][TYPE_HEXADECIMAL][TYPE_BINARY] = "float";
        typeCube[OP_DIV][TYPE_HEXADECIMAL][TYPE_OCTAL] = "float";
        typeCube[OP_DIV][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "float";

        // Modulo Operator
        // int % TYPE
        typeCube[OP_MOD][TYPE_INT][TYPE_INT] = "int";
        typeCube[OP_MOD][TYPE_INT][TYPE_BINARY] = "int";
        typeCube[OP_MOD][TYPE_INT][TYPE_OCTAL] = "int";
        typeCube[OP_MOD][TYPE_INT][TYPE_HEXADECIMAL] = "int";

        // binary % TYPE
        typeCube[OP_MOD][TYPE_BINARY][TYPE_INT] = "int";
        typeCube[OP_MOD][TYPE_BINARY][TYPE_BINARY] = "int";
        typeCube[OP_MOD][TYPE_BINARY][TYPE_OCTAL] = "int";
        typeCube[OP_MOD][TYPE_BINARY][TYPE_HEXADECIMAL] = "int";

        // octal % TYPE
        typeCube[OP_MOD][TYPE_OCTAL][TYPE_INT] = "int";
        typeCube[OP_MOD][TYPE_OCTAL][TYPE_BINARY] = "int";
        typeCube[OP_MOD][TYPE_OCTAL][TYPE_OCTAL] = "int";
        typeCube[OP_MOD][TYPE_OCTAL][TYPE_HEXADECIMAL] = "int";

        // hexadecimal % TYPE
        typeCube[OP_MOD][TYPE_HEXADECIMAL][TYPE_INT] = "int";
        typeCube[OP_MOD][TYPE_HEXADECIMAL][TYPE_BINARY] = "int";
        typeCube[OP_MOD][TYPE_HEXADECIMAL][TYPE_OCTAL] = "int";
        typeCube[OP_MOD][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "int";

        // Assignment Operator
        // int = TYPE
        typeCube[OP_ASSIGN][TYPE_INT][TYPE_INT] = "OK";
        typeCube[OP_ASSIGN][TYPE_INT][TYPE_FLOAT] = "OK";
        typeCube[OP_ASSIGN][TYPE_INT][TYPE_BINARY] = "OK";
        typeCube[OP_ASSIGN][TYPE_INT][TYPE_OCTAL] = "OK";
        typeCube[OP_ASSIGN][TYPE_INT][TYPE_HEXADECIMAL] = "OK";

        // float = TYPE
        typeCube[OP_ASSIGN][TYPE_FLOAT][TYPE_INT] = "OK";
        typeCube[OP_ASSIGN][TYPE_FLOAT][TYPE_FLOAT] = "OK";
        typeCube[OP_ASSIGN][TYPE_FLOAT][TYPE_BINARY] = "OK";
        typeCube[OP_ASSIGN][TYPE_FLOAT][TYPE_OCTAL] = "OK";
        typeCube[OP_ASSIGN][TYPE_FLOAT][TYPE_HEXADECIMAL] = "OK";

        // boolean = TYPE
        typeCube[OP_ASSIGN][TYPE_BOOLEAN][TYPE_BOOLEAN] = "OK";

        // char = char
        typeCube[OP_ASSIGN][TYPE_CHAR][TYPE_CHAR] = "OK";

        // string = TYPE
        typeCube[OP_ASSIGN][TYPE_STRING][TYPE_STRING] = "OK";

        // binary = TYPE
        typeCube[OP_ASSIGN][TYPE_BINARY][TYPE_INT] = "OK";
        typeCube[OP_ASSIGN][TYPE_BINARY][TYPE_BINARY] = "OK";
        typeCube[OP_ASSIGN][TYPE_BINARY][TYPE_OCTAL] = "OK";
        typeCube[OP_ASSIGN][TYPE_BINARY][TYPE_HEXADECIMAL] = "OK";

        // octal = TYPE
        typeCube[OP_ASSIGN][TYPE_OCTAL][TYPE_INT] = "OK";
        typeCube[OP_ASSIGN][TYPE_OCTAL][TYPE_BINARY] = "OK";
        typeCube[OP_ASSIGN][TYPE_OCTAL][TYPE_OCTAL] = "OK";
        typeCube[OP_ASSIGN][TYPE_OCTAL][TYPE_HEXADECIMAL] = "OK";

        // hexadecimal = TYPE
        typeCube[OP_ASSIGN][TYPE_HEXADECIMAL][TYPE_INT] = "OK";
        typeCube[OP_ASSIGN][TYPE_HEXADECIMAL][TYPE_BINARY] = "OK";
        typeCube[OP_ASSIGN][TYPE_HEXADECIMAL][TYPE_OCTAL] = "OK";
        typeCube[OP_ASSIGN][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "OK";

        // Logical AND
        // boolean && TYPE
        typeCube[OP_AND][TYPE_BOOLEAN][TYPE_BOOLEAN] = "boolean";

        // Logical OR
        // boolean && TYPE
        typeCube[OP_OR][TYPE_BOOLEAN][TYPE_BOOLEAN] = "boolean";

        // Less than Operator
        // int < TYPE
        typeCube[OP_LESS][TYPE_INT][TYPE_INT] = "boolean";
        typeCube[OP_LESS][TYPE_INT][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESS][TYPE_INT][TYPE_BINARY] = "boolean";
        typeCube[OP_LESS][TYPE_INT][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESS][TYPE_INT][TYPE_HEXADECIMAL] = "boolean";

        // float < TYPE
        typeCube[OP_LESS][TYPE_FLOAT][TYPE_INT] = "boolean";
        typeCube[OP_LESS][TYPE_FLOAT][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESS][TYPE_FLOAT][TYPE_BINARY] = "boolean";
        typeCube[OP_LESS][TYPE_FLOAT][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESS][TYPE_FLOAT][TYPE_HEXADECIMAL] = "boolean";

        // binary < TYPE
        typeCube[OP_LESS][TYPE_BINARY][TYPE_INT] = "boolean";
        typeCube[OP_LESS][TYPE_BINARY][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESS][TYPE_BINARY][TYPE_BINARY] = "boolean";
        typeCube[OP_LESS][TYPE_BINARY][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESS][TYPE_BINARY][TYPE_HEXADECIMAL] = "boolean";

        // octal < TYPE
        typeCube[OP_LESS][TYPE_OCTAL][TYPE_INT] = "boolean";
        typeCube[OP_LESS][TYPE_OCTAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESS][TYPE_OCTAL][TYPE_BINARY] = "boolean";
        typeCube[OP_LESS][TYPE_OCTAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESS][TYPE_OCTAL][TYPE_HEXADECIMAL] = "boolean";

        // hexadecimal < TYPE
        typeCube[OP_LESS][TYPE_HEXADECIMAL][TYPE_INT] = "boolean";
        typeCube[OP_LESS][TYPE_HEXADECIMAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESS][TYPE_HEXADECIMAL][TYPE_BINARY] = "boolean";
        typeCube[OP_LESS][TYPE_HEXADECIMAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESS][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "boolean";

        // Greater than Operator
        // int > TYPE
        typeCube[OP_GREATER][TYPE_INT][TYPE_INT] = "boolean";
        typeCube[OP_GREATER][TYPE_INT][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATER][TYPE_INT][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATER][TYPE_INT][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATER][TYPE_INT][TYPE_HEXADECIMAL] = "boolean";

        // float > TYPE
        typeCube[OP_GREATER][TYPE_FLOAT][TYPE_INT] = "boolean";
        typeCube[OP_GREATER][TYPE_FLOAT][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATER][TYPE_FLOAT][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATER][TYPE_FLOAT][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATER][TYPE_FLOAT][TYPE_HEXADECIMAL] = "boolean";

        // binary > TYPE
        typeCube[OP_GREATER][TYPE_BINARY][TYPE_INT] = "boolean";
        typeCube[OP_GREATER][TYPE_BINARY][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATER][TYPE_BINARY][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATER][TYPE_BINARY][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATER][TYPE_BINARY][TYPE_HEXADECIMAL] = "boolean";

        // octal > TYPE
        typeCube[OP_GREATER][TYPE_OCTAL][TYPE_INT] = "boolean";
        typeCube[OP_GREATER][TYPE_OCTAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATER][TYPE_OCTAL][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATER][TYPE_OCTAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATER][TYPE_OCTAL][TYPE_HEXADECIMAL] = "boolean";

        // hexadecimal > TYPE
        typeCube[OP_GREATER][TYPE_HEXADECIMAL][TYPE_INT] = "boolean";
        typeCube[OP_GREATER][TYPE_HEXADECIMAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATER][TYPE_HEXADECIMAL][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATER][TYPE_HEXADECIMAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATER][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "boolean";

        // Less equal than Operator
        typeCube[OP_LESSEQ][TYPE_INT][TYPE_INT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_INT][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_INT][TYPE_BINARY] = "boolean";
        typeCube[OP_LESSEQ][TYPE_INT][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESSEQ][TYPE_INT][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_LESSEQ][TYPE_FLOAT][TYPE_INT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_FLOAT][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_FLOAT][TYPE_BINARY] = "boolean";
        typeCube[OP_LESSEQ][TYPE_FLOAT][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESSEQ][TYPE_FLOAT][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_LESSEQ][TYPE_BINARY][TYPE_INT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_BINARY][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_BINARY][TYPE_BINARY] = "boolean";
        typeCube[OP_LESSEQ][TYPE_BINARY][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESSEQ][TYPE_BINARY][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_LESSEQ][TYPE_OCTAL][TYPE_INT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_OCTAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_OCTAL][TYPE_BINARY] = "boolean";
        typeCube[OP_LESSEQ][TYPE_OCTAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESSEQ][TYPE_OCTAL][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_LESSEQ][TYPE_HEXADECIMAL][TYPE_INT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_HEXADECIMAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_LESSEQ][TYPE_HEXADECIMAL][TYPE_BINARY] = "boolean";
        typeCube[OP_LESSEQ][TYPE_HEXADECIMAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_LESSEQ][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "boolean";

        // Greater equal than Operator
        typeCube[OP_GREATEREQ][TYPE_INT][TYPE_INT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_INT][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_INT][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_INT][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_INT][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_GREATEREQ][TYPE_FLOAT][TYPE_INT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_FLOAT][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_FLOAT][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_FLOAT][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_FLOAT][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_GREATEREQ][TYPE_BINARY][TYPE_INT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_BINARY][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_BINARY][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_BINARY][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_BINARY][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_GREATEREQ][TYPE_OCTAL][TYPE_INT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_OCTAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_OCTAL][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_OCTAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_OCTAL][TYPE_HEXADECIMAL] = "boolean";

        typeCube[OP_GREATEREQ][TYPE_HEXADECIMAL][TYPE_INT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_HEXADECIMAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_HEXADECIMAL][TYPE_BINARY] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_HEXADECIMAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_GREATEREQ][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "boolean";

        // Equal Operator
        // int == TYPE
        typeCube[OP_EQUAL][TYPE_INT][TYPE_INT] = "boolean";
        typeCube[OP_EQUAL][TYPE_INT][TYPE_FLOAT] = "boolean";
        typeCube[OP_EQUAL][TYPE_INT][TYPE_BINARY] = "boolean";
        typeCube[OP_EQUAL][TYPE_INT][TYPE_OCTAL] = "boolean";
        typeCube[OP_EQUAL][TYPE_INT][TYPE_HEXADECIMAL] = "boolean";

        // float == TYPE
        typeCube[OP_EQUAL][TYPE_FLOAT][TYPE_INT] = "boolean";
        typeCube[OP_EQUAL][TYPE_FLOAT][TYPE_FLOAT] = "boolean";
        typeCube[OP_EQUAL][TYPE_FLOAT][TYPE_BINARY] = "boolean";
        typeCube[OP_EQUAL][TYPE_FLOAT][TYPE_OCTAL] = "boolean";
        typeCube[OP_EQUAL][TYPE_FLOAT][TYPE_HEXADECIMAL] = "boolean";

        // boolean == TYPE
        typeCube[OP_EQUAL][TYPE_BOOLEAN][TYPE_BOOLEAN] = "boolean";

        // char == TYPE
        typeCube[OP_EQUAL][TYPE_CHAR][TYPE_CHAR] = "boolean";

        // string == TYPE
        typeCube[OP_EQUAL][TYPE_STRING][TYPE_STRING] = "boolean";

        // binary == TYPE
        typeCube[OP_EQUAL][TYPE_BINARY][TYPE_INT] = "boolean";
        typeCube[OP_EQUAL][TYPE_BINARY][TYPE_FLOAT] = "boolean";
        typeCube[OP_EQUAL][TYPE_BINARY][TYPE_BINARY] = "boolean";
        typeCube[OP_EQUAL][TYPE_BINARY][TYPE_OCTAL] = "boolean";
        typeCube[OP_EQUAL][TYPE_BINARY][TYPE_HEXADECIMAL] = "boolean";

        // octal == TYPE
        typeCube[OP_EQUAL][TYPE_OCTAL][TYPE_INT] = "boolean";
        typeCube[OP_EQUAL][TYPE_OCTAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_EQUAL][TYPE_OCTAL][TYPE_BINARY] = "boolean";
        typeCube[OP_EQUAL][TYPE_OCTAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_EQUAL][TYPE_OCTAL][TYPE_HEXADECIMAL] = "boolean";

        // hexadecimal == TYPE
        typeCube[OP_EQUAL][TYPE_HEXADECIMAL][TYPE_INT] = "boolean";
        typeCube[OP_EQUAL][TYPE_HEXADECIMAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_EQUAL][TYPE_HEXADECIMAL][TYPE_BINARY] = "boolean";
        typeCube[OP_EQUAL][TYPE_HEXADECIMAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_EQUAL][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "boolean";

        // Not equal Operator
        // int != TYPE
        typeCube[OP_NOTEQUAL][TYPE_INT][TYPE_INT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_INT][TYPE_FLOAT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_INT][TYPE_BINARY] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_INT][TYPE_OCTAL] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_INT][TYPE_HEXADECIMAL] = "boolean";

        // float != TYPE
        typeCube[OP_NOTEQUAL][TYPE_FLOAT][TYPE_INT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_FLOAT][TYPE_FLOAT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_FLOAT][TYPE_BINARY] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_FLOAT][TYPE_OCTAL] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_FLOAT][TYPE_HEXADECIMAL] = "boolean";

        // boolean != TYPE
        typeCube[OP_NOTEQUAL][TYPE_BOOLEAN][TYPE_BOOLEAN] = "boolean";

        // char != TYPE
        typeCube[OP_NOTEQUAL][TYPE_CHAR][TYPE_CHAR] = "boolean";

        // string != TYPE
        typeCube[OP_NOTEQUAL][TYPE_STRING][TYPE_STRING] = "boolean";

        // binary != TYPE
        typeCube[OP_NOTEQUAL][TYPE_BINARY][TYPE_INT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_BINARY][TYPE_FLOAT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_BINARY][TYPE_BINARY] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_BINARY][TYPE_OCTAL] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_BINARY][TYPE_HEXADECIMAL] = "boolean";

        // octal != TYPE
        typeCube[OP_NOTEQUAL][TYPE_OCTAL][TYPE_INT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_OCTAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_OCTAL][TYPE_BINARY] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_OCTAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_OCTAL][TYPE_HEXADECIMAL] = "boolean";

        // hexadecimal != TYPE
        typeCube[OP_NOTEQUAL][TYPE_HEXADECIMAL][TYPE_INT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_HEXADECIMAL][TYPE_FLOAT] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_HEXADECIMAL][TYPE_BINARY] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_HEXADECIMAL][TYPE_OCTAL] = "boolean";
        typeCube[OP_NOTEQUAL][TYPE_HEXADECIMAL][TYPE_HEXADECIMAL] = "boolean";

        // Logical NOT Operator
        typeCube[OP_NOT][TYPE_BOOLEAN][TYPE_BOOLEAN] = "boolean";
    }

    private int typeIndexOf(String type) {
        return switch (type.toLowerCase()) {
            case "int" -> TYPE_INT;
            case "float" -> TYPE_FLOAT;
            case "boolean" -> TYPE_BOOLEAN;
            case "char" -> TYPE_CHAR;
            case "string" -> TYPE_STRING;
            case "binary" -> TYPE_BINARY;
            case "octal" -> TYPE_OCTAL;
            case "hexadecimal" -> TYPE_HEXADECIMAL;
            case "void" -> TYPE_VOID;
            default -> -1;
        };
    }

    private int operatorIndexOf(String operator) {
        return switch (operator) {
            case "+" -> OP_PLUS;
            case "-" -> OP_MINUS;
            case "*" -> OP_MULT;
            case "/" -> OP_DIV;
            case "%" -> OP_MOD;
            case "=" -> OP_ASSIGN;
            case "&&", "&" -> OP_AND;
            case "||", "|" -> OP_OR;
            case "<" -> OP_LESS;
            case ">" -> OP_GREATER;
            case "<=" -> OP_LESSEQ;
            case ">=" -> OP_GREATEREQ;
            case "==" -> OP_EQUAL;
            case "!=" -> OP_NOTEQUAL;
            case "!" -> OP_NOT;
            default -> -1;
        };
    }
}
