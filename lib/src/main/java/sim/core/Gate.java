package sim.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all digital logic gates in the simulator.
 * 
 * <p>This class provides the common structure and behavior that all gates share:
 * - Input and output pin management
 * - Input value setting and output value retrieval
 * - Abstract evaluation method that each concrete gate must implement
 * 
 * <p>All gates have exactly 1 output by default, but can have varying numbers of inputs.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public abstract class Gate {
    
    /** Unique identifier for this gate instance */
    protected final String id;
    
    /** Number of input pins this gate has */
    protected final int numInputs;
    
    /** Number of output pins this gate has (always 1 for current implementation) */
    protected final int numOutputs;
    
    /** List of input values for each input pin */
    protected final List<Boolean> inputs;
    
    /** List of output values for each output pin */
    protected final List<Boolean> outputs;
    
    /**
     * Constructs a new gate with the specified parameters.
     * 
     * @param id unique identifier for this gate
     * @param numInputs number of input pins
     * @param numOutputs number of output pins (typically 1)
     */
    protected Gate(String id, int numInputs, int numOutputs) {
        this.id = id;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        
        // Initialize inputs and outputs with false values
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        
        for (int i = 0; i < numInputs; i++) {
            this.inputs.add(false);
        }
        
        for (int i = 0; i < numOutputs; i++) {
            this.outputs.add(false);
        }
    }
    
    /**
     * Sets the value of a specific input pin.
     * 
     * @param pin the input pin number (0-based indexing)
     * @param value the boolean value to set
     * @throws IllegalArgumentException if pin number is out of range
     */
    public void setInput(int pin, boolean value) {
        if (pin < 0 || pin >= numInputs) {
            throw new IllegalArgumentException(
                "Input pin " + pin + " is out of range. Valid pins: 0 to " + (numInputs - 1)
            );
        }
        inputs.set(pin, value);
    }
    
    /**
     * Gets the value of a specific output pin.
     * 
     * @param pin the output pin number (0-based indexing)
     * @return the boolean value of the specified output pin
     * @throws IllegalArgumentException if pin number is out of range
     */
    public boolean getOutput(int pin) {
        if (pin < 0 || pin >= numOutputs) {
            throw new IllegalArgumentException(
                "Output pin " + pin + " is out of range. Valid pins: 0 to " + (numOutputs - 1)
            );
        }
        return outputs.get(pin);
    }
    
    /**
     * Gets the unique identifier of this gate.
     * 
     * @return the gate's ID string
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the number of input pins.
     * 
     * @return the number of input pins
     */
    public int getNumInputs() {
        return numInputs;
    }
    
    /**
     * Gets the number of output pins.
     * 
     * @return the number of output pins
     */
    public int getNumOutputs() {
        return numOutputs;
    }
    
    /**
     * Abstract method that each concrete gate must implement to compute its outputs
     * based on current input values.
     * 
     * <p>This method should read the current input values and set the appropriate
     * output values according to the gate's logic.
     */
    public abstract void evaluate();
}
