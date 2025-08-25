package sim.image.lut;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import sim.image.color.Srgb;

/**
 * Quality metrics and optimization for 3D LUTs.
 * 
 * <p>Provides MSE/PSNR calculations and automatic size optimization
 * by sweeping different LUT sizes and selecting the smallest meeting
 * a quality threshold.
 */
public final class LUTQuality {
    private LUTQuality() {}
    
    /**
     * Result of a LUT optimization sweep.
     */
    public static class Result {
        public final int size;
        public final double trainMse;
        public final double trainPsnr;
        public final double valMse;
        public final double valPsnr;
        public final boolean hasVal;
        public final Path outTrainPreview;
        public final Path outValPreview;
        
        public Result(int size, double trainMse, double trainPsnr, Path outTrainPreview) {
            this(size, trainMse, trainPsnr, 0.0, 0.0, false, outTrainPreview, null);
        }
        
        public Result(int size, double trainMse, double trainPsnr, double valMse, double valPsnr, 
                     boolean hasVal, Path outTrainPreview, Path outValPreview) {
            this.size = size;
            this.trainMse = trainMse;
            this.trainPsnr = trainPsnr;
            this.valMse = valMse;
            this.valPsnr = valPsnr;
            this.hasVal = hasVal;
            this.outTrainPreview = outTrainPreview;
            this.outValPreview = outValPreview;
        }
        
        @Override
        public String toString() {
            if (hasVal) {
                return String.format("size=%d, train_PSNR=%.2f dB, val_PSNR=%.2f dB", 
                                   size, trainPsnr, valPsnr);
            } else {
                return String.format("size=%d, train_PSNR=%.2f dB", size, trainPsnr);
            }
        }
    }
    
    /**
     * Calculates Mean Squared Error between two images in linear light.
     * 
     * @param a first image
     * @param b second image
     * @return MSE averaged across all channels in linear light
     */
    public static double mse(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            throw new IllegalArgumentException("Images must have same dimensions");
        }
        
        int width = a.getWidth();
        int height = a.getHeight();
        double totalSquaredError = 0.0;
        int pixelCount = width * height * 3; // 3 channels
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color colorA = new Color(a.getRGB(x, y));
                Color colorB = new Color(b.getRGB(x, y));
                
                // Convert to sRGB [0, 1] then to linear light
                float rA = Srgb.srgbToLinear(colorA.getRed() / 255.0f);
                float gA = Srgb.srgbToLinear(colorA.getGreen() / 255.0f);
                float bA = Srgb.srgbToLinear(colorA.getBlue() / 255.0f);
                
                float rB = Srgb.srgbToLinear(colorB.getRed() / 255.0f);
                float gB = Srgb.srgbToLinear(colorB.getGreen() / 255.0f);
                float bB = Srgb.srgbToLinear(colorB.getBlue() / 255.0f);
                
                double rError = rA - rB;
                double gError = gA - gB;
                double bError = bA - bB;
                
