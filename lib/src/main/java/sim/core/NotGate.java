package sim.core;
/**
 * A 1-input NOT logic gate (inverter).
 * 
 * <p>A NOT gate outputs the opposite of its input value.
 * The truth table for a NOT gate is:
 * <pre>
 * Input | Output
 * ------|--------
 * false | true
 * true  | false
 * </pre>
 * 
 * <p>This gate has exactly 1 input and 1 output.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class NotGate extends Gate {
    
    /**
     * Constructs a new 1-input NOT gate with the specified ID.
     * 
     * @param id unique identifier for this gate
     */
    public NotGate(String id) {
        super(id, 1, 1);
    }
    
    /**
     * Evaluates the NOT gate logic.
     * 
     * <p>Sets the output to the opposite of the input value.
     * The output is computed as: output = NOT input[0]
     */
    @Override
    public void evaluate() {
        // NOT logic: output is the opposite of the input
        boolean result = !inputs.get(0);
        outputs.set(0, result);
    }
}
