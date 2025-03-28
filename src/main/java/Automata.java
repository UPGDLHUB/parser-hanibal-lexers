import java.util.*;

/**
 * Automata class represent a DFA.
 * This version is implemented with a HashMap to store the transition table.
 *
 * @author javiergs
 * @version 1.0
 */
public class Automata {

    private final HashMap<String, String> table = new HashMap<>();
    private final HashMap<String, String> acceptStates = new HashMap<>();

    public void addTransition(String currentState, String inputSymbol, String nextState) {
        table.put(currentState + "/" + inputSymbol, nextState);
    }

    /**
     * Adds transitions for each character in the given set of symbols to the specified state.
     *
     * @param sourceState The state from which transitions originate.
     * @param symbols     A string containing all characters to be used as transitions.
     * @param targetState The destination state for each transition.
     */
    public void addTransitions(String sourceState, String symbols, String targetState) {
        if(Objects.equals(symbols, "ANY")){
            this.addTransition(sourceState, symbols, targetState);
            return;
        }

        symbols.chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .forEach(symbol -> this.addTransition(sourceState, symbol, targetState));
    }

    public String getNextState(String currentState, char inputSymbol) {

        // If there a specific char state return that for Strings/Chars
        if(table.containsKey(currentState + "/" + inputSymbol)){
            return table.get(currentState + "/" + inputSymbol);
        }

        // If there is a any state, accept all chars
        if(table.containsKey(currentState + "/" + "ANY")){
            return table.get(currentState + "/" + "ANY");
        }

        // If no other condition is available, error will return
        return table.get(currentState + "/" + inputSymbol);
    }

    public void addAcceptState(String state, String name) {
        acceptStates.put(state, name);
    }

    public boolean isAcceptState(String name) {
        return acceptStates.containsKey(name);
    }

    public String getAcceptStateName(String state) {
        return acceptStates.get(state);
    }

    public void printTable() {
        System.out.println("DFA Transition Table:");
        for (String state : table.keySet()) {
            String[] parts = state.split("/");
            System.out.println(parts[0] + " -> " + table.get(state) + " [label=\"" + parts[1] + "\"];");
        }
    }

}