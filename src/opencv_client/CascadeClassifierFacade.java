package opencv_client;

import application.ClassifierSettings;
import javafx.embed.swing.SwingFXUtils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import static org.opencv.highgui.HighGui.WINDOW_AUTOSIZE;
import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class CascadeClassifierFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Rect[] classify(javafx.scene.image.Image fxImage, String cascadeXmlPath,
                           int minNeighbors, double scaleFactor,
                           Size minSize, Size maxSize) {

        Mat mat_src = fxImageToMat(fxImage);
        HighGui.namedWindow("wnd_in", WINDOW_AUTOSIZE);

        /* 検出器のロード */
        CascadeClassifier face_cascade = new CascadeClassifier( cascadeXmlPath);

        MatOfRect detects = new MatOfRect();
        face_cascade.detectMultiScale(mat_src, detects, scaleFactor, minNeighbors, CASCADE_SCALE_IMAGE, minSize, maxSize);
        System.out.println("faces.size = " + detects.size().toString());

        Rect[] rects = detects.toArray();
        for(int i = 0; i < rects.length; i++) {
            Rect r = rects[i];
            Imgproc.rectangle(mat_src, new Point(r.x, r.y), new Point(r.x+r.width, r.y + r.height), new Scalar(0, 0, 255));
        }

        /*
        HighGui.imshow("wnd_in", mat_src);
        HighGui.waitKey(0);
        */
        HighGui.destroyAllWindows();

        return detects.toArray();
    }

    /**
     * BufferedImage型（TYPE_3BYTE_RGB）をMat型（CV_8UC3）に変換します
//     * @param image 変換したいBufferedImage型
     * @return 変換したMat型
    */
    public static Mat bufferedImageToMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.out.println("bufferedimage:" + data.length);
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

    public static Mat fxImageToMat( javafx.scene.image.Image image) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        bImage = toBufferedImageOfType(bImage, BufferedImage.TYPE_3BYTE_BGR);

        return bufferedImageToMat( bImage);
    }

}
