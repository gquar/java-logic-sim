package sim.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD
import java.util.Collections;
// import java.util.ArrayDeque;
// import java.util.Queue;

=======
import java.util.Queue;
>>>>>>> origin/feat/composite-gates

/**
 * A digital circuit that contains gates, wires, and manages signal propagation.
 * 
 * <p>A circuit represents a complete digital logic design with:
 * - Multiple gates connected by wires
 * - Primary inputs that can be set from outside the circuit
 * - Primary outputs that can be read from outside the circuit
 * - Automatic signal propagation through the circuit using topological ordering
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
        if (name == null || name.isBlank()) 
            throw new IllegalArgumentException("Primary input name can't be empty");
        if (!gates.contains(gate)) 
            throw new IllegalArgumentException("Gate not in circuit: " + gate.getId());
        if (pin < 0 || pin >= gate.getNumInputs())
            throw new IllegalArgumentException("Invalid pin " + pin + " for gate " + gate.getId());
    
        // Create binding: associate input name with specific gate and pin
        // If this input name doesn't exist yet, create a new list for it
        primaryInputBindings.computeIfAbsent(name, k -> new ArrayList<>())
                           .add(new InputBinding(gate, pin));
        
        // Initialize the primary input value to false (default state)
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
     * Computes a topological ordering of gates using Kahn's algorithm.
     * 
     * <p>This method builds an adjacency list from the wires, computes in-degrees
     * for each gate, and returns gates in dependency order. If a cycle is detected,
     * it throws an IllegalStateException.
     * 
     * @return list of gates in topological order
     * @throws IllegalStateException if a combinational cycle is detected
     */
    public List<Gate> topologicalOrder() {
        // Build adjacency list: gate -> list of gates it connects to
        Map<Gate, List<Gate>> adjacencyList = new HashMap<>();
        Map<Gate, Integer> inDegree = new HashMap<>();
        
        // Initialize adjacency list and in-degree for all gates
        for (Gate gate : gates) {
            adjacencyList.put(gate, new ArrayList<>());
            inDegree.put(gate, 0);
        }
        
        // Build adjacency list from wires and compute in-degrees
        for (Wire wire : wires) {
            Gate fromGate = wire.getFromGate();
            Gate toGate = wire.getToGate();
            
            adjacencyList.get(fromGate).add(toGate);
            inDegree.put(toGate, inDegree.get(toGate) + 1);
        }
        
        // Kahn's algorithm: find gates with no incoming edges
        Queue<Gate> queue = new LinkedList<>();
        for (Gate gate : gates) {
            if (inDegree.get(gate) == 0) {
                queue.offer(gate);
            }
        }
        
        List<Gate> topologicalOrder = new ArrayList<>();
        int processedCount = 0;
        
        while (!queue.isEmpty()) {
            Gate currentGate = queue.poll();
            topologicalOrder.add(currentGate);
            processedCount++;
            
            // Remove current gate's outgoing edges and update in-degrees
            for (Gate neighbor : adjacencyList.get(currentGate)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // Check if all gates were processed (no cycles)
        if (processedCount != gates.size()) {
            throw new IllegalStateException("Combinational cycle detected; cannot topologically sort");
        }
        
        return topologicalOrder;
    }
    
    /**
     * Propagates signals through the circuit using topological ordering.
     * 
     * <p>This method:
     * 1. Applies primary input values to their bound gate input pins
     * 2. Evaluates gates once in topological order (no iterative settling)
     * 3. Pushes wire values after each gate evaluation for immediate propagation
     * 
     * <p>Choice: Push wire values after each gate evaluation rather than in a post-pass
     * because it ensures that downstream gates receive updated values immediately,
     * maintaining the correct dependency order and avoiding the need for multiple passes.
     */
    public void propagate() {
<<<<<<< HEAD
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
=======
        // Step 1: Apply primary input values to bound gate input pins
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
        
        // Step 2: Get topological ordering of gates
        List<Gate> topoOrder = topologicalOrder();
        
        // Step 3: Evaluate gates in topological order and push wire values immediately
        for (Gate gate : topoOrder) {
            // Evaluate the gate to compute its outputs
            gate.evaluate();
            
            // Push wire values from this gate's outputs to downstream inputs
            // This ensures immediate propagation and maintains dependency order
            for (Wire wire : wires) {
                if (wire.getFromGate() == gate) {
                    boolean wireValue = wire.read();
                    wire.getToGate().setInput(wire.getToPin(), wireValue);
                }
>>>>>>> origin/feat/composite-gates
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


public Map<String, List<InputBinding>> getPrimaryInputBindings() {
    return Collections.unmodifiableMap(primaryInputBindings);
}

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
}
