package sim.core;

/**
 * Demonstration class showing how to use the digital logic gates.
 * 
 * <p>This class creates instances of each gate type and demonstrates
 * their behavior with various input combinations.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class LogicSimulatorDemo {
    
    /**
     * Main method that demonstrates the logic gates.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Digital Logic Simulator Demo ===\n");
        
        // Create instances of each gate type
        AndGate andGate = new AndGate("AND1");
        OrGate orGate = new OrGate("OR1");
        NotGate notGate = new NotGate("NOT1");
        XorGate xorGate = new XorGate("XOR1");
        
        // Test AND gate
        System.out.println("Testing AND Gate:");
        testGate(andGate, "AND");
        
        // Test OR gate
        System.out.println("\nTesting OR Gate:");
        testGate(orGate, "OR");
        
        // Test NOT gate
        System.out.println("\nTesting NOT Gate:");
        testNotGate(notGate);
        
        // Test XOR gate
        System.out.println("\nTesting XOR Gate:");
        testGate(xorGate, "XOR");
    }
    
    /**
     * Tests a 2-input gate with all possible input combinations.
     * 
     * @param gate the gate to test
     * @param gateName the name of the gate type for display
     */
    private static void testGate(Gate gate, String gateName) {
        boolean[][] testInputs = {
            {false, false},
            {false, true},
            {true, false},
            {true, true}
        };
        
        for (boolean[] inputs : testInputs) {
            gate.setInput(0, inputs[0]);
            gate.setInput(1, inputs[1]);
            gate.evaluate();
            
            System.out.printf("  %s(%s, %s) = %s%n", 
                gateName, 
                inputs[0], 
                inputs[1], 
                gate.getOutput(0)
            );
        }
    }
    
    /**
     * Tests a 1-input NOT gate with both possible input values.
     * 
     * @param gate the NOT gate to test
     */
    private static void testNotGate(NotGate gate) {
        boolean[] testInputs = {false, true};
        
        for (boolean input : testInputs) {
            gate.setInput(0, input);
            gate.evaluate();
            
            System.out.printf("  NOT(%s) = %s%n", input, gate.getOutput(0));
        }
    }
}
