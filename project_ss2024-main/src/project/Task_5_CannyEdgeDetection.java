package project;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Task_5_CannyEdgeDetection implements PlugInFilter {

    @Override
    public void run(ImageProcessor imageProcessor) {
        int[][] SobelX = {{1,0,-1},{2,0,-2},{1,0,-1}};
        int[][] SobelY = {{1,2,1},{0,0,0},{-1,-2,-1}};

        // Create the dialog for the user input
        GenericDialog gd = new GenericDialog("Advanced Edge Detection Parameters");

        // Add fields for sigma, upper threshold, and lower threshold
        gd.addNumericField("Sigma (σ):", 1.0, 2);
        gd.addNumericField("Upper Threshold:", 150, 0);
        gd.addNumericField("Lower Threshold:", 50, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            IJ.error("Edge Detetcion Params dialog was cancelled");
            return;
        }

        double sigma = gd.getNextNumber();
        int upperThreshold = (int) gd.getNextNumber();
        int lowerThreshold = (int) gd.getNextNumber();

        // 1. blurred the image
        FloatProcessor imageFloatIp = imageProcessor.duplicate().convertToFloatProcessor();
        imageFloatIp.blurGaussian(sigma); //starting point

        Task_4_Filters filtersObject = new Task_4_Filters();

        // 2. image derivatives in both X and Y direction
        FloatProcessor imageDerivativeX = filtersObject.applyFilter(imageFloatIp,SobelX);
        FloatProcessor imageDerivativeY = filtersObject.applyFilter(imageFloatIp,SobelY);

        // 3. image Gradient
        FloatProcessor gradientImage = filtersObject.getGradient(imageDerivativeX,imageDerivativeY);

        // 4. gradientDirections
        ByteProcessor gradientDirected = getDir(imageDerivativeX,imageDerivativeY);

        // 5. Non-Maximum Suppression
        FloatProcessor imageNMS = nonMaxSuppress(gradientImage,gradientDirected);

        //6. Hysteresis Thresholding
        ByteProcessor imageHysteresisThreshold = hysteresisThreshold(imageNMS, upperThreshold, lowerThreshold);

        //7. Output Image
        ImagePlus cannyEdgeDetectedImg = new ImagePlus("Output Image: ", imageHysteresisThreshold);
        cannyEdgeDetectedImg.show();
    }

    public ByteProcessor getDir (FloatProcessor X_Deriv, FloatProcessor Y_Deriv){

        int imageHeight = X_Deriv.getHeight();
        int imageWidth = X_Deriv.getWidth();

        ByteProcessor gradientImgDirections = new ByteProcessor(imageWidth,imageHeight);
        int[] angles = {0,45,90,135,180};

        for (int y = 0 ; y < imageHeight ; y++){
            for (int x = 0 ; x < imageWidth ; x++){

                double gradientXValue = X_Deriv.getPixelValue(x,y);
                double gradientYValue = Y_Deriv.getPixelValue(x,y);

                double gradientDirectionAngle = getClosestMatch(gradientXValue,gradientYValue,angles);
                gradientImgDirections.putPixelValue(x,y,gradientDirectionAngle);
            }
        }
        return gradientImgDirections;
    }

    public FloatProcessor nonMaxSuppress(FloatProcessor Grad, ByteProcessor Dir) {
        FloatProcessor resultNMS = new FloatProcessor(Grad.getWidth(),Grad.getHeight());

        int imageHeight = Grad.getHeight();
        int imageWidth = Grad.getWidth();

        for (int y = 1 ; y < imageHeight - 1 ; y++){
            for (int x = 1 ; x < imageWidth - 1 ; x++){
                double dir = Dir.getPixelValue(x,y);

                double currentCellPixelValue = Grad.getPixelValue(x,y);
                double neighborCell1PixelValue = 0;
                double neighborCell2PixelValue = 0;

                if(dir == 0){
                neighborCell1PixelValue = Grad.getPixelValue(x-1,y);
                neighborCell2PixelValue = Grad.getPixelValue(x+1,y);
                }
                else if (dir == 45){
                    neighborCell1PixelValue = Grad.getPixelValue(x+1,y-1);
                    neighborCell2PixelValue = Grad.getPixelValue(x-1,y+1);
                }
                else if (dir == 90){
                    neighborCell1PixelValue = Grad.getPixelValue(x,y-1);
                    neighborCell2PixelValue = Grad.getPixelValue(x,y+1);
                }
                else if (dir == 135){
                    neighborCell1PixelValue = Grad.getPixelValue(x-1,y-1);
                    neighborCell2PixelValue = Grad.getPixelValue(x+1,y+1);
                }

                if(currentCellPixelValue > neighborCell1PixelValue && currentCellPixelValue > neighborCell2PixelValue){
                    resultNMS.putPixelValue(x,y,currentCellPixelValue);
                }
                else{
                    resultNMS.putPixelValue(x,y,0);
                }
            }
        }
        return resultNMS;
    }

    public ByteProcessor hysteresisThreshold (FloatProcessor In, int upper, int lower){
        float tHigh = ((float)In.getMax()*upper)/100f;
        float tLow = ((float)In.getMax()*lower)/100f;

        ByteProcessor resultHysteresisTheshold = new ByteProcessor(In.getWidth(),In.getHeight());

        int imageHeight = In.getHeight();
        int imageWidth = In.getWidth();

        for (int y = 0 ; y < imageHeight - 1 ; y++){
            for (int x = 0 ; x < imageWidth - 1 ; x++){
                double pixelValue = In.getPixelValue(x,y);

                if (pixelValue > tHigh){
                    resultHysteresisTheshold.putPixelValue(x,y,255);
                }
                else{
                    resultHysteresisTheshold.putPixelValue(x,y,0);
                }
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int x = 0; x < In.getWidth(); x++) {
                for (int y = 0; y < In.getHeight(); y++) {
                    if (In.getPixelValue(x, y) >= tLow && hasNeighbours(resultHysteresisTheshold, x, y) && resultHysteresisTheshold.getPixel(x,y)==0) {
                        resultHysteresisTheshold.set(x, y, 255);
                        changed = true;
                    }
                }
            }
        }
        return resultHysteresisTheshold;
    }

    public double getClosestMatch(double gradX,double gradY, int angles[]){
        double gradientEstimatedDirection = Math.toDegrees(Math.atan2(-gradY,gradX));
        //Boolean isNegativeValue = gradientEstimatedDirection < 0;


        if (gradientEstimatedDirection < 0){ //negative case
            gradientEstimatedDirection += 180;
        }

        int angleIndex = 0;
        double closestDistance = Math.abs(angles[0] - gradientEstimatedDirection);

            for(int i = 1 ; i < angles.length ; i++){
                double absoluteDst = Math.abs(angles[i] - gradientEstimatedDirection);
                if (absoluteDst < closestDistance){
                    closestDistance = absoluteDst;
                    angleIndex = i;
                }
            }

        double resultAngle = 0;

        if(/*isNegativeValue &&*/ angles[angleIndex] == 180 ){
            resultAngle = 0;
        }
        else{
            resultAngle = angles[angleIndex];
        }
        return resultAngle;
    }

    public boolean hasNeighbours(ByteProcessor BP, int x, int y ){
        int count = (BP.getPixel(x+1,y)+BP.getPixel(x-1,y)+BP.getPixel(x,y+1)+BP.getPixel(x,y-1)+BP.getPixel(x+1,y+1)+
                BP.getPixel(x-1,y-1)+BP.getPixel(x-1,y+1)+BP.getPixel(x+1,y-1));
        count/=255;
        return (count>0) ;
    }


    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }
}
