package sim.core;

/**
 * A 2-input AND logic gate.
 * 
 * <p>An AND gate outputs true only when ALL of its inputs are true.
 * The truth table for a 2-input AND gate is:
 * <pre>
 * Input A | Input B | Output
 * --------|---------|--------
 * false   | false   | false
 * false   | true    | false
 * true    | false   | false
 * true    | true    | true
 * </pre>
 * 
 * <p>This gate has exactly 2 inputs and 1 output.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class AndGate extends Gate {
    
    /**
     * Constructs a new 2-input AND gate with the specified ID.
     * 
     * @param id unique identifier for this gate
     */
    public AndGate(String id) {
        super(id, 2, 1);
    }
    
    /**
     * Evaluates the AND gate logic.
     * 
     * <p>Sets the output to true only if both inputs are true.
     * The output is computed as: output = input[0] AND input[1]
     */
    @Override
    public void evaluate() {
        // AND logic: output is true only if ALL inputs are true
        boolean result = inputs.get(0) && inputs.get(1);
        outputs.set(0, result);
    }
}
