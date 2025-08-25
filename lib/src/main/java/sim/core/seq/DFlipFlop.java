package sim.core.seq;

import sim.core.Gate;

/**
 * D Flip-Flop with rising-edge capture.
 * 
 * <p>Inputs: D (data, pin 0), CLK (clock, pin 1)
 * Output: Q (pin 0)
 * 
 * <p>Behavior: On rising edge of CLK (false→true), captures the value of D.
 * Output Q holds the last captured value.
 */
public class DFlipFlop extends Gate {
    
    private boolean q;        // Current output value
    private boolean lastClk;  // Previous clock value for edge detection
    
    /**
     * Constructs a D Flip-Flop with the given ID.
     * 
     * @param id unique identifier for this flip-flop
     */
    public DFlipFlop(String id) {
        super(id, 2, 1); // 2 inputs (D, CLK), 1 output (Q)
        this.q = false;
        this.lastClk = false;
    }
    
    /**
     * Evaluates the flip-flop on rising edge of clock.
     * 
     * <p>On rising edge (lastClk=false && clk=true), captures D value.
     * Output Q holds the last captured value.
     */
    @Override
    public void evaluate() {
        boolean d = inputs.get(0);   // D input
        boolean clk = inputs.get(1); // CLK input
        
        // Rising edge detection: lastClk=false && clk=true
        if (!lastClk && clk) {
            q = d; // Capture D value on rising edge
        }
        
        // Update output and last clock value
        outputs.set(0, q);
        lastClk = clk;
    }
    
    /**
     * Gets the current output value Q.
     * 
     * @return the current output value
     */
    public boolean getQ() {
        return q;
    }
    
    /**
     * Gets the last clock value (for debugging).
     * 
     * @return the last clock value
     */
    public boolean getLastClk() {
        return lastClk;
    }
}
