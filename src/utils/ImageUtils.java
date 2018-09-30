package utils;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;
import groovy.transform.PackageScope;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.*;

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
import java.util.Locale;

public class ImageUtils {
    private static int a(int c) {
        return c>>>24;
    }
    private static int r(int c) {
        return c>>16&0xff;
    }
    private static int g(int c) {
        return c>>8&0xff;
    }
    private static int b(int c){
        return c&0xff;
    }

    static public Image bufferedImageToFxImage(BufferedImage bImage) {
        return SwingFXUtils.toFXImage(bImage,null);
    }

    static public Image toReverceBinaryFxImage(Image fxImage)
    {
        return toBinaryFxImage(fxImage, 80, true);
    }

    static public Image toBinaryFxImage(Image fxImage, int threshold, boolean fgReverce)
    {
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        BufferedImage binImage = toBinImage(bImage, threshold, fgReverce);
        return bufferedImageToFxImage(binImage);
    }

    static public Image toGrayScaleFxImage(Image fxImage)
    {
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        BufferedImage binImage = toGrayScaleImage(bImage);
        return bufferedImageToFxImage(binImage);
    }


    @PackageScope
    static BufferedImage toBinImage(BufferedImage readImage, int threshold, boolean fgReverce) {
        int w = readImage.getWidth();
        int h = readImage.getHeight();

        BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = readImage.getRGB(x, y);
                int mono = (int) (0.299 * r(c) + 0.587 * g(c) + 0.114 * b(c));

                if (fgReverce) {
                    if (mono > threshold) {
                        write.setRGB(x, y, 0x000000);
                    } else {
                        write.setRGB(x, y, 0xFFFFFF);
                    }
                }
                else {
                    if (mono > threshold) {
                        write.setRGB(x, y, 0xFFFFFF);
                    } else {
                        write.setRGB(x, y, 0x000000);
                    }
                }
            }
        }
        return write;
    }

    @PackageScope
    static BufferedImage toGrayScaleImage(BufferedImage readImage) {
        int w = readImage.getWidth();
        int h = readImage.getHeight();

        BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float r = new java.awt.Color(readImage.getRGB(x, y)).getRed();
                float g = new java.awt.Color(readImage.getRGB(x, y)).getGreen();
                float b = new java.awt.Color(readImage.getRGB(x, y)).getBlue();
                int grayScaled = 255 - (int)Math.min(255, ((r+g+b) * 2.0)/3);
//                int grayScaled = 255 - (int)Math.min(255, ((r+g+b) * 1.0)/3);
                grayScaled = (grayScaled >= 128)? 255: grayScaled;
                write.setRGB(x, y, new java.awt.Color(grayScaled, grayScaled, grayScaled).getRGB());
            }
        }
        return write;
    }

    /**
     * BufferedImage型（TYPE_3BYTE_RGB）をMat型（CV_8UC3）に変換します
//     * @param image 変換したいBufferedImage型
     * @return 変換したMat型
    */
    @PackageScope
    static Mat bufferedImageToMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    @PackageScope
    static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
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

    static public void saveTiff(String path, BufferedImage bImage) {
        try {
            ImageWriterSpi tiffspi = new TIFFImageWriterSpi();
            TIFFImageWriter writer = (TIFFImageWriter) tiffspi.createWriterInstance();

            // TIFFImageWriteParam param = (TIFFImageWriteParam) writer.getDefaultWriteParam();
            TIFFImageWriteParam param = new TIFFImageWriteParam(Locale.US);

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("LZW");

            File fOutputFile = new File(path);
            OutputStream fos = new BufferedOutputStream(new FileOutputStream(fOutputFile));
            ImageOutputStream ios = ImageIO.createImageOutputStream(fos);

            writer.setOutput(ios);
            writer.write(null, new IIOImage(bImage, null, null), param);

            ios.flush();
            writer.dispose();
            fos.close();
            ios.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    static public BufferedImage getRectSubImage(BufferedImage orgImage, javafx.scene.shape.Rectangle rect) {
        return orgImage.getSubimage((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
    }
}
