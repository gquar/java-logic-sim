package sim.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Collections;


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
    
    public record InputBinding(Gate gate, int pin) { }

    /** Map of primary input names to their gate and pin bindings */
    private final Map<String, List<InputBinding>> primaryInputBindings;
    
    /** List of gates designated as primary outputs */
    private final List<Gate> primaryOutputs;
    
    /** Maximum number of propagation iterations to prevent infinite loops */
    private static final int MAX_ITERATIONS = 1000;
    
// Fast edge representation for outgoing connections
private static final class Edge {
    final Gate to;
    final int toPin;
    final int fromPin;
    Edge(Gate to, int toPin, int fromPin) {
        this.to = to; this.toPin = toPin; this.fromPin = fromPin;
    }
}

// Index of each gate for O(1) lookups in topo/adjacency
private final Map<Gate, Integer> gateIndex = new HashMap<>();
private final List<List<Edge>> outgoing = new ArrayList<>();

// Cache topo order; recompute only when structure changes
private List<Gate> topoOrderCache = null;
private boolean structureDirty = true;


    /**
     * Constructs a new empty circuit.
     */
    public Circuit() {
        this.gates = new ArrayList<>();
        this.wires = new ArrayList<>();
        this.primaryInputs = new HashMap<>();
        this.primaryInputBindings = new LinkedHashMap<>();
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
        int idx = gates.size() - 1;
        gateIndex.put(gate, idx);
        outgoing.add(new ArrayList<>());
        structureDirty = true;
        return idx;
    }
    
    

/**
 * Returns a topological ordering of gates using Kahn's algorithm.
 * Throws IllegalStateException if the circuit contains a cycle.
 */
public List<Gate> topologicalOrder() {
    if (!structureDirty && topoOrderCache != null) {
        return topoOrderCache;
    }
    int n = gates.size();
    int[] indeg = new int[n];

    // compute indegrees from outgoing lists
    for (int i = 0; i < n; i++) {
        for (Edge e : outgoing.get(i)) {
            int j = gateIndex.get(e.to);
            indeg[j]++;
        }
    }

    java.util.ArrayDeque<Integer> q = new java.util.ArrayDeque<>();
    for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);

    java.util.ArrayList<Gate> order = new java.util.ArrayList<>(n);
    int processed = 0;
    while (!q.isEmpty()) {
        int i = q.removeFirst();
        order.add(gates.get(i));
        processed++;
        for (Edge e : outgoing.get(i)) {
            int j = gateIndex.get(e.to);
            if (--indeg[j] == 0) q.add(j);
        }
    }

    if (processed != n) {
        throw new IllegalStateException("Cycle detected");
    }
    topoOrderCache = order;
    structureDirty = false;
    return topoOrderCache;
}


    
    /**
     * Adds a wire to the circuit.
     * 
     * @param wire the wire to add
     */
    public void addWire(Wire wire) {
        wires.add(wire);
    
        Integer fromIdx = gateIndex.get(wire.getFromGate());
        Integer toIdx   = gateIndex.get(wire.getToGate());
        if (fromIdx == null || toIdx == null) {
            throw new IllegalArgumentException("Wire connects a gate not in this circuit");
        }
        outgoing.get(fromIdx).add(new Edge(wire.getToGate(), wire.getToPin(), wire.getFromPin()));
        structureDirty = true;
    }
    
    
    /**
     * Binds a primary input name to a specific gate's input pin.
     * 
     * @param name the name of the primary input
     * @param gate the gate to bind to
     * @param pin the input pin number on the gate
     */
    public void connectPrimaryInput(String name, Gate gate, int pin) {
        // Validate input parameters
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Primary input name cannot be null or blank");
        }
        if (gate == null) {
            throw new IllegalArgumentException("Gate cannot be null");
        }
        if (pin < 0 || pin >= gate.getNumInputs()) {
            throw new IllegalArgumentException("Invalid pin " + pin + " for gate " + gate.getId());
        }
        
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
        // 1) Apply primary inputs to bound pins (usually small; OK to use maps here)
        for (Map.Entry<String, Boolean> entry : primaryInputs.entrySet()) {
            List<InputBinding> bindings = primaryInputBindings.get(entry.getKey());
            if (bindings != null) {
                boolean val = entry.getValue();
                for (InputBinding b : bindings) {
                    b.gate().setInput(b.pin(), val);
                }
            }
        }
    
        // 2) Evaluate once in topo order and push outputs along only each gate's outgoing edges
        List<Gate> order = topologicalOrder();
        for (Gate g : order) {
            g.evaluate();
            int fromIdx = gateIndex.get(g);
            for (Edge e : outgoing.get(fromIdx)) {
                boolean outVal = g.getOutput(e.fromPin);
                e.to.setInput(e.toPin, outVal);
            }
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
     * Gets the primary input bindings map.
     * 
     * @return unmodifiable map of primary input names to their bindings
     */
    public Map<String, List<InputBinding>> getPrimaryInputBindings() {
        return Collections.unmodifiableMap(primaryInputBindings);
    }

    /**
     * Gets the list of primary output gates.
     * 
     * @return unmodifiable list of primary output gates
     */
    public List<Gate> getPrimaryOutputs() {
        return Collections.unmodifiableList(primaryOutputs);
    }
    
    /**
     * Gets the list of wires in the circuit.
     * 
     * @return the list of wires
     */
    public List<Wire> getWires() {
        return new ArrayList<>(wires);
    }
    
    /**
     * Performs a complete clock cycle: low→high→low with propagate() on each step.
     * 
     * <p>This method:
     * 1. Sets the clock to false and propagates
     * 2. Sets the clock to true and propagates (rising edge)
     * 3. Sets the clock to false and propagates
     * 
     * @param clkName the name of the clock primary input
     */
    public void tick(String clkName) {
        setPrimaryInput(clkName, false);
        propagate();
        setPrimaryInput(clkName, true);
        propagate(); // rising edge
        setPrimaryInput(clkName, false);
        propagate();
    }
}
