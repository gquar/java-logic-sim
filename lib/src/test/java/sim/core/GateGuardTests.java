package sim.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GateGuardTests {
    @Test
    void setInput_outOfRange_throws() {
        Gate and2 = new AndGate("A 2");
        assertThrows(IllegalArgumentException.class, () -> and2.setInput(-1, true));
        assertThrows(IllegalArgumentException.class, () -> and2.setInput(2, true));
    }

    @Test
    void wire_ctor_invalidPins_throw() {
        Gate and2 = new AndGate("A 2");
        Gate not1 = new NotGate("N");
        // and2 has 1 output, so fromPin 1 is invalid
        assertThrows(IllegalArgumentException.class, () -> new Wire(and2, 1, not1, 0));
        // not1 has 1 input, so toPin 1 is invalid
        assertThrows(IllegalArgumentException.class, () -> new Wire(and2, 0, not1, 1));
    }

    @Test
    void circuit_connectPrimaryInput_checks() {
        Circuit c = new Circuit();
        Gate and2 = new AndGate("A 2");
        c.addGate(and2);
        assertThrows(IllegalArgumentException.class, () -> c.connectPrimaryInput("", and2, 0));
        assertThrows(IllegalArgumentException.class, () -> c.connectPrimaryInput("X", and2, 2));
    }
}
