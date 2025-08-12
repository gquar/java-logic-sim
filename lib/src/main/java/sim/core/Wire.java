package sim.core;

/**
 * A wire that connects the output of one gate to the input of another gate.
 * 
 * <p>A wire represents a physical connection between gates in a digital circuit.
 * It transfers the output value from one gate's output pin to another gate's input pin.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class Wire {
    
    /** The source gate that this wire connects from */
    private final Gate fromGate;
    
    /** The output pin number on the source gate */
    private final int fromPin;
    
    /** The destination gate that this wire connects to */
    private final Gate toGate;
    
    /** The input pin number on the destination gate */
    private final int toPin;
    
    /**
     * Constructs a new wire connecting two gates.
     * 
     * @param fromGate the source gate
     * @param fromPin the output pin number on the source gate
     * @param toGate the destination gate
     * @param toPin the input pin number on the destination gate
     * @throws IllegalArgumentException if pin numbers are out of range
     */
    public Wire(Gate fromGate, int fromPin, Gate toGate, int toPin) {
        // Validate pin ranges
        if (fromPin < 0 || fromPin >= fromGate.getNumOutputs()) {
            throw new IllegalArgumentException(
                "Output pin " + fromPin + " is out of range for gate " + fromGate.getId() + 
                ". Valid pins: 0 to " + (fromGate.getNumOutputs() - 1)
            );
        }
        
        if (toPin < 0 || toPin >= toGate.getNumInputs()) {
            throw new IllegalArgumentException(
                "Input pin " + toPin + " is out of range for gate " + toGate.getId() + 
                ". Valid pins: 0 to " + (toGate.getNumInputs() - 1)
            );
        }
        
        this.fromGate = fromGate;
        this.fromPin = fromPin;
        this.toGate = toGate;
        this.toPin = toPin;
    }
    
    /**
     * Reads the current value from the source gate's output pin.
     * 
     * @return the boolean value from the source gate's output
     */
    public boolean read() {
        return fromGate.getOutput(fromPin);
    }
    
    /**
     * Gets the source gate of this wire.
     * 
     * @return the source gate
     */
    public Gate getFromGate() {
        return fromGate;
    }
    
    /**
     * Gets the output pin number on the source gate.
     * 
     * @return the output pin number
     */
    public int getFromPin() {
        return fromPin;
    }
    
    /**
     * Gets the destination gate of this wire.
     * 
     * @return the destination gate
     */
    public Gate getToGate() {
        return toGate;
    }
    
    /**
     * Gets the input pin number on the destination gate.
     * 
     * @return the input pin number
     */
    public int getToPin() {
        return toPin;
    }
}
