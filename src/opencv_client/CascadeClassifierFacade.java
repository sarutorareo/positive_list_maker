package opencv_client;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import static org.opencv.highgui.HighGui.WINDOW_AUTOSIZE;
import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class CascadeClassifierFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ArrayList<Rectangle> classify(javafx.scene.image.Image fxImage, String cascadeXmlPath,
                                         int minNeighbors, double scaleFactor,
                                         Size minSize, Size maxSize, boolean removeDuplicate) {

        Mat mat_src = fxImageToMat(fxImage);
        HighGui.namedWindow("wnd_in", WINDOW_AUTOSIZE);

        /* 検出器のロード */
        CascadeClassifier face_cascade = new CascadeClassifier( cascadeXmlPath);

        MatOfRect detects = new MatOfRect();
        face_cascade.detectMultiScale(mat_src, detects, scaleFactor, minNeighbors, CASCADE_SCALE_IMAGE, minSize, maxSize);
        System.out.println("faces.size = " + detects.size().toString());

        HighGui.destroyAllWindows();

        Rect[] rects = detects.toArray();
        ArrayList<Rectangle> la = new ArrayList<Rectangle>();
        for(int i = 0; i < rects.length; i++) {
            Rect r = rects[i];
            la.add(m_rectToRectanble(r));
        }

        // 重複している領域は削除
        if (removeDuplicate) {
            la = removeDuplicatedRects(la);
        }
        return la;
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

    public static Mat fxImageToMat( javafx.scene.image.Image image) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        bImage = toBufferedImageOfType(bImage, BufferedImage.TYPE_3BYTE_BGR);

        return bufferedImageToMat( bImage);
    }

    public boolean isDuplicated(Rectangle rectA, Rectangle rectB) {
        double areaA = rectA.getWidth() * rectA.getHeight();
        double areaB = rectB.getWidth() * rectB.getHeight();
        Rectangle duplicated = duplicatedRectangle(rectA, rectB);
        if (duplicated == null) {
            return false;
        }
        double duplicatedArea = duplicated.getWidth() * duplicated.getHeight();

        return (duplicatedArea > areaA / 2) && (duplicatedArea > areaB / 2);
    }

    public Rectangle duplicatedRectangle(Rectangle rectA, Rectangle rectB) {
        if (rectA.getX() + rectA.getWidth() - 1 < rectB.getX()) {
            return null;
        }
        if (rectB.getX() + rectB.getWidth() - 1 < rectA.getX()) {
            return null;
        }
        if (rectA.getY() + rectA.getHeight() - 1 < rectB.getY()) {
            return null;
        }
        if (rectB.getY() + rectB.getHeight() - 1 < rectA.getY()) {
            return null;
        }
        double left = Math.max(rectA.getX(), rectB.getX());
        double right = Math.min(rectA.getX() + rectA.getWidth() - 1, rectB.getX() + rectB.getWidth() -1);
        double top = Math.max(rectA.getY(), rectB.getY());
        double bottom = Math.min(rectA.getY() + rectA.getHeight() - 1, rectB.getY() + rectB.getHeight() -1);

        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    public boolean existsDuplicatedRectangle(ArrayList<Rectangle> list, Rectangle rect) {
        for (Rectangle rectangle : list) {
            if (isDuplicated(rectangle, rect)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Rectangle> removeDuplicatedRects(final ArrayList<Rectangle> list) {
        ArrayList<Rectangle> newList = new ArrayList<Rectangle>();
        list.forEach( r-> {
            if (!existsDuplicatedRectangle(newList, r)) {
                newList.add(r);
            }
        });
        return newList;
    }

    private Rectangle m_rectToRectanble(Rect r) {
        Rectangle result = new Rectangle(r.x, r.y, r.width, r.height);
        result.setFill(Color.TRANSPARENT);
        return result;
    }

    public boolean isSamePosSize(Rectangle r1, Rectangle r2) {
        return ((r1.getX() == r2.getX()) && (r1.getY() == r2.getY())
                && (r1.getWidth() == r2.getWidth()) && (r1.getHeight() == r2.getHeight()));
    }
}
