package sim.core;

import java.util.Objects;

/** Binds a primary input name to a specific gate input pin. */
public record InputBinding(Gate gate, int pin) {
    public InputBinding {
        Objects.requireNonNull(gate, "gate must not be null");
        if (pin < 0) throw new IllegalArgumentException("pin must be >= 0");
    }
}
