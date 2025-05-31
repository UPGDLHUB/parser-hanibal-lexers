import java.io.*;
import java.util.Set;
import java.util.Vector;

/**
 * TheLexer class to analyze the input file. Recognizes: identifiers, keywords, numbers
 * (bin/oct/dec/hex), floats, chars, strings, operators, delimiters. Uses a DFA plus a small
 * post-check for CHAR literals.
 *
 * @author javiergs
 * @version 0.2
 */
public class TheLexer {

    private final File file;
    private final Automata dfa;
    private final Vector<TheToken> tokens = new Vector<>();

    private static final Set<String> KEYWORDS =
            Set.of(
                    "int",
                    "float",
                    "string",
                    "char",
                    "boolean",
                    "if",
                    "else",
                    "for",
                    "while",
                    "do",
                    "switch",
                    "case",
                    "break",
                    "continue",
                    "return",
                    "void",
                    "const",
                    "class",
                    "public",
                    "private",
                    "protected",
                    "static",
                    "new",
                    "delete",
                    "true",
                    "false");

    public TheLexer(File file) {
        this.file = file;
        this.dfa = new Automata();

        /* Operators */
        dfa.addTransitions("SS", "+-*/=%^&|<>!~@", "OPS");
        dfa.addTransitions("OPS", "0123456789", "IS");
        dfa.addAcceptState("OPS", "OPERATOR");

        /* Delimiters */
        dfa.addTransitions("SS", "({[)}];,", "DELS");
        dfa.addTransitions("SS", ".", "PDS");
        dfa.addAcceptState("DELS", "DELIMITER");

        /* Numbers */
        dfa.addTransition("SS", "0", "BOHNS");
        dfa.addTransitions("BOHNS", "0123456789", "IS"); // 0…9  → decimal

        /* binary */
        dfa.addTransitions("BOHNS", "bB", "BIN_START");
        dfa.addTransitions("BIN_START", "01", "BS");
        dfa.addTransitions("BS", "01", "BS");
        dfa.addAcceptState("BS", "BINARY");

        /* octal */
        dfa.addTransitions("BOHNS", "01234567", "OS");
        dfa.addTransitions("OS", "01234567", "OS");
        dfa.addAcceptState("OS", "OCTAL");

        /* hexadecimal */
        dfa.addTransitions("BOHNS", "xX", "HEX_START");
        dfa.addTransitions("HEX_START", "0123456789abcdefABCDEF", "HS");
        dfa.addTransitions("HS", "0123456789abcdefABCDEF", "HS");
        dfa.addAcceptState("HS", "HEXADECIMAL");

        /* CHAR literal */
        dfa.addTransitions("SS", "'", "CCS");
        dfa.addTransitions("CCS", "\\\\", "ESC"); // escape start
        dfa.addTransitions("ESC", "'", "CS"); // handles '\'' and  '\'
        dfa.addTransitions("ESC", "ANY", "CES"); // '\n', '\\', etc.
        dfa.addTransitions(
                "CCS",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,<.>/?`~",
                "CES");
        dfa.addTransitions("CES", "'", "CS");
        dfa.addAcceptState("CS", "CHAR");

        /* STRING literal */
        dfa.addTransitions("SS", "\"", "SCS");
        dfa.addTransitions("SCS", "ANY", "SCS");
        dfa.addTransitions("SCS", "\"", "STS");
        dfa.addAcceptState("STS", "STRING");

        /* Integers (decimal) */
        dfa.addTransitions("SS", "123456789", "IS");
        dfa.addTransitions("IS", "0123456789", "IS");
        dfa.addAcceptState("IS", "INTEGER");

        /* Floats */
        dfa.addTransitions("SS", ".", "FS");
        dfa.addTransitions("OPS", ".", "FS");
        dfa.addTransitions("BOHNS", ".", "DS");
        dfa.addTransitions("IS", ".", "DS");
        dfa.addTransitions("DS", "0123456789", "FS");
        dfa.addTransitions("DS", "eE", "ES");
        dfa.addTransitions("IS", "eE", "ES");
        dfa.addTransitions("FS", "0123456789", "FS");
        dfa.addTransitions("FS", "eE", "ES");
        dfa.addTransitions("ES", "0123456789", "ILS");
        dfa.addTransitions("ILS", "0123456789", "ILS");
        dfa.addAcceptState("FS", "FLOAT");
        dfa.addAcceptState("ILS", "FLOAT");

        /* Identifiers */
        dfa.addTransitions("SS", "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", "IDS");
        dfa.addTransitions(
                "IDS", "$_abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", "IDS");
        dfa.addAcceptState("IDS", "ID");
    }

    public void run() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                algorithm(line);
            }
        }
    }

    public void printTokens() {
        for (TheToken t : tokens) {
            System.out.printf("%10s\t|\t%s%n", t.getValue(), t.getType());
        }
    }

    public Vector<TheToken> getTokens() {
        return tokens;
    }

    private void algorithm(String line) {
        String state = "SS";
        StringBuilder lexeme = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (!(isOperator(ch, state) || isDelimiter(ch, state) || isSpace(ch, state))) {
                state = dfa.getNextState(state, ch);
                lexeme.append(ch);
            } else {
                flushToken(state, lexeme.toString());
                if (isOperator(ch, state)) addToken(String.valueOf(ch), "OPERATOR");
                if (isDelimiter(ch, state)) addToken(String.valueOf(ch), "DELIMITER");
                state = "SS";
                lexeme.setLength(0);
            }
        }

        flushToken(state, lexeme.toString());
    }

    private void flushToken(String state, String lexeme) {
        if (lexeme.isEmpty()) return;

        if (dfa.isAcceptState(state)) {
            String type = dfa.getAcceptStateName(state);
            if ("CHAR".equals(type) && !isValidCharLiteral(lexeme)) type = "ERROR";
            addToken(lexeme, type);
        } else if (!"SS".equals(state)) {
            addToken(lexeme, "ERROR");
        }
    }

    private void addToken(String lexeme, String type) {
        if ("ID".equals(type) && KEYWORDS.contains(lexeme)) type = "KEYWORD";
        tokens.add(new TheToken(lexeme, type));
    }

    private boolean isSpace(char c, String st) {
        return !("SCS".equals(st) || "CES".equals(st)) && (c == ' ' || c == '\t' || c == '\n');
    }

    private boolean isDelimiter(char c, String st) {
        return !("SCS".equals(st) || "CES".equals(st)) && ",;()[]{}:".indexOf(c) >= 0;
    }

    private boolean isOperator(char c, String st) {
        return !("SCS".equals(st) || "CES".equals(st)) && "=*/%^&|<>!~+-".indexOf(c) >= 0;
    }

    private boolean isValidCharLiteral(String s) {
        if (s.length() == 3) {
            return s.charAt(0) == '\'' && s.charAt(2) == '\'' && s.charAt(1) != '\'';
        }
        if (s.length() == 4) {
            return s.charAt(0) == '\'' && s.charAt(1) == '\\' && s.charAt(3) == '\'';
        }
        return false;
    }
}