                totalSquaredError += rError * rError + gError * gError + bError * bError;
            }
        }
        
        return totalSquaredError / pixelCount;
    }
    
    /**
     * Calculates Peak Signal-to-Noise Ratio from MSE.
     * 
     * @param mse Mean Squared Error
     * @return PSNR in decibels
     */
    public static double psnrFromMse(double mse) {
        if (mse <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return 10.0 * Math.log10(1.0 / mse);
    }
    
    /**
     * Sweeps different LUT sizes and evaluates quality.
     * 
     * @param trainSrc training source image
     * @param trainTgt training target image
     * @param valSrc validation source image (may be null)
     * @param valTgt validation target image (may be null)
     * @param sizes array of LUT sizes to test
     * @param trilinear if true, use trilinear interpolation
     * @param outDir output directory for preview images
     * @param csvOut CSV output path (may be null)
     * @return list of results for each size
     */
    public static List<Result> sweep(BufferedImage trainSrc, BufferedImage trainTgt,
                                   BufferedImage valSrc, BufferedImage valTgt,
                                   int[] sizes, boolean trilinear, Path outDir, Path csvOut) throws Exception {
        if (outDir != null) {
            Files.createDirectories(outDir);
        }
        
        List<Result> results = new ArrayList<>();
        boolean hasValidation = (valSrc != null && valTgt != null);
        
        // Write CSV header if requested
        if (csvOut != null) {
            System.out.println("Creating CSV file: " + csvOut.toAbsolutePath());
            try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                    csvOut, java.nio.charset.StandardCharsets.UTF_8)) {
                if (hasValidation) {
                    w.write("size,train_mse,train_psnr,val_mse,val_psnr,path_train,path_val\n");
                } else {
                    w.write("size,train_mse,train_psnr,path_train\n");
                }
            } catch (Exception e) {
                System.err.println("Error writing CSV header: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No CSV path provided");
        }
        
        for (int size : sizes) {
            // Learn LUT from training data
            LUT3D lut = LUTLearner.learnFromPair(trainSrc, trainTgt, size);
            
            // Apply to training data
            BufferedImage trainOut = LUTLearner.apply(trainSrc, lut, trilinear);
            double trainMse = mse(trainOut, trainTgt);
            double trainPsnr = psnrFromMse(trainMse);
            
            // Save training preview image
            Path outTrainPreview = null;
            if (outDir != null) {
                outTrainPreview = outDir.resolve(String.format("out_train_size%02d.png", size));
                javax.imageio.ImageIO.write(trainOut, "png", outTrainPreview.toFile());
            }
            
            // Handle validation if provided
            double valMse = 0.0;
            double valPsnr = 0.0;
            Path outValPreview = null;
            boolean valMseZero = false;
            
            if (hasValidation) {
                // Always use trilinear for validation to reduce quantization artifacts
                BufferedImage valOut = LUTLearner.apply(valSrc, lut, true);
                valMse = mse(valOut, valTgt);
                valPsnr = psnrFromMse(valMse);
                valMseZero = (valMse == 0.0);
                
                if (outDir != null) {
                    outValPreview = outDir.resolve(String.format("out_val_size%02d.png", size));
                    javax.imageio.ImageIO.write(valOut, "png", outValPreview.toFile());
                }
            }
            
            Result result = new Result(size, trainMse, trainPsnr, valMse, valPsnr, 
                                     hasValidation, outTrainPreview, outValPreview);
            results.add(result);
            
            // Log diagnostics
            int populatedBins = LUTLearner.populatedBins(lut);
            int totalBins = size * size * size;
            double binPercentage = (double) populatedBins / totalBins * 100.0;
            System.out.printf("  Size %d: binsPopulated=%.1f%% (%d/%d)%s%n", 
                            size, binPercentage, populatedBins, totalBins,
                            valMseZero ? " val_mse_zero" : "");
            
            // Write CSV row if requested
            if (csvOut != null) {
                try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                        csvOut, java.nio.charset.StandardCharsets.UTF_8, 
                        java.nio.file.StandardOpenOption.APPEND)) {
                    if (hasValidation) {
                        w.write(String.format("%d,%.6f,%s,%.6f,%s,%s,%s\n", 
                            size, trainMse, 
                            Double.isInfinite(trainPsnr) ? "inf" : String.format("%.2f", trainPsnr),
                            valMse, 
                            Double.isInfinite(valPsnr) ? "inf" : String.format("%.2f", valPsnr),
                            outTrainPreview != null ? outTrainPreview.toString() : "",
                            outValPreview != null ? outValPreview.toString() : ""));
                    } else {
                        w.write(String.format("%d,%.6f,%s,%s\n", 
                            size, trainMse, 
                            Double.isInfinite(trainPsnr) ? "inf" : String.format("%.2f", trainPsnr),
                            outTrainPreview != null ? outTrainPreview.toString() : ""));
                    }
                } catch (Exception e) {
                    System.err.println("Error writing CSV: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        return results;
    }
    
    /**
     * Sweeps different LUT sizes and evaluates quality (legacy method).
     * 
     * @param src source image
     * @param tgt target image
     * @param sizes array of LUT sizes to test
     * @param trilinear if true, use trilinear interpolation
     * @param outDir output directory for preview images
     * @return list of results for each size
     */
    public static List<Result> sweep(BufferedImage src, BufferedImage tgt, int[] sizes, 
                                   boolean trilinear, Path outDir) throws Exception {
        return sweep(src, tgt, null, null, sizes, trilinear, outDir, null);
    }
    
    /**
     * Finds the smallest LUT size meeting a PSNR goal.
     * 
     * @param results list of sweep results
     * @param goalPsnr minimum PSNR required (in dB)
     * @return best result, or null if no size meets the goal
     */
    public static Result findBestSize(List<Result> results, double goalPsnr) {
        Result best = null;
        
        for (Result result : results) {
            double psnrToCheck = result.hasVal ? result.valPsnr : result.trainPsnr;
            if (psnrToCheck >= goalPsnr) {
                if (best == null || result.size < best.size) {
                    best = result;
                }
            }
        }
        
        return best;
    }
    
    /**
     * Assert helper for tests to check non-zero values.
     * 
     * @param val value to check
     * @param hint description for error message
     * @throws AssertionError if val is zero or negative
     */
    public static void assertNonZero(double val, String hint) {
        if (val <= 0.0) {
            throw new AssertionError("Expected non-zero value for " + hint + ", got: " + val);
        }
    }
}
