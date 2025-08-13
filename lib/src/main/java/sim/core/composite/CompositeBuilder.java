package sim.core.composite;

import sim.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for building composite gates by wiring primitive gates together.
 * 
 * <p>This builder provides a clean, fluent API for creating composite gates
 * that internally use primitive gates and wires. It handles the creation
 * and connection of gates automatically.
 * 
 * <p>Usage example:
 * <pre>
 * CompositeBuilder builder = new CompositeBuilder();
 * HalfAdder ha = builder.buildHalfAdder("HA1");
 * </pre>
 * 
 * @author Digital Logic Simulator
 * @version 1.0
 */
public class CompositeBuilder {
    
    /**
     * Builds a HalfAdder composite gate.
     * 
     * @param id unique identifier for the composite gate
     * @return a new HalfAdder instance
     */
    public HalfAdder buildHalfAdder(String id) {
        return new HalfAdder(id);
    }
    
    /**
     * Builds a FullAdder composite gate.
     * 
     * @param id unique identifier for the composite gate
     * @return a new FullAdder instance
     */
    public FullAdder buildFullAdder(String id) {
        return new FullAdder(id);
    }
    
    /**
     * Builds a 2-to-1 multiplexer (Mux2) composite gate.
     * 
     * @param id unique identifier for the composite gate
     * @return a new Mux2 instance
     */
    public Mux2 buildMux2(String id) {
        return new Mux2(id);
    }
    
    /**
     * Builds a 4-to-1 multiplexer (Mux4) composite gate.
     * 
     * @param id unique identifier for the composite gate
     * @return a new Mux4 instance
     */
    public Mux4 buildMux4(String id) {
        return new Mux4(id);
    }
}
