package sim.cli.netlist;

import sim.core.*;
import sim.dot.GraphvizExporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * CLI for parsing and executing a simple netlist DSL.
 * 
 * <p>Grammar:
 * INPUTS A,B,Cin
 * X1 = XOR(A,B)
 * SUM = XOR(X1,Cin)
 * C1  = AND(A,B)
 * C2  = AND(X1,Cin)
 * Cout= OR(C1,C2)
 * OUTPUTS SUM,Cout
 */
public final class NetlistCLI {
    private NetlistCLI() {}
    
    public static void main(String[] args) throws Exception {
        String netlistFile = null;
        Map<String, Boolean> inputs = new HashMap<>();
        String dotFile = null;
        
        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--file":
                    if (++i < args.length) netlistFile = args[i];
                    break;
                case "--set":
                    if (++i < args.length) {
                        String[] assignments = args[i].split(",");
                        for (String assignment : assignments) {
                            String[] parts = assignment.split("=");
                            if (parts.length == 2) {
                                inputs.put(parts[0].trim(), Boolean.parseBoolean(parts[1].trim()));
                            }
                        }
                    }
                    break;
                case "--dot":
                    if (++i < args.length) dotFile = args[i];
                    break;
            }
        }
        
        if (netlistFile == null) {
            System.err.println("Usage: NetlistCLI --file <netlist.nl> --set A=1,B=1,Cin=0 [--dot <output.dot>]");
            System.exit(1);
        }
        
        // Parse and build circuit
        Circuit circuit = parseNetlist(Paths.get(netlistFile));
        
        // Set inputs
        for (Map.Entry<String, Boolean> entry : inputs.entrySet()) {
            circuit.setPrimaryInput(entry.getKey(), entry.getValue());
        }
        
        // Propagate
        circuit.propagate();
        
        // Print outputs
        List<Boolean> outputs = circuit.readPrimaryOutputs();
        List<Gate> outputGates = circuit.getPrimaryOutputs();
        
        for (int i = 0; i < outputs.size() && i < outputGates.size(); i++) {
            System.out.printf("OUT%d=%s%n", i, outputs.get(i) ? "1" : "0");
        }
        
        // Generate DOT if requested
        if (dotFile != null) {
            Path dotPath = Paths.get(dotFile);
            Map<Gate, String> outputNames = new HashMap<>();
            List<Gate> gates = circuit.getPrimaryOutputs();
            for (int i = 0; i < gates.size(); i++) {
                outputNames.put(gates.get(i), "OUT" + i);
            }
            GraphvizExporter.writeDotAndPng(circuit, dotPath, dotPath, outputNames);
            System.out.println("Generated: " + dotPath + " and " + dotPath.toString().replace(".dot", ".png"));
        }
    }
    
    private static Circuit parseNetlist(Path file) throws Exception {
        Circuit circuit = new Circuit();
        Map<String, Gate> gates = new HashMap<>();
        List<String> inputNames = new ArrayList<>();
        List<String> outputNames = new ArrayList<>();
        
        List<String> lines = Files.readAllLines(file);
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            if (line.startsWith("INPUTS")) {
                String[] inputs = line.substring(7).split(",");
                for (String input : inputs) {
                    String name = input.trim();
                    inputNames.add(name);
                    // Create input gates (will be connected later)
                    gates.put(name, null);
                }
            } else if (line.startsWith("OUTPUTS")) {
                String[] outputs = line.substring(8).split(",");
                for (String output : outputs) {
                    outputNames.add(output.trim());
                }
            } else if (line.contains("=")) {
                // Gate assignment: X1 = XOR(A,B)
                String[] parts = line.split("=", 2);
                String gateName = parts[0].trim();
                String gateDef = parts[1].trim();
                
                // Parse gate definition
                String[] gateParts = gateDef.split("\\(", 2);
                String gateType = gateParts[0].trim();
                String[] gateInputs = gateParts[1].substring(0, gateParts[1].length() - 1).split(",");
                
                // Create gate
                Gate gate = createGate(gateType, gateName);
                circuit.addGate(gate);
                gates.put(gateName, gate);
                
                // Connect inputs
                for (int i = 0; i < gateInputs.length; i++) {
                    String inputName = gateInputs[i].trim();
                    Gate inputGate = gates.get(inputName);
                    if (inputGate != null) {
                        // Connect to existing gate
                        Wire wire = new Wire(inputGate, 0, gate, i);
                        circuit.addWire(wire);
                    } else {
                        // Connect to primary input
                        circuit.connectPrimaryInput(inputName, gate, i);
                    }
                }
            }
        }
        
        // Connect primary inputs - we'll connect them directly to gates that use them
        // The gates map will be populated as we parse gate definitions
        
        // Connect primary outputs
        for (String outputName : outputNames) {
            Gate outputGate = gates.get(outputName);
            if (outputGate != null) {
                circuit.addPrimaryOutput(outputGate);
            }
        }
        
        return circuit;
    }
    
    private static Gate createGate(String gateType, String name) {
        switch (gateType.toUpperCase()) {
            case "AND": return new sim.core.AndGate(name);
            case "OR": return new sim.core.OrGate(name);
            case "NOT": return new sim.core.NotGate(name);
            case "XOR": return new sim.core.XorGate(name);
            default: throw new IllegalArgumentException("Unknown gate type: " + gateType);
        }
    }
}
