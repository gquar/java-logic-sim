package sim.dot;

import sim.core.Circuit;
import sim.core.Gate;
import sim.core.Wire;
import sim.core.AndGate;
import sim.core.OrGate;
import sim.core.NotGate;
import sim.core.XorGate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Exports a Circuit (gates + wires + primary I/O) to Graphviz DOT (and optionally PNG). */
public final class GraphvizExporter {
    private GraphvizExporter() {}

    /** Write DOT without custom output names. */
    public static void writeDot(Circuit c, Path outDot) throws IOException {
        writeDot(c, outDot, Collections.emptyMap());
    }

    /**
     * Write DOT with optional custom labels for primary outputs.
     * Keys must be the Gate objects that are primary outputs; values are their names (e.g., "SUM", "Cout").
     */
    public static void writeDot(Circuit c, Path outDot, Map<Gate, String> outputNames) throws IOException {
        StringBuilder sb = new StringBuilder(8_192);
        sb.append("digraph Logic {\n");
        sb.append("  graph [rankdir=LR, nodesep=0.4, ranksep=0.6];\n");
        sb.append("  node  [shape=box, style=filled, fillcolor=\"#f6f6f6\", color=\"#555555\", fontname=Helvetica, fontsize=10];\n");
        sb.append("  edge  [color=\"#333333\", arrowsize=0.7];\n\n");

        // Primary inputs
        Map<String, List<Circuit.InputBinding>> inMap = c.getPrimaryInputBindings();
        for (String name : inMap.keySet()) {
            sb.append("  ").append(inNode(name)).append(" ")
              .append("[label=\"").append(escape(name)).append("\\nINPUT\", ")
              .append("shape=diamond, fillcolor=\"#e8f5e9\", color=\"#2e7d32\", penwidth=1.5];\n");
        }
        sb.append("\n");

        // Gates (label = TYPE [+ id if different])
        for (Gate g : c.getGates()) {
            String type = shortType(g); // AND/OR/NOT/XOR
            String id = g.getId();
            String label = id.equalsIgnoreCase(type) ? type : (type + "\\n(" + escape(id) + ")");
            String color = colorFor(g);
            sb.append("  ").append(gNode(g)).append(" ")
              .append("[label=\"").append(label).append("\", fillcolor=\"").append(color).append("\"];\n");
        }
        sb.append("\n");

        // Edges from primary inputs to gate pins
        for (Map.Entry<String, List<Circuit.InputBinding>> e : inMap.entrySet()) {
            String name = e.getKey();
            for (Circuit.InputBinding b : e.getValue()) {
                sb.append("  ").append(inNode(name)).append(" -> ").append(gNode(b.gate()))
                  .append(" [taillabel=\"").append(escape(name))
                  .append("\", headlabel=\"in").append(b.pin())
                  .append("\", labeldistance=2, labelfontsize=9];\n");
            }
        }
        sb.append("\n");

        // Wires between gates
        for (Wire w : c.getWires()) {
            sb.append("  ").append(gNode(w.getFromGate())).append(" -> ").append(gNode(w.getToGate()))
              .append(" [headlabel=\"in").append(w.getToPin())
              .append("\", taillabel=\"out").append(w.getFromPin())
              .append("\", labeldistance=2, labelfontsize=9];\n");
        }
        sb.append("\n");

        // Primary outputs with live value labeling + colored edge by value
        List<Gate> outs = c.getPrimaryOutputs();
        List<Boolean> outVals = c.readPrimaryOutputs(); // assume caller called propagate()
        for (int i = 0; i < outs.size(); i++) {
            Gate g = outs.get(i);
            boolean v = (i < outVals.size()) && Boolean.TRUE.equals(outVals.get(i));
            String base = outputNames.getOrDefault(g, "OUT");
            String nodeLabel = base + " = " + (v ? "1" : "0");
            String color = v ? "#1565c0" : "#9e9e9e";
            String pen   = v ? "2.2" : "1.2";

            String outId = outNode(g);
            sb.append("  ").append(outId).append(" ")
              .append("[label=\"").append(escape(nodeLabel)).append("\", ")
              .append("shape=oval, fillcolor=\"#e3f2fd\", color=\"").append(color).append("\", penwidth=1.8];\n");

            sb.append("  ").append(gNode(g)).append(" -> ").append(outId)
              .append(" [color=\"").append(color).append("\", penwidth=").append(pen).append("];\n");
        }

        sb.append("}\n");
        if (outDot.getParent() != null) Files.createDirectories(outDot.getParent());
        Files.writeString(outDot, sb.toString());
    }

    /** Write DOT and render PNG via Graphviz `dot`. */
    public static void writeDotAndPng(Circuit c, Path outDot, Path outPng, Map<Gate, String> outputNames) throws IOException {
        writeDot(c, outDot, outputNames);
        try {
            Process p = new ProcessBuilder("dot", "-Tpng", "-o", outPng.toString(), outDot.toString())
                    .inheritIO()
                    .start();
            int code = p.waitFor();
            if (code != 0) {
                System.err.println("[GraphvizExporter] dot exited with code " + code + " (PNG not generated).");
            }
        } catch (Exception ex) {
            System.err.println("[GraphvizExporter] Could not run `dot`. Install Graphviz or run manually.");
        }
    }

    // ---- helpers ----
    private static String shortType(Gate g) {
        String n = g.getClass().getSimpleName();
        if (n.endsWith("Gate")) n = n.substring(0, n.length()-4);
        return n.toUpperCase();
    }
    private static String gNode(Gate g) { return "g_" + sanitize(g.getId()); }
    private static String inNode(String name) { return "in_" + sanitize(name); }
    private static String outNode(Gate g) { return "out_" + sanitize(g.getId()); }
    private static String sanitize(String s) { return s.replaceAll("[^A-Za-z0-9_]", "_"); }
    private static String escape(String s) { return s.replace("\"", "\\\""); }
    private static String colorFor(Gate g) {
        if (g instanceof AndGate) return "#fff4e5";
        if (g instanceof OrGate)  return "#e8eaf6";
        if (g instanceof NotGate) return "#fce4ec";
        if (g instanceof XorGate) return "#e0f7fa";
        return "#f6f6f6";
    }
}
