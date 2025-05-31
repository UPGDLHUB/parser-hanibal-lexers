// ParserUtils.java

import java.util.Set;
import java.util.Vector;

/**
 * Utility class that encapsulates common parsing functionality:
 *  - Token storage and index (tokens, currentToken)
 *  - enterRule/exitRule tracing
 *  - expectValue/expectIdentifier/expectType checks
 *  - error reporting and simple panic‐mode recovery based on SYNC set
 *  - “call” method to drive FIRST/FOLLOW‐based rule dispatch
 *  - peekValue/peekType accessors
 *
 * To use: have your main parser (e.g. TheParser) extend ParserUtils, and invoke
 * these protected methods from within each RULE_* method.
 */
public class ParserUtils {
    protected final Vector<TheToken> tokens;
    protected int currentToken;
    private static int indent = 0;

    // Synchronization set used by error(...) to decide whether to skip one token
    protected static final Set<String> SYNC = Set.of(
            ";", "}", ",", ")", "class", "if", "for", "while",
            "do", "switch", "return"
    );

    public ParserUtils(Vector<TheToken> tokens) {
        this.tokens = tokens;
        this.currentToken = 0;
    }

    /**
     * Print a tracing line when entering a rule, with indentation.
     */
    protected void enterRule(String name) {
        System.out.println(" ".repeat(indent * 2) + "- " + name);
        indent++;
    }

    /**
     * Decrease indentation when exiting a rule.
     */
    protected void exitRule() {
        indent = Math.max(0, indent - 1);
    }

    /**
     * Print a tracing line for something “found” inside a rule.
     */
    protected void found(String msg) {
        System.out.println(" ".repeat(indent * 2) + "-- " + msg);
    }

    /**
     * Checks if the given string is a built‐in type keyword.
     */
    protected boolean isType(String v) {
        return switch (v) {
            case "int", "boolean", "float", "void", "char", "string" -> true;
            default -> false;
        };
    }

    /**
     * Expect the next token’s value to be exactly v. If it matches, consume it
     * and return true; otherwise report an error and return false.
     */
    protected boolean expectValue(String v, String rule) {
        if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(v)) {
            found("Found '" + v + "'");
            currentToken++;
            return true;
        }
        error(rule, "'" + v + "'");
        return false;
    }

    /**
     * Expect the next token’s type to be "ID". If so, consume it and return true;
     * otherwise report an error and return false.
     */
    protected boolean expectIdentifier(String rule) {
        if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("ID")) {
            found("Identifier: " + tokens.get(currentToken).getValue());
            currentToken++;
            return true;
        }
        error(rule, "identifier");
        return false;
    }

    /**
     * Expect the next token’s value to be one of the built‐in types (int, boolean, ...).
     * If so, consume it and return true; otherwise report an error and return false.
     */
    protected boolean expectType(String rule) {
        if (currentToken < tokens.size() && isType(tokens.get(currentToken).getValue())) {
            found("Type: " + tokens.get(currentToken).getValue());
            currentToken++;
            return true;
        }
        error(rule, "type");
        return false;
    }

    /**
     * Basic panic‐mode error recovery (synchronizing on SYNC set). If the current token
     * is not in SYNC, skip exactly one token. If it is in SYNC or we’ve run off the end,
     * just do nothing (letting the caller loop handle it).
     */
    protected void error(String rule, String expected) {
        if (currentToken >= tokens.size()) return;

        System.err.printf("%s: expected %s at %s%n",
                rule, expected, tokens.get(currentToken));
        if (!SYNC.contains(tokens.get(currentToken).getValue())) {
            currentToken++;
        }
    }

    /**
     * “call” drives a FIRST/FOLLOW‐based dispatch:
     *  - If peekValue() or peekType() belongs to FIRST(ruleName), run action.run() and return.
     *  - Otherwise, skip tokens until one is in FOLLOW(ruleName) or EOF.
     *  - FIRST_MAP and FOLLOW_MAP are assumed to be populated elsewhere.
     */
    public void call(Runnable action, String ruleName) {
        while (currentToken < tokens.size()) {
            String val  = tokens.get(currentToken).getValue();
            String type = tokens.get(currentToken).getType();

            boolean inFirst =
                    FirstSets.FIRST_MAP.get(ruleName).contains(val) ||
                            FirstSets.FIRST_MAP.get(ruleName).contains(type);

            if (inFirst) {
                action.run();
                return;
            }

            System.err.println(ruleName + ": error on " + val);
            currentToken++;
            if (currentToken >= tokens.size()) return;

            val  = tokens.get(currentToken).getValue();
            type = tokens.get(currentToken).getType();
            boolean inFollow =
                    FollowSets.FOLLOW_MAP.get(ruleName).contains(val) ||
                            FollowSets.FOLLOW_MAP.get(ruleName).contains(type);

            if (inFollow) {
                System.err.println(ruleName + ": recovered at " + val);
                return;
            }
        }
    }

    /**
     * Safe peek of current token’s value; returns empty string if at EOF.
     */
    protected String peekValue() {
        if (currentToken < tokens.size()) {
            return tokens.get(currentToken).getValue();
        }
        return "";
    }

    /**
     * Safe peek of current token’s type; returns empty string if at EOF.
     */
    protected String peekType() {
        if (currentToken < tokens.size()) {
            return tokens.get(currentToken).getType();
        }
        return "";
    }
}
