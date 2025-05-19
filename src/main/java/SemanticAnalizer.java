import java.util.*;

public class SemanticAnalizer {
    private Hashtable<String, Vector<SymbolTableItem>> symbolTable;
    private Stack<String> scopeStack;
    private int errorCount;

    public SemanticAnalizer() {
        this.symbolTable = new Hashtable<>();
        this.scopeStack = new Stack<>();
        this.errorCount = 0;
        scopeStack.push("global");
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

    public int getErrorCount() {
        return errorCount;
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
        return switch (type) {
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
}
