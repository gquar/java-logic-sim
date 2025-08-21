# Changelog

## [v0.1.0]
### Added
- Composite gates: HalfAdder, FullAdder with exhaustive truth-table tests.
- Graphviz diagrams: logic and full-adder exporters; output value labels & colored edges.
- Bench harness + scaling table in README.
- GitHub Actions CI (Java 21).

### Changed
- Engine: switched from "until stable" to topological evaluation (Kahn's) + immediate wire propagation.
- Cycle detection with clear exception.

### Performance
- 1k-gate × 100 runs: 1509 ms → 7.7 ms (~195× speedup).
