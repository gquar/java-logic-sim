package sim.core.composite;

import org.junit.jupiter.api.Test;
<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.*;

import sim.core.Circuit;

public class CompositeGatesTest {

    @Test
    void halfAdder_truthTable() {
        // SUM = A ^ B
        // CARRY = A & B
        for (boolean A : new boolean[]{false, true}) {
            for (boolean B : new boolean[]{false, true}) {
                Circuit c = new Circuit();
                // Add HA and expose outputs as primary outputs
                CompositeGates.HalfAdderHandle ha =
                        CompositeGates.buildHalfAdder(c, "HA", "A", "B", /*addAsPrimaryOutputs=*/true);

                // Drive inputs
                c.setPrimaryInput("A", A);
                c.setPrimaryInput("B", B);

                // Evaluate
                c.propagate();

                var outs = c.readPrimaryOutputs(); // [SUM, CARRY] (by our builder order)
                boolean sum   = outs.get(0);
                boolean carry = outs.get(1);

                boolean expSum   = A ^ B;
                boolean expCarry = A & B;

                assertEquals(
                        expSum, sum,
                        "HA SUM mismatch for A=" + A + " B=" + B);

                assertEquals(
                        expCarry, carry,
                        "HA CARRY mismatch for A=" + A + " B=" + B);
            }
        }
    }

    @Test
    void fullAdder_truthTable() {
        // SUM = (A ^ B) ^ Cin
        // COUT = (A & B) | ((A ^ B) & Cin)
        for (boolean A : new boolean[]{false, true}) {
            for (boolean B : new boolean[]{false, true}) {
                for (boolean Cin : new boolean[]{false, true}) {
                    Circuit c = new Circuit();
                    CompositeGates.FullAdderHandle fa =
                            CompositeGates.buildFullAdder(c, "FA", "A", "B", "Cin", /*addAsPrimaryOutputs=*/true);

                    c.setPrimaryInput("A", A);
                    c.setPrimaryInput("B", B);
                    c.setPrimaryInput("Cin", Cin);

                    c.propagate();

                    var outs = c.readPrimaryOutputs(); // [SUM, COUT] (by our builder order)
                    boolean sum  = outs.get(0);
                    boolean cout = outs.get(1);

                    boolean expSum  = (A ^ B) ^ Cin;
                    boolean expCout = (A & B) | ((A ^ B) & Cin);

                    // Helpful internals for debugging if this ever fails again
                    boolean xor1 = fa.xor1.getOutput(0);
                    boolean and1 = fa.and1.getOutput(0);
                    boolean and2 = fa.and2.getOutput(0);
                    boolean dbg_cout = fa.cout.getOutput(0);

                    assertEquals(
                            expSum, sum,
                            "SUM mismatch for A=" + A + " B=" + B + " Cin=" + Cin
                                    + " [xor1=" + xor1 + "]");

                    assertEquals(
                            expCout, cout,
                            "COUT mismatch for A=" + A + " B=" + B + " Cin=" + Cin
                                    + " [and1=" + and1 + ", and2=" + and2 + ", cout=" + dbg_cout + "]");
                }
            }
        }
    }
=======
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import sim.core.*;

/**
 * Comprehensive tests for all composite gates.
 * 
 * <p>This test class verifies the truth tables and functionality of:
 * - HalfAdder
 * - FullAdder
 * - Mux2 (2-to-1 multiplexer)
 * - Mux4 (4-to-1 multiplexer)
 * 
 * @author Digital Logic Simulator Tests
 * @version 1.0
 */
public class CompositeGatesTest {
    
    private CompositeBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new CompositeBuilder();
    }
    
