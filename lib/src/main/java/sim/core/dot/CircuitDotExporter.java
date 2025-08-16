package sim.core.dot;

import sim.core.Circuit;
import sim.core.Gate;
import sim.core.Wire;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts a {@link Circuit} into a Graphviz DOT representation.
 *
 * <p>Rules:
 * - One node per {@link Gate}, labeled with the gate's type and id
 * - One directed edge per {@link Wire} from source gate to destination gate
 *   with an edge label that includes the destination input pin number
 */
public final class CircuitDotExporter {
	/**
	 * Returns a DOT string for the given circuit.
	 */
	public String toDot(Circuit circuit) {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph Circuit {\n");
		// Nodes
		for (Gate gate : circuit.getGates()) {
			String nodeId = escapeId(gate.getId());
			String label = gate.getClass().getSimpleName() + " " + gate.getId();
			sb.append("  \"").append(nodeId).append("\" [label=\"")
			  .append(escapeLabel(label)).append("\"];\n");
		}
		// Edges
		for (Wire wire : circuit.getWires()) {
			String fromId = escapeId(wire.getFromGate().getId());
			String toId = escapeId(wire.getToGate().getId());
			sb.append("  \"").append(fromId).append("\" -> \"")
			  .append(toId).append("\" [label=\"in ")
			  .append(wire.getToPin()).append("\"];\n");
		}
		sb.append("}\n");
		return sb.toString();
	}

	/** Writes the DOT output to a file. */
	public void writeToFile(Circuit circuit, Path outFile) throws IOException {
		Files.createDirectories(outFile.toAbsolutePath().getParent());
		Files.writeString(outFile, toDot(circuit));
	}

	private static String escapeId(String s) {
		// Keep simple: rely on quoting, but normalize quotes in ids
		return s.replace("\"", "'");
	}

	private static String escapeLabel(String s) {
		return s.replace("\"", "'");
	}
}
