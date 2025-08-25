package sim.image.lut;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Tests for LUT learning functionality.
 */
public class LUTLearnerTest {
    
    @Test
    void testSimpleOffsetLearning() {
        // Create 2x2 synthetic images for simple testing
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        BufferedImage tgt = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        
        // Simple test pattern: black to white
        src.setRGB(0, 0, Color.BLACK.getRGB());
        src.setRGB(1, 0, Color.WHITE.getRGB());
        src.setRGB(0, 1, Color.GRAY.getRGB());
        src.setRGB(1, 1, Color.RED.getRGB());
        
        // Target: slightly brighter versions
        tgt.setRGB(0, 0, new Color(32, 32, 32).getRGB());
        tgt.setRGB(1, 0, Color.WHITE.getRGB());
        tgt.setRGB(0, 1, new Color(160, 160, 160).getRGB());
        tgt.setRGB(1, 1, new Color(255, 128, 128).getRGB());
        
        // Learn LUT
        LUT3D lut = LUTLearner.learnFromPair(src, tgt, 17);
        
        // Test application
        BufferedImage result = LUTLearner.apply(src, lut, false);
        
        // Just verify the result has the right dimensions
        assertEquals(2, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(BufferedImage.TYPE_INT_RGB, result.getType());
    }
    
    @Test
    void testInvalidInputs() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        
        // Test null inputs
        assertThrows(IllegalArgumentException.class, () -> 
            LUTLearner.learnFromPair(null, img, 17));
        assertThrows(IllegalArgumentException.class, () -> 
            LUTLearner.learnFromPair(img, null, 17));
        
        // Test different dimensions
        BufferedImage img2 = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        assertThrows(IllegalArgumentException.class, () -> 
            LUTLearner.learnFromPair(img, img2, 17));
    }
    
    @Test
    void testApplyWithNullInputs() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        LUT3D lut = new LUT3D(17);
        
        assertThrows(IllegalArgumentException.class, () -> 
            LUTLearner.apply(null, lut, false));
        assertThrows(IllegalArgumentException.class, () -> 
            LUTLearner.apply(img, null, false));
    }
}
