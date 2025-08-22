package sim.core.composite;

import sim.core.*; // Circuit, Gate, Wire, AndGate, OrGate, NotGate, XorGate

/** Composite 1-bit adders built from primitive gates. */
public final class CompositeGates {
    private CompositeGates() {}

    /** Handle for a Half Adder. */
    public static final class HalfAdderHandle {
        public final XorGate sum;    // SUM = A ^ B
        public final AndGate carry;  // CARRY = A & B
        HalfAdderHandle(XorGate sum, AndGate carry) {
            this.sum = sum; this.carry = carry;
        }
    }

    /** Handle for a Full Adder (exposes internal gates for debugging). */
    public static final class FullAdderHandle {
        public final XorGate xor1; // A^B
        public final AndGate and1; // A&B
        public final XorGate sum;  // (A^B)^Cin
        public final AndGate and2; // (A^B)&Cin
        public final OrGate  cout; // and1 | and2
        FullAdderHandle(XorGate xor1, AndGate and1, XorGate sum, AndGate and2, OrGate cout) {
            this.xor1 = xor1; this.and1 = and1; this.sum = sum; this.and2 = and2; this.cout = cout;
        }
    }

    /**
     * Build a Half Adder in the given circuit.
     * @param addAsPrimaryOutputs if true, register SUM and CARRY as circuit primary outputs
     */
    public static HalfAdderHandle buildHalfAdder(
            Circuit c, String id, String inA, String inB, boolean addAsPrimaryOutputs) {

        XorGate xor = new XorGate(id + "_XOR"); // SUM
        AndGate and = new AndGate(id + "_AND"); // CARRY
        c.addGate(xor);
        c.addGate(and);

        // Inputs A,B to both xor and and (pins 0..1)
        c.connectPrimaryInput(inA, xor, 0);
        c.connectPrimaryInput(inB, xor, 1);
        c.connectPrimaryInput(inA, and, 0);
        c.connectPrimaryInput(inB, and, 1);

        if (addAsPrimaryOutputs) {
            c.addPrimaryOutput(xor); // SUM
            c.addPrimaryOutput(and); // CARRY
        }
        return new HalfAdderHandle(xor, and);
    }

    /**
     * Build a Full Adder: SUM=(A^B)^Cin, COUT=(A&B) | ((A^B)&Cin).
     * @param addAsPrimaryOutputs if true, register SUM and COUT as circuit primary outputs
     */
    public static FullAdderHandle buildFullAdder(
            Circuit c, String id, String inA, String inB, String inCin, boolean addAsPrimaryOutputs) {

        // Stage 1
        XorGate xor1 = new XorGate(id + "_X1"); // A^B
        AndGate and1 = new AndGate(id + "_A1"); // A&B
        c.addGate(xor1);
        c.addGate(and1);
        c.connectPrimaryInput(inA, xor1, 0);
        c.connectPrimaryInput(inB, xor1, 1);
        c.connectPrimaryInput(inA, and1, 0);
        c.connectPrimaryInput(inB, and1, 1);

        // Stage 2
        XorGate xor2 = new XorGate(id + "_X2"); // SUM = (A^B)^Cin
        AndGate and2 = new AndGate(id + "_A2"); // (A^B)&Cin
        c.addGate(xor2);
        c.addGate(and2);
        c.addWire(new Wire(xor1, 0, xor2, 0));  // (A^B) -> xor2.in0
        c.addWire(new Wire(xor1, 0, and2, 0));  // (A^B) -> and2.in0
        c.connectPrimaryInput(inCin, xor2, 1);  // Cin   -> xor2.in1
        c.connectPrimaryInput(inCin, and2, 1);  // Cin   -> and2.in1

        // Output
        OrGate or1 = new OrGate(id + "_OR");    // COUT = and1 | and2
        c.addGate(or1);
        c.addWire(new Wire(and1, 0, or1, 0));
        c.addWire(new Wire(and2, 0, or1, 1));

        if (addAsPrimaryOutputs) {
            c.addPrimaryOutput(xor2); // SUM
            c.addPrimaryOutput(or1);  // COUT
        }

        return new FullAdderHandle(xor1, and1, xor2, and2, or1);
    }
}
