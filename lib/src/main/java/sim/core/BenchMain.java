package sim.core;

public class BenchMain {
    public static void main(String[] args) {
        final int GATES = 1000;
        final int RUNS  = 100;

        Circuit c = new Circuit();

        NotGate first = new NotGate("IN");
        c.addGate(first);
        c.connectPrimaryInput("IN", first, 0);

        Gate prev = first;
        for (int i = 1; i < GATES; i++) {
            NotGate g = new NotGate("NG" + i);
            c.addGate(g);
            c.addWire(new Wire(prev, 0, g, 0));
            prev = g;
        }
        c.addPrimaryOutput(prev);

        // Warmup
        c.setPrimaryInput("IN", true);
        c.propagate();

        long t0 = System.nanoTime();
        for (int r = 0; r < RUNS; r++) {
            c.setPrimaryInput("IN", (r & 1) == 0);
            c.propagate();
        }
        long t1 = System.nanoTime();

        double totalMs = (t1 - t0) / 1_000_000.0;
        double avgMs   = totalMs / RUNS;

        System.out.printf("BENCH: gates=%d runs=%d totalMs=%.3f avgMs=%.4f%n",
                GATES, RUNS, totalMs, avgMs);
    }
}
