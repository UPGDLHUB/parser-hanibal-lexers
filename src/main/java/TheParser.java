import java.util.Set;
import java.util.Vector;

public class TheParser {


    private final Vector<TheToken> tokens;
    private int currentToken;
    private int errorCount;
    private SemanticAnalizer semanticAnalizer;
    private Vector<Vector<String>> semanticNamesTypes = new Vector<>();
    private int repeatedForScopeCounter = 0;
    private int repeatedSwitchScopeCounter = 0;
    private int repeatedWhileScopeCounter = 0;
    private int repeatedDoWhileScopeCounter = 0;
    private int repeatedIfScopeCounter = 0;

    public TheParser(Vector<TheToken> tokens) {
        this.tokens = tokens;
        this.currentToken = 0;
        this.errorCount = 0;
        this.semanticAnalizer = new SemanticAnalizer();
    }

    private static int indent = 0;

    private void enterRule(String name) {
        System.out.println(" ".repeat(indent * 2) + "- " + name);
        indent++;
    }

    private void exitRule() { indent = Math.max(0, indent - 1); }

    private void found(String msg) {
        System.out.println(" ".repeat(indent * 2) + "-- " + msg);
    }


    private boolean isType(String v) {
        return switch (v) {
            case "int", "boolean", "float", "void", "char", "string" -> true;
            default -> false;
        };
    }

    private boolean expectValue(String v, String rule) {
        if (currentToken < tokens.size() &&
                tokens.get(currentToken).getValue().equals(v)) {
            found("Found '" + v + "'");
            currentToken++;
            return true;
        }
        error(rule, "'" + v + "'");
        return false;
    }

    private boolean expectIdentifier(String rule) {
        if (currentToken < tokens.size() &&
                tokens.get(currentToken).getType().equals("ID")) {
            found("Identifier: " + tokens.get(currentToken).getValue());
            currentToken++;
            return true;
        }
        error(rule, "identifier");
        return false;
    }

    private boolean expectType(String rule) {
        if (currentToken < tokens.size() &&
                isType(tokens.get(currentToken).getValue())) {
            found("Type: " + tokens.get(currentToken).getValue());
            currentToken++;
            return true;
        }
        error(rule, "type");
        return false;
    }

    private static final Set<String> SYNC = Set.of(
            ";", "}", ",", ")", "class", "if", "for", "while",
            "do", "switch", "return"
    );


    private void error(String rule, String expected) {
        if (currentToken >= tokens.size()) return;

        System.err.printf("%s: expected %s at %s%n",
                rule, expected, tokens.get(currentToken).getValue());
        errorCount++;
        if (!SYNC.contains(tokens.get(currentToken).getValue()))
            currentToken++;                    // discard exactly one token
    }


    public int run() {
        RULE_PROGRAM();
        semanticAnalizer.printSymbolTable();
        if (errorCount > 0 || semanticAnalizer.getErrorCount() > 0) {
            System.err.println("Errors found: " + errorCount + " sintax, " + semanticAnalizer.getErrorCount() + " semantic");
        }
        else { System.out.println("Parsed Successfully"); }
        return errorCount;
    }


