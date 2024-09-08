package project;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

public class Task_3_Otsu implements PlugInFilter {
    int NUM_INTENSITY_LEVELS = 256;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        int threshold = otsuGetThreshold(imageProcessor);

        ByteProcessor result = otsuSegementation(imageProcessor, threshold);

        ImagePlus resultImage = new ImagePlus("Otsu Segmentation", result);
        resultImage.show();

        System.out.println("Otsu Optimal Threshold: " + threshold);
    }

    public ByteProcessor otsuSegementation(ImageProcessor ip, int threshold) {

        Task_1_Threshold Threshold = new Task_1_Threshold();
        ByteProcessor illuminatedImg = Threshold.correctIllumination(ip);

        return Threshold.threshold(illuminatedImg, threshold);
    }

    public double[] getHistogram(ImageProcessor in) {

        double[] imageHistogram = new double[NUM_INTENSITY_LEVELS];

        int imageWidth = in.getWidth();
        int imageHeight = in.getHeight();
        double totalNoOfPixels = imageWidth * imageHeight;

        // updating corresponding historgram entry
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int pixel = in.getPixel(x, y);
                imageHistogram[pixel]++;
            }
        }

        // normalizing histogram to create probability distribution
        for (int x = 0; x < NUM_INTENSITY_LEVELS; x++) {
            imageHistogram[x] = imageHistogram[x] / totalNoOfPixels;
        }

        return imageHistogram;
    }

    public double[] getP1(double[] histogram) {
        double[] P1 = new double[histogram.length];
        P1[0] = histogram[0];

        for (int theta = 1; theta < histogram.length; theta++) {
                P1[theta] = P1[theta - 1] + histogram[theta];
        }

        return P1;
    }

    public double[] getP2(double[] P1) {
        double[] P2 = new double[P1.length];

        for (int i = 0; i < P1.length; i++) {
            P2[i] = 1 - P1[i];
        }
        return P2;
    }

    public double[] getMu1(double[] histogram, double[] P1){

        double [] Mu1 = new double[P1.length];
        int length = Mu1.length;

        for (int theta = 0; theta < length; theta++) {

            double sumTillThreshold = 0;
            for (int j = 0; j <= theta; j++) { // 0 to theta
                sumTillThreshold += (j+1) * histogram[j];
            }
            if(P1[theta]> 0.0){
                Mu1[theta] = sumTillThreshold / P1[theta];
            }
            else{
                Mu1[theta] = sumTillThreshold / 10e-10;
            }
        }
        return Mu1;

    }
    public double[] getMu2(double[] histogram, double[] P2){
        double [] Mu2 = new double[NUM_INTENSITY_LEVELS];
        int historgramlength = Mu2.length;

        for (int theta = 0; theta < historgramlength ; theta++) {
            double sumTillHistrogramEnd = 0;
            for (int j = theta+1; j < historgramlength; j++) { // theta + 1 to L-1
                sumTillHistrogramEnd += (j+1) * histogram[j];
            }
            if(P2[theta]> 0.0){
                Mu2[theta] = sumTillHistrogramEnd / P2[theta];
            }
            else{
                Mu2[theta] = sumTillHistrogramEnd / 10e-10;
            };
        }
        return Mu2;
    }

    public double[] getSigmas(double[] histogram, double[] P1, double[] P2, double[] mu1, double[] mu2) {
        double[] sigma = new double[P1.length];

        for (int i = 0; i < histogram.length; i++) {
            double muDifferenceSquared = (mu1[i] - mu2[i]) * (mu1[i] - mu2[i]);
            sigma[i] = P1[i] * P2[i] * muDifferenceSquared;
        }
        return sigma;
    };

    public int getMaximum(double[] sigmas){
        double maxSigma = sigmas[0];
        int maxSigmaIndex = 0;

        for (int theta = 1; theta < sigmas.length; theta++) {
            if (sigmas[theta] > maxSigma) {
                maxSigma = sigmas[theta];
                maxSigmaIndex = theta;
            }
        }

        return maxSigmaIndex;
    };

    public int otsuGetThreshold(ImageProcessor in) {
        double [] histogram = getHistogram(in);
        double [] P1 = getP1(histogram);
        double [] P2 = getP2(P1);
        double [] Mu1 = getMu1(histogram, P1);
        double [] Mu2 = getMu2(histogram, P2);
        double [] sigmas = getSigmas(histogram,P1,P2,Mu1,Mu2);

        int otsuThreshold = getMaximum(sigmas);

        return otsuThreshold;
    }
}
