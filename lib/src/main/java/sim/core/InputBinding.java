package sim.core;

/** Binds a primary input name to a specific gate input pin. */
public class InputBinding {
    public final Gate gate;
    public final int pin;

    public InputBinding(Gate gate, int pin) {
        this.gate = gate;
        this.pin = pin;
    }
}
