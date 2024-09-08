package project;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Task_2_EvaluateSegmentation implements PlugInFilter {
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        ImageProcessor segmentedImg = imageProcessor.duplicate();
        ImagePlus referencedImgPlus = IJ.openImage();

        if (referencedImgPlus == null) {
            throw new IllegalArgumentException("Image File Not loaded successfully");
        }

        ImageProcessor referencedImage = referencedImgPlus.getProcessor();
        EvaluationResult evaluationResult = evaluateSegmentation(segmentedImg, referencedImage);

        if(evaluationResult == null){
            IJ.error("Both Segmented and Referenced Image have different dimensions");
        }
        else{
            double sensitivity = evaluationResult.getSensitivity();
            double specificity = evaluationResult.getSpecificity();

            IJ.log("Sensitivity = " + sensitivity);
            IJ.log("Specificity = " + specificity);
        }
    }

    private EvaluationResult evaluateSegmentation ( ImageProcessor segmentation , ImageProcessor reference ){

        int segmentationWidth  = segmentation.getWidth();
        int segmentationHeight = segmentation.getHeight();

        int referenceWidth  = reference.getWidth();
        int referenceHeight = reference.getHeight();

        // Check for same dimensions
        if(referenceWidth != segmentationWidth || referenceHeight != segmentationHeight){
            return null;
        }

        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;

        //System.out.println("Total pixels" + segmentation.getPixelCount());
        for(int y = 0; y < segmentationHeight; y++){
            for(int x = 0; x < segmentationWidth; x++){

                int segmentedPixel = segmentation.getPixel(x, y);
                int referencePixel = reference.getPixel(x, y);

                if( segmentedPixel == 255 && referencePixel == 255 ){
                    TP++; // True Positives
                }
                else if(referencePixel == 0 && segmentedPixel == 0){
                    TN++; // True Negatives
                }
                else if(segmentedPixel == 255 && referencePixel == 0){
                    FP++; // False Positives
                }
                else if(segmentedPixel == 0 && referencePixel == 255) {
                    FN++; // False Negatives
                }
            }
        }

        double specificity = (double) TN / (TN + FP);
        double sensitivity = (double) TP / (TP + FN);

        return new EvaluationResult(specificity, sensitivity);
    }



}
