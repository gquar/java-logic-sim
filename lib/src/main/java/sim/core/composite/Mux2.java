package sim.core.composite;

import sim.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A 2-to-1 multiplexer (Mux2) composite gate that selects between two data inputs.
 * 
 * <p>This composite gate implements:
 * - Out = (~Sel & D0) | (Sel & D1)
 * - When Sel=0, output D0; when Sel=1, output D1
 * 
 * <p>Pin ordering:
 * - Input 0: Sel (selection signal)
 * - Input 1: D0 (data input 0, selected when Sel=0)
 * - Input 2: D1 (data input 1, selected when Sel=1)
 * - Output 0: Out (selected data)
 * 
 * <p>Truth table:
 * <pre>
 * Sel | D0 | D1 | Out
 * ----+----+----+-----
 *  0  |  0 |  0 |  0
 *  0  |  0 |  1 |  0
 *  0  |  1 |  0 |  1
 *  0  |  1 |  1 |  1
 *  1  |  0 |  0 |  0
 *  1  |  0 |  1 |  1
 *  1  |  1 |  0 |  0
 *  1  |  1 |  1 |  1
 * </pre>
 * 
 * <p>Internal structure: Uses NOT, AND, and OR gates to implement the selection logic.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class Mux2 extends Gate {
    

    
    /** Internal NOT gate for inverting selection signal */
    private final NotGate notGate;
    
    /** Internal AND gates for data selection */
    private final AndGate and1, and2;
    
    /** Internal OR gate for combining selected data */
    private final OrGate orGate;
    
    /**
     * Constructs a new Mux2 composite gate.
     * 
     * @param id unique identifier for this gate
     */
    public Mux2(String id) {
        super(id, 3, 1); // 3 inputs (Sel, D0, D1), 1 output (Out)
        
        // Create internal gates
        this.notGate = new NotGate(id + "_NOT");
        this.and1 = new AndGate(id + "_AND1");
        this.and2 = new AndGate(id + "_AND2");
        this.orGate = new OrGate(id + "_OR");
    }
    

    
    @Override
    public void evaluate() {
        // Set inputs on internal gates
        notGate.setInput(0, inputs.get(0)); // Sel
        
        // Evaluate NOT gate to get ~Sel
        notGate.evaluate();
        
        // Set inputs for AND1: (~Sel & D0)
        and1.setInput(0, notGate.getOutput(0)); // ~Sel
        and1.setInput(1, inputs.get(1));        // D0
        
        // Set inputs for AND2: (Sel & D1)
        and2.setInput(0, inputs.get(0));        // Sel
        and2.setInput(1, inputs.get(2));        // D1
        
        // Evaluate AND gates
        and1.evaluate();
        and2.evaluate();
        
        // Set inputs for OR gate: (~Sel & D0) | (Sel & D1)
        orGate.setInput(0, and1.getOutput(0)); // ~Sel & D0
        orGate.setInput(1, and2.getOutput(0)); // Sel & D1
        
        // Evaluate OR gate
        orGate.evaluate();
        
        // Read output
        outputs.set(0, orGate.getOutput(0)); // Out = (~Sel & D0) | (Sel & D1)
    }
}