    private void RULE_PROGRAM() {
        enterRule("RULE_PROGRAM");
        try {
            expectValue("class", "RULE_PROGRAM");
            expectIdentifier("RULE_PROGRAM");
            expectValue("{", "RULE_PROGRAM");

            while (currentToken < tokens.size() &&
                    !tokens.get(currentToken).getValue().equals("}")) {
                if (isType(tokens.get(currentToken).getValue())) {
                    int startToken = currentToken;
                    call(this::RULE_TYPE, "type");
                    if (!expectIdentifier("RULE_PROGRAM")) continue;
                    if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
                        currentToken = startToken;
                        call(this::RULE_METHODS, "method");
                    } else {
                        currentToken = startToken;
                        call(this::RULE_VARIABLE, "variable");
                        expectValue(";", "RULE_PROGRAM");
                    }
                } else {
                    error("RULE_PROGRAM", "method or variable declaration");
                }
            }
            expectValue("}", "RULE_PROGRAM");
        } finally { exitRule(); }
    }

    private void RULE_METHODS() {
        enterRule("RULE_METHODS");
        try {
            String methodType = tokens.get(currentToken).getValue();
            call(this::RULE_TYPE, "type");

            String methodName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_METHODS");

            semanticAnalizer.enterScope("function");
            semanticAnalizer.checkVariable(methodName, methodType, "");

            expectValue("(", "RULE_METHODS");
            if (!tokens.get(currentToken).getValue().equals(")"))
                call(this::RULE_PARAMS, "params");

            StringBuilder methodScope = new StringBuilder(methodName);
            for (Vector<String> nameType : semanticNamesTypes) {
                methodScope.append("-").append(nameType.get(1)).append('_').append(nameType.get(0));
            }
            semanticAnalizer.enterScope(methodScope.toString());
            for (Vector<String> nameType : semanticNamesTypes) {
                semanticAnalizer.checkVariable(nameType.get(0), nameType.get(1), "");
            }
            semanticNamesTypes.clear();

            expectValue(")", "RULE_METHODS");
            expectValue("{", "RULE_METHODS");

            while (currentToken < tokens.size() &&
                    !tokens.get(currentToken).getValue().equals("}")) {
                call(this::RULE_BODY, "body");
            }
            expectValue("}", "RULE_METHODS");
            for (Vector<String> nameType : semanticNamesTypes) {
                semanticAnalizer.checkVariable(nameType.get(0), nameType.get(1), "");
            }
            semanticAnalizer.exitScope();
        } finally {
            semanticAnalizer.exitScope();
            semanticNamesTypes.clear();
            repeatedForScopeCounter = 0;
            repeatedSwitchScopeCounter = 0;
            repeatedWhileScopeCounter = 0;
            repeatedDoWhileScopeCounter = 0;
            repeatedIfScopeCounter = 0;
            exitRule();
        }
    }

    private void RULE_RETURN() {
        enterRule("RULE_RETURN");
        try {
            expectValue("return", "RULE_RETURN");
            if (!tokens.get(currentToken).getValue().equals(";") &&
                    !tokens.get(currentToken).getValue().equals("}")) {
                call(this::RULE_EXPRESSION, "expression");
            }
        } finally { exitRule(); }
    }

    private void RULE_DO_WHILE() {
        enterRule("RULE_DO_WHILE");
        try {
            expectValue("do", "RULE_DO_WHILE");

            semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-do_while_" + repeatedDoWhileScopeCounter);
            if (tokens.get(currentToken).getValue().equals("{")) {
                expectValue("{", "RULE_DO_WHILE");
                while (currentToken < tokens.size() &&
                        !tokens.get(currentToken).getValue().equals("}")) {
                    call(this::RULE_BODY, "body");
                }
                expectValue("}", "RULE_DO_WHILE");
            } else {
                call(this::RULE_BODY, "body");
            }
            semanticAnalizer.exitScope();

            expectValue("while", "RULE_DO_WHILE");
            expectValue("(", "RULE_DO_WHILE");
            call(this::RULE_EXPRESSION, "expression");
            expectValue(")", "RULE_DO_WHILE");
            expectValue(";", "RULE_DO_WHILE");
        } finally {
            repeatedDoWhileScopeCounter++;
            exitRule();
        }
    }

    private void RULE_SWITCH() {
        enterRule("RULE_SWITCH");
        try {
            expectValue("switch", "RULE_SWITCH");
            expectValue("(",      "RULE_SWITCH");
            call(this::RULE_EXPRESSION, "expression");
            expectValue(")",      "RULE_SWITCH");
            expectValue("{",      "RULE_SWITCH");
            int caseCount = 0;
            while (currentToken < tokens.size() &&
                    tokens.get(currentToken).getValue().equals("case")) {
                expectValue("case", "RULE_SWITCH");

                String tp = tokens.get(currentToken).getType();
                String v = tokens.get(currentToken).getValue();

                if (Set.of("INTEGER","OCTAL","HEXADECIMAL","BINARY","STRING",
                        "CHAR").contains(tp) || v.equals("true") || v.equals("false")) {
                    found("Literal: " + v);
                    currentToken++;
                } else if (tp.equals("ID")) {
                    expectIdentifier("RULE_SWITCH");
                } else {
                    error("RULE_SWITCH", "case label (literal or identifier)");
                    // Try to recover by skipping until the colon
                    while (currentToken < tokens.size() &&
                            !tokens.get(currentToken).getValue().equals(":")) {
                        currentToken++;
                    }
                }

                expectValue(":", "RULE_SWITCH");
                while (currentToken < tokens.size() &&
                        !Set.of("case", "default", "}").contains(
                                tokens.get(currentToken).getValue())) {
                    semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-switch_" + repeatedSwitchScopeCounter + "_case_" + ++caseCount);
                    call(this::RULE_BODY, "body");
                    semanticAnalizer.exitScope();
                }
            }
            if (tokens.get(currentToken).getValue().equals("default")) {
                expectValue("default", "RULE_SWITCH");
                expectValue(":",       "RULE_SWITCH");
                semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-switch_" + repeatedSwitchScopeCounter + "default");
                while (currentToken < tokens.size() &&
                        !tokens.get(currentToken).getValue().equals("}")) {
                    call(this::RULE_BODY, "body");
                }
                semanticAnalizer.exitScope();
            }

            expectValue("}", "RULE_SWITCH");
        } finally {
            repeatedSwitchScopeCounter++;
            exitRule();
        }
    }

    private void RULE_PRINT() {
        enterRule("RULE_PRINT");
        try {
            expectValue("print", "RULE_PRINT");
            expectValue("(",     "RULE_PRINT");
            call(this::RULE_EXPRESSION, "expression");
            expectValue(")", "RULE_PRINT");
            expectValue(";", "RULE_PRINT");
        } finally { exitRule(); }
    }

    private void RULE_BODY() {
        enterRule("RULE_BODY");
        try {
            String v = tokens.get(currentToken).getValue();

            switch (v) {

                case "return" -> {
                    call(this::RULE_RETURN, "return");
                    expectValue(";", "RULE_BODY");
                }
                case "while"  -> call(this::RULE_WHILE,  "while");
                case "do"     -> call(this::RULE_DO_WHILE,"doWhile");
                case "for"    -> call(this::RULE_FOR,    "for");
                case "switch" -> call(this::RULE_SWITCH, "switch");
                case "if"     -> call(this::RULE_IF,     "if");
                case "print"  -> call(this::RULE_PRINT,  "print");


                case "break", "continue" -> {
                    currentToken++;                       // consume keyword
                    expectValue(";", "RULE_BODY");
                }


                case ";" -> {
                    while (currentToken < tokens.size() &&
                            tokens.get(currentToken).getValue().equals(";"))
                        currentToken++;                   // absorb all stray ;
                }


                default -> {

                    if (isType(v)) {
                        call(this::RULE_VARIABLE, "variable");
                        expectValue(";", "RULE_BODY");
                    }

                    else if (tokens.get(currentToken).getType().equals("ID")) {
                        String nxt = tokens.get(currentToken + 1).getValue();

                        if (nxt.equals("(")) {                 // method call
                            call(this::RULE_CALL_METHOD, "callMethod");
                            expectValue(";", "RULE_BODY");
                        } else if (nxt.equals("=")) {           // assignment
                            call(this::RULE_ASSIGNMENT, "assignment");
                            expectValue(";", "RULE_BODY");
                        } else {

                            error("RULE_BODY", "assignment or call");

                            while (currentToken < tokens.size() &&
                                    !Set.of(";", "}").contains(
                                            tokens.get(currentToken).getValue())) {
                                currentToken++;                 // discard tokens
                            }
                            if (currentToken < tokens.size() &&
                                    tokens.get(currentToken).getValue().equals(";"))
                                currentToken++;                 // eat the ';'

                            return;   // give control back to caller loop
                        }
                    }

                    else {
                        error("RULE_BODY", "statement");


                        currentToken++;
                        return;
                    }
                }
            }
        } finally { exitRule(); }
    }


    private void RULE_ASSIGNMENT() {
        enterRule("RULE_ASSIGNMENT");
        try {
            String assignementName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_ASSIGNMENT");
            expectValue("=", "RULE_ASSIGNMENT");
            call(this::RULE_EXPRESSION, "expression");
            // Store value to table id
        } finally { exitRule(); }
    }

    private void RULE_CALL_METHOD() {
        enterRule("RULE_CALL_METHOD");
        try {
            expectIdentifier("RULE_CALL_METHOD");
            expectValue("(", "RULE_CALL_METHOD");
            call(this::RULE_PARAM_VALUES, "paramValues");
            expectValue(")", "RULE_CALL_METHOD");
        } finally { exitRule(); }
    }

    private void RULE_PARAM_VALUES() {
        enterRule("RULE_PARAM_VALUES");
        try {
            if (tokens.get(currentToken).getValue().equals(")")) return;

            call(this::RULE_EXPRESSION, "expression");

            while (currentToken < tokens.size() &&
                    tokens.get(currentToken).getValue().equals(",")) {
                expectValue(",", "RULE_PARAM_VALUES");
                call(this::RULE_EXPRESSION, "expression");
            }
        } finally { exitRule(); }
    }


    private void RULE_EXPRESSION() {
        enterRule("RULE_EXPRESSION");
        try {
            call(this::RULE_X, "x");
            while (Set.of("|", "||").contains(tokens.get(currentToken).getValue())) {
                found("Operator |");
                currentToken++;
                call(this::RULE_X, "x");
            }
        } finally { exitRule(); }
    }

    private void RULE_X() {
        enterRule("RULE_X");
        try {
            call(this::RULE_Y, "y");
            while (Set.of("&", "&&").contains(tokens.get(currentToken).getValue())) {
                found("Operator &");
                currentToken++;
                call(this::RULE_Y, "y");
            }
        } finally { exitRule(); }
    }

    private void RULE_Y() {
        enterRule("RULE_Y");
        try {
            while (tokens.get(currentToken).getValue().equals("!")) {
                found("!");
                currentToken++;
            }
            call(this::RULE_R, "R");
        } finally { exitRule(); }
    }

    private void RULE_R() {
        enterRule("RULE_R");
        try {
            call(this::RULE_E, "E");
            while (Set.of("<", ">", "==", "!=")
                    .contains(tokens.get(currentToken).getValue())) {
                found("Relational op");
                currentToken++;
                call(this::RULE_E, "E");
            }
        } finally { exitRule(); }
    }

    private void RULE_E() {
        enterRule("RULE_E");
        try {
            call(this::RULE_A, "A");
            while (Set.of("+", "-").contains(tokens.get(currentToken).getValue())) {
                found("Additive op");
                currentToken++;
                call(this::RULE_A, "A");
            }
        } finally { exitRule(); }
    }

    private void RULE_A() {
        enterRule("RULE_A");
        try {
            call(this::RULE_B, "B");
            while (Set.of("*", "/").contains(tokens.get(currentToken).getValue())) {
                found("Mul op");
                currentToken++;
                call(this::RULE_B, "B");
            }
        } finally { exitRule(); }
    }

    private void RULE_B() {
        enterRule("RULE_B");
        try {
            if (tokens.get(currentToken).getValue().equals("-")) {
                found("Unary -");
                currentToken++;
            }
            call(this::RULE_C, "C");
        } finally { exitRule(); }
    }

    private void RULE_C() {
        enterRule("RULE_C");
        try {
            String v  = tokens.get(currentToken).getValue();
            String tp = tokens.get(currentToken).getType();

            if (Set.of("INTEGER","OCTAL","HEXADECIMAL","BINARY","STRING",
                    "CHAR","FLOAT").contains(tp) || v.equals("true")||v.equals("false")) {
                found("Literal " + v);
                currentToken++;
            } else if (tp.equals("ID")) {
                expectIdentifier("RULE_C");
                if (tokens.get(currentToken).getValue().equals("(")) {
                    expectValue("(", "RULE_C");
                    call(this::RULE_PARAM_VALUES, "paramValues");
                    expectValue(")", "RULE_C");
                }
            } else if (expectValue("(", "RULE_C")) {
                call(this::RULE_EXPRESSION, "expression");
                expectValue(")", "RULE_C");
            } else {
                error("RULE_C", "expression atom");
                currentToken++;               // consume offending token
            }
        } finally { exitRule(); }
    }

    private void RULE_TYPE() {
        enterRule("RULE_TYPE");
        try { expectType("RULE_TYPE"); }
        finally { exitRule(); }
    }

    private void RULE_PARAMS() {
        enterRule("RULE_PARAMS");
        try {
            if (tokens.get(currentToken).getValue().equals(")")) return;

            String paramType = tokens.get(currentToken).getValue();
            call(this::RULE_TYPE, "type");

            String paramName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_PARAMS");

            Vector<String> ps = new Vector<>();
            ps.add(paramName);
            ps.add(paramType);
            semanticNamesTypes.add(ps);

            while (currentToken < tokens.size() &&
                    tokens.get(currentToken).getValue().equals(",")) {
                expectValue(",", "RULE_PARAMS");
                paramType = tokens.get(currentToken).getValue();
                call(this::RULE_TYPE, "type");
                paramName = tokens.get(currentToken).getValue();
                expectIdentifier("RULE_PARAMS");
                Vector<String> psr = new Vector<>();
                psr.add(paramName);
                psr.add(paramType);
                semanticNamesTypes.add(psr);
            }
        } finally { exitRule(); }
    }

    private void RULE_FOR() {
        enterRule("RULE_FOR");
        try {
            expectValue("for", "RULE_FOR");
            expectValue("(",   "RULE_FOR");
            if (!tokens.get(currentToken).getValue().equals(";")) {
                if (isType(tokens.get(currentToken).getValue())) {
                    semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-for_" + repeatedForScopeCounter);
                    call(this::RULE_VARIABLE, "variable");
                    semanticAnalizer.exitScope();
                } else
                    call(this::RULE_ASSIGNMENT, "assignment");
            }
            expectValue(";", "RULE_FOR");

            if (!tokens.get(currentToken).getValue().equals(";"))
                call(this::RULE_EXPRESSION, "expression");
            expectValue(";", "RULE_FOR");

            if (!tokens.get(currentToken).getValue().equals(")"))
                call(this::RULE_ASSIGNMENT, "assignment");
            expectValue(")", "RULE_FOR");

            semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-for_" + repeatedForScopeCounter);
            if (tokens.get(currentToken).getValue().equals("{")) {
                expectValue("{", "RULE_FOR");
                while (currentToken < tokens.size() &&
                        !tokens.get(currentToken).getValue().equals("}")) {
                    call(this::RULE_BODY, "body");
                }
                expectValue("}", "RULE_FOR");
            } else {
                call(this::RULE_BODY, "body");
            }
        } finally {
            repeatedForScopeCounter++;
            semanticAnalizer.exitScope();
            exitRule();
        }
    }

    private void RULE_WHILE() {
        enterRule("RULE_WHILE");
        try {
            expectValue("while", "RULE_WHILE");
            expectValue("(",     "RULE_WHILE");
            call(this::RULE_EXPRESSION, "expression");
            expectValue(")", "RULE_WHILE");

            semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-while_" + repeatedWhileScopeCounter);
            if (tokens.get(currentToken).getValue().equals("{")) {
                expectValue("{", "RULE_WHILE");
                while (currentToken < tokens.size() &&
                        !tokens.get(currentToken).getValue().equals("}")) {
                    call(this::RULE_BODY, "body");
                }
                expectValue("}", "RULE_WHILE");
            } else {
                call(this::RULE_BODY, "body");
            }
        } finally {
            repeatedWhileScopeCounter++;
            semanticAnalizer.exitScope();
            exitRule();
        }
    }

    private void RULE_VARIABLE() {
        enterRule("RULE_VARIABLE");
        try {
            String type = tokens.get(currentToken).getValue();
            call(this::RULE_TYPE, "type");
            String id = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_VARIABLE");
            semanticAnalizer.checkVariable(id, type, "");
            if (tokens.get(currentToken).getValue().equals("=")) {
                found("=");
                currentToken++;
                call(this::RULE_EXPRESSION, "expression");
                // store value
            }
        } finally { exitRule(); }
    }

    private void RULE_IF() {
        enterRule("RULE_IF");
        try {
            expectValue("if", "RULE_IF");
            expectValue("(",  "RULE_IF");
            call(this::RULE_EXPRESSION, "expression");
            expectValue(")", "RULE_IF");

            semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-if_" + repeatedIfScopeCounter);
            if (tokens.get(currentToken).getValue().equals("{")) {
                expectValue("{", "RULE_IF");
                while (currentToken < tokens.size() &&
                        !tokens.get(currentToken).getValue().equals("}")) {
                    call(this::RULE_BODY, "body");
                }
                expectValue("}", "RULE_IF");
            } else {
                call(this::RULE_BODY, "body");
            }
            semanticAnalizer.exitScope();

            if (tokens.get(currentToken).getValue().equals("else")) {
                expectValue("else", "RULE_IF");
                if (tokens.get(currentToken).getValue().equals("if")) {
                    repeatedIfScopeCounter++;
                    call(this::RULE_IF, "if");
                } else {
                    semanticAnalizer.enterScope(semanticAnalizer.currentScope() + "-else_" + repeatedIfScopeCounter);
                    if (tokens.get(currentToken).getValue().equals("{")) {
                        expectValue("{", "RULE_IF");
                        while (currentToken < tokens.size() &&
                                !tokens.get(currentToken).getValue().equals("}")) {
                            call(this::RULE_BODY, "body");
                        }
                        expectValue("}", "RULE_IF");
                    } else {
                        call(this::RULE_BODY, "body");
                    }
                    semanticAnalizer.exitScope();
                }
            }
        } finally {
            repeatedIfScopeCounter++;
            exitRule();
        }
    }


    public void call(Runnable action, String ruleName) {
        while (currentToken < tokens.size()) {
            String val  = tokens.get(currentToken).getValue();
            String type = tokens.get(currentToken).getType();

            boolean inFirst =
                    FirstSets.FIRST_MAP.get(ruleName).contains(val)  ||
                            FirstSets.FIRST_MAP.get(ruleName).contains(type);

            if (inFirst) { action.run(); return; }

            error(ruleName, "token in FIRST set of " + ruleName + ", found " + val);

            if (currentToken >= tokens.size()) return;

            val  = tokens.get(currentToken).getValue();
            type = tokens.get(currentToken).getType();

            boolean inFollow =
                    FollowSets.FOLLOW_MAP.get(ruleName).contains(val)  ||
                            FollowSets.FOLLOW_MAP.get(ruleName).contains(type);

            if (inFollow) {
                found("Recovered at '" + val + "' for " + ruleName);
                return;
            }
        }
    }
}
