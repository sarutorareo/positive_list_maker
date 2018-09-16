package utils;

import classifier_ui.CFResult;
import classifier_ui.CFSettings;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import static org.opencv.highgui.HighGui.WINDOW_AUTOSIZE;
import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class ImageUtils {
    public static int a(int c){
        return c>>>24;
    }
    public static int r(int c){
        return c>>16&0xff;
    }
    public static int g(int c){
        return c>>8&0xff;
    }
    public static int b(int c){
        return c&0xff;
    }

    static public Image bufferedImageToFxImage(BufferedImage bImage) {
        return SwingFXUtils.toFXImage(bImage,null);
    }

    static public Image toBinaryFxImage(Image fxImage, int threshold)
    {
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        BufferedImage binImage = toBinImage(bImage, threshold);
        return bufferedImageToFxImage(binImage);
    }

    static public BufferedImage toBinImage(BufferedImage readImage, int threshold) {
        int w = readImage.getWidth();
        int h = readImage.getHeight();

        BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = readImage.getRGB(x, y);
                int mono = (int) (0.299 * r(c) + 0.587 * g(c) + 0.114 * b(c));

                if (mono > threshold) {
                    write.setRGB(x, y, 0xFFFFFF);
                } else {
                    write.setRGB(x, y, 0x000000);
                }
            }
        }
        return write;
    }
    /**
     * BufferedImage型（TYPE_3BYTE_RGB）をMat型（CV_8UC3）に変換します
//     * @param image 変換したいBufferedImage型
     * @return 変換したMat型
    */
    public static Mat bufferedImageToMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }

        // Create a buffered image
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

        // Draw the image onto the new buffer
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        }
        finally {
            g.dispose();
        }

        return image;
    }

    public static Mat fxImageToMat( Image image) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        bImage = toBufferedImageOfType(bImage, BufferedImage.TYPE_3BYTE_BGR);

        return bufferedImageToMat( bImage);
    }

    static public void saveTiff(BufferedImage bImage) {
        try {
            ImageWriterSpi tiffspi = new TIFFImageWriterSpi();
            TIFFImageWriter writer = (TIFFImageWriter) tiffspi.createWriterInstance();

            // TIFFImageWriteParam param = (TIFFImageWriteParam) writer.getDefaultWriteParam();
            TIFFImageWriteParam param = new TIFFImageWriteParam(Locale.US);

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("LZW");

            File fOutputFile = new File("d:\\temp\\out.tiff");
            OutputStream fos = new BufferedOutputStream(new FileOutputStream(fOutputFile));
            ImageOutputStream ios = ImageIO.createImageOutputStream(fos);

            writer.setOutput(ios);
            writer.write(null, new IIOImage(bImage, null, null), param);

            ios.flush();
            writer.dispose();
            ios.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

}
