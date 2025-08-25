// lib/src/test/java/sim/core/XorGateTest.java
package sim.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class XorGateTest {
    @Test
    void twoInputTruthTable() {
        XorGate g = new XorGate("X");
        g.setInput(0, false); g.setInput(1, false); g.evaluate(); assertFalse(g.getOutput(0));
        g.setInput(0, false); g.setInput(1, true ); g.evaluate(); assertTrue (g.getOutput(0));
        g.setInput(0, true ); g.setInput(1, false); g.evaluate(); assertTrue (g.getOutput(0));
        g.setInput(0, true ); g.setInput(1, true ); g.evaluate(); assertFalse(g.getOutput(0));
    }
}
