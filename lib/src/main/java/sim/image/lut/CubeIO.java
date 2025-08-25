package sim.image.lut;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * I/O utilities for .cube LUT files.
 * 
 * <p>Supports reading and writing standard .cube format files
 * used in color grading and post-production.
 */
public final class CubeIO {
    private CubeIO() {}
    
    /**
     * Writes a 3D LUT to a .cube file.
     * 
     * <p>Standard .cube format:
     * - TITLE (optional)
     * - LUT_3D_SIZE N
     * - DOMAIN_MIN 0 0 0
     * - DOMAIN_MAX 1 1 1
     * - N³ lines of "R G B" values in [0, 1]
     * 
     * <p>Iteration order: i,j,k (red varies fastest, then green, then blue)
     * 
     * @param file the output file
     * @param lut the 3D LUT to write
     * @param title optional title for the LUT
     * @param size the size of the LUT (should match lut.getSize())
     */
    public static void writeCube(Path file, LUT3D lut, String title, int size) throws IOException {
        if (lut.getSize() != size) {
            throw new IllegalArgumentException("LUT size mismatch: " + lut.getSize() + " vs " + size);
        }
        
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            // Write header
            if (title != null && !title.isBlank()) {
                w.write("TITLE " + title + "\n");
            }
            w.write("LUT_3D_SIZE " + size + "\n");
            w.write("DOMAIN_MIN 0.0 0.0 0.0\n");
            w.write("DOMAIN_MAX 1.0 1.0 1.0\n");
            
            // Write RGB values in standard order: i,j,k (red varies fastest)
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        float[] rgb = lut.get(i, j, k);
                        w.write(String.format("%.6f %.6f %.6f\n", rgb[0], rgb[1], rgb[2]));
                    }
                }
            }
        }
    }
    
    /**
     * Reads a 3D LUT from a .cube file.
     * 
     * @param file the input file
     * @return the loaded 3D LUT
     */
    public static LUT3D readCube(Path file) throws IOException {
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name())) {
            int size = -1;
            
            // Parse header
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                if (line.startsWith("LUT_3D_SIZE")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        size = Integer.parseInt(parts[1]);
                        break;
                    }
                }
            }
            
            if (size <= 0) {
                throw new IOException("Invalid or missing LUT_3D_SIZE in cube file");
            }
            
            LUT3D lut = new LUT3D(size);
            
            // Skip remaining header lines
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("TITLE") || 
                    line.startsWith("DOMAIN_MIN") || line.startsWith("DOMAIN_MAX")) {
                    continue;
                }
                
                // Parse RGB values
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    float r = Float.parseFloat(parts[0]);
                    float g = Float.parseFloat(parts[1]);
                    float b = Float.parseFloat(parts[2]);
                    
                    // Calculate indices (assuming sequential order)
                    int index = 0;
                    for (int ri = 0; ri < size; ri++) {
                        for (int gi = 0; gi < size; gi++) {
                            for (int bi = 0; bi < size; bi++) {
                                if (index == 0) {
                                    lut.set(ri, gi, bi, new float[]{r, g, b});
                                    index++;
                                    break;
                                }
                                index++;
                            }
                            if (index > 0) break;
                        }
                        if (index > 0) break;
                    }
                }
            }
            
            return lut;
        }
    }
}
