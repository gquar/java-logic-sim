package sim.demo;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates sample images for LUT optimization testing.
 * 
 * <p>Creates a source image (ungraded) and target image (graded)
 * with a simple color transformation for testing the LUT system.
 */
public class LUTSampleGenerator {
    public static void main(String[] args) throws Exception {
        // Create output directories
        Path samplesDir = Paths.get("docs/samples");
        Files.createDirectories(samplesDir);
        
        // Generate source image (ungraded) - smaller size for more quantization effects
        BufferedImage src = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = src.createGraphics();
        
        // Create a gradient pattern with more quantization
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                // Use fewer distinct values to create quantization effects
                int r = (x * 4) & 0xFF;  // Quantize to ~16 levels
                int g_val = (y * 4) & 0xFF;
                int b = ((x + y) * 2) & 0xFF;
                
                // Add some color variation
                Color color = new Color(r, g_val, b);
                src.setRGB(x, y, color.getRGB());
            }
        }
        g.dispose();
        
        // Generate validation source image (different pattern with different seed)
        BufferedImage valSrc = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D gVal = valSrc.createGraphics();
        
        // Create a different pattern for validation with different seed
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                // Use different quantization pattern with different seed
                int r = ((x * 7 + y * 3) * 2) & 0xFF;
                int g_val = ((y * 11 + x * 5) * 3) & 0xFF;
                int b = ((x * 13 + y * 7) * 5) & 0xFF;
                
                Color color = new Color(r, g_val, b);
                valSrc.setRGB(x, y, color.getRGB());
            }
        }
        gVal.dispose();
        
        // Generate target image (graded) - apply non-linear transformation
        BufferedImage tgt = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tgt.createGraphics();
        
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                Color srcColor = new Color(src.getRGB(x, y));
                
                // Convert to [0,1] sRGB
                double r = srcColor.getRed() / 255.0;
                double green = srcColor.getGreen() / 255.0;
                double b = srcColor.getBlue() / 255.0;
                
                        // Apply more complex non-linear transformation with dither
        // Per-channel gamma with position-dependent variation
        double rp = Math.pow(r, 1.9);
        double gp = Math.pow(green, 2.1);
        double bp = Math.pow(b, 1.8);
        
        // Cross-channel mixing with position-dependent weights
        double mixFactor = Math.sin((x + y) * 0.05) * 0.15;
        double r2 = (0.85 + mixFactor) * rp + (0.10 - mixFactor) * gp + 0.05 * bp;
        double green2 = 0.05 * rp + (0.85 - mixFactor) * gp + (0.10 + mixFactor) * bp;
        double b2 = 0.10 * rp + 0.05 * gp + (0.85 + mixFactor) * bp;
        
        // Add small dither (±1 LSB) to prevent perfect reconstruction
        double dither = (Math.sin(x * 0.7) * Math.cos(y * 0.9) * 0.5 + 0.5) * 0.002 - 0.001;
        r2 += dither;
        green2 += dither * 0.8;
        b2 += dither * 1.2;
                
                // Clamp to [0,1]
                r2 = Math.max(0, Math.min(1, r2));
                green2 = Math.max(0, Math.min(1, green2));
                b2 = Math.max(0, Math.min(1, b2));
                
                // Convert back to [0,255]
                Color tgtColor = new Color((float)r2, (float)green2, (float)b2);
                tgt.setRGB(x, y, tgtColor.getRGB());
            }
        }
        g2.dispose();
        
        // Generate validation target image with same transformation
        BufferedImage valTgt = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = valTgt.createGraphics();
        
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                Color srcColor = new Color(valSrc.getRGB(x, y));
                
                // Convert to [0,1] sRGB
                double r = srcColor.getRed() / 255.0;
                double green = srcColor.getGreen() / 255.0;
                double b = srcColor.getBlue() / 255.0;
                
                // Apply slightly different transformation for validation
                double rp = Math.pow(r, 1.85);  // Slightly different gamma
                double gp = Math.pow(green, 2.15);
                double bp = Math.pow(b, 1.75);
                
                double mixFactor = Math.sin((x + y) * 0.07) * 0.12;  // Different frequency
                double r2 = (0.87 + mixFactor) * rp + (0.08 - mixFactor) * gp + 0.05 * bp;
                double green2 = 0.05 * rp + (0.87 - mixFactor) * gp + (0.08 + mixFactor) * bp;
                double b2 = 0.08 * rp + 0.05 * gp + (0.87 + mixFactor) * bp;
                
                // Add +/- 1 LSB dither to validation target only
                double dither = (Math.sin(x * 0.9) * Math.cos(y * 1.1) * 0.5 + 0.5) * 0.004 - 0.002;
                r2 += dither;
                green2 += dither * 0.9;
                b2 += dither * 1.1;
                
                r2 = Math.max(0, Math.min(1, r2));
                green2 = Math.max(0, Math.min(1, green2));
                b2 = Math.max(0, Math.min(1, b2));
                
                Color tgtColor = new Color((float)r2, (float)green2, (float)b2);
                valTgt.setRGB(x, y, tgtColor.getRGB());
            }
        }
        g3.dispose();
        
        // Save images
        Path srcPath = samplesDir.resolve("src.png");
        Path tgtPath = samplesDir.resolve("tgt.png");
        Path valSrcPath = samplesDir.resolve("val_src.png");
        Path valTgtPath = samplesDir.resolve("val_tgt.png");
        
        ImageIO.write(src, "png", srcPath.toFile());
        ImageIO.write(tgt, "png", tgtPath.toFile());
        ImageIO.write(valSrc, "png", valSrcPath.toFile());
        ImageIO.write(valTgt, "png", valTgtPath.toFile());
        
        System.out.println("Generated sample images:");
        System.out.println("  Source (ungraded): " + srcPath);
        System.out.println("  Target (graded): " + tgtPath);
        System.out.println("  Validation Source: " + valSrcPath);
        System.out.println("  Validation Target: " + valTgtPath);
        System.out.println("Transformation: gamma (1.9, 2.1, 1.8) + cross-channel mixing + dither");
        System.out.println("Validation: gamma (1.85, 2.15, 1.75) + different mixing + enhanced dither");
    }
}
