import java.io.*;
import java.util.Objects;
import java.util.Vector;

/**
 * Lexer class to analyze the input file.
 * This version uses a DFA (Automata) to recognize various tokens,
 * including numbers. It has been modified so that a single "0" is accepted
 * as an INTEGER.
 *
 * @author javiergs
 * @version 0.2
 */
public class TheLexer {

    private File file;
    private Automata dfa;
    private Vector<TheToken> tokens;

    public TheLexer(File file) {
        this.file = file;
        tokens = new Vector<>();
        dfa = new Automata();

        // Delimiters
        dfa.addTransitions("SS", "({[)}];,", "DELS");
        dfa.addTransitions("SS", ".", "PDS");
        dfa.addAcceptState("DELS", "DELIMITER");

        // BOHN numbers:
        // When '0' is encountered in state SS, transition to "BOHNS".
        dfa.addTransition("SS", "0", "BOHNS");
        // Accept a single "0" as an INTEGER.
        dfa.addAcceptState("BOHNS", "INTEGER");

        // Octal number: if a digit 0-7 follows the initial 0,
        // transition from BOHNS to octal state "OS".
        dfa.addTransitions("BOHNS", "01234567", "OS");
        dfa.addTransitions("OS", "01234567", "OS");
        dfa.addAcceptState("OS", "OCTAL");

        // Binary number: if a 'b' or 'B' follows the initial 0,
        // transition to "BIN_START" then read binary digits.
        dfa.addTransitions("BOHNS", "bB", "BIN_START");
        dfa.addTransitions("BIN_START", "01", "BS");
        dfa.addTransitions("BS", "01", "BS");
        dfa.addAcceptState("BS", "BINARY");

        // Hexadecimal number: if an 'x' or 'X' follows the initial 0,
        // transition to "HEX_START" then read hexadecimal digits.
        dfa.addTransitions("BOHNS", "xX", "HEX_START");
        dfa.addTransitions("HEX_START", "0123456789abcdefABCDEF", "HS");
        dfa.addTransitions("HS", "0123456789abcdefABCDEF", "HS");
        dfa.addAcceptState("HS", "HEXADECIMAL");

        // Char literals
        dfa.addTransitions("SS", "'", "CCS");
        dfa.addTransitions("CCS", "ANY", "CES");
        dfa.addTransitions("CCS", "'", "CS");
        dfa.addTransitions("CES", "'", "CS");
        dfa.addAcceptState("CS", "CHAR");

        // String literals
        dfa.addTransitions("SS", "\"", "SCS");
        dfa.addTransitions("SCS", "ANY", "SCS");
        dfa.addTransitions("SCS", "\"", "STS");
        dfa.addTransitions("SCS", "\"", "STS");
        dfa.addAcceptState("STS", "STRING");

        // Non-zero Integers
        dfa.addTransitions("SS", "123456789", "IS");
        dfa.addTransitions("IS", "0123456789", "IS");
        dfa.addAcceptState("IS", "INTEGER");

        // Floats
        dfa.addTransitions("SS", ".", "FS");
        dfa.addTransitions("OPS", ".", "FS");
        dfa.addTransitions("BOHNS", ".", "DS");
        dfa.addTransitions("IS", ".", "DS");
        // dfa.addTransitions("PDS", "0123456789", "DS"); // .01 -> FLOAT
        dfa.addTransitions("DS", "0123456789", "FS");
        dfa.addTransitions("DS", "eE", "ES");
        dfa.addTransitions("IS", "eE", "ES");
        dfa.addTransitions("FS", "0123456789", "FS");
        dfa.addTransitions("FS", "eE", "ES");
        dfa.addTransitions("ES", "0123456789", "ILS");
        // dfa.addTransitions("ES", ".", "DLS"); // 1.0e.
        dfa.addTransitions("ILS", "0123456789", "ILS");
        // dfa.addTransitions("ILS", ".", "DLS"); // 1.0e1.
        // dfa.addTransitions("DLS", "0123456789", "FLS"); // 1.0e1.0
        // dfa.addTransitions("FLS", "0123456789", "FLS"); // 1.0e1.01
        dfa.addAcceptState("FS", "FLOAT");
        dfa.addAcceptState("ILS", "FLOAT");
        // dfa.addAcceptState("FLS", "FLOAT"); // 1.0e1.01 FLOAT

        // Identifiers
        dfa.addTransitions("SS", "$_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", "IDS");
        dfa.addTransitions("IDS", "$_abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", "IDS");
        dfa.addAcceptState("IDS", "ID");

//        // Comments (currently disabled)
//        dfa.addTransitions("SS", "#", "CMSS");
//        dfa.addTransitions("CMSS", "ANY", "CMSS");
//        dfa.addAcceptState("CMSS", "COMMENT");
    }

