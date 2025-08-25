package sim.image.lut;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

/**
 * Test validation PSNR computation to ensure finite values.
 */
public class LUTQualityValidationTest {
    
    @TempDir
    Path tempDir;
    
    private BufferedImage trainSrc, trainTgt, valSrc, valTgt;
    
    @BeforeEach
    void setUp() {
        // Create tiny 8x8 test images with different patterns
        trainSrc = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        trainTgt = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        valSrc = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        valTgt = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        
        // Fill training images with one pattern
        Graphics2D g1 = trainSrc.createGraphics();
        Graphics2D g2 = trainTgt.createGraphics();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int r = (x * 32) & 0xFF;
                int g = (y * 32) & 0xFF;
                int b = ((x + y) * 16) & 0xFF;
                
                Color srcColor = new Color(r, g, b);
                trainSrc.setRGB(x, y, srcColor.getRGB());
                
                // Apply transformation to target
                float rNorm = r / 255.0f;
                float gNorm = g / 255.0f;
                float bNorm = b / 255.0f;
                
                float rOut = (float) Math.pow(rNorm, 1.9);
                float gOut = (float) Math.pow(gNorm, 2.1);
                float bOut = (float) Math.pow(bNorm, 1.8);
                
                Color tgtColor = new Color(rOut, gOut, bOut);
                trainTgt.setRGB(x, y, tgtColor.getRGB());
            }
        }
        g1.dispose();
        g2.dispose();
        
        // Fill validation images with different pattern
        Graphics2D g3 = valSrc.createGraphics();
        Graphics2D g4 = valTgt.createGraphics();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int r = ((x * 7 + y * 3) * 2) & 0xFF;
                int g = ((y * 11 + x * 5) * 3) & 0xFF;
                int b = ((x * 13 + y * 7) * 5) & 0xFF;
                
                Color srcColor = new Color(r, g, b);
                valSrc.setRGB(x, y, srcColor.getRGB());
                
                // Apply slightly different transformation
                float rNorm = r / 255.0f;
                float gNorm = g / 255.0f;
                float bNorm = b / 255.0f;
                
                float rOut = (float) Math.pow(rNorm, 1.85);
                float gOut = (float) Math.pow(gNorm, 2.15);
                float bOut = (float) Math.pow(bNorm, 1.75);
                
                // Add dither to validation target
                double dither = (Math.sin(x * 0.9) * Math.cos(y * 1.1) * 0.5 + 0.5) * 0.004 - 0.002;
                rOut += dither;
                gOut += dither * 0.9;
                bOut += dither * 1.1;
                
                rOut = Math.max(0, Math.min(1, rOut));
                gOut = Math.max(0, Math.min(1, gOut));
                bOut = Math.max(0, Math.min(1, bOut));
                
                Color tgtColor = new Color(rOut, gOut, bOut);
                valTgt.setRGB(x, y, tgtColor.getRGB());
            }
        }
        g3.dispose();
        g4.dispose();
    }
    
    @Test
    void testValidationProducesFinitePSNR() throws Exception {
        // Run sweep with size=9, nearest on train, trilinear on val
        int[] sizes = {9};
        List<LUTQuality.Result> results = LUTQuality.sweep(
            trainSrc, trainTgt, valSrc, valTgt, sizes, false, tempDir, null);
        
        assertFalse(results.isEmpty(), "Should have results");
        
        LUTQuality.Result result = results.get(0);
        assertEquals(9, result.size, "Should test size 9");
        assertTrue(result.hasVal, "Should have validation data");
        
        // Check that validation MSE is non-zero
        LUTQuality.assertNonZero(result.valMse, "validation MSE");
        
        // Check that validation PSNR is finite
        assertFalse(Double.isInfinite(result.valPsnr), 
                   "Validation PSNR should be finite, got: " + result.valPsnr);
        assertTrue(result.valPsnr > 0, 
                  "Validation PSNR should be positive, got: " + result.valPsnr);
        
        // Training PSNR might be infinite (perfect reconstruction)
        // but validation should not be
        if (Double.isInfinite(result.trainPsnr)) {
            System.out.println("Training PSNR is infinite (perfect reconstruction)");
        }
        
        System.out.printf("Training PSNR: %.2f dB, Validation PSNR: %.2f dB%n", 
                         result.trainPsnr, result.valPsnr);
    }
    
    @Test
    void testMSEComputationInLinearLight() {
        // Create two images with same sRGB values but different linear light
        BufferedImage img1 = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                // Same sRGB values
                int rgb = new Color(x * 64, y * 64, 128).getRGB();
                img1.setRGB(x, y, rgb);
                img2.setRGB(x, y, rgb);
            }
        }
        
        // MSE should be zero for identical images
        double mse = LUTQuality.mse(img1, img2);
        assertEquals(0.0, mse, 1e-10, "MSE should be zero for identical images");
        
        // Create slightly different images
        img2.setRGB(0, 0, new Color(65, 64, 128).getRGB());
        double mseDiff = LUTQuality.mse(img1, img2);
        assertTrue(mseDiff > 0, "MSE should be positive for different images");
    }
}
