package sim.core;

/**
 * 3-input Look-Up Table (LUT) gate with configurable truth table.
 * 
 * <p>Inputs: in0, in1, in2 (pins 0, 1, 2)
 * Output: single output (pin 0)
 * 
 * <p>The truth table is defined by an 8-bit mask where:
 * - bit 0 = output when inputs are 000
 * - bit 1 = output when inputs are 001  
 * - bit 2 = output when inputs are 010
 * - bit 3 = output when inputs are 011
 * - bit 4 = output when inputs are 100
 * - bit 5 = output when inputs are 101
 * - bit 6 = output when inputs are 110
 * - bit 7 = output when inputs are 111
 */
public class LUT3 extends Gate {
    
    private final int mask8;
    
    /**
     * Constructs a 3-input LUT with the specified truth table mask.
     * 
     * @param id unique identifier for this LUT
     * @param mask8 8-bit mask defining the truth table (0x00 to 0xFF)
     */
    public LUT3(String id, int mask8) {
        super(id, 3, 1); // 3 inputs, 1 output
        if (mask8 < 0 || mask8 > 0xFF) {
            throw new IllegalArgumentException("mask8 must be 0x00 to 0xFF, got: " + mask8);
        }
        this.mask8 = mask8;
    }
    
    /**
     * Gets the truth table mask for this LUT.
     * 
     * @return the 8-bit mask
     */
    public int getMask() {
        return mask8;
    }
    
    /**
     * Evaluates the LUT based on current input values.
     * 
     * <p>Computes index = (in2?4:0) | (in1?2:0) | (in0?1:0)
     * Sets output = ((mask8 >> index) & 1) == 1
     */
    @Override
    public void evaluate() {
        boolean in0 = inputs.get(0);
        boolean in1 = inputs.get(1);
        boolean in2 = inputs.get(2);
        
        // Compute index: in2*4 + in1*2 + in0*1
        int index = (in2 ? 4 : 0) | (in1 ? 2 : 0) | (in0 ? 1 : 0);
        
        // Extract bit at index position
        boolean output = ((mask8 >> index) & 1) == 1;
        
        outputs.set(0, output);
    }
}
