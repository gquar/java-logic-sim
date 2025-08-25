package sim.image.lut;

/**
 * 3D color lookup table for color transformations.
 * 
 * <p>Stores a cube of RGB values indexed by input RGB coordinates.
 * Each dimension has size N, creating an N³ cube of output colors.
 * All values are in sRGB space [0, 1].
 */
public final class LUT3D {
    
    private final float[][][][] cube; // [r][g][b][3] where 3 = RGB
    private final int size;
    
    /**
     * Creates a 3D LUT with the specified size.
     * 
     * @param size the size of each dimension (typically 17, 33, 65)
     */
    public LUT3D(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive, got: " + size);
        }
        this.size = size;
        this.cube = new float[size][size][size][3];
        
        // Initialize with identity mapping
        for (int r = 0; r < size; r++) {
            for (int g = 0; g < size; g++) {
                for (int b = 0; b < size; b++) {
                    cube[r][g][b][0] = (float) r / (size - 1); // R
                    cube[r][g][b][1] = (float) g / (size - 1); // G
                    cube[r][g][b][2] = (float) b / (size - 1); // B
                }
            }
        }
    }
    
    /**
     * Gets the size of the LUT cube.
     * 
     * @return the size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Creates an identity LUT (no transformation).
     * 
     * @param size the size of each dimension
     * @return an identity LUT
     */
    public static LUT3D identity(int size) {
        return new LUT3D(size);
    }
    
    /**
     * Sets a value in the 3D LUT.
     * 
     * @param r red index (0 to size-1)
     * @param g green index (0 to size-1)
     * @param b blue index (0 to size-1)
     * @param rgb output RGB values (0.0 to 1.0)
     */
    public void set(int r, int g, int b, float[] rgb) {
        if (r < 0 || r >= size || g < 0 || g >= size || b < 0 || b >= size) {
            throw new IllegalArgumentException("Index out of bounds: " + r + "," + g + "," + b);
        }
        if (rgb.length != 3) {
            throw new IllegalArgumentException("RGB array must have 3 elements");
        }
        
        cube[r][g][b][0] = clamp(rgb[0]);
        cube[r][g][b][1] = clamp(rgb[1]);
        cube[r][g][b][2] = clamp(rgb[2]);
    }
    
    /**
     * Gets a value from the 3D LUT.
     * 
     * @param r red index (0 to size-1)
     * @param g green index (0 to size-1)
     * @param b blue index (0 to size-1)
     * @return output RGB values (0.0 to 1.0)
     */
    public float[] get(int r, int g, int b) {
        if (r < 0 || r >= size || g < 0 || g >= size || b < 0 || b >= size) {
            throw new IllegalArgumentException("Index out of bounds: " + r + "," + g + "," + b);
        }
        
        return new float[] {
            cube[r][g][b][0],
            cube[r][g][b][1],
            cube[r][g][b][2]
        };
    }
    
    /**
     * Converts a value to the nearest grid index.
     * 
     * @param v value in [0, 1]
     * @return grid index in [0, size-1]
     */
    public int nearestIndex(float v) {
        v = clamp(v);
        int index = Math.round(v * (size - 1));
        return Math.max(0, Math.min(size - 1, index));
    }
    
    /**
     * Applies the LUT to a pixel using nearest neighbor interpolation.
     * 
     * @param r input red value (0.0 to 1.0)
     * @param g input green value (0.0 to 1.0)
     * @param b input blue value (0.0 to 1.0)
     * @return output RGB values (0.0 to 1.0)
     */
    public float[] applyNearest(float r, float g, float b) {
        // Clamp inputs
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        
        // Convert to indices
        int ri = nearestIndex(r);
        int gi = nearestIndex(g);
        int bi = nearestIndex(b);
        
        return get(ri, gi, bi);
    }
    
    /**
     * Applies the LUT to a pixel using trilinear interpolation.
     * 
     * @param r input red value (0.0 to 1.0)
     * @param g input green value (0.0 to 1.0)
     * @param b input blue value (0.0 to 1.0)
     * @return output RGB values (0.0 to 1.0)
     */
    public float[] applyTrilinear(float r, float g, float b) {
        // Clamp inputs
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        
        // Convert to continuous indices
        float rf = r * (size - 1);
        float gf = g * (size - 1);
        float bf = b * (size - 1);
        
        // Get integer indices
        int r0 = (int) Math.floor(rf);
        int g0 = (int) Math.floor(gf);
        int b0 = (int) Math.floor(bf);
        
        // Clamp to valid range
        r0 = Math.max(0, Math.min(size - 2, r0));
        g0 = Math.max(0, Math.min(size - 2, g0));
        b0 = Math.max(0, Math.min(size - 2, b0));
        
        int r1 = r0 + 1;
        int g1 = g0 + 1;
        int b1 = b0 + 1;
        
        // Calculate interpolation weights
        float wr = rf - r0;
        float wg = gf - g0;
        float wb = bf - b0;
        
        // Interpolate each channel
        float[] result = new float[3];
        for (int c = 0; c < 3; c++) {
            float v000 = cube[r0][g0][b0][c];
            float v001 = cube[r0][g0][b1][c];
            float v010 = cube[r0][g1][b0][c];
            float v011 = cube[r0][g1][b1][c];
            float v100 = cube[r1][g0][b0][c];
            float v101 = cube[r1][g0][b1][c];
            float v110 = cube[r1][g1][b0][c];
            float v111 = cube[r1][g1][b1][c];
            
            // Trilinear interpolation
            float v00 = v000 * (1 - wb) + v001 * wb;
            float v01 = v010 * (1 - wb) + v011 * wb;
            float v10 = v100 * (1 - wb) + v101 * wb;
            float v11 = v110 * (1 - wb) + v111 * wb;
            
            float v0 = v00 * (1 - wg) + v01 * wg;
            float v1 = v10 * (1 - wg) + v11 * wg;
            
            result[c] = v0 * (1 - wr) + v1 * wr;
        }
        
        return result;
    }
    
    /**
     * Applies the LUT to a pixel using nearest neighbor interpolation.
     * 
     * @param r input red value (0.0 to 1.0)
     * @param g input green value (0.0 to 1.0)
     * @param b input blue value (0.0 to 1.0)
     * @return output RGB values (0.0 to 1.0)
     */
    public float[] applyPixel(float r, float g, float b) {
        return applyNearest(r, g, b);
    }
    
    /**
     * Clamps a value to the range [0.0, 1.0].
     * 
     * @param value the value to clamp
     * @return the clamped value
     */
    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
