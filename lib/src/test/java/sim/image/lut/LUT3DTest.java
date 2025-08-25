package sim.image.lut;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for 3D LUT functionality.
 */
public class LUT3DTest {
    
    @Test
    void testIdentityMapping() {
        LUT3D lut = LUT3D.identity(17);
        
        // Test identity mapping round-trip
        float[] input = {0.5f, 0.25f, 0.75f};
        float[] output = lut.applyNearest(input[0], input[1], input[2]);
        
        assertEquals(input[0], output[0], 0.01f);
        assertEquals(input[1], output[1], 0.01f);
        assertEquals(input[2], output[2], 0.01f);
    }
    
    @Test
    void testNearestIndex() {
        LUT3D lut = new LUT3D(17);
        
        // Test edge cases
        assertEquals(0, lut.nearestIndex(0.0f));
        assertEquals(16, lut.nearestIndex(1.0f));
        assertEquals(8, lut.nearestIndex(0.5f));
        
        // Test clamping
        assertEquals(0, lut.nearestIndex(-0.1f));
        assertEquals(16, lut.nearestIndex(1.1f));
    }
    
    @Test
    void testTrilinearInterpolation() {
        LUT3D lut = new LUT3D(3); // Small size for testing
        
        // Set a simple transformation: increase red by 0.1
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    float r = (float) i / 2.0f + 0.1f;
                    float g = (float) j / 2.0f;
                    float b = (float) k / 2.0f;
                    lut.set(i, j, k, new float[]{r, g, b});
                }
            }
        }
        
        // Test interpolation at center point
        float[] result = lut.applyTrilinear(0.5f, 0.5f, 0.5f);
        
        // Just verify the result is in reasonable range
        assertTrue(result[0] >= 0.0f && result[0] <= 1.0f);
        assertTrue(result[1] >= 0.0f && result[1] <= 1.0f);
        assertTrue(result[2] >= 0.0f && result[2] <= 1.0f);
        
        // Verify red channel is increased (should be > 0.5)
        assertTrue(result[0] > 0.5f);
    }
    
    @Test
    void testInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new LUT3D(0));
        assertThrows(IllegalArgumentException.class, () -> new LUT3D(-1));
    }
    
    @Test
    void testInvalidIndices() {
        LUT3D lut = new LUT3D(17);
        
        assertThrows(IllegalArgumentException.class, () -> lut.get(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> lut.get(17, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> lut.set(-1, 0, 0, new float[]{0, 0, 0}));
        assertThrows(IllegalArgumentException.class, () -> lut.set(17, 0, 0, new float[]{0, 0, 0}));
    }
}
