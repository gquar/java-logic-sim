package sim.core;
import java.util.List;

/**
 * Demonstration of building and testing a digital logic circuit.
 * 
 * <p>This demo creates the circuit: (A AND B) OR (NOT C)
 * 
 * <p>Circuit structure:
 * - G1: AND gate (inputs: A, B)
 * - G2: NOT gate (input: C)  
 * - G3: OR gate (inputs: G1 output, G2 output)
 * 
 * <p>Primary inputs: A, B, C
 * Primary output: G3
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class Demo {
    
    /**
     * Main method that builds and tests the circuit.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Digital Logic Circuit Demo ===");
        System.out.println("Building circuit: (A AND B) OR (NOT C)\n");
        
        // Create a new circuit
        Circuit circuit = new Circuit();
        
        // Create the gates
        AndGate g1 = new AndGate("G1");      // AND gate for A AND B
        NotGate g2 = new NotGate("G2");      // NOT gate for NOT C
        OrGate g3 = new OrGate("G3");        // OR gate for final output
        
        // Add gates to the circuit
        circuit.addGate(g1);
        circuit.addGate(g2);
        circuit.addGate(g3);
        
        // Bind primary inputs
        circuit.connectPrimaryInput("A", g1, 0);  // A -> G1.in0
        circuit.connectPrimaryInput("B", g1, 1);  // B -> G1.in1
        circuit.connectPrimaryInput("C", g2, 0);  // C -> G2.in0
        
        // Create wires to connect gates
        Wire wire1 = new Wire(g1, 0, g3, 0);  // G1.out0 -> G3.in0
        Wire wire2 = new Wire(g2, 0, g3, 1);  // G2.out0 -> G3.in1
        
        // Add wires to the circuit
        circuit.addWire(wire1);
        circuit.addWire(wire2);
        
        // Add G3 as a primary output
        circuit.addPrimaryOutput(g3);
        
        // Set input values: A=true, B=true, C=false
        System.out.println("Setting inputs:");
        System.out.println("  A = true");
        System.out.println("  B = true");
        System.out.println("  C = false");
        System.out.println();
        
        circuit.setPrimaryInput("A", true);
        circuit.setPrimaryInput("B", true);
        circuit.setPrimaryInput("C", false);
        
        // Propagate signals through the circuit
        System.out.println("Propagating signals through the circuit...");
        circuit.propagate();
        System.out.println("Propagation complete!\n");
        
        // Read and display the outputs
        List<Boolean> outputs = circuit.readPrimaryOutputs();
        System.out.println("Primary outputs:");
        for (int i = 0; i < outputs.size(); i++) {
            System.out.println("  Output " + i + ": " + outputs.get(i));
        }
        
        // Show the expected result
        System.out.println("\nExpected result:");
        System.out.println("  (A AND B) OR (NOT C)");
        System.out.println("  (true AND true) OR (NOT false)");
        System.out.println("  true OR true");
        System.out.println("  = true");
        
        // Verify the result matches expectation
        boolean actualResult = outputs.get(0);
        
        System.out.println("\nVerification:");
        System.out.println("  Expected: true");
        System.out.println("  Actual:   " + actualResult);
        System.out.println("  Match:    " + (actualResult ? "✓" : "✗"));
    }
}
