# Digital Logic Simulator (Java)

**Combinational logic simulator** with gates, wires, named primary I/O, and a deterministic **topological evaluation** engine. Built with **Java + Gradle + JUnit**, emphasizing **DSA**, **SWE best practices**, and concepts adjacent to **FPGA/EDA** flows.

![Java](https://img.shields.io/badge/Java-21%2B%20(tested%20on%2024)-blue)
![Build](https://img.shields.io/badge/Build-Gradle-green)
![Tests](https://img.shields.io/badge/Tests-JUnit-informational)
![License: MIT](https://img.shields.io/badge/License-MIT-success)

---

## Why this matters (FPGA / ECE / SWE)

- Models a **netlist-like graph** (gates = nodes, wires = edges), then evaluates in **topological order** (Kahn’s algorithm) with **cycle detection**—the same graph ideas behind **EDA/FPGA** toolchains.
- Clear **time complexity (O(V+E))**, **unit tests**, and **CLI builds** → strong **SWE** hygiene plus **DSA** reasoning.
- Extensible: add **NAND/NOR**, **critical path** (timing proxy), **sequential elements** (DFF), or **fault injection** (reliability)—useful talking points for ECE/FPGA roles.

---

## Features

- Gates: **AND, OR, NOT, XOR** (+ composition tests)
- **Deterministic `propagate()`** using **topological sort**; throws **`IllegalStateException("Cycle detected")`** for loops
- Named **primary inputs/outputs**
- **JUnit** truth-table & negative tests
- **Gradle** build/test; runs on **Java 21+** (developed & tested on **Java 24**)

---

## Requirements

- **Java 21+** (LTS recommended; developed on **Java 24**)
- **Gradle wrapper included** (`./gradlew`), no global Gradle needed
- macOS/Linux/WSL-bash/PowerShell

---

## Quick start

```bash
# clone, build, test
git clone https://github.com/gquar/java-logic-sim.git
cd java-logic-sim
./gradlew :lib:build
./gradlew :lib:test

```


## Example (tiny demo)

```java
// (A AND B) OR (NOT C)
Circuit c = new Circuit();
// build gates, connect wires, set inputs, then:
c.propagate();
System.out.println(c.readPrimaryOutputs().get(0));
```

## Design Notes

- *Graph model*: gates = nodes, wires = edges
- *Topological sort*: Kahn’s algorithm (queue) — O(V+E)
- *Cycle detection*: `if processed < total`, throw: 
```java
 throw IllegalStateException("Cycle detected")
 ```
- *Propagation*: single pass in topo order; push outputs immediately after each gate’s `evaluate()`

---

## Tests

```bash
./gradlew :lib:test
```
*Included*:
- **XOR truth table**
- **Negative cycle test**: `assertThrows(IllegalStateException)` and message contains `"Cycle detected"`
- Composition / fan-in / fan-out checks

---

## Roadmap

- **Graphviz DOT export → PNG**
- Mini netlist DSL + CLI loader
- **D flip-flop** + `tick()` (sequential logic)
- Critical path (longest path in DAG)
- GitHub Actions CI (Java 21 matrix)

---

## Benchmarks

**Scenario:** DAG ≈ 1,000 gates, 100 calls to `propagate()`  
**Machine:** macOS, CPU/RAM (fill in), Java 24.0.2, Gradle wrapper

- **Before:** 1509.101 ms total → **15.0910 ms/run**
- **After:** **7.728 ms** total → **0.0773 ms/run**

**~195× speedup** from:
- Kahn’s topological evaluation (O(V+E))
- Immediate wire propagation
- JIT warm-up & targeted bench harness


---

### License
Released under the [MIT License](./LICENSE).

---

