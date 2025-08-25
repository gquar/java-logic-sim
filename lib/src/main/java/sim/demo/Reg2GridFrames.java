package sim.demo;

import sim.core.Circuit;
import sim.core.seq.DFlipFlop;
import sim.image.GridPNG;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class Reg2GridFrames {
    public static void main(String[] args) throws Exception {
        Circuit c = new Circuit();

        // Gates
        DFlipFlop r0 = new DFlipFlop("R0"); // pins: D=0, CLK=1
        DFlipFlop r1 = new DFlipFlop("R1");
        c.addGate(r0);
        c.addGate(r1);

        // Primary inputs and outputs
        c.connectPrimaryInput("D0", r0, 0);
        c.connectPrimaryInput("CLK", r0, 1);
        c.connectPrimaryInput("CLK", r1, 1);
        c.connectPrimaryInput("D1", r1, 0);
        c.addPrimaryOutput(r0); // Q0
        c.addPrimaryOutput(r1); // Q1

        Path dir = Paths.get("docs/frames_grid");
        Files.createDirectories(dir);
        Path csv = dir.resolve("frames.csv");
        initCsv(csv);

        // Frame 0
        c.setPrimaryInput("D0", false);
        c.setPrimaryInput("D1", false);
        c.setPrimaryInput("CLK", false);
        c.propagate();
        saveFrame(c, 0, dir.resolve("reg_00.png"), csv);

        // Frame 1 (capture D0=1)
        c.setPrimaryInput("D0", true);
        c.setPrimaryInput("D1", false);
        c.tick("CLK");
        saveFrame(c, 1, dir.resolve("reg_01.png"), csv);

        // Frame 2 (capture D1=1)
        c.setPrimaryInput("D0", false);
        c.setPrimaryInput("D1", true);
        c.tick("CLK");
        saveFrame(c, 2, dir.resolve("reg_02.png"), csv);

        // Frame 3 (capture D0=1 and D1=1)
        c.setPrimaryInput("D0", true);
        c.setPrimaryInput("D1", true);
        c.tick("CLK");
        saveFrame(c, 3, dir.resolve("reg_03.png"), csv);

        System.out.println("Done. PNGs in " + dir + " and CSV at " + csv);
    }

    private static void initCsv(Path csv) throws Exception {
        // Overwrite with header each run for reproducibility
        try (BufferedWriter w = Files.newBufferedWriter(csv, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("frame,q0,q1\n");
        }
    }

    private static void appendCsv(Path csv, int frame, boolean q0, boolean q1) throws Exception {
        try (BufferedWriter w = Files.newBufferedWriter(csv, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(frame + "," + (q0 ? 1 : 0) + "," + (q1 ? 1 : 0) + "\n");
        }
    }

    private static void saveFrame(Circuit c, int frameIndex, Path outPng, Path csv) throws Exception {
        var outs = c.readPrimaryOutputs(); // [Q0, Q1]
        boolean q0 = outs.get(0);
        boolean q1 = outs.get(1);

        boolean[][] grid = new boolean[][] { { q0, q1 } }; // 1 row, 2 cols
        String title = String.format("Frame %02d  Q0=%s  Q1=%s", frameIndex, q0 ? "1" : "0", q1 ? "1" : "0");
        GridPNG.save(grid, outPng, /*scale*/48, /*margin*/12, title);
        appendCsv(csv, frameIndex, q0, q1);
        System.out.println("Wrote " + outPng);
    }
}