    public void run() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            algorithm(line);
        }
    }

    private void algorithm(String line) {
        String currentState = "SS";
        String nextState;
        String string = "";
        int index = 0;

        while (index < line.length()) {
            char currentChar = line.charAt(index);

            // Check if the current character is whitespace, delimiter, or operator.
            if (isSpace(currentChar, currentState)
                    || isDelimiter(currentChar, currentState)
                    || isOperator(currentChar, currentState)) {

                // If we were building up a token in 'string' and currentState is an accept state,
                // emit that token.
                if (dfa.isAcceptState(currentState)) {
                    String stateName = dfa.getAcceptStateName(currentState);
                    tokens.add(new TheToken(string, stateName));
                }
                else if (!string.isEmpty() && !currentState.equals("SS")) {
                    // Emit an ERROR token if we have a partial token not accepted.
                    tokens.add(new TheToken(string, "ERROR"));
                }

                // Now handle the delimiter/operator/whitespace character itself.
                if (isSpace(currentChar, currentState)) {
                    // Skip whitespace.
                }
                else if (isDelimiter(currentChar, currentState)) {
                    tokens.add(new TheToken(String.valueOf(currentChar), "DELIMITER"));
                }
                else if (isOperator(currentChar, currentState)) {
                    // Look ahead to check for two-character operators.
                    if (index + 1 < line.length()) {
                        char nextChar = line.charAt(index + 1);
                        if ((currentChar == '&' && nextChar == '&') ||
                                (currentChar == '|' && nextChar == '|') ||
                                (currentChar == '=' && nextChar == '=') ||
                                (currentChar == '!' && nextChar == '=') ||
                                (currentChar == '<' && nextChar == '=') ||
                                (currentChar == '>' && nextChar == '=') ||
                                (currentChar == '+' && nextChar == '+') ||
                                (currentChar == '-' && nextChar == '-'))
                        {
                            tokens.add(new TheToken("" + currentChar + nextChar, "OPERATOR"));
                            index += 2;
                            currentState = "SS";
                            string = "";
                            continue;
                        }
                    }
                    tokens.add(new TheToken(String.valueOf(currentChar), "OPERATOR"));
                }

                // Reset DFA state and token string.
                currentState = "SS";
                string = "";
            } else {
                // Feed the character into the DFA.
                nextState = dfa.getNextState(currentState, currentChar);
                string += currentChar;
                currentState = nextState;
            }
            index++;
        }

        // Emit any token that remains after the loop.
        if (dfa.isAcceptState(currentState)) {
            String stateName = dfa.getAcceptStateName(currentState);
            tokens.add(new TheToken(string, stateName));
        }
        else if (!string.isEmpty() && !currentState.equals("SS")) {
            tokens.add(new TheToken(string, "ERROR"));
        }
    }

    private boolean isSpace(char c, String currentState) {
        if (!Objects.equals(currentState, "SCS") && !Objects.equals(currentState, "CES")) {
            return c == ' ' || c == '\t' || c == '\n';
        }
        return false;
    }

    private boolean isDelimiter(char c, String currentState) {
        if (!Objects.equals(currentState, "SCS") && !Objects.equals(currentState, "CES")) {
            return c == ',' || c == ';' || c == '(' || c == ')' ||
                    c == '[' || c == ']' || c == '{' || c == '}' || c == ':';
        }
        return false;
    }

    private boolean isOperator(char c, String currentState) {
        if (!Objects.equals(currentState, "SCS") && !Objects.equals(currentState, "CES")) {
            return c == '=' || c == '*' || c == '/' ||
                    c == '%' || c == '^' || c == '&' || c == '|' || c == '<' ||
                    c == '>' || c == '!' || c == '~' || c == '+' || c == '-';
        }
        return false;
    }

    public void printTokens() {
        for (TheToken token : tokens) {
            System.out.printf("%10s\t|\t%s\n", token.getValue(), token.getType());
        }
    }

    public Vector<TheToken> getTokens() {
        return tokens;
    }
}
