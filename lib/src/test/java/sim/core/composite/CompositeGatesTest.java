package sim.core.composite;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sim.core.Circuit;

public class CompositeGatesTest {

    @Test
    void halfAdder_truthTable() {
        // SUM = A ^ B
        // CARRY = A & B
        for (boolean A : new boolean[]{false, true}) {
            for (boolean B : new boolean[]{false, true}) {
                Circuit c = new Circuit();
                // Add HA and expose outputs as primary outputs
                CompositeGates.HalfAdderHandle ha =
                        CompositeGates.buildHalfAdder(c, "HA", "A", "B", /*addAsPrimaryOutputs=*/true);

                // Drive inputs
                c.setPrimaryInput("A", A);
                c.setPrimaryInput("B", B);

                // Evaluate
                c.propagate();

                var outs = c.readPrimaryOutputs(); // [SUM, CARRY] (by our builder order)
                boolean sum   = outs.get(0);
                boolean carry = outs.get(1);

                boolean expSum   = A ^ B;
                boolean expCarry = A & B;

                assertEquals(
                        expSum, sum,
                        "HA SUM mismatch for A=" + A + " B=" + B);

                assertEquals(
                        expCarry, carry,
                        "HA CARRY mismatch for A=" + A + " B=" + B);
            }
        }
    }

    @Test
    void fullAdder_truthTable() {
        // SUM = (A ^ B) ^ Cin
        // COUT = (A & B) | ((A ^ B) & Cin)
        for (boolean A : new boolean[]{false, true}) {
            for (boolean B : new boolean[]{false, true}) {
                for (boolean Cin : new boolean[]{false, true}) {
                    Circuit c = new Circuit();
                    CompositeGates.FullAdderHandle fa =
                            CompositeGates.buildFullAdder(c, "FA", "A", "B", "Cin", /*addAsPrimaryOutputs=*/true);

                    c.setPrimaryInput("A", A);
                    c.setPrimaryInput("B", B);
                    c.setPrimaryInput("Cin", Cin);

                    c.propagate();

                    var outs = c.readPrimaryOutputs(); // [SUM, COUT] (by our builder order)
                    boolean sum  = outs.get(0);
                    boolean cout = outs.get(1);

                    boolean expSum  = (A ^ B) ^ Cin;
                    boolean expCout = (A & B) | ((A ^ B) & Cin);

                    // Helpful internals for debugging if this ever fails again
                    boolean xor1 = fa.xor1.getOutput(0);
                    boolean and1 = fa.and1.getOutput(0);
                    boolean and2 = fa.and2.getOutput(0);
                    boolean dbg_cout = fa.cout.getOutput(0);

                    assertEquals(
                            expSum, sum,
                            "SUM mismatch for A=" + A + " B=" + B + " Cin=" + Cin
                                    + " [xor1=" + xor1 + "]");

                    assertEquals(
                            expCout, cout,
                            "COUT mismatch for A=" + A + " B=" + B + " Cin=" + Cin
                                    + " [and1=" + and1 + ", and2=" + and2 + ", cout=" + dbg_cout + "]");
                }
            }
        }
    }
}
