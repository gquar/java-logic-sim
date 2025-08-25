package sim.image.lut;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Renders preview swatches for 3D LUTs.
 * 
 * <p>Creates a 16×16 grid showing how the LUT transforms
 * a range of input colors to output colors.
 */
public final class CubePreview {
    private CubePreview() {}
    
    /**
     * Renders a preview image of the LUT transformation.
     * 
     * @param lut the 3D LUT to preview
     * @param outPath path to save the preview image
     * @param title optional title for the preview
     */
    public static void renderPreview(LUT3D lut, Path outPath, String title) throws Exception {
        final int gridSize = 16;
        final int cellSize = 32;
        final int margin = 16;
        
        int titleH = (title == null || title.isBlank()) ? 0 : 20;
        int width = gridSize * cellSize + margin * 2;
        int height = gridSize * cellSize + margin * 2 + titleH;
        
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Background
            g.setColor(new Color(0xf8f9fb));
            g.fillRect(0, 0, width, height);
            
            // Title
            int offsetY = margin;
            if (titleH > 0) {
                g.setColor(new Color(0x223344));
                g.setFont(new Font("Helvetica", Font.PLAIN, 14));
                g.drawString(title, margin, margin + 14);
                offsetY += titleH - 2;
            }
            
            // Draw color swatches
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    // Generate input color based on grid position
                    float r = (float) x / (gridSize - 1);
                    float green = (float) y / (gridSize - 1);
                    float b = 0.5f; // Fixed blue for 2D preview
                    
                    // Apply LUT transformation
                    float[] output = lut.applyPixel(r, green, b);
                    
                    // Draw swatch
                    int px = margin + x * cellSize;
                    int py = offsetY + y * cellSize;
                    
                    // Input color (top half)
                    Color inputColor = new Color(r, green, b);
                    g.setColor(inputColor);
                    g.fillRect(px, py, cellSize, cellSize / 2);
                    
                    // Output color (bottom half)
                    Color outputColor = new Color(output[0], output[1], output[2]);
                    g.setColor(outputColor);
                    g.fillRect(px, py + cellSize / 2, cellSize, cellSize / 2);
                    
                    // Border
                    g.setColor(new Color(0xb0b7c3));
                    g.drawRect(px, py, cellSize, cellSize);
                }
            }
        } finally {
            g.dispose();
        }
        
        if (outPath.getParent() != null) {
            Files.createDirectories(outPath.getParent());
        }
        ImageIO.write(img, "png", outPath.toFile());
    }
}
