package project;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class User_Interface implements PlugInFilter {

    Task_3_Otsu Otsu = new Task_3_Otsu();

    // 1 - there was a potential bug in this file as Evaluate.run method was taking the
    // original image instead of segmented one

    // 2 - As our return types are void for Task 1 and Task 3 i.e Otsu, I needed to call all
    //  their run implementation just to store their result to resolve for the issue
    //  I mentioned above

    // 3 - I passed the updated results now to Evaluate.run() method

    // 4 - For Primitive and Canny, same implementation as before

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        Task_1_Threshold Threshold = new Task_1_Threshold();
        Task_2_EvaluateSegmentation Evaluate = new Task_2_EvaluateSegmentation();
        Task_3_Otsu Otsu = new Task_3_Otsu();
        Task_4_Filters Filters = new Task_4_Filters();
        Task_5_CannyEdgeDetection Canny = new Task_5_CannyEdgeDetection();


        GenericDialog GD = new GenericDialog("Selection panel");
        String[] firstChoice ={"Thresholding","Edge-Detection"};
        GD.addChoice("Choose an action: ", firstChoice,firstChoice[0]);
        GD.showDialog();

        if (GD.wasCanceled()){
            return;
        }

        int firstChoiceIdx = GD.getNextChoiceIndex();

        if (firstChoiceIdx ==0){

            String[] ThreshChoice ={"regular Thresholding","Otsu"};
            GD.addChoice("Choose Thresholding-technique:",ThreshChoice,ThreshChoice[0]);
            GD.addCheckbox("Evaluate Segmentation?",false);
            GD.showDialog();

            int temp = GD.getNextChoiceIndex();
            int ThreshChoiceIdx = GD.getNextChoiceIndex();
            boolean eval = GD.getNextBoolean();

            ByteProcessor resultIp = new ByteProcessor(imageProcessor.getWidth(),imageProcessor.getHeight());

            if (ThreshChoiceIdx == 0){
                // Threshold.run(imageProcessor); // did it to change my implementation of storing result
                GenericDialog gd = new GenericDialog("Thresholding");
                gd.addNumericField("Threshold value:", 128, 0);
                gd.addCheckbox("Correct uneven illumination", false);
                gd.showDialog();

                //check if the dialog was canceled
                if (gd.wasCanceled())
                    return;

                //get user choices  - remove everything from here to check installation
                int threshold = (int) gd.getNextNumber();
                boolean correct = gd.getNextBoolean();

                //correct illumination if selected
                ImageProcessor ipCopy;
                if (correct) {
                    ipCopy = Threshold.correctIllumination(imageProcessor);
                } else {
                    ipCopy = imageProcessor;
                }
                // threshold the image
                resultIp = Threshold.threshold(ipCopy, threshold);
                ImagePlus thresholdedImage = new ImagePlus("Thresholded Image", resultIp);
                thresholdedImage.show();
            }
            else{
                // Otsu.run(imageProcessor); // did it to change my implementation of storing result
                int threshold = Otsu.otsuGetThreshold(imageProcessor);
                resultIp = Otsu.otsuSegementation(imageProcessor, threshold);
                ImagePlus resultImage = new ImagePlus("Otsu Segmentation", resultIp);
                resultImage.show();
                System.out.println("Otsu Optimal Threshold: " + threshold);
            }

            if (eval){
                Evaluate.run(resultIp);  // potential bug here original grayscale image
                // is being passed here instead of segmented one which should we get from line 52 or 55,
                // that is why we were getting Specificity = 1 and Sensitivity = 1 before
                // Therefore I have resolved the issue by changing this file
            }
        }

        else{
            String[] EDChoice ={"Primitive","Canny-Edge-Detection"};
            GD.addChoice("Choose edge-detection-technique:",EDChoice,EDChoice[0]);
            GD.showDialog();
            int temp = GD.getNextChoiceIndex();
            int EDChoiceIdx = GD.getNextChoiceIndex();

            if (EDChoiceIdx == 0){
                Filters.run(imageProcessor);
            }
            else{
                Canny.run(imageProcessor);
            }
        }
    }
}

