package sim.core;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BenchMain {

    public static void main(String[] args) {
        // Args can be "sizes=100,200,500,1000,2000 runs=500 repeats=5 warmup=200"
        // or positional: "<sizesCSV> <runs> <repeats> <warmup>"
        Map<String,String> kv = parseKeyVals(args);

        String sizesCSV = kv.getOrDefault("sizes", argOr(args, 0, "100,200,500,1000,2000"));
        int runs        = parseIntOr(kv.getOrDefault("runs",    argOr(args, 1, "500")),  500);
        int repeats     = parseIntOr(kv.getOrDefault("repeats", argOr(args, 2, "5")),      5);
        int warmup      = parseIntOr(kv.getOrDefault("warmup",  argOr(args, 3, "200")),  200);

        int[] sizes = parseCSVInts(sizesCSV);

        System.out.printf("BENCH: sizes=%s runs=%d repeats=%d warmup=%d%n",
                Arrays.toString(sizes), runs, repeats, warmup);

        // Optional: do a short global warm-up on a medium circuit to stabilize JIT
        globalWarmup(1000, 200);

        for (int gates : sizes) {
            Circuit circuit = buildNotChain(gates);
            double[] totals = new double[repeats];

            // PER-SIZE warm-up (important to avoid comparing a cold vs hot size)
            timePropagate(circuit, /*runs*/ Math.max(warmup, 50), /*warmup*/ 0);

            for (int r = 0; r < repeats; r++) {
                double totalMs = timePropagate(circuit, runs, 0);
                totals[r] = totalMs;
                System.out.printf("TRIAL %d: gates=%d runs=%d totalMs=%.3f avgMs=%.4f%n",
                        (r + 1), gates, runs, totalMs, totalMs / runs);
            }

            Stats s = stats(totals);
            System.out.printf(
                "SUMMARY (gates=%d): minMs=%.3f maxMs=%.3f meanMs=%.3f medianMs=%.3f stdevMs=%.3f avgPerRun(median)=%.6f%n",
                gates, s.min, s.max, s.mean, s.median, s.stdev, s.median / runs
            );
            // CSV line for plotting (paste into a spreadsheet):
            System.out.printf("CSV,size=%d,medianTotalMs=%.3f,medianAvgMsPerRun=%.6f%n",
                    gates, s.median, s.median / runs);
        }
    }

    // --- Build a simple Not-gate chain of length n ---
    private static Circuit buildNotChain(int n) {
        Circuit c = new Circuit();
        List<Gate> gates = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            gates.add(new NotGate("N" + i));
            c.addGate(gates.get(i));
        }
        for (int i = 0; i < n - 1; i++) {
            c.addWire(new Wire(gates.get(i), 0, gates.get(i + 1), 0));
        }
        // Expose first input and last output
        c.connectPrimaryInput("IN", gates.get(0), 0);
        c.addPrimaryOutput(gates.get(n - 1));
        // Set an initial value
        c.setPrimaryInput("IN", false);
        return c;
    }

    // --- Run propagate() many times; toggle input to avoid constant states ---
    private static double timePropagate(Circuit c, int runs, int warmup) {
        boolean val = false;

        // Warm-up loop (ignored in timing)
        for (int i = 0; i < warmup; i++) {
            val = !val;
            c.setPrimaryInput("IN", val);
            c.propagate();
            // consume output so JIT can't trivially drop work
            boolean out = c.readPrimaryOutputs().get(0);
            if (ThreadLocalRandom.current().nextInt(1) == -1 && out) System.out.print(""); // no-op
        }

        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            val = !val;
            c.setPrimaryInput("IN", val);
            c.propagate();
            boolean out = c.readPrimaryOutputs().get(0);
            if (ThreadLocalRandom.current().nextInt(1) == -1 && out) System.out.print(""); // no-op
        }
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0; // ms
    }

    private static void globalWarmup(int gates, int iterations) {
        Circuit c = buildNotChain(gates);
        timePropagate(c, iterations, 50);
    }

    // --- Utilities ---

    private static int[] parseCSVInts(String csv) {
        String[] parts = csv.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
        return out;
    }

    private static Map<String,String> parseKeyVals(String[] args) {
        Map<String,String> map = new HashMap<>();
        for (String a : args) {
            int eq = a.indexOf('=');
            if (eq > 0 && eq < a.length() - 1) {
                map.put(a.substring(0, eq).trim(), a.substring(eq + 1).trim());
            }
        }
        return map;
    }
    private static String argOr(String[] args, int idx, String def) {
        return (idx < args.length && !args[idx].contains("=")) ? args[idx] : def;
    }
    private static int parseIntOr(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private record Stats(double min, double max, double mean, double median, double stdev) {}
    private static Stats stats(double[] a) {
        double[] b = a.clone();
        Arrays.sort(b);
        double min = b[0], max = b[b.length - 1];
        double sum = 0;
        for (double v : b) sum += v;
        double mean = sum / b.length;
        double median = (b.length % 2 == 1) ? b[b.length / 2]
                                            : 0.5 * (b[b.length / 2 - 1] + b[b.length / 2]);
        double var = 0;
        for (double v : b) var += (v - mean) * (v - mean);
        double stdev = Math.sqrt(var / b.length);
        return new Stats(min, max, mean, median, stdev);
    }
}
