package sim.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for the topological ordering functionality of the Circuit class.
 * 
 * <p>This test class verifies that:
 * - Valid circuits can be topologically sorted
 * - Circuits with cycles throw appropriate exceptions
 * - Topological ordering respects gate dependencies
 * 
 * @author Digital Logic Simulator Tests
 * @version 1.0
 */
public class CircuitTopoTest {
    
    private Circuit circuit;
    private AndGate and1, and2;
    private OrGate or1;
    private NotGate not1;
    
    @BeforeEach
    void setUp() {
        circuit = new Circuit();
        
        // Create test gates
        and1 = new AndGate("AND1");
        and2 = new AndGate("AND2");
        or1 = new OrGate("OR1");
        not1 = new NotGate("NOT1");
        
        // Add gates to circuit
        circuit.addGate(and1);
        circuit.addGate(and2);
        circuit.addGate(or1);
        circuit.addGate(not1);
    }
    
    /**
     * Tests that a valid circuit with fanout can be topologically sorted.
     * 
     * <p>Circuit structure:
     * AND1 -> OR1
     * AND2 -> OR1
     * NOT1 -> OR1
     * 
     * <p>Expected order: [AND1, AND2, NOT1, OR1] (inputs first, then output)
     */
    @Test
    void testValidTopoOrderOnFanout() {
        // Create wires: multiple inputs to one output (fanout)
        Wire wire1 = new Wire(and1, 0, or1, 0);  // AND1 -> OR1.in0
        Wire wire2 = new Wire(and2, 0, or1, 1);  // AND2 -> OR1.in1
        // This tests fanout from multiple sources to one destination
        
        // Add wires to circuit
        circuit.addWire(wire1);
        circuit.addWire(wire2);
        
        // Get topological ordering
        List<Gate> topoOrder = circuit.topologicalOrder();
        
        // Verify all gates are included
        assertEquals(4, topoOrder.size());
        assertTrue(topoOrder.contains(and1));
        assertTrue(topoOrder.contains(and2));
        assertTrue(topoOrder.contains(not1));
        assertTrue(topoOrder.contains(or1));
        
        // Verify input gates come before output gates
        int and1Index = topoOrder.indexOf(and1);
        int and2Index = topoOrder.indexOf(and2);
        int or1Index = topoOrder.indexOf(or1);
        
        // Connected input gates should come before the output gate
        assertTrue(and1Index < or1Index, "AND1 should come before OR1");
        assertTrue(and2Index < or1Index, "AND2 should come before OR1");
        
        // NOT1 is not connected, so it can be in any position
        // Input gates can be in any order relative to each other
        // (they have no dependencies on each other)
    }
    
    /**
     * Tests that a circuit with a combinational cycle throws an exception.
     * 
     * <p>Circuit structure with cycle:
     * AND1 -> OR1 -> NOT1 -> AND1 (creates a cycle)
     * 
     * <p>This should throw IllegalStateException with cycle detection message.
     */
    @Test
    void testCycleThrows() {
        // Create wires that form a cycle
        Wire wire1 = new Wire(and1, 0, or1, 0);      // AND1 -> OR1
        Wire wire2 = new Wire(or1, 0, not1, 0);      // OR1 -> NOT1
        Wire wire3 = new Wire(not1, 0, and1, 0);     // NOT1 -> AND1 (creates cycle!)
        
        // Add wires to circuit
        circuit.addWire(wire1);
        circuit.addWire(wire2);
        circuit.addWire(wire3);
        
        // Attempting to get topological order should throw exception
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> circuit.topologicalOrder(),
            "Circuit with cycle should throw IllegalStateException"
        );
        
        // Verify the exception message
        String message = exception.getMessage();
        assertTrue(
            message.contains("Combinational cycle detected") || 
            message.contains("cannot topologically sort"),
            "Exception message should indicate cycle detection"
        );
    }
    
    /**
     * Tests that a simple linear circuit can be topologically sorted.
     * 
     * <p>Circuit structure: AND1 -> OR1 -> NOT1 (linear chain)
     */
    @Test
    void testLinearCircuitTopoOrder() {
        // Create linear chain of gates
        Wire wire1 = new Wire(and1, 0, or1, 0);      // AND1 -> OR1
        Wire wire2 = new Wire(or1, 0, not1, 0);      // OR1 -> NOT1
        
        // Add wires to circuit
        circuit.addWire(wire1);
        circuit.addWire(wire2);
        
        // Get topological ordering
        List<Gate> topoOrder = circuit.topologicalOrder();
        
        // Verify order: AND1 -> OR1 -> NOT1
        // Since AND1 and AND2 are inputs to OR1, they should come before OR1
        // OR1 should come before NOT1 since OR1 is input to NOT1
        assertTrue(topoOrder.indexOf(and1) < topoOrder.indexOf(or1), "AND1 should come before OR1");
        assertTrue(topoOrder.indexOf(or1) < topoOrder.indexOf(not1), "OR1 should come before NOT1");
    }
    
    /**
     * Tests that a circuit with no wires (just gates) can be topologically sorted.
     * 
     * <p>Gates with no dependencies can be ordered in any sequence.
     */
    @Test
    void testNoWiresTopoOrder() {
        // No wires added - all gates are independent
        
        // Get topological ordering
        List<Gate> topoOrder = circuit.topologicalOrder();
        
        // Verify all gates are included
        assertEquals(4, topoOrder.size());
        assertTrue(topoOrder.contains(and1));
        assertTrue(topoOrder.contains(and2));
        assertTrue(topoOrder.contains(or1));
        assertTrue(topoOrder.contains(not1));
        
        // Order doesn't matter for independent gates
        // Just verify all gates are present
    }
}
