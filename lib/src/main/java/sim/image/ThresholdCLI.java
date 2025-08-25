package sim.image;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * CLI tool to convert images to boolean grids via thresholding.
 * 
 * <p>Usage: ThresholdCLI --in <path> [--out <png>] [--threshold <0..255>] [--invert] [--csv <path>]
 */
public final class ThresholdCLI {
    private ThresholdCLI() {}
    
    public static void main(String[] args) throws Exception {
        String inPath = null;
        String outPath = "docs/threshold/out.png";
        int threshold = 128;
        boolean invert = false;
        String csvPath = null;
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--in":
                    if (++i < args.length) inPath = args[i];
                    break;
                case "--out":
                    if (++i < args.length) outPath = args[i];
                    break;
                case "--threshold":
                    if (++i < args.length) threshold = Integer.parseInt(args[i]);
                    break;
                case "--invert":
                    invert = true;
                    break;
                case "--csv":
                    if (++i < args.length) csvPath = args[i];
                    break;
            }
        }
        
        if (inPath == null) {
            System.err.println("Usage: ThresholdCLI --in <path> [--out <png>] [--threshold <0..255>] [--invert] [--csv <path>]");
            System.exit(1);
        }
        
        if (threshold < 0 || threshold > 255) {
            System.err.println("Threshold must be 0-255, got: " + threshold);
            System.exit(1);
        }
        
        // Load and process image
        BufferedImage img = ImageIO.read(Paths.get(inPath).toFile());
        if (img == null) {
            System.err.println("Could not load image: " + inPath);
            System.exit(1);
        }
        
        boolean[][] grid = thresholdImage(img, threshold, invert);
        
        // Save grid as PNG
        Path outPng = Paths.get(outPath);
        String title = String.format("Threshold %d%s (%dx%d)", 
            threshold, invert ? " inverted" : "", grid[0].length, grid.length);
        GridPNG.save(grid, outPng, 8, 16, title);
        
        // Save CSV if requested
        if (csvPath != null) {
            Path csv = Paths.get(csvPath);
            saveCsv(grid, csv);
        }
        
        // Auto-open on desktop systems
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                new ProcessBuilder("open", outPng.toString()).start();
            } else if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", outPng.toString()).start();
            } else {
                new ProcessBuilder("xdg-open", outPng.toString()).start();
            }
        } catch (Exception ignore) {}
        
        System.out.println("Wrote " + outPng + " (" + grid[0].length + "x" + grid.length + ")");
        if (csvPath != null) {
            System.out.println("Wrote CSV: " + csvPath);
        }
    }
    
    /**
     * Converts an image to a boolean grid using thresholding.
     * 
     * @param img the input image
     * @param threshold threshold value (0-255)
     * @param invert if true, invert the result
     * @return boolean grid where true = above threshold
     */
    static boolean[][] thresholdImage(BufferedImage img, int threshold, boolean invert) {
        int width = img.getWidth();
        int height = img.getHeight();
        boolean[][] grid = new boolean[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(img.getRGB(x, y));
                
                // Convert to grayscale using luma formula
                int gray = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
                
                boolean above = gray >= threshold;
                grid[y][x] = invert ? !above : above;
            }
        }
        
        return grid;
    }
    
    /**
     * Saves a boolean grid as CSV.
     * 
     * @param grid the boolean grid
     * @param csvPath path to save CSV
     */
    static void saveCsv(boolean[][] grid, Path csvPath) throws Exception {
        if (csvPath.getParent() != null) {
            Files.createDirectories(csvPath.getParent());
        }
        
        try (BufferedWriter w = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("width,height\n");
            w.write(grid[0].length + "," + grid.length + "\n");
            w.write("data\n");
            
            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[y].length; x++) {
                    w.write(grid[y][x] ? "1" : "0");
                    if (x < grid[y].length - 1) w.write(",");
                }
                w.write("\n");
            }
        }
    }
}
