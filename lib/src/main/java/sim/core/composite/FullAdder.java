package sim.core.composite;

import sim.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A FullAdder composite gate that computes the sum and carry-out of three binary inputs.
 * 
 * <p>This composite gate implements:
 * - SUM = A ⊕ B ⊕ Cin (XOR of all three inputs)
 * - Cout = majority(A, B, Cin) (carry-out when at least 2 inputs are 1)
 * 
 * <p>Pin ordering:
 * - Input 0: A (first operand)
 * - Input 1: B (second operand)
 * - Input 2: Cin (carry-in)
 * - Output 0: SUM (A ⊕ B ⊕ Cin)
 * - Output 1: Cout (majority of A, B, Cin)
 * 
 * <p>Truth table:
 * <pre>
 * A | B | Cin | SUM | Cout
 * ---+---+-----+-----+------
 * 0 | 0 |  0  |  0  |  0
 * 0 | 0 |  1  |  1  |  0
 * 0 | 1 |  0  |  1  |  0
 * 0 | 1 |  1  |  0  |  1
 * 1 | 0 |  0  |  1  |  0
 * 1 | 0 |  1  |  0  |  1
 * 1 | 1 |  0  |  0  |  1
 * 1 | 1 |  1  |  1  |  1
 * </pre>
 * 
 * <p>Internal structure: Uses XOR gates for SUM and AND/OR gates for majority function.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class FullAdder extends Gate {
    

    
    /** Internal XOR gates for computing SUM */
    private final XorGate xor1, xor2;
    
    /** Internal AND gates for computing majority */
    private final AndGate and1, and2, and3;
    
    /** Internal OR gate for computing Cout */
    private final OrGate orGate;
    
    /**
     * Constructs a new FullAdder composite gate.
     * 
     * @param id unique identifier for this gate
     */
    public FullAdder(String id) {
        super(id, 3, 2); // 3 inputs (A, B, Cin), 2 outputs (SUM, Cout)
        
        // Create internal gates
        this.xor1 = new XorGate(id + "_XOR1");
        this.xor2 = new XorGate(id + "_XOR2");
        this.and1 = new AndGate(id + "_AND1");
        this.and2 = new AndGate(id + "_AND2");
        this.and3 = new AndGate(id + "_AND3");
        this.orGate = new OrGate(id + "_OR");
    }
    

    
    @Override
    public void evaluate() {
        // Set inputs on internal gates
        xor1.setInput(0, inputs.get(0)); // A
        xor1.setInput(1, inputs.get(1)); // B
        
        // Evaluate first XOR (A ⊕ B)
        xor1.evaluate();
        
        // Set inputs for second XOR (A ⊕ B) ⊕ Cin
        xor2.setInput(0, xor1.getOutput(0)); // A ⊕ B
        xor2.setInput(1, inputs.get(2));     // Cin
        
        // Evaluate second XOR for SUM
        xor2.evaluate();
        
        // Compute majority function for Cout
        // Cout = (A & B) | (A & Cin) | (B & Cin)
        and1.setInput(0, inputs.get(0)); // A
        and1.setInput(1, inputs.get(1)); // B
        
        and2.setInput(0, inputs.get(0)); // A
        and2.setInput(1, inputs.get(2)); // Cin
        
        and3.setInput(0, inputs.get(1)); // B
        and3.setInput(1, inputs.get(2)); // Cin
        
        // Evaluate AND gates
        and1.evaluate();
        and2.evaluate();
        and3.evaluate();
        
        // Set inputs for OR gate
        orGate.setInput(0, and1.getOutput(0)); // A & B
        orGate.setInput(1, and2.getOutput(0)); // A & Cin
        
        // Evaluate OR gate
        orGate.evaluate();
        
        // Set inputs for final OR (majority)
        orGate.setInput(0, orGate.getOutput(0)); // (A & B) | (A & Cin)
        orGate.setInput(1, and3.getOutput(0));  // B & Cin
        
        // Final evaluation for Cout
        orGate.evaluate();
        
        // Read outputs
        outputs.set(0, xor2.getOutput(0)); // SUM = A ⊕ B ⊕ Cin
        outputs.set(1, orGate.getOutput(0)); // Cout = majority(A, B, Cin)
    }
}
