package sim.demo;

import sim.core.Circuit;
import sim.core.Gate;
import sim.core.LUT3;
import sim.dot.GraphvizExporter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple demo showing 3D LUT concept via Graphviz.
 * 
 * <p>Creates a basic schematic: R,G,B → LUT3D → R',G',B'
 * Uses LUT3 gates to represent the concept.
 */
public class LUTCubeDemo {
    public static void main(String[] args) throws Exception {
        Circuit c = new Circuit();
        
        // Create LUT3 gates representing 3D LUT concept
        LUT3 lutR = new LUT3("LUT_R", 0x96); // Example mapping for red channel
        LUT3 lutG = new LUT3("LUT_G", 0x5A); // Example mapping for green channel  
        LUT3 lutB = new LUT3("LUT_B", 0x3C); // Example mapping for blue channel
        
        c.addGate(lutR);
        c.addGate(lutG);
        c.addGate(lutB);
        
        // Connect inputs: R, G, B
        c.connectPrimaryInput("R", lutR, 0);
        c.connectPrimaryInput("G", lutR, 1);
        c.connectPrimaryInput("B", lutR, 2);
        
        c.connectPrimaryInput("R", lutG, 0);
        c.connectPrimaryInput("G", lutG, 1);
        c.connectPrimaryInput("B", lutG, 2);
        
        c.connectPrimaryInput("R", lutB, 0);
        c.connectPrimaryInput("G", lutB, 1);
        c.connectPrimaryInput("B", lutB, 2);
        
        // Set as primary outputs
        c.addPrimaryOutput(lutR);
        c.addPrimaryOutput(lutG);
        c.addPrimaryOutput(lutB);
        
        // Create output names
        Map<Gate, String> outNames = new LinkedHashMap<>();
        outNames.put(lutR, "R'");
        outNames.put(lutG, "G'");
        outNames.put(lutB, "B'");
        
        // Test case: RGB = 1,1,0
        c.setPrimaryInput("R", true);
        c.setPrimaryInput("G", true);
        c.setPrimaryInput("B", false);
        c.propagate();
        
        // Generate diagram
        Path dot = Paths.get("docs/lut_cube.dot");
        Path png = Paths.get("docs/lut_cube.png");
        GraphvizExporter.writeDotAndPng(c, dot, png, outNames);
        
        // Auto-open on desktop systems
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                new ProcessBuilder("open", png.toString()).start();
            } else if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", png.toString()).start();
            } else {
                new ProcessBuilder("xdg-open", png.toString()).start();
            }
        } catch (Exception ignore) {}
        
        System.out.println("3D LUT Demo:");
        System.out.println("R=1, G=1, B=0 → R'=" + (lutR.getOutput(0) ? "1" : "0") + 
                          ", G'=" + (lutG.getOutput(0) ? "1" : "0") + 
                          ", B'=" + (lutB.getOutput(0) ? "1" : "0"));
        System.out.println("Wrote " + dot + " and " + png);
    }
}
