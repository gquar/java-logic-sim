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
 * Demo that builds a full adder using LUT3 gates (FPGA-style).
 * 
 * <p>Uses two LUT3 gates:
 * - SUM = LUT3("L_SUM", 0x96) - 3-bit XOR parity
 * - COUT = LUT3("L_COUT", 0xE8) - majority function
 * 
 * <p>This demonstrates how FPGAs use configurable LUTs to implement
 * arbitrary boolean functions.
 */
public class LUTFabricDemo {
    public static void main(String[] args) throws Exception {
        Circuit c = new Circuit();
        
        // Create LUT3 gates with full adder truth tables
        LUT3 sumLut = new LUT3("L_SUM", 0x96);  // XOR parity for SUM
        LUT3 coutLut = new LUT3("L_COUT", 0xE8); // Majority for COUT
        
        c.addGate(sumLut);
        c.addGate(coutLut);
        
        // Connect primary inputs: A, B, Cin
        c.connectPrimaryInput("A", sumLut, 0);
        c.connectPrimaryInput("B", sumLut, 1);
        c.connectPrimaryInput("Cin", sumLut, 2);
        
        c.connectPrimaryInput("A", coutLut, 0);
        c.connectPrimaryInput("B", coutLut, 1);
        c.connectPrimaryInput("Cin", coutLut, 2);
        
        // Set as primary outputs
        c.addPrimaryOutput(sumLut);
        c.addPrimaryOutput(coutLut);
        
        // Create output names for Graphviz
        Map<Gate, String> outNames = new LinkedHashMap<>();
        outNames.put(sumLut, "SUM");
        outNames.put(coutLut, "Cout");
        
        // Test case: A=1, B=1, Cin=0 → SUM=0, Cout=1
        c.setPrimaryInput("A", true);
        c.setPrimaryInput("B", true);
        c.setPrimaryInput("Cin", false);
        c.propagate();
        
        // Generate diagram
        Path dot = Paths.get("docs/lut_fabric.dot");
        Path png = Paths.get("docs/lut_fabric.png");
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
        
        System.out.println("LUT Fabric Demo:");
        System.out.println("A=1, B=1, Cin=0 → SUM=" + (sumLut.getOutput(0) ? "1" : "0") + 
                          ", Cout=" + (coutLut.getOutput(0) ? "1" : "0"));
        System.out.println("Wrote " + dot + " and " + png);
    }
}
