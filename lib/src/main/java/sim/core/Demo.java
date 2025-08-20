package sim.core;

import java.nio.file.Path;

import sim.dot.CircuitDotExporter;

public class Demo {
	public static void main(String[] args) throws Exception {
		boolean writeDot = false;
		Path dotPath = null;
		for (int i = 0; i < args.length; i++) {
			if ("--dot".equals(args[i]) && i + 1 < args.length) {
				writeDot = true;
				dotPath = Path.of(args[i + 1]);
				i++;
			}
		}

		Circuit circuit = new Circuit();
		// Build (A AND B) OR (NOT C)
		AndGate g1 = new AndGate("G1");
		NotGate g2 = new NotGate("G2");
		OrGate g3 = new OrGate("G3");
		circuit.addGate(g1);
		circuit.addGate(g2);
		circuit.addGate(g3);
		circuit.connectPrimaryInput("A", g1, 0);
		circuit.connectPrimaryInput("B", g1, 1);
		circuit.connectPrimaryInput("C", g2, 0);
		circuit.addWire(new Wire(g1, 0, g3, 0));
		circuit.addWire(new Wire(g2, 0, g3, 1));
		circuit.addPrimaryOutput(g3);

		if (writeDot && dotPath != null) {
			new CircuitDotExporter().writeToFile(circuit, dotPath);
			System.out.println("Wrote DOT to: " + dotPath);
		}

		// Run a simple demo
		System.out.println("=== Digital Logic Circuit Demo ===");
		System.out.println("Building circuit: (A AND B) OR (NOT C)\n");
		System.out.println("Setting inputs:\n  A = true\n  B = true\n  C = false\n");
		circuit.setPrimaryInput("A", true);
		circuit.setPrimaryInput("B", true);
		circuit.setPrimaryInput("C", false);
		circuit.propagate();
		System.out.println("Primary outputs:\n  Output 0: " + circuit.readPrimaryOutputs().get(0));
	}
}
