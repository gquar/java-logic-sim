package sim.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Tests for ThresholdCLI image-to-grid conversion.
 */
public class ThresholdCLITest {
    
    @Test
    void testThresholdImage(@TempDir Path tempDir) throws Exception {
        // Create a test image: 8x8 with top half bright, bottom half dark
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        
        // Top half: bright (white)
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 8; x++) {
                img.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        
        // Bottom half: dark (black)
        for (int y = 4; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                img.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
        
        // Save test image
        Path testImg = tempDir.resolve("test.png");
        javax.imageio.ImageIO.write(img, "png", testImg.toFile());
        
        // Test thresholding
        boolean[][] grid = ThresholdCLI.thresholdImage(img, 128, false);
        
        // Verify dimensions
        assertEquals(8, grid.length);
        assertEquals(8, grid[0].length);
        
        // Verify top half is true (above threshold)
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 8; x++) {
                assertTrue(grid[y][x], "Top half should be above threshold");
            }
        }
        
        // Verify bottom half is false (below threshold)
        for (int y = 4; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                assertFalse(grid[y][x], "Bottom half should be below threshold");
            }
        }
        
        // Test inverted thresholding
        boolean[][] inverted = ThresholdCLI.thresholdImage(img, 128, true);
        
        // Verify inverted results
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 8; x++) {
                assertFalse(inverted[y][x], "Inverted top half should be false");
            }
        }
        for (int y = 4; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                assertTrue(inverted[y][x], "Inverted bottom half should be true");
            }
        }
    }
    
    @Test
    void testSaveCsv(@TempDir Path tempDir) throws Exception {
        // Create a simple 2x3 grid
        boolean[][] grid = {
            {true, false, true},
            {false, true, false}
        };
        
        Path csvPath = tempDir.resolve("test.csv");
        ThresholdCLI.saveCsv(grid, csvPath);
        
        // Verify CSV content
        List<String> lines = Files.readAllLines(csvPath);
        assertEquals(5, lines.size());
        assertEquals("width,height", lines.get(0));
        assertEquals("3,2", lines.get(1));
        assertEquals("data", lines.get(2));
        assertEquals("1,0,1", lines.get(3));
        assertEquals("0,1,0", lines.get(4));
    }
    
    @Test
    void testLumaConversion() {
        // Test that luma conversion works correctly
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        
        // Red pixel: should convert to grayscale
        img.setRGB(0, 0, Color.RED.getRGB());
        boolean[][] grid = ThresholdCLI.thresholdImage(img, 128, false);
        
        // Red (255,0,0) → luma = 0.299*255 + 0.587*0 + 0.114*0 = 76
        // 76 < 128, so should be false
        assertFalse(grid[0][0]);
        
        // White pixel: should be above threshold
        img.setRGB(0, 0, Color.WHITE.getRGB());
        grid = ThresholdCLI.thresholdImage(img, 128, false);
        assertTrue(grid[0][0]);
    }
}
