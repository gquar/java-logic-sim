package sim.demo;

import sim.core.Circuit;
import sim.core.Gate;
import sim.core.Wire;
import sim.core.seq.DFlipFlop;
import sim.dot.GraphvizExporter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demo that builds a 2-bit register and generates frame-by-frame Graphviz diagrams.
 * 
 * <p>Creates two D flip-flops (R0, R1) with shared clock.
 * For each tick, sets D0/D1 values and captures the state in PNG frames.
 */
public class Reg2Demo {
    public static void main(String[] args) throws Exception {
        // Create the circuit
        Circuit c = new Circuit();
        
        // Create two D flip-flops
        DFlipFlop r0 = new DFlipFlop("R0");
        DFlipFlop r1 = new DFlipFlop("R1");
        c.addGate(r0);
        c.addGate(r1);
        
        // Connect primary inputs
        c.connectPrimaryInput("D0", r0, 0); // D input to R0
        c.connectPrimaryInput("D1", r1, 0); // D input to R1
        c.connectPrimaryInput("CLK", r0, 1); // CLK to R0
        c.connectPrimaryInput("CLK", r1, 1); // CLK to R1
        
        // Set as primary outputs
        c.addPrimaryOutput(r0);
        c.addPrimaryOutput(r1);
        
        // Create output names for Graphviz
        Map<Gate, String> outNames = new LinkedHashMap<>();
        outNames.put(r0, "Q0");
        outNames.put(r1, "Q1");
        
        // Create frames directory
        Path framesDir = Paths.get("docs/frames");
        Files.createDirectories(framesDir);
        
        // Generate frames for ticks 0-3
        for (int t = 0; t <= 3; t++) {
            // Set D values based on tick number
            boolean d0 = (t & 1) != 0; // bit 0
            boolean d1 = (t & 2) != 0; // bit 1
            
            c.setPrimaryInput("D0", d0);
            c.setPrimaryInput("D1", d1);
            
            // Perform clock tick
            c.tick("CLK");
            
            // Generate frame
            String frameNum = String.format("%02d", t);
            Path dotFile = framesDir.resolve("reg_" + frameNum + ".dot");
            Path pngFile = framesDir.resolve("reg_" + frameNum + ".png");
            
            GraphvizExporter.writeDotAndPng(c, dotFile, pngFile, outNames);
            
            System.out.printf("Tick %d: D0=%b, D1=%b → Q0=%b, Q1=%b%n", 
                t, d0, d1, r0.getQ(), r1.getQ());
        }
        
        System.out.println("Wrote docs/frames/reg_*.png");
    }
}
