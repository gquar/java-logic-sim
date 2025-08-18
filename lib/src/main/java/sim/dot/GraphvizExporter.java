package sim.dot;

import sim.core.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class GraphvizExporter {
    private GraphvizExporter() {}

    /** Write a DOT file only. */
    public static void writeDot(Circuit c, Path outDot) throws IOException {
        StringBuilder sb = new StringBuilder(8_192);
        sb.append("digraph Logic {\n");
        sb.append("  graph [rankdir=LR, nodesep=0.4, ranksep=0.6];\n");
        sb.append("  node  [shape=box, style=filled, fillcolor=\"#f6f6f6\", color=\"#555555\", fontname=Helvetica, fontsize=10];\n");
        sb.append("  edge  [color=\"#333333\"];\n\n");

        // Primary input nodes
        Map<String, List<Circuit.InputBinding>> inMap = c.getPrimaryInputBindings();
        for (String name : inMap.keySet()) {
            String nodeId = inNode(name);
            sb.append("  ").append(nodeId)
              .append(" [label=\"").append(escape(name))
              .append("\\nINPUT\", shape=diamond, fillcolor=\"#e8f5e9\", color=\"#2e7d32\", penwidth=1.5];\n");
        }
        sb.append("\n");

        // Gate nodes (color by type)
        // Gate nodes (ID inside the node; type as a small external xlabel)
        for (Gate g : c.getGates()) {
            String color = colorFor(g);
            String type  = shortType(g); // AND / OR / NOT / XOR / GATE
            sb.append("  ").append(gNode(g))
            .append(" [label=\"").append(escape(g.getId()))
            .append("\", xlabel=\"").append(type)
            .append("\", fillcolor=\"").append(color)
            .append("\"];\n");
        }

        sb.append("\n");

        // Edges from primary inputs to bound gate pins
        for (Map.Entry<String, List<Circuit.InputBinding>> e : inMap.entrySet()) {
            String name = e.getKey();
            for (Circuit.InputBinding b : e.getValue()) {
                sb.append("  ").append(inNode(name)).append(" -> ")
                  .append(gNode(b.gate()))
                  .append(" [taillabel=\"").append(escape(name))
                  .append("\", headlabel=\"in").append(b.pin()).append("\", labeldistance=2, labelfontsize=9];\n");
            }
        }
        sb.append("\n");

        // Wires between gates (colored by current signal after propagate())
        for (Wire w : c.getWires()) {
            boolean v = w.read();
            String color = v ? "#1565c0" : "#cccccc";  // blue when true, gray when false
            String pen   = v ? "2.2"     : "1.2";
            sb.append("  ").append(gNode(w.getFromGate())).append(" -> ")
              .append(gNode(w.getToGate()))
              .append(" [headlabel=\"in").append(w.getToPin())
              .append("\", taillabel=\"out").append(w.getFromPin())
              .append("\", labeldistance=2, labelfontsize=9")
              .append(", color=\"").append(color).append("\"")
              .append(", penwidth=").append(pen)
              .append("];\n");
        }
        sb.append("\n");

        // Primary outputs
        for (Gate g : c.getPrimaryOutputs()) {
            String outId = outNode(g);
            sb.append("  ").append(outId)
              .append(" [label=\"OUTPUT\", shape=oval, fillcolor=\"#e3f2fd\", color=\"#1565c0\", penwidth=1.8];\n");
            sb.append("  ").append(gNode(g)).append(" -> ").append(outId)
              .append(" [penwidth=1.6, color=\"#1565c0\"];\n");
        }

        sb.append("}\n");
        if (outDot.getParent() != null) Files.createDirectories(outDot.getParent());
        Files.writeString(outDot, sb.toString());
    }

    /** Write DOT, then try to render PNG via the `dot` CLI (Graphviz). */
    public static void writeDotAndPng(Circuit c, Path outDot, Path outPng) throws IOException {
        writeDot(c, outDot);
        try {
            Process p = new ProcessBuilder("dot", "-Tpng", "-o", outPng.toString(), outDot.toString())
                    .inheritIO()
                    .start();
            int code = p.waitFor();
            if (code != 0) {
                System.err.println("[GraphvizExporter] dot exited with code " + code + " (PNG not generated).");
            }
        } catch (Exception ex) {
            System.err.println("[GraphvizExporter] Could not run `dot`. Install Graphviz or run it manually.");
        }
    }

    // -------- helpers --------
    private static String gNode(Gate g) { return "g_" + sanitize(g.getId()); }
    private static String inNode(String name) { return "in_" + sanitize(name); }
    private static String outNode(Gate g) { return "out_" + sanitize(g.getId()); }
    private static String sanitize(String s) { return s.replaceAll("[^A-Za-z0-9_]", "_"); }
    private static String escape(String s) { return s.replace("\"", "\\\""); }

    private static String shortType(Gate g) {
        if (g instanceof AndGate) return "AND";
        if (g instanceof OrGate)  return "OR";
        if (g instanceof NotGate) return "NOT";
        if (g instanceof XorGate) return "XOR";
        return "GATE";
    }
    

    private static String colorFor(Gate g) {
        if (g instanceof AndGate) return "#fff4e5"; // soft orange
        if (g instanceof OrGate)  return "#e8eaf6"; // soft indigo
        if (g instanceof NotGate) return "#fce4ec"; // soft pink
        if (g instanceof XorGate) return "#e0f7fa"; // soft teal
        return "#f6f6f6";
    }
}
