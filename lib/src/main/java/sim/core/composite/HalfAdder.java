package sim.core.composite;

import sim.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A HalfAdder composite gate that computes the sum and carry of two binary inputs.
 * 
 * <p>This composite gate implements:
 * - SUM = A ⊕ B (XOR of inputs A and B)
 * - CARRY = A & B (AND of inputs A and B)
 * 
 * <p>Pin ordering:
 * - Input 0: A (first operand)
 * - Input 1: B (second operand)
 * - Output 0: SUM (A ⊕ B)
 * - Output 1: CARRY (A & B)
 * 
 * <p>Truth table:
 * <pre>
 * A | B | SUM | CARRY
 * ---+---+-----+-------
 * 0 | 0 |  0  |   0
 * 0 | 1 |  1  |   0
 * 1 | 0 |  1  |   0
 * 1 | 1 |  0  |   1
 * </pre>
 * 
 * <p>Internal structure: Uses one XOR gate for SUM and one AND gate for CARRY.
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class HalfAdder extends Gate {
    

    
    /** Internal XOR gate for computing SUM */
    private final XorGate xorGate;
    
    /** Internal AND gate for computing CARRY */
    private final AndGate andGate;
    
    /** Internal wires connecting inputs to gates */
    private final List<Wire> internalWires;
    
    /**
     * Constructs a new HalfAdder composite gate.
     * 
     * @param id unique identifier for this gate
     */
    public HalfAdder(String id) {
        super(id, 2, 2); // 2 inputs (A, B), 2 outputs (SUM, CARRY)
        
        // Create internal gates
        this.xorGate = new XorGate(id + "_XOR");
        this.andGate = new AndGate(id + "_AND");
        
        // Create internal wires
        this.internalWires = new ArrayList<>();
        
        // Wire A to both XOR and AND gates (input 0)
        Wire wireAtoXOR = new Wire(this, 0, xorGate, 0);
        Wire wireAtoAND = new Wire(this, 0, andGate, 0);
        
        // Wire B to both XOR and AND gates (input 1)
        Wire wireBtoXOR = new Wire(this, 1, xorGate, 1);
        Wire wireBtoAND = new Wire(this, 1, andGate, 1);
        
        // Store internal wires for evaluation
        this.internalWires.add(wireAtoXOR);
        this.internalWires.add(wireAtoAND);
        this.internalWires.add(wireBtoXOR);
        this.internalWires.add(wireBtoAND);
    }
    

    
    @Override
    public void evaluate() {
        // Set inputs on internal gates
        xorGate.setInput(0, inputs.get(0)); // A
        xorGate.setInput(1, inputs.get(1)); // B
        andGate.setInput(0, inputs.get(0)); // A
        andGate.setInput(1, inputs.get(1)); // B
        
        // Evaluate internal gates
        xorGate.evaluate();
        andGate.evaluate();
        
        // Read outputs from internal gates
        outputs.set(0, xorGate.getOutput(0)); // SUM = A ⊕ B
        outputs.set(1, andGate.getOutput(0)); // CARRY = A & B
    }
}