    /**
     * Tests HalfAdder truth table exhaustively.
     */
    @Test
    void testHalfAdderTruthTable() {
        HalfAdder ha = builder.buildHalfAdder("HA1");
        
        // Test all input combinations
        boolean[][] testCases = {
            {false, false}, // A=0, B=0
            {false, true},  // A=0, B=1
            {true, false},  // A=1, B=0
            {true, true}    // A=1, B=1
        };
        
        boolean[][] expectedOutputs = {
            {false, false}, // SUM=0, CARRY=0
            {true, false},  // SUM=1, CARRY=0
            {true, false},  // SUM=1, CARRY=0
            {false, true}   // SUM=0, CARRY=1
        };
        
        for (int i = 0; i < testCases.length; i++) {
            boolean[] inputs = testCases[i];
            boolean[] expected = expectedOutputs[i];
            
            // Set inputs
            ha.setInput(0, inputs[0]); // A
            ha.setInput(1, inputs[1]); // B
            
            // Evaluate
            ha.evaluate();
            
            // Check outputs
            assertEquals(expected[0], ha.getOutput(0), 
                "SUM mismatch for A=" + inputs[0] + ", B=" + inputs[1]);
            assertEquals(expected[1], ha.getOutput(1), 
                "CARRY mismatch for A=" + inputs[0] + ", B=" + inputs[1]);
        }
    }
    
    /**
     * Tests FullAdder truth table exhaustively.
     */
    @Test
    void testFullAdderTruthTable() {
        FullAdder fa = builder.buildFullAdder("FA1");
        
        // Test all input combinations (8 cases)
        boolean[][] testCases = {
            {false, false, false}, // A=0, B=0, Cin=0
            {false, false, true},  // A=0, B=0, Cin=1
            {false, true, false},  // A=0, B=1, Cin=0
            {false, true, true},   // A=0, B=1, Cin=1
            {true, false, false},  // A=1, B=0, Cin=0
            {true, false, true},   // A=1, B=0, Cin=1
            {true, true, false},   // A=1, B=1, Cin=0
            {true, true, true}     // A=1, B=1, Cin=1
        };
        
        boolean[][] expectedOutputs = {
            {false, false}, // SUM=0, Cout=0
            {true, false},  // SUM=1, Cout=0
            {true, false},  // SUM=1, Cout=0
            {false, true},  // SUM=0, Cout=1
            {true, false},  // SUM=1, Cout=0
            {false, true},  // SUM=0, Cout=1
            {false, true},  // SUM=0, Cout=1
            {true, true}    // SUM=1, Cout=1
        };
        
        for (int i = 0; i < testCases.length; i++) {
            boolean[] inputs = testCases[i];
            boolean[] expected = expectedOutputs[i];
            
            // Set inputs
            fa.setInput(0, inputs[0]); // A
            fa.setInput(1, inputs[1]); // B
            fa.setInput(2, inputs[2]); // Cin
            
            // Evaluate
            fa.evaluate();
            
            // Check outputs
            assertEquals(expected[0], fa.getOutput(0), 
                "SUM mismatch for A=" + inputs[0] + ", B=" + inputs[1] + ", Cin=" + inputs[2]);
            assertEquals(expected[1], fa.getOutput(1), 
                "Cout mismatch for A=" + inputs[0] + ", B=" + inputs[1] + ", Cin=" + inputs[2]);
        }
    }
    
    /**
     * Tests Mux2 truth table exhaustively.
     */
    @Test
    void testMux2TruthTable() {
        Mux2 mux = builder.buildMux2("MUX1");
        
        // Test all input combinations (8 cases)
        boolean[][] testCases = {
            {false, false, false}, // Sel=0, D0=0, D1=0
            {false, false, true},  // Sel=0, D0=0, D1=1
            {false, true, false},  // Sel=0, D0=1, D1=0
            {false, true, true},   // Sel=0, D0=1, D1=1
            {true, false, false},  // Sel=1, D0=0, D1=0
            {true, false, true},   // Sel=1, D0=0, D1=1
            {true, true, false},   // Sel=1, D0=1, D1=0
            {true, true, true}     // Sel=1, D0=1, D1=1
        };
        
        boolean[] expectedOutputs = {
            false, // Sel=0, D0=0 → Out=0
            false, // Sel=0, D0=0 → Out=0
            true,  // Sel=0, D0=1 → Out=1
            true,  // Sel=0, D0=1 → Out=1
            false, // Sel=1, D1=0 → Out=0
            true,  // Sel=1, D1=1 → Out=1
            false, // Sel=1, D1=0 → Out=0
            true   // Sel=1, D1=1 → Out=1
        };
        
        for (int i = 0; i < testCases.length; i++) {
            boolean[] inputs = testCases[i];
            boolean expected = expectedOutputs[i];
            
            // Set inputs
            mux.setInput(0, inputs[0]); // Sel
            mux.setInput(1, inputs[1]); // D0
            mux.setInput(2, inputs[2]); // D1
            
            // Evaluate
            mux.evaluate();
            
            // Check output
            assertEquals(expected, mux.getOutput(0), 
                "Out mismatch for Sel=" + inputs[0] + ", D0=" + inputs[1] + ", D1=" + inputs[2]);
        }
    }
    
