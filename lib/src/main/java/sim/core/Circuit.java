package sim.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A digital circuit that contains gates, wires, and manages signal propagation.
 * 
 * <p>A circuit represents a complete digital logic design with:
 * - Multiple gates connected by wires
 * - Primary inputs that can be set from outside the circuit
 * - Primary outputs that can be read from outside the circuit
 * - Automatic signal propagation through the circuit
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class Circuit {
    
    /** List of all gates in the circuit */
    private final List<Gate> gates;
    
    /** List of all wires connecting gates in the circuit */
    private final List<Wire> wires;
    
    /** Map of primary input names to their boolean values */
    private final Map<String, Boolean> primaryInputs;
    

    /** Map of primary input names to their gate and pin bindings */
    private final Map<String, List<InputBinding>> primaryInputBindings = new LinkedHashMap<>();
    
    /** List of gates designated as primary outputs */
    private final List<Gate> primaryOutputs;
    
    /** Maximum number of propagation iterations to prevent infinite loops */
    private static final int MAX_ITERATIONS = 1000;
    
    /**
     * Constructs a new empty circuit.
     */
    public Circuit() {
        this.gates = new ArrayList<>();
        this.wires = new ArrayList<>();
        this.primaryInputs = new HashMap<>();
        this.primaryOutputs = new ArrayList<>();
    }
    
    /**
     * Adds a gate to the circuit.
     * 
     * @param gate the gate to add
     * @return the index of the added gate in the circuit's gate list
     */
    public int addGate(Gate gate) {
        gates.add(gate);
        return gates.size() - 1;
    }
    
    /**
     * Adds a wire to the circuit.
     * 
     * @param wire the wire to add
     */
    public void addWire(Wire wire) {
        wires.add(wire);
    }
    
    /**
     * Binds a primary input name to a specific gate's input pin.
     * 
     * @param name the name of the primary input
     * @param gate the gate to bind to
     * @param pin the input pin number on the gate
     */
    public void connectPrimaryInput(String name, Gate gate, int pin) {
        // Find the gate index
        int gateIndex = gates.indexOf(gate);
        if (gateIndex == -1) {
            throw new IllegalArgumentException("Gate not found in circuit: " + gate.getId());
        }
        
        // Store the binding
        primaryInputBindings.computeIfAbsent(name, k -> new ArrayList<>())
                           .add(new InputBinding(gate, pin));
        
        // Initialize the primary input value to false
        primaryInputs.putIfAbsent(name, false);
    }
    
    /**
     * Sets the value of a primary input.
     * 
     * @param name the name of the primary input
     * @param value the boolean value to set
     */
    public void setPrimaryInput(String name, boolean value) {
        primaryInputs.put(name, value);
    }
    
    /**
     * Adds a gate as a primary output.
     * 
     * @param gate the gate to designate as a primary output
     */
    public void addPrimaryOutput(Gate gate) {
        if (!gates.contains(gate)) {
            throw new IllegalArgumentException("Gate not found in circuit: " + gate.getId());
        }
        primaryOutputs.add(gate);
    }
    
    /**
     * Reads the values from all primary outputs.
     * 
     * @return a list of boolean values from the primary outputs
     */
    public List<Boolean> readPrimaryOutputs() {
        List<Boolean> outputs = new ArrayList<>();
        for (Gate gate : primaryOutputs) {
            outputs.add(gate.getOutput(0)); // All gates have 1 output
        }
        return outputs;
    }
    
    /**
     * Propagates signals through the circuit until stable or max iterations reached.
     * 
     * <p>This method:
     * 1. Copies primary input values to bound gate input pins
     * 2. Evaluates all gates
     * 3. Pushes outputs along wires to downstream inputs
     * 4. Repeats until no changes occur or max iterations reached
     */
    public void propagate() {
        int iterations = 0;
        boolean changes;
        
        do {
            changes = false;
            iterations++;
            
            // Copy primary input values to bound gate input pins
            for (Map.Entry<String, Boolean> entry : primaryInputs.entrySet()) {
                String inputName = entry.getKey();
                Boolean inputValue = entry.getValue();
                
                List<InputBinding> bindings = primaryInputBindings.get(inputName);
                if (bindings != null) {
                    for (InputBinding binding : bindings) {
                        binding.gate().setInput(binding.pin(), inputValue);
                    }
                }
            }
            
            // Evaluate all gates and track changes
            for (Gate gate : gates) {
                // Store old output values
                List<Boolean> oldOutputs = new ArrayList<>();
                for (int i = 0; i < gate.getNumOutputs(); i++) {
                    oldOutputs.add(gate.getOutput(i));
                }
                
                // Evaluate the gate
                gate.evaluate();
                
                // Check if any output changed
                for (int i = 0; i < gate.getNumOutputs(); i++) {
                    if (!oldOutputs.get(i).equals(gate.getOutput(i))) {
                        changes = true;
                    }
                }
            }
            
            // Push outputs along wires to downstream inputs
            for (Wire wire : wires) {
                boolean wireValue = wire.read();
                wire.getToGate().setInput(wire.getToPin(), wireValue);
            }
            
        } while (changes && iterations < MAX_ITERATIONS);
        
        if (iterations >= MAX_ITERATIONS) {
            System.out.println("Warning: Circuit propagation stopped after " + MAX_ITERATIONS + " iterations");
        }
    }
    
    /**
     * Gets the list of gates in the circuit.
     * 
     * @return the list of gates
     */
    public List<Gate> getGates() {
        return new ArrayList<>(gates);
    }
    
    /**
     * Gets the list of wires in the circuit.
     * 
     * @return the list of wires
     */
    public List<Wire> getWires() {
        return new ArrayList<>(wires);
    }
}
