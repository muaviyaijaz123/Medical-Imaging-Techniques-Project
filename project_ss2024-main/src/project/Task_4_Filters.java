package project;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.*;

public class Task_4_Filters implements PlugInFilter {

    protected int[][] SobelX = {{1,0,-1},{2,0,-2},{1,0,-1}};
    protected int[][] SobelY = {{1,2,1},{0,0,0},{-1,-2,-1}};

    protected int[][] ScharrX = {{47,0,-47},{162,0,-162},{47,0,-47}};
    protected int[][] ScharrY = {{47,162,47},{0,0,0},{-47,-162,-47}};

    protected int[][] PrewittX = {{1,0,-1},{1,0,-1},{1,0,-1}};
    protected int[][] PrewittY = {{1,1,1},{0,0,0},{-1,-1,-1}};

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        GenericDialog filterSelectionDialog = new GenericDialog("Edge Detection Filters");
        String[] Filters = {"Sobel","Scharr","Prewitt"};

        filterSelectionDialog.addChoice("Select Kernel Filter:", Filters, Filters[0]);
        filterSelectionDialog.showDialog();

        if (filterSelectionDialog.wasCanceled()) {
            IJ.error("Filter selection dialog was cancelled");
            return;
        }

        int [][] kernelX = new int [3][3];
        int [][] kernelY = new int [3][3];

       String kernelChoice = filterSelectionDialog.getNextChoice();

       if (kernelChoice.equals("Sobel")) {
           System.out.println("Sobel called");
           kernelX = SobelX;
           kernelY = SobelY;
       }
        if (kernelChoice.equals("Scharr")) {
           System.out.println("Scharr called");
           kernelX = ScharrX;
           kernelY = ScharrY;
       }
       else if (kernelChoice.equals("Prewitt")) {
           System.out.println("Prewitt called");
           kernelX = PrewittX;
           kernelY = PrewittY;
       }

       FloatProcessor imageFloatIp = imageProcessor.duplicate().convertToFloatProcessor();

       //image derivative calculation
       FloatProcessor imageDerivativeX = applyFilter(imageFloatIp,kernelX);
       FloatProcessor imageDerivativeY = applyFilter(imageFloatIp,kernelY);

        //image gradient calculation
       FloatProcessor gradientImage = getGradient(imageDerivativeX,imageDerivativeY);

       ImagePlus resultedGradientImage = new ImagePlus("Edge Detection using " + kernelChoice + " Filter: ", gradientImage);
       resultedGradientImage.show();
    }

    public FloatProcessor applyFilter (FloatProcessor In, int[][] kernel){

        int imageHeight = In.getHeight();
        int imageWidth = In.getWidth();

        FloatProcessor filteredResult = new FloatProcessor(imageWidth, imageHeight);

        for(int y=0; y < imageHeight; y++){
            for(int x=0; x < imageWidth; x++){

                float sum = 0;

                for(int kernelY =-1; kernelY <= 1; kernelY++){
                    for(int kernelX =-1; kernelX <= 1; kernelX++) {
                        int imagePositionY = kernelY + y;
                        int imagePositionX = kernelX + x;

                        int kernelPositionY = kernelY + 1;
                        int kernelPositionX = kernelX + 1;

                        float pixelValue = In.getPixelValue(imagePositionX, imagePositionY);
                        sum += pixelValue * kernel[kernelPositionY][kernelPositionX];
                    }
                }
                filteredResult.putPixelValue(x,y,sum);
            }
        }

        return filteredResult;
    }

    public FloatProcessor getGradient (FloatProcessor In_X, FloatProcessor In_Y){

        int inYHeight = In_Y.getHeight();
        int inYWidth = In_Y.getWidth();

        int inXHeight = In_X.getHeight();
        int inXWidth = In_X.getWidth();

        if(inXWidth != inYWidth || inXHeight != inYHeight){
            throw new IllegalArgumentException("Input images X and Y are not the same size");
        }

        FloatProcessor imageGradient = new FloatProcessor(inXWidth, inYHeight);

        for(int y=0; y < inYHeight; y++){
            for(int x=0; x < inXWidth; x++){

                double gradientXValue = In_X.getPixelValue(x,y);
                double gradientYValue = In_Y.getPixelValue(x,y);

                double gradientXSquared = gradientXValue * gradientXValue;
                double gradientYSquared = gradientYValue * gradientYValue;

                double gradientSum = gradientXSquared + gradientYSquared;
                float gradient = (float) Math.sqrt(gradientSum);

                imageGradient.putPixelValue(x,y,gradient);
            }
        }
        return imageGradient;
    }
}