    /**
     * Tests Mux4 truth table for key selection patterns.
     */
    @Test
    void testMux4TruthTable() {
        Mux4 mux = builder.buildMux4("MUX4_1");
        
        // Test key selection patterns
        // Sel1=0, Sel0=0 → should select D0
        mux.setInput(0, false); // Sel0
        mux.setInput(1, false); // Sel1
        mux.setInput(2, true);  // D0
        mux.setInput(3, false); // D1
        mux.setInput(4, false); // D2
        mux.setInput(5, false); // D3
        mux.evaluate();
        assertTrue(mux.getOutput(0), "Should select D0 when Sel1=0, Sel0=0");
        
        // Sel1=0, Sel0=1 → should select D1
        mux.setInput(0, true);  // Sel0
        mux.setInput(1, false); // Sel1
        mux.setInput(2, false); // D0
        mux.setInput(3, true);  // D1
        mux.setInput(4, false); // D2
        mux.setInput(5, false); // D3
        mux.evaluate();
        assertTrue(mux.getOutput(0), "Should select D1 when Sel1=0, Sel0=1");
        
        // Sel1=1, Sel0=0 → should select D2
        mux.setInput(0, false); // Sel0
        mux.setInput(1, true);  // Sel1
        mux.setInput(2, false); // D0
        mux.setInput(3, false); // D1
        mux.setInput(4, true);  // D2
        mux.setInput(5, false); // D3
        mux.evaluate();
        assertTrue(mux.getOutput(0), "Should select D2 when Sel1=1, Sel0=0");
        
        // Sel1=1, Sel0=1 → should select D3
        mux.setInput(0, true);  // Sel0
        mux.setInput(1, true);  // Sel1
        mux.setInput(2, false); // D0
        mux.setInput(3, false); // D1
        mux.setInput(4, false); // D2
        mux.setInput(5, true);  // D3
        mux.evaluate();
        assertTrue(mux.getOutput(0), "Should select D3 when Sel1=1, Sel0=1");
    }
    
    /**
     * Tests that composite gates implement the Gate interface correctly.
     */
    @Test
    void testCompositeGatesImplementGateInterface() {
        // Test HalfAdder
        HalfAdder ha = builder.buildHalfAdder("HA2");
        assertEquals(2, ha.getNumInputs());
        assertEquals(2, ha.getNumOutputs());
        assertEquals("HA2", ha.getId());
        
        // Test FullAdder
        FullAdder fa = builder.buildFullAdder("FA2");
        assertEquals(3, fa.getNumInputs());
        assertEquals(2, fa.getNumOutputs());
        assertEquals("FA2", fa.getId());
        
        // Test Mux2
        Mux2 mux2 = builder.buildMux2("MUX2");
        assertEquals(3, mux2.getNumInputs());
        assertEquals(1, mux2.getNumOutputs());
        assertEquals("MUX2", mux2.getId());
        
        // Test Mux4
        Mux4 mux4 = builder.buildMux4("MUX4_2");
        assertEquals(6, mux4.getNumInputs());
        assertEquals(1, mux4.getNumOutputs());
        assertEquals("MUX4_2", mux4.getId());
    }
    
    /**
     * Tests input validation for composite gates.
     */
    @Test
    void testInputValidation() {
        HalfAdder ha = builder.buildHalfAdder("HA3");
        
        // Test valid input range
        assertDoesNotThrow(() -> ha.setInput(0, true));
        assertDoesNotThrow(() -> ha.setInput(1, false));
        
        // Test invalid input range
        assertThrows(IllegalArgumentException.class, () -> ha.setInput(-1, true));
        assertThrows(IllegalArgumentException.class, () -> ha.setInput(2, true));
        
        // Test output validation
        assertThrows(IllegalArgumentException.class, () -> ha.getOutput(-1));
        assertThrows(IllegalArgumentException.class, () -> ha.getOutput(2));
    }
>>>>>>> origin/feat/composite-gates
}
