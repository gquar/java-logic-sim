package sim.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for 3-input LUT gate with configurable truth tables.
 */
public class LUT3Test {
    
    @Test
    void testZeroMask() {
        LUT3 lut = new LUT3("ZERO", 0x00);
        
        // All 8 input combinations should output false
        boolean[][] inputs = {
            {false, false, false}, // 000
            {false, false, true},  // 001
            {false, true, false},  // 010
            {false, true, true},   // 011
            {true, false, false},  // 100
            {true, false, true},   // 101
            {true, true, false},   // 110
            {true, true, true}     // 111
        };
        
        for (boolean[] input : inputs) {
            lut.setInput(0, input[0]);
            lut.setInput(1, input[1]);
            lut.setInput(2, input[2]);
            lut.evaluate();
            assertFalse(lut.getOutput(0), "Zero mask should always output false");
        }
    }
    
    @Test
    void testAllOnesMask() {
        LUT3 lut = new LUT3("ONES", 0xFF);
        
        // All 8 input combinations should output true
        boolean[][] inputs = {
            {false, false, false}, // 000
            {false, false, true},  // 001
            {false, true, false},  // 010
            {false, true, true},   // 011
            {true, false, false},  // 100
            {true, false, true},   // 101
            {true, true, false},   // 110
            {true, true, true}     // 111
        };
        
        for (boolean[] input : inputs) {
            lut.setInput(0, input[0]);
            lut.setInput(1, input[1]);
            lut.setInput(2, input[2]);
            lut.evaluate();
            assertTrue(lut.getOutput(0), "All-ones mask should always output true");
        }
    }
    
    @Test
    void testXorParityMask() {
        // 0x96 = 10010110 in binary
        // This implements 3-bit XOR parity (odd number of 1s = true)
        LUT3 lut = new LUT3("XOR3", 0x96);
        
        // Expected outputs for XOR parity (odd number of 1s = true)
        boolean[] expected = {
            false, // 000 (0 ones)
            true,  // 001 (1 one)
            true,  // 010 (1 one)
            false, // 011 (2 ones)
            true,  // 100 (1 one)
            false, // 101 (2 ones)
            false, // 110 (2 ones)
            true   // 111 (3 ones)
        };
        
        boolean[][] inputs = {
            {false, false, false}, // 000
            {false, false, true},  // 001
            {false, true, false},  // 010
            {false, true, true},   // 011
            {true, false, false},  // 100
            {true, false, true},   // 101
            {true, true, false},   // 110
            {true, true, true}     // 111
        };
        
        for (int i = 0; i < inputs.length; i++) {
            lut.setInput(0, inputs[i][0]);
            lut.setInput(1, inputs[i][1]);
            lut.setInput(2, inputs[i][2]);
            lut.evaluate();
            assertEquals(expected[i], lut.getOutput(0), 
                "XOR parity failed for input " + i);
        }
    }
    
    @Test
    void testMajorityMask() {
        // 0xE8 = 11101000 in binary
        // This implements majority function (2+ inputs true = true)
        LUT3 lut = new LUT3("MAJ", 0xE8);
        
        // Expected outputs for majority (2+ inputs true = true)
        boolean[] expected = {
            false, // 000 (0 ones)
            false, // 001 (1 one)
            false, // 010 (1 one)
            true,  // 011 (2 ones)
            false, // 100 (1 one)
            true,  // 101 (2 ones)
            true,  // 110 (2 ones)
            true   // 111 (3 ones)
        };
        
        boolean[][] inputs = {
            {false, false, false}, // 000
            {false, false, true},  // 001
            {false, true, false},  // 010
            {false, true, true},   // 011
            {true, false, false},  // 100
            {true, false, true},   // 101
            {true, true, false},   // 110
            {true, true, true}     // 111
        };
        
        for (int i = 0; i < inputs.length; i++) {
            lut.setInput(0, inputs[i][0]);
            lut.setInput(1, inputs[i][1]);
            lut.setInput(2, inputs[i][2]);
            lut.evaluate();
            assertEquals(expected[i], lut.getOutput(0), 
                "Majority failed for input " + i);
        }
    }
    
    @Test
    void testInvalidMask() {
        assertThrows(IllegalArgumentException.class, () -> new LUT3("BAD", -1));
        assertThrows(IllegalArgumentException.class, () -> new LUT3("BAD", 0x100));
        assertThrows(IllegalArgumentException.class, () -> new LUT3("BAD", 0x1000));
    }
    
    @Test
    void testGetMask() {
        LUT3 lut = new LUT3("TEST", 0x5A);
        assertEquals(0x5A, lut.getMask());
    }
}
