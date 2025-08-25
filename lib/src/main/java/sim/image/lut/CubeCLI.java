package sim.image.lut;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * CLI for 3D LUT operations.
 * 
 * <p>Subcommands:
 * - generate: Create LUT from source/target image pair
 * - apply: Apply LUT to an image
 */
public final class CubeCLI {
    private CubeCLI() {}
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        
        String command = args[0];
        switch (command) {
            case "generate":
                handleGenerate(args);
                break;
            case "apply":
                handleApply(args);
                break;
            case "optimize":
                handleOptimize(args);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  CubeCLI generate --src <src.png> --tgt <tgt.png> --size <size> --out <cube.cube> --preview <preview.png> [--trilinear]");
        System.err.println("  CubeCLI apply --in <img.png> --cube <cube.cube> --out <out.png> [--trilinear]");
        System.err.println("  CubeCLI optimize --src <src.png> --tgt <tgt.png> --sizes <9,17,33> --goal-psnr <36> --out-dir <dir> [--trilinear] [--csv <results.csv>]");
    }
    
    private static void handleGenerate(String[] args) throws Exception {
        String srcPath = null;
        String tgtPath = null;
        int size = 17;
        String outPath = null;
        String previewPath = null;
        boolean trilinear = false;
        
        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--src":
                    if (++i < args.length) srcPath = args[i];
                    break;
                case "--tgt":
                    if (++i < args.length) tgtPath = args[i];
                    break;
                case "--size":
                    if (++i < args.length) size = Integer.parseInt(args[i]);
                    break;
                case "--out":
                    if (++i < args.length) outPath = args[i];
                    break;
                case "--preview":
                    if (++i < args.length) previewPath = args[i];
                    break;
                case "--trilinear":
                    trilinear = true;
                    break;
            }
        }
        
        if (srcPath == null || tgtPath == null || outPath == null) {
            System.err.println("generate requires --src, --tgt, and --out");
            System.exit(1);
        }
        
        // Load images
        BufferedImage srcImg = ImageIO.read(Paths.get(srcPath).toFile());
        BufferedImage tgtImg = ImageIO.read(Paths.get(tgtPath).toFile());
        
        if (srcImg == null || tgtImg == null) {
            System.err.println("Could not load source or target image");
            System.exit(1);
        }
        
        if (srcImg.getWidth() != tgtImg.getWidth() || srcImg.getHeight() != tgtImg.getHeight()) {
            System.err.println("Source and target images must have same dimensions");
            System.exit(1);
        }
        
                // Generate LUT
        LUT3D lut = LUTLearner.learnFromPair(srcImg, tgtImg, size);
        
        // Save cube file
        Path cubePath = Paths.get(outPath);
        CubeIO.writeCube(cubePath, lut, "Generated LUT", size);
        
        // Generate preview if requested
        if (previewPath != null) {
            BufferedImage preview = LUTLearner.apply(srcImg, lut, trilinear);
            Path previewPathObj = Paths.get(previewPath);
            ImageIO.write(preview, "png", previewPathObj.toFile());
        }       
        
        System.out.println("Generated LUT: " + cubePath);
        if (previewPath != null) {
            System.out.println("Preview: " + previewPath);
        }
    }
    
    private static void handleApply(String[] args) throws Exception {
        String inPath = null;
        String cubePath = null;
        String outPath = null;
        boolean trilinear = false;
        
        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--in":
                    if (++i < args.length) inPath = args[i];
                    break;
                case "--cube":
                    if (++i < args.length) cubePath = args[i];
                    break;
                case "--out":
                    if (++i < args.length) outPath = args[i];
                    break;
                case "--trilinear":
                    trilinear = true;
                    break;
            }
        }
        
        if (inPath == null || cubePath == null || outPath == null) {
            System.err.println("apply requires --in, --cube, and --out");
            System.exit(1);
        }
        
        // Load image and LUT
        BufferedImage img = ImageIO.read(Paths.get(inPath).toFile());
        LUT3D lut = CubeIO.readCube(Paths.get(cubePath));
        
        if (img == null) {
            System.err.println("Could not load input image");
            System.exit(1);
        }
        
        // Apply LUT
        BufferedImage result = LUTLearner.apply(img, lut, trilinear);
        
        // Save result
        Path out = Paths.get(outPath);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        ImageIO.write(result, "png", out.toFile());
        
        System.out.println("Applied LUT: " + out);
    }
    
    private static void handleOptimize(String[] args) throws Exception {
        String srcPath = null;
        String tgtPath = null;
        String valSrcPath = null;
        String valTgtPath = null;
        String sizesStr = null;
        double goalPsnr = 36.0;
        String outDir = null;
        boolean trilinear = false;
        String csvPath = null;
        
        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--src":
                    if (++i < args.length) srcPath = args[i];
                    break;
                case "--tgt":
                    if (++i < args.length) tgtPath = args[i];
                    break;
                case "--sizes":
                    if (++i < args.length) sizesStr = args[i];
                    break;
                case "--goal-psnr":
                    if (++i < args.length) goalPsnr = Double.parseDouble(args[i]);
                    break;
                case "--out-dir":
                    if (++i < args.length) outDir = args[i];
                    break;
                case "--trilinear":
                    trilinear = true;
                    break;
                case "--val-src":
                    if (++i < args.length) valSrcPath = args[i];
                    break;
                case "--val-tgt":
                    if (++i < args.length) valTgtPath = args[i];
                    break;
                case "--csv":
                    if (++i < args.length) csvPath = args[i];
                    break;
            }
        }
        
        if (srcPath == null || tgtPath == null || sizesStr == null || outDir == null) {
            System.err.println("optimize requires --src, --tgt, --sizes, and --out-dir");
            System.exit(1);
        }
        
        // Parse sizes
        String[] sizeStrs = sizesStr.split(",");
        int[] sizes = new int[sizeStrs.length];
        for (int i = 0; i < sizeStrs.length; i++) {
            sizes[i] = Integer.parseInt(sizeStrs[i].trim());
        }
        
        // Load training images
        BufferedImage srcImg = ImageIO.read(Paths.get(srcPath).toFile());
        BufferedImage tgtImg = ImageIO.read(Paths.get(tgtPath).toFile());
        
        if (srcImg == null || tgtImg == null) {
            System.err.println("Could not load source or target image");
            System.exit(1);
        }
        
        // Load validation images if provided
        BufferedImage valSrcImg = null;
        BufferedImage valTgtImg = null;
        if (valSrcPath != null && valTgtPath != null) {
            valSrcImg = ImageIO.read(Paths.get(valSrcPath).toFile());
            valTgtImg = ImageIO.read(Paths.get(valTgtPath).toFile());
            
            if (valSrcImg == null || valTgtImg == null) {
                System.err.println("Could not load validation images");
                System.exit(1);
            }
        }
        
        // Run optimization sweep
        Path outDirPath = Paths.get(outDir);
        Path csvPathObj = csvPath != null ? Paths.get(csvPath) : null;
        List<LUTQuality.Result> results = LUTQuality.sweep(srcImg, tgtImg, valSrcImg, valTgtImg, 
                                                          sizes, trilinear, outDirPath, csvPathObj);
        
        // Find best size
        LUTQuality.Result best = LUTQuality.findBestSize(results, goalPsnr);
        
        // Print absolute paths
        System.out.println("Training images:");
        System.out.println("  Source: " + Paths.get(srcPath).toAbsolutePath());
        System.out.println("  Target: " + Paths.get(tgtPath).toAbsolutePath());
        if (valSrcPath != null && valTgtPath != null) {
            System.out.println("Validation images:");
            System.out.println("  Source: " + Paths.get(valSrcPath).toAbsolutePath());
            System.out.println("  Target: " + Paths.get(valTgtPath).toAbsolutePath());
        }
        System.out.println();
        
        // Print results table
        System.out.println("Optimization Results:");
        System.out.println("Size | Train PSNR | Val PSNR");
        System.out.println("-----|------------|---------");
        for (LUTQuality.Result result : results) {
            String trainPsnrStr = Double.isInfinite(result.trainPsnr) ? "∞" : String.format("%.2f", result.trainPsnr);
            if (result.hasVal) {
                String valPsnrStr = Double.isInfinite(result.valPsnr) ? "∞" : String.format("%.2f", result.valPsnr);
                System.out.printf("%4d | %10s | %8s%n", result.size, trainPsnrStr, valPsnrStr);
            } else {
                System.out.printf("%4d | %10s | %8s%n", result.size, trainPsnrStr, "N/A");
            }
        }
        
        if (best != null) {
            System.out.println("Best size meeting PSNR goal: " + best.size);
        } else {
            System.out.println("No size meets PSNR goal of " + goalPsnr + " dB");
        }
        
        if (csvPath != null) {
            System.out.println("Wrote CSV: " + csvPath);
        }
    }
    

}
