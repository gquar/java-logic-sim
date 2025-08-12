package sim.core;

/**
 * A 2-input XOR (exclusive OR) logic gate.
 * 
 * <p>An XOR gate outputs true when exactly ONE of its inputs is true (but not both).
 * The truth table for a 2-input XOR gate is:
 * <pre>
 * Input A | Input B | Output
 * --------|---------|--------
 * false   | false   | false
 * false   | true    | true
 * true    | false   | true
 * true    | true    | false
 * </pre>
 * 
 * <p>This gate has exactly 2 inputs and 1 output.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class XorGate extends Gate {
    
    /**
     * Constructs a new 2-input XOR gate with the specified ID.
     * 
     * @param id unique identifier for this gate
     */
    public XorGate(String id) {
        super(id, 2, 1);
    }
    
    /**
     * Evaluates the XOR gate logic.
     * 
     * <p>Sets the output to true when exactly one input is true.
     * The output is computed as: output = input[0] XOR input[1]
     * 
     * <p>Note: XOR can be implemented as (input[0] != input[1]) or
     * (input[0] ^ input[1]) using Java's logical XOR operator.
     */
    @Override
    public void evaluate() {
        // XOR logic: output is true when inputs are different
        boolean result = inputs.get(0) != inputs.get(1);
        outputs.set(0, result);
    }
}
