package sim.demo;

import sim.image.lut.LUT3D;
import sim.image.lut.LUTLearner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demo comparing nearest neighbor vs trilinear interpolation for LUTs.
 * 
 * <p>Trains a LUT on sample images and applies it using both interpolation
 * methods to demonstrate the difference in quality.
 */
public class LUTCompareDemo {
    public static void main(String[] args) throws Exception {
        // Create output directory
        Path outDir = Paths.get("docs/lut_opt");
        Files.createDirectories(outDir);
        
        // Load sample images
        BufferedImage src = ImageIO.read(Paths.get("docs/samples/src.png").toFile());
        BufferedImage tgt = ImageIO.read(Paths.get("docs/samples/tgt.png").toFile());
        
        if (src == null || tgt == null) {
            System.err.println("Could not load sample images. Run :lib:lutSamples first.");
            System.exit(1);
        }
        
        // Train LUT
        System.out.println("Training LUT (size=17)...");
        LUT3D lut = LUTLearner.learnFromPair(src, tgt, 17);
        
        // Apply with nearest neighbor
        System.out.println("Applying with nearest neighbor interpolation...");
        BufferedImage nearest = LUTLearner.apply(src, lut, false);
        Path nearestPath = outDir.resolve("nearest.png");
        ImageIO.write(nearest, "png", nearestPath.toFile());
        
        // Apply with trilinear interpolation
        System.out.println("Applying with trilinear interpolation...");
        BufferedImage trilinear = LUTLearner.apply(src, lut, true);
        Path trilinearPath = outDir.resolve("trilinear.png");
        ImageIO.write(trilinear, "png", trilinearPath.toFile());
        
        System.out.println("Comparison images saved:");
        System.out.println("  Nearest neighbor: " + nearestPath);
        System.out.println("  Trilinear: " + trilinearPath);
        System.out.println("\nNote: Trilinear interpolation typically produces smoother gradients");
        System.out.println("      with fewer quantization artifacts, especially for small LUT sizes.");
    }
}
