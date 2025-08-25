package sim.image.color;

/**
 * sRGB color space utilities.
 * 
 * <p>Implements IEC 61966-2-1 sRGB transfer function for converting
 * between sRGB and linear RGB color spaces.
 */
public final class Srgb {
    private Srgb() {}
    
    /**
     * Converts sRGB value to linear RGB.
     * 
     * <p>Transfer function:
     * - s ≤ 0.04045: l = s / 12.92
     * - s > 0.04045: l = ((s + 0.055) / 1.055)^2.4
     * 
     * @param s sRGB value in [0, 1]
     * @return linear RGB value in [0, 1]
     */
    public static float srgbToLinear(float s) {
        s = clamp(s);
        
        if (s <= 0.04045f) {
            return s / 12.92f;
        } else {
            return (float) Math.pow((s + 0.055f) / 1.055f, 2.4);
        }
    }
    
    /**
     * Converts linear RGB value to sRGB.
     * 
     * <p>Inverse transfer function:
     * - l ≤ 0.0031308: s = l * 12.92
     * - l > 0.0031308: s = 1.055 * l^(1/2.4) - 0.055
     * 
     * @param l linear RGB value in [0, 1]
     * @return sRGB value in [0, 1]
     */
    public static float linearToSrgb(float l) {
        l = clamp(l);
        
        if (l <= 0.0031308f) {
            return l * 12.92f;
        } else {
            return (float) (1.055 * Math.pow(l, 1.0 / 2.4) - 0.055);
        }
    }
    
    /**
     * Clamps a value to the range [0, 1].
     * 
     * @param value the value to clamp
     * @return the clamped value
     */
    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
