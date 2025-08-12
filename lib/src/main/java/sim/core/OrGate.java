package sim.core;

/**
 * A 2-input OR logic gate.
 * 
 * <p>An OR gate outputs true when ANY of its inputs are true.
 * The truth table for a 2-input OR gate is:
 * <pre>
 * Input A | Input B | Output
 * --------|---------|--------
 * false   | false   | false
 * false   | true    | true
 * true    | false   | true
 * true    | true    | true
 * </pre>
 * 
 * <p>This gate has exactly 2 inputs and 1 output.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class OrGate extends Gate {
    
    /**
     * Constructs a new 2-input OR gate with the specified ID.
     * 
     * @param id unique identifier for this gate
     */
    public OrGate(String id) {
        super(id, 2, 1);
    }
    
    /**
     * Evaluates the OR gate logic.
     * 
     * <p>Sets the output to true if at least one input is true.
     * The output is computed as: output = input[0] OR input[1]
     */
    @Override
    public void evaluate() {
        // OR logic: output is true if ANY input is true
        boolean result = inputs.get(0) || inputs.get(1);
        outputs.set(0, result);
    }
}
