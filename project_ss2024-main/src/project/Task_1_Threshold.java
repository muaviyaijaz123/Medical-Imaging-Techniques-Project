package project;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

public class Task_1_Threshold implements PlugInFilter {
    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
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
            ipCopy = correctIllumination(ip);
        } else {
            ipCopy = ip;
        }
        // threshold the image
        ByteProcessor thresholdedIp = threshold(ipCopy, threshold);
        ImagePlus thresholdedImage = new ImagePlus("Thresholded Image", thresholdedIp);
        thresholdedImage.show();
    }

    public ByteProcessor threshold ( ImageProcessor ip , int threshold ){

        int imageHeight = ip.getHeight();
        int imageWidth = ip.getWidth();

        ByteProcessor thresholdImage = new ByteProcessor(imageWidth, imageHeight);
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int imagePixel = ip.getPixel(x, y);
                if (imagePixel > threshold) {
                    thresholdImage.putPixel(x,y, 255);
                }
                else{
                    thresholdImage.putPixel(x,y, 0);
                }
            }
        }

        return thresholdImage;

    }

    public ByteProcessor correctIllumination ( ImageProcessor ip ){

        FloatProcessor originalFloatIp = ip.duplicate().convertToFloatProcessor();
        FloatProcessor floatIp = ip.duplicate().convertToFloatProcessor();

        floatIp.blurGaussian(75);

        int imageHeight =  floatIp.getHeight();
        int imageWidth = floatIp.getWidth();

        FloatProcessor result = new FloatProcessor(imageWidth, imageHeight);

        for (int y = 0; y < imageHeight ; y++) {
            for (int x = 0; x < imageWidth; x++) {

                double originalPixel = originalFloatIp.getPixelValue(x, y);
                double blurredPixel = floatIp.getPixelValue(x, y);

                double resultFloatPixel = originalPixel / blurredPixel;
                result.putPixelValue(x,y, resultFloatPixel);
            }
        }

        return result.convertToByteProcessor();
    }
}
