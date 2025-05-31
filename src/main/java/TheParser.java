// TheParser.java

import java.util.Set;
import java.util.Vector;

/**
 * TheParser now extends ParserUtils so that all token‐handling helpers
 * (expectValue, expectIdentifier, expectType, call, peekValue, peekType, etc.)
 * come “for free.”  We only store semantic‐analysis state and define
 * RULE_* methods here.
 */
public class TheParser extends ParserUtils {

    // Semantic‐analysis fields
    private int errorCount;
    private SemanticAnalizer semanticAnalizer;

    // Temporary storage for <paramName, paramType> pairs in a method
    private Vector<Vector<String>> semanticNamesTypes = new Vector<>();

    // Current function’s declared return type (used inside RULE_RETURN)
    private String currentFunctionReturnType = null;

    public TheParser(Vector<TheToken> tokens) {
        super(tokens);
        this.errorCount = 0;
        this.semanticAnalizer = new SemanticAnalizer();
    }

    public int run() {
        RULE_PROGRAM();
        semanticAnalizer.printSymbolTable();

        int semErrors = semanticAnalizer.getErrorCount();
        if (errorCount > 0 || semErrors > 0) {
            System.err.println("Errors found: " + errorCount + " syntax, " + semErrors + " semantic");
        } else {
            System.out.println("Parsed Successfully");
        }
        return errorCount;
    }

