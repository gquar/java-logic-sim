package sim.demo;

import sim.core.*;
import sim.dot.GraphvizExporter;

import java.awt.Desktop;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GraphvizDemo {
    public static void main(String[] args) throws Exception {
        // Build: (A AND B) OR (NOT C)
        Circuit c = new Circuit();
        Gate and1 = new AndGate("AND");
        Gate not1 = new NotGate("NOT");
        Gate or1  = new OrGate("OR");
        c.addGate(and1);
        c.addGate(not1);
        c.addGate(or1);

        // Primary inputs
        c.connectPrimaryInput("A", and1, 0);
        c.connectPrimaryInput("B", and1, 1);
        c.connectPrimaryInput("C", not1, 0);

        // Wires and primary output
        c.addWire(new Wire(and1, 0, or1, 0));
        c.addWire(new Wire(not1, 0, or1, 1));
        c.addPrimaryOutput(or1);

        // Quick check
        c.setPrimaryInput("A", true);
        c.setPrimaryInput("B", true);
        c.setPrimaryInput("C", false);
        c.propagate();
        System.out.println("Demo output: " + c.readPrimaryOutputs().get(0)); // true

        var outNames = new java.util.LinkedHashMap<Gate, String>();
        outNames.put(or1, "OUT"); // label the single output as OUT

        java.nio.file.Path dot = java.nio.file.Paths.get("docs/logic.dot");
        java.nio.file.Path png = java.nio.file.Paths.get("docs/logic.png");
        sim.dot.GraphvizExporter.writeDotAndPng(c, dot, png, outNames);

        // Auto-open cross-platform
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

        System.out.println("Wrote " + dot + " and " + png);
    }
}
