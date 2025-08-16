package sim.core.dot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sim.core.*;

public class CircuitDotExporterTest {
	private static String normalize(String s) {
		return s.replaceAll("[\n\r]+", "\n").replaceAll("\s+", " ").trim();
	}

	@Test
	void exportsThreeGateCircuit() {
		Circuit c = new Circuit();
		AndGate g1 = new AndGate("G1");
		NotGate g2 = new NotGate("G2");
		OrGate g3 = new OrGate("G3");
		c.addGate(g1);
		c.addGate(g2);
		c.addGate(g3);
		c.addWire(new Wire(g1, 0, g3, 0));
		c.addWire(new Wire(g2, 0, g3, 1));

		String dot = new CircuitDotExporter().toDot(c);

		String expected = "digraph Circuit {\n" +
			"  \"G1\" [label=\"AndGate G1\"];\n" +
			"  \"G2\" [label=\"NotGate G2\"];\n" +
			"  \"G3\" [label=\"OrGate G3\"];\n" +
			"  \"G1\" -> \"G3\" [label=\"in 0\"];\n" +
			"  \"G2\" -> \"G3\" [label=\"in 1\"];\n" +
			"}\n";

		assertEquals(normalize(expected), normalize(dot));
	}
}
