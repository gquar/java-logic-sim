# Changelog

## [v0.3.0] - 2024-08-24

### Added
- **Validation support in LUT optimization** - prevents overfitting with separate validation images
- **Non-linear sample generation** - gamma (1.9, 2.1, 1.8) + cross-channel mixing + dither
- **Enhanced CSV format** - includes train_psnr and val_psnr columns with proper infinity handling
- **Python plotting improvements** - pandas fallback, infinity display, dual-series plotting
- **Graphviz legend** - explains color coding and output value display
- **Nearest vs Trilinear comparison** - side-by-side demo showing interpolation differences
- **Tiny Netlist DSL** - simple grammar for circuit description with AND/OR/NOT/XOR gates
- **CI enhancements** - Python dependency installation and plot artifact generation

### Changed
- **LUT optimization defaults** - now includes validation images by default
- **Sample transformation** - more complex non-linear mapping that's harder to perfectly represent
- **CSV output** - "inf" for infinite PSNR values, proper validation column handling
- **CubeCLI preview** - trilinear flag now affects preview generation

### Fixed
- **Infinity PSNR display** - shows "∞" in console, "inf" in CSV
- **Python dependencies** - graceful fallback when pandas not available
- **Validation metrics** - proper train vs validation PSNR comparison

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