    // ------------------------------------------------------------
    // RULE_PROGRAM
    //   - class <ID> { (method-decl | var-decl)* }
    //   Distinguish method vs variable by peeking “(” after ID.
    // ------------------------------------------------------------
    private void RULE_PROGRAM() {
        enterRule("RULE_PROGRAM");
        try {
            expectValue("class", "RULE_PROGRAM");
            expectIdentifier("RULE_PROGRAM");        // class name
            expectValue("{", "RULE_PROGRAM");

            while (currentToken < tokens.size()
                    && !peekValue().equals("}")) {

                if (isType(peekValue())) {
                    // Save start position:
                    int startTok = currentToken;

                    // Parse a type (could be return type or var type)
                    call(this::RULE_TYPE, "type");

                    // Next token must be an identifier (either method name or var name)
                    String name = tokens.get(currentToken).getValue();
                    expectIdentifier("RULE_PROGRAM");

                    // If next token is "(", it’s a method declaration; otherwise variable
                    if (currentToken < tokens.size()
                            && peekValue().equals("(")) {
                        // Reset back to “startTok” so RULE_METHODS sees the type again
                        currentToken = startTok;
                        call(this::RULE_METHODS, "method");
                    } else {
                        // Reset back to startTok so RULE_VARIABLE sees the type again
                        currentToken = startTok;
                        call(this::RULE_VARIABLE, "variable");
                        expectValue(";", "RULE_PROGRAM");
                    }
                } else {
                    error("RULE_PROGRAM", "method or variable declaration");
                }
            }
            expectValue("}", "RULE_PROGRAM");
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_METHODS
    //   - <returnType> <methodName>( [params] ) { body }
    // ------------------------------------------------------------
    private void RULE_METHODS() {
        enterRule("RULE_METHODS");
        try {
            // 1) Parse return type
            String methodType = peekValue();
            call(this::RULE_TYPE, "type");

            // 2) Parse method name (identifier)
            String methodName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_METHODS");

            // 3) Parse parameter list into semanticNamesTypes
            expectValue("(", "RULE_METHODS");
            if (!peekValue().equals(")")) {
                call(this::RULE_PARAMS, "params");
            }
            expectValue(")", "RULE_METHODS");

            // 4) Enter “function” group scope (once per class)
            semanticAnalizer.enterScope("function");
            // 4a) Insert the method name itself into that “function” scope
            semanticAnalizer.checkVariable(methodName, methodType, "");

            // 5) Enter the method’s own scope
            currentFunctionReturnType = methodType;
            String methodScope = methodName + "@" + methodType;
            semanticAnalizer.enterScope(methodScope);

            // 6) Insert parameters into the method’s new scope
            for (Vector<String> pair : semanticNamesTypes) {
                String paramName = pair.get(0);
                String paramType = pair.get(1);
                semanticAnalizer.checkVariable(paramName, paramType, "");
            }
            semanticNamesTypes.clear();

            // 7) Parse method body
            expectValue("{", "RULE_METHODS");
            while (currentToken < tokens.size()
                    && !peekValue().equals("}")) {
                call(this::RULE_BODY, "body");
            }
            expectValue("}", "RULE_METHODS");

            // 8) Exit the method’s own scope
            semanticAnalizer.exitScope();
            currentFunctionReturnType = null;

            // 9) Exit the “function” group scope
            semanticAnalizer.exitScope();

        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_PARAMS
    //   - <type> <id> (, <type> <id>)*
    //   Push each (name,type) into semanticNamesTypes
    // ------------------------------------------------------------
    private void RULE_PARAMS() {
        enterRule("RULE_PARAMS");
        try {
            // If next token is “)”, then params are empty
            if (peekValue().equals(")")) {
                return;
            }

            // Parse first parameter
            String paramType = peekValue();
            call(this::RULE_TYPE, "type");

            String paramName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_PARAMS");

            Vector<String> pair = new Vector<>();
            pair.add(paramName);
            pair.add(paramType);
            semanticNamesTypes.add(pair);

            // Zero or more “, <type> <id>”
            while (currentToken < tokens.size() && peekValue().equals(",")) {
                expectValue(",", "RULE_PARAMS");
                paramType = peekValue();
                call(this::RULE_TYPE, "type");
                paramName = tokens.get(currentToken).getValue();
                expectIdentifier("RULE_PARAMS");

                Vector<String> nextPair = new Vector<>();
                nextPair.add(paramName);
                nextPair.add(paramType);
                semanticNamesTypes.add(nextPair);
            }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_RETURN
    //   - return [expression];
    //   Compare expression type to currentFunctionReturnType
    // ------------------------------------------------------------
    private void RULE_RETURN() {
        enterRule("RULE_RETURN");
        try {
            expectValue("return", "RULE_RETURN");

            // Peek next to see if it's “;” or “}”
            String nextVal = currentToken < tokens.size() ? peekValue() : "";

            if (!nextVal.equals(";") && !nextVal.equals("}")) {
                // Parse an expression and check its type
                call(this::RULE_EXPRESSION, "expression");
                String exprType = semanticAnalizer.getLastExpressionType();

                if (currentFunctionReturnType == null) {
                    semanticAnalizer.reportError(
                            "No enclosing function return type for “return” at token " + currentToken);
                } else if (currentFunctionReturnType.equals("void")) {
                    semanticAnalizer.reportError(
                            "Method declares void but return has a value (“" + exprType + "”)");
                } else if (!exprType.equals(currentFunctionReturnType)) {
                    semanticAnalizer.reportError(
                            "Return‐type mismatch: expected “" + currentFunctionReturnType
                                    + "”, found “" + exprType + "” at token " + currentToken);
                }
            } else {
                // “return;” (no expression) only valid if return type is void
                if (currentFunctionReturnType != null && !currentFunctionReturnType.equals("void")) {
                    semanticAnalizer.reportError(
                            "Method declares “" + currentFunctionReturnType
                                    + "” but return has no value at token " + currentToken);
                }
            }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_DO_WHILE
    //   - do { body } while(expression);
    //   Enforce: “expression” must evaluate to boolean.
    // ------------------------------------------------------------
    private void RULE_DO_WHILE() {
        enterRule("RULE_DO_WHILE");
        try {
            expectValue("do", "RULE_DO_WHILE");

            // Enter do–while block scope
            String doWhileScope = "doWhile@" + currentToken;
            semanticAnalizer.enterScope(doWhileScope);
            try {
                expectValue("{", "RULE_DO_WHILE");
                while (currentToken < tokens.size() && !peekValue().equals("}")) {
                    call(this::RULE_BODY, "body");
                }
                expectValue("}", "RULE_DO_WHILE");
            } finally {
                semanticAnalizer.exitScope();
            }

            expectValue("while", "RULE_DO_WHILE");
            expectValue("(", "RULE_DO_WHILE");
            call(this::RULE_EXPRESSION, "expression");

            // ───────────────────────────────────────────────────────
            // TYPE CHECK: must be boolean, not null/void/other
            String condType = semanticAnalizer.getLastExpressionType();
            if (condType == null) {
                semanticAnalizer.reportError("'do-while' condition type is null/unknown at token " + currentToken);
            } else if (condType.equals("void")) {
                semanticAnalizer.reportError("'do-while' condition has type void (invalid) at token " + currentToken);
            } else if (!condType.equals("boolean")) {
                semanticAnalizer.reportError("'do-while' condition must be boolean (found '"
                        + condType + "') at token " + currentToken);
            }
            // ───────────────────────────────────────────────────────

            expectValue(")", "RULE_DO_WHILE");
            expectValue(";", "RULE_DO_WHILE");
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
// RULE_SWITCH
//   - switch(expression) { case <literal|id>: body* } [ default: body* ]
//   Enforce: “expression” must evaluate to int (no null/void/other).
// ------------------------------------------------------------
    private void RULE_SWITCH() {
        enterRule("RULE_SWITCH");
        try {
            expectValue("switch", "RULE_SWITCH");
            expectValue("(", "RULE_SWITCH");
            call(this::RULE_EXPRESSION, "expression");

            // ───────────────────────────────────────────────────────
            // TYPE CHECK: must be int, not null/void/other
            String switchType = semanticAnalizer.getLastExpressionType();
            if (switchType == null) {
                semanticAnalizer.reportError("'switch' expression type is null/unknown at token " + currentToken);
            } else if (switchType.equals("void")) {
                semanticAnalizer.reportError("'switch' expression has type void (invalid) at token " + currentToken);
            } else if (!switchType.equals("int")) {
                semanticAnalizer.reportError("'switch' expression must be int (found '"
                        + switchType + "') at token " + currentToken);
            }
            // ───────────────────────────────────────────────────────

            expectValue(")", "RULE_SWITCH");
            expectValue("{", "RULE_SWITCH");

            // Each “case” clause gets its own scope
            while (currentToken < tokens.size() && peekValue().equals("case")) {
                expectValue("case", "RULE_SWITCH");

                // Parse case label (literal or identifier)
                String tp = tokens.get(currentToken).getType();
                String v  = peekValue();
                if (Set.of("INTEGER", "OCTAL", "HEXADECIMAL", "BINARY", "STRING", "CHAR")
                        .contains(tp) || v.equals("true") || v.equals("false")) {
                    found("Literal: " + v);
                    currentToken++;
                } else if (tp.equals("ID")) {
                    expectIdentifier("RULE_SWITCH");
                } else {
                    error("RULE_SWITCH", "case label (literal or identifier)");
                    // Recover until “:”
                    while (currentToken < tokens.size() && !peekValue().equals(":")) {
                        currentToken++;
                    }
                }

                expectValue(":", "RULE_SWITCH");

                // Enter a fresh scope for this case
                String caseScope = "case@" + currentToken;
                semanticAnalizer.enterScope(caseScope);
                try {
                    while (currentToken < tokens.size()
                            && !Set.of("case", "default", "}").contains(peekValue())) {
                        call(this::RULE_BODY, "body");
                    }
                } finally {
                    semanticAnalizer.exitScope();
                }
            }

            // Optional “default:”
            if (currentToken < tokens.size() && peekValue().equals("default")) {
                expectValue("default", "RULE_SWITCH");
                expectValue(":", "RULE_SWITCH");

                String defaultScope = "default@" + currentToken;
                semanticAnalizer.enterScope(defaultScope);
                try {
                    while (currentToken < tokens.size() && !peekValue().equals("}")) {
                        call(this::RULE_BODY, "body");
                    }
                } finally {
                    semanticAnalizer.exitScope();
                }
            }

            expectValue("}", "RULE_SWITCH");
        } finally {
            exitRule();
        }
    }


    // ------------------------------------------------------------
    // RULE_PRINT
    //   - print(expression);
    // ------------------------------------------------------------
    private void RULE_PRINT() {
        enterRule("RULE_PRINT");
        try {
            expectValue("print", "RULE_PRINT");
            expectValue("(", "RULE_PRINT");
            call(this::RULE_EXPRESSION, "expression");
            expectValue(")", "RULE_PRINT");
            expectValue(";", "RULE_PRINT");
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_BODY
    //   - dispatch based on first token: return, while, do, for, switch, if, print,
    //     break/continue, semicolon, variable declaration, method call, assignment, or error.
    // ------------------------------------------------------------
    private void RULE_BODY() {
        enterRule("RULE_BODY");
        try {
            String v = peekValue();
            switch (v) {
                case "return" -> {
                    call(this::RULE_RETURN, "return");
                    expectValue(";", "RULE_BODY");
                }
                case "while" -> call(this::RULE_WHILE, "while");
                case "do"    -> call(this::RULE_DO_WHILE, "doWhile");
                case "for"   -> call(this::RULE_FOR, "for");
                case "switch"-> call(this::RULE_SWITCH, "switch");
                case "if"    -> call(this::RULE_IF, "if");
                case "print" -> call(this::RULE_PRINT, "print");

                case "break", "continue" -> {
                    currentToken++;
                    expectValue(";", "RULE_BODY");
                }

                case ";" -> {
                    // Absorb any stray semicolons
                    while (currentToken < tokens.size() && peekValue().equals(";")) {
                        currentToken++;
                    }
                }

                default -> {
                    if (isType(v)) {
                        // A local variable declaration inside the body
                        call(this::RULE_VARIABLE, "variable");
                        expectValue(";", "RULE_BODY");
                    }
                    else if (peekType().equals("ID")) {
                        // Could be a method call or an assignment
                        String nxt = (currentToken + 1 < tokens.size())
                                ? tokens.get(currentToken + 1).getValue()
                                : "";

                        if (nxt.equals("(")) {
                            call(this::RULE_CALL_METHOD, "callMethod");
                            expectValue(";", "RULE_BODY");
                        } else if (nxt.equals("=")) {
                            call(this::RULE_ASSIGNMENT, "assignment");
                            expectValue(";", "RULE_BODY");
                        } else {
                            error("RULE_BODY", "assignment or call");
                            // Skip until “;” or “}”
                            while (currentToken < tokens.size()
                                    && !Set.of(";", "}").contains(peekValue())) {
                                currentToken++;
                            }
                            if (currentToken < tokens.size() && peekValue().equals(";")) {
                                currentToken++;
                            }
                            return;
                        }
                    }
                    else {
                        error("RULE_BODY", "statement");
                        currentToken++;
                        return;
                    }
                }
            }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_ASSIGNMENT
    //   - <identifier> = <expression>
    //   Use lookupVariable(...) instead of checkVariable(...)
    // ------------------------------------------------------------
    private void RULE_ASSIGNMENT() {
        enterRule("RULE_ASSIGNMENT");
        try {
            String assignName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_ASSIGNMENT");

            // Check that variable was declared
            if (!semanticAnalizer.lookupVariable(assignName)) {
                semanticAnalizer.reportError(
                        "Use of undeclared variable “" + assignName + "” at token " + currentToken);
            }

            expectValue("=", "RULE_ASSIGNMENT");
            call(this::RULE_EXPRESSION, "expression");

            // (Optional) Enforce assignment‐type compatibility:
            // String varType = semanticAnalizer.getDeclaredType(assignName);
            // String exprType = semanticAnalizer.getLastExpressionType();
            // if (varType != null && !varType.equals(exprType)) {
            //     semanticAnalizer.reportError("Cannot assign “" + exprType + "” to “" + varType + "” variable");
            // }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_CALL_METHOD
    //   - <identifier> ( [paramValues] )
    // ------------------------------------------------------------
    private void RULE_CALL_METHOD() {
        enterRule("RULE_CALL_METHOD");
        try {
            String callName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_CALL_METHOD");

            // Verify the method was declared
            if (!semanticAnalizer.lookupVariable(callName)) {
                semanticAnalizer.reportError(
                        "Call to undeclared method “" + callName + "” at token " + currentToken);
            }

            expectValue("(", "RULE_CALL_METHOD");
            call(this::RULE_PARAM_VALUES, "paramValues");
            expectValue(")", "RULE_CALL_METHOD");

            // (Optional) Check argument count/types against the method’s signature here
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_PARAM_VALUES
    //   - <expression> (, <expression>)*
    // ------------------------------------------------------------
    private void RULE_PARAM_VALUES() {
        enterRule("RULE_PARAM_VALUES");
        try {
            if (peekValue().equals(")")) {
                return;
            }
            call(this::RULE_EXPRESSION, "expression");
            while (currentToken < tokens.size() && peekValue().equals(",")) {
                expectValue(",", "RULE_PARAM_VALUES");
                call(this::RULE_EXPRESSION, "expression");
            }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_EXPRESSION  (and sub‐rules X, Y, R, E, A, B, C)
    // ------------------------------------------------------------
    private void RULE_EXPRESSION() {
        enterRule("RULE_EXPRESSION");
        try {
            call(this::RULE_X, "x");
            while (Set.of("|", "||").contains(peekValue())) {
                found("Operator |");
                currentToken++;
                call(this::RULE_X, "x");
            }
        } finally {
            exitRule();
        }
    }

    private void RULE_X() {
        enterRule("RULE_X");
        try {
            call(this::RULE_Y, "y");
            while (Set.of("&", "&&").contains(peekValue())) {
                found("Operator &");
                currentToken++;
                call(this::RULE_Y, "y");
            }
        } finally {
            exitRule();
        }
    }

    private void RULE_Y() {
        enterRule("RULE_Y");
        try {
            while (peekValue().equals("!")) {
                found("!");
                currentToken++;
            }
            call(this::RULE_R, "R");
        } finally {
            exitRule();
        }
    }

    private void RULE_R() {
        enterRule("RULE_R");
        try {
            call(this::RULE_E, "E");
            while (Set.of("<", ">", "==", "!=", "<=", ">=").contains(peekValue())) {
                found("Relational op");
                currentToken++;
                call(this::RULE_E, "E");
            }
        } finally {
            exitRule();
        }
    }

    private void RULE_E() {
        enterRule("RULE_E");
        try {
            call(this::RULE_A, "A");
            while (Set.of("+", "-").contains(peekValue())) {
                found("Additive op");
                currentToken++;
                call(this::RULE_A, "A");
            }
        } finally {
            exitRule();
        }
    }

    private void RULE_A() {
        enterRule("RULE_A");
        try {
            call(this::RULE_B, "B");
            while (Set.of("*", "/").contains(peekValue())) {
                found("Mul op");
                currentToken++;
                call(this::RULE_B, "B");
            }
        } finally {
            exitRule();
        }
    }

    private void RULE_B() {
        enterRule("RULE_B");
        try {
            if (peekValue().equals("-")) {
                found("Unary -");
                currentToken++;
            }
            call(this::RULE_C, "C");
        } finally {
            exitRule();
        }
    }

    private void RULE_C() {
        enterRule("RULE_C");
        try {
            String v  = peekValue();
            String tp = peekType();

            if (Set.of(
                    "INTEGER", "OCTAL", "HEXADECIMAL", "BINARY",
                    "STRING", "CHAR", "FLOAT"
            ).contains(tp)
                    || v.equals("true") || v.equals("false")) {
                found("Literal " + v);
                currentToken++;
                // (you could push literal types here if desired)
            }
            else if (tp.equals("ID")) {
                String identName = tokens.get(currentToken).getValue();
                expectIdentifier("RULE_C");

                // Lookup identifier’s type for future semantic checks
                if (!semanticAnalizer.lookupVariable(identName)) {
                    semanticAnalizer.reportError(
                            "Use of undeclared variable “" + identName + "” at token " + currentToken);
                } else {
                    // ───────────────────────────────────────────────────────────────
                    // HERE: push the declared type of this variable onto typeStack
                    String declared = semanticAnalizer.getDeclaredType(identName);
                    if (declared != null) {
                        semanticAnalizer.pushExpressionType(declared);
                    }
                    // ───────────────────────────────────────────────────────────────
                }

                if (peekValue().equals("(")) {
                    expectValue("(", "RULE_C");
                    call(this::RULE_PARAM_VALUES, "paramValues");
                    expectValue(")", "RULE_C");
                    // (Optional) Check method-call argument count/types here
                }
            }
            else if (expectValue("(", "RULE_C")) {
                call(this::RULE_EXPRESSION, "expression");
                expectValue(")", "RULE_C");
            }
            else {
                error("RULE_C", "expression atom");
                currentToken++; // consume offending token
            }
        } finally {
            exitRule();
        }
    }


    // ------------------------------------------------------------
    // RULE_TYPE
    //   - one of: int | boolean | float | void | char | string
    // ------------------------------------------------------------
    private void RULE_TYPE() {
        enterRule("RULE_TYPE");
        try {
            expectType("RULE_TYPE");
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_VARIABLE
    //   - <type> <id> [= expression]
    // ------------------------------------------------------------
    private void RULE_VARIABLE() {
        enterRule("RULE_VARIABLE");
        try {
            String varType = peekValue();
            call(this::RULE_TYPE, "type");

            String varName = tokens.get(currentToken).getValue();
            expectIdentifier("RULE_VARIABLE");

            // Declare the new variable in the current scope:
            semanticAnalizer.checkVariable(varName, varType, "");

            if (peekValue().equals("=")) {
                found("=");
                currentToken++;
                call(this::RULE_EXPRESSION, "expression");
                // (Optional) Check that the expression’s type matches varType
            }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
// RULE_IF
//   - if (expression) <bodyOrBlock> [ else <bodyOrBlock> ]
//   Enforce: “expression” must evaluate to boolean.
// ------------------------------------------------------------
    private void RULE_IF() {
        enterRule("RULE_IF");
        try {
            expectValue("if", "RULE_IF");
            expectValue("(", "RULE_IF");
            call(this::RULE_EXPRESSION, "expression");

            // ───────────────────────────────────────────────────────
            // TYPE CHECK: must be boolean, not null/void/other
            String condType = semanticAnalizer.getLastExpressionType();
            if (condType == null) {
                semanticAnalizer.reportError("'if' condition type is null/unknown at token " + currentToken);
            } else if (condType.equals("void")) {
                semanticAnalizer.reportError("'if' condition has type void (invalid) at token " + currentToken);
            } else if (!condType.equals("boolean")) {
                semanticAnalizer.reportError("'if' condition must be boolean (found '"
                        + condType + "') at token " + currentToken);
            }
            // ───────────────────────────────────────────────────────

            expectValue(")", "RULE_IF");

            // “then” branch scope
            String thenScope = "if@" + currentToken;
            semanticAnalizer.enterScope(thenScope);
            try {
                if (peekValue().equals("{")) {
                    expectValue("{", "RULE_IF");
                    while (currentToken < tokens.size() && !peekValue().equals("}")) {
                        call(this::RULE_BODY, "body");
                    }
                    expectValue("}", "RULE_IF");
                } else {
                    call(this::RULE_BODY, "body");
                }
            } finally {
                semanticAnalizer.exitScope();
            }

            // Optional “else”
            if (currentToken < tokens.size() && peekValue().equals("else")) {
                expectValue("else", "RULE_IF");
                if (peekValue().equals("if")) {
                    call(this::RULE_IF, "if");
                } else {
                    String elseScope = "else@" + currentToken;
                    semanticAnalizer.enterScope(elseScope);
                    try {
                        if (peekValue().equals("{")) {
                            expectValue("{", "RULE_IF");
                            while (currentToken < tokens.size() && !peekValue().equals("}")) {
                                call(this::RULE_BODY, "body");
                            }
                            expectValue("}", "RULE_IF");
                        } else {
                            call(this::RULE_BODY, "body");
                        }
                    } finally {
                        semanticAnalizer.exitScope();
                    }
                }
            }
        } finally {
            exitRule();
        }
    }


    // ------------------------------------------------------------
    // RULE_FOR
    //   - for ( [varDecl | assignment]; [expression]; [assignment] ) <bodyOrBlock>
    //   Enforce: the middle “test” clause must evaluate to boolean.
    // ------------------------------------------------------------
    private void RULE_FOR() {
        enterRule("RULE_FOR");
        try {
            expectValue("for", "RULE_FOR");
            expectValue("(", "RULE_FOR");

            // Possibly a variable declaration or assignment in the initialization
            if (!peekValue().equals(";")) {
                if (isType(peekValue())) {
                    // Enter a short scope just for this var–decl
                    String initVarScope = "forInit@" + currentToken;
                    semanticAnalizer.enterScope(initVarScope);
                    try {
                        call(this::RULE_VARIABLE, "variable");
                    } finally {
                        semanticAnalizer.exitScope();
                    }
                } else {
                    call(this::RULE_ASSIGNMENT, "assignment");
                }
            }
            expectValue(";", "RULE_FOR");

            // ───────────────────────────────────────────────────────
            // Condition expression (middle clause)
            if (!peekValue().equals(";")) {
                call(this::RULE_EXPRESSION, "expression");

                // TYPE CHECK: must be boolean, not null/void/other
                String condType = semanticAnalizer.getLastExpressionType();
                if (condType == null) {
                    semanticAnalizer.reportError("'for' condition type is null/unknown at token " + currentToken);
                } else if (condType.equals("void")) {
                    semanticAnalizer.reportError("'for' condition has type void (invalid) at token " + currentToken);
                } else if (!condType.equals("boolean")) {
                    semanticAnalizer.reportError("'for' condition must be boolean (found '"
                            + condType + "') at token " + currentToken);
                }
            }
            expectValue(";", "RULE_FOR");
            // ───────────────────────────────────────────────────────

            // Optional “update” assignment
            if (!peekValue().equals(")")) {
                call(this::RULE_ASSIGNMENT, "assignment");
            }
            expectValue(")", "RULE_FOR");

            // Now the body scope
            String forScope = "for@" + currentToken;
            semanticAnalizer.enterScope(forScope);
            try {
                if (peekValue().equals("{")) {
                    expectValue("{", "RULE_FOR");
                    while (currentToken < tokens.size() && !peekValue().equals("}")) {
                        call(this::RULE_BODY, "body");
                    }
                    expectValue("}", "RULE_FOR");
                } else {
                    call(this::RULE_BODY, "body");
                }
            } finally {
                semanticAnalizer.exitScope();
            }
        } finally {
            exitRule();
        }
    }

    // ------------------------------------------------------------
    // RULE_WHILE
    //   - while (expression) <bodyOrBlock>
    //   Enforce: “expression” must evaluate to boolean.
    // ------------------------------------------------------------
    private void RULE_WHILE() {
        enterRule("RULE_WHILE");
        try {
            expectValue("while", "RULE_WHILE");
            expectValue("(", "RULE_WHILE");
            call(this::RULE_EXPRESSION, "expression");

            // ───────────────────────────────────────────────────────
            // TYPE CHECK: must be boolean, not null/void/other
            String condType = semanticAnalizer.getLastExpressionType();
            if (condType == null) {
                semanticAnalizer.reportError("'while' condition type is null/unknown at token " + currentToken);
            } else if (condType.equals("void")) {
                semanticAnalizer.reportError("'while' condition has type void (invalid) at token " + currentToken);
            } else if (!condType.equals("boolean")) {
                semanticAnalizer.reportError("'while' condition must be boolean (found '"
                        + condType + "') at token " + currentToken);
            }
            // ───────────────────────────────────────────────────────

            expectValue(")", "RULE_WHILE");

            String whileScope = "while@" + currentToken;
            semanticAnalizer.enterScope(whileScope);
            try {
                if (peekValue().equals("{")) {
                    expectValue("{", "RULE_WHILE");
                    while (currentToken < tokens.size() && !peekValue().equals("}")) {
                        call(this::RULE_BODY, "body");
                    }
                    expectValue("}", "RULE_WHILE");
                } else {
                    call(this::RULE_BODY, "body");
                }
            } finally {
                semanticAnalizer.exitScope();
            }
        } finally {
            exitRule();
        }
    }

}
