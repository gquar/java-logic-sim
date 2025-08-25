package sim.image.lut;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Learns 3D LUTs from source/target image pairs.
 * 
 * <p>Given a pair of images (source=ungraded, target=graded),
 * learns a 3D LUT that approximates the color transformation.
 * 
 * <p>Note: Currently uses sRGB space directly. Linear-space learning
 * is a future improvement for better accuracy.
 */
public final class LUTLearner {
    private LUTLearner() {}
    
    /**
     * Learns a 3D LUT from a source/target image pair.
     * 
     * @param src source image (ungraded)
     * @param tgt target image (graded)
     * @param size LUT size (e.g., 17, 33, 65)
     * @return learned 3D LUT
     */
    public static LUT3D learnFromPair(BufferedImage src, BufferedImage tgt, int size) {
        // Validate inputs
        if (src == null || tgt == null) {
            throw new IllegalArgumentException("Source and target images cannot be null");
        }
        
        if (src.getWidth() != tgt.getWidth() || src.getHeight() != tgt.getHeight()) {
            throw new IllegalArgumentException("Source and target images must have same dimensions");
        }
        
        // Create LUT
        LUT3D lut = new LUT3D(size);
        
        int width = src.getWidth();
        int height = src.getHeight();
        
        // Accumulate target colors for each bin
        float[][][][] accum = new float[size][size][size][3];
        int[][][] counts = new int[size][size][size];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color srcColor = new Color(src.getRGB(x, y));
                Color tgtColor = new Color(tgt.getRGB(x, y));
                
                // Convert to sRGB [0, 1]
                float srcR = srcColor.getRed() / 255.0f;
                float srcG = srcColor.getGreen() / 255.0f;
                float srcB = srcColor.getBlue() / 255.0f;
                
                float tgtR = tgtColor.getRed() / 255.0f;
                float tgtG = tgtColor.getGreen() / 255.0f;
                float tgtB = tgtColor.getBlue() / 255.0f;
                
                // Find nearest grid index
                int ri = lut.nearestIndex(srcR);
                int gi = lut.nearestIndex(srcG);
                int bi = lut.nearestIndex(srcB);
                
                // Accumulate target color
                accum[ri][gi][bi][0] += tgtR;
                accum[ri][gi][bi][1] += tgtG;
                accum[ri][gi][bi][2] += tgtB;
                counts[ri][gi][bi]++;
            }
        }
        
        // Average accumulated colors
        for (int r = 0; r < size; r++) {
            for (int g = 0; g < size; g++) {
                for (int b = 0; b < size; b++) {
                    if (counts[r][g][b] > 0) {
                        float[] avg = {
                            accum[r][g][b][0] / counts[r][g][b],
                            accum[r][g][b][1] / counts[r][g][b],
                            accum[r][g][b][2] / counts[r][g][b]
                        };
                        lut.set(r, g, b, avg);
                    }
                    // Unfilled bins keep identity mapping (already set in constructor)
                }
            }
        }
        
        return lut;
    }
    
    /**
     * Applies a 3D LUT to an image.
     * 
     * @param in input image
     * @param lut 3D LUT to apply
     * @param trilinear if true, use trilinear interpolation; otherwise nearest neighbor
     * @return transformed image
     */
    public static BufferedImage apply(BufferedImage in, LUT3D lut, boolean trilinear) {
        if (in == null || lut == null) {
            throw new IllegalArgumentException("Input image and LUT cannot be null");
        }
        
        int width = in.getWidth();
        int height = in.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(in.getRGB(x, y));
                
                // Convert to sRGB [0, 1]
                float r = color.getRed() / 255.0f;
                float g = color.getGreen() / 255.0f;
                float b = color.getBlue() / 255.0f;
                
                // Apply LUT
                float[] output;
                if (trilinear) {
                    output = lut.applyTrilinear(r, g, b);
                } else {
                    output = lut.applyNearest(r, g, b);
                }
                
                // Convert back to [0, 255] and create new color
                Color newColor = new Color(output[0], output[1], output[2]);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        
        return result;
    }
    
    /**
     * Count the number of populated bins in a LUT.
     * 
     * @param lut the LUT to analyze
     * @return number of bins that have been set (non-zero count)
     */
    public static int populatedBins(LUT3D lut) {
        int size = lut.getSize();
        int count = 0;
        
        for (int r = 0; r < size; r++) {
            for (int g = 0; g < size; g++) {
                for (int b = 0; b < size; b++) {
                    float[] rgb = lut.get(r, g, b);
                    // Check if this bin has been set (not the default identity mapping)
                    if (rgb[0] != (float)r / (size - 1) || 
                        rgb[1] != (float)g / (size - 1) || 
                        rgb[2] != (float)b / (size - 1)) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
}
