package sim.demo;

import sim.core.Circuit;
import sim.core.Gate;
import sim.core.composite.CompositeGates;


public class GraphvizFADemo {
    public static void main(String[] args) throws Exception {
        // 1) Build the Full Adder in a Circuit
        Circuit c = new Circuit();
        var fa = CompositeGates.buildFullAdder(
                c, "FA", "A", "B", "Cin", /*addAsPrimaryOutputs=*/false
        );

        // 2) Mark FA outputs as primary outputs (SUM, Cout)
        c.addPrimaryOutput(fa.sum);
        c.addPrimaryOutput(fa.cout);

        // (Optional) Set inputs and propagate so you can also color by value later
        c.setPrimaryInput("A",   true);
        c.setPrimaryInput("B",   true);
        c.setPrimaryInput("Cin", false);
        c.propagate();

        // 3) Name the outputs for edge labels → SUM / Cout
        var outNames = new java.util.LinkedHashMap<Gate, String>();
        outNames.put(fa.sum, "SUM");
        outNames.put(fa.cout, "Cout");

        java.nio.file.Path dot = java.nio.file.Paths.get("docs/fa.dot");
        java.nio.file.Path png = java.nio.file.Paths.get("docs/fa.png");
        sim.dot.GraphvizExporter.writeDotAndPng(c, dot, png, outNames);

        // Auto-open
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
