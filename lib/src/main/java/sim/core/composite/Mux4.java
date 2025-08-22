package sim.core.composite;

import sim.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A 4-to-1 multiplexer (Mux4) composite gate that selects between four data inputs.
 * 
 * <p>This composite gate implements 4-to-1 multiplexing using two Mux2s and a final Mux2:
 * - First level: Two Mux2s select between D0/D1 and D2/D3 based on Sel0
 * - Second level: Final Mux2 selects between the two intermediate results based on Sel1
 * 
 * <p>Pin ordering:
 * - Input 0: Sel0 (least significant selection bit)
 * - Input 1: Sel1 (most significant selection bit)
 * - Input 2: D0 (data input 0, selected when Sel1=0, Sel0=0)
 * - Input 3: D1 (data input 1, selected when Sel1=0, Sel0=1)
 * - Input 4: D2 (data input 2, selected when Sel1=1, Sel0=0)
 * - Input 5: D3 (data input 3, selected when Sel1=1, Sel0=1)
 * - Output 0: Out (selected data)
 * 
 * <p>Truth table:
 * <pre>
 * Sel1 | Sel0 | D0 | D1 | D2 | D3 | Out
 * ------+------+----+----+----+----+-----
 *   0   |  0   |  0 |  0 |  0 |  0 |  0
 *   0   |  0   |  1 |  0 |  0 |  0 |  1
 *   0   |  1   |  0 |  0 |  0 |  0 |  0
 *   0   |  1   |  0 |  1 |  0 |  0 |  1
 *   1   |  0   |  0 |  0 |  0 |  0 |  0
 *   1   |  0   |  0 |  0 |  1 |  0 |  1
 *   1   |  1   |  0 |  0 |  0 |  0 |  0
 *   1   |  1   |  0 |  0 |  0 |  1 |  1
 * </pre>
 * 
 * <p>Internal structure: Uses three Mux2 gates in a hierarchical arrangement.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class Mux4 extends Gate {
    
    /** Internal Mux2 gates */
    private final Mux2 muxLower, muxUpper, muxFinal;
    
    /**
     * Constructs a new Mux4 composite gate.
     * 
     * @param id unique identifier for this gate
     */
    public Mux4(String id) {
        super(id, 6, 1); // 6 inputs (Sel0, Sel1, D0..D3), 1 output (Out)
        
        // Create internal Mux2 gates
        this.muxLower = new Mux2(id + "_MUX_LOWER");  // Selects between D0 and D1
        this.muxUpper = new Mux2(id + "_MUX_UPPER");  // Selects between D2 and D3
        this.muxFinal = new Mux2(id + "_MUX_FINAL");  // Selects between lower and upper results
    }
    
    @Override
    public void evaluate() {
        // Set inputs for lower Mux2 (D0 vs D1, controlled by Sel0)
        muxLower.setInput(0, inputs.get(0)); // Sel0
        muxLower.setInput(1, inputs.get(2)); // D0
        muxLower.setInput(2, inputs.get(3)); // D1
        
        // Set inputs for upper Mux2 (D2 vs D3, controlled by Sel0)
        muxUpper.setInput(0, inputs.get(0)); // Sel0
        muxUpper.setInput(1, inputs.get(4)); // D2
        muxUpper.setInput(2, inputs.get(5)); // D3
        
        // Evaluate lower and upper Mux2s
        muxLower.evaluate();
        muxUpper.evaluate();
        
        // Set inputs for final Mux2 (lower vs upper result, controlled by Sel1)
        muxFinal.setInput(0, inputs.get(1));           // Sel1
        muxFinal.setInput(1, muxLower.getOutput(0));   // Result from lower Mux2
        muxFinal.setInput(2, muxUpper.getOutput(0));   // Result from upper Mux2
        
        // Evaluate final Mux2
        muxFinal.evaluate();
        
        // Read output
        outputs.set(0, muxFinal.getOutput(0)); // Final selected data
    }
}
