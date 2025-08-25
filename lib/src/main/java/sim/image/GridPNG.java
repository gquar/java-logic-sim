package sim.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Renders a boolean[][] matrix as a pixel grid PNG.
 * true  = "on" cell (blue), false = "off" cell (light gray).
 * Use scale for cell size, margin for padding, title for overlay text.
 */
public final class GridPNG {
    private GridPNG() {}

    public static void save(boolean[][] grid, Path outPng, int scale, int margin, String title) throws Exception {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            throw new IllegalArgumentException("grid must be non-empty");
        }
        final int rows = grid.length;
        final int cols = grid[0].length;

        int titleH = (title == null || title.isBlank()) ? 0 : 18;
        int width  = cols * scale + margin * 2;
        int height = rows * scale + margin * 2 + titleH;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // background
            g.setColor(new Color(0xf8f9fb));
            g.fillRect(0, 0, width, height);

            // optional title
            int offsetY = margin;
            if (titleH > 0) {
                g.setColor(new Color(0x223344));
                g.setFont(new Font("Helvetica", Font.PLAIN, 12));
                g.drawString(title, margin, margin + 12);
                offsetY += titleH - 2;
            }

            // draw cells
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    boolean on = grid[r][c];
                    int x = margin + c * scale;
                    int y = offsetY + r * scale;

                    // fill
                    g.setColor(on ? new Color(0x1e88e5) : new Color(0xdfe3eb));
                    g.fillRect(x, y, scale, scale);

                    // stroke
                    g.setColor(new Color(0xb0b7c3));
                    g.drawRect(x, y, scale, scale);
                }
            }
        } finally {
            g.dispose();
        }

        if (outPng.getParent() != null) Files.createDirectories(outPng.getParent());
        ImageIO.write(img, "png", outPng.toFile());
    }
}
