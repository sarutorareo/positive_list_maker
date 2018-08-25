package opencv_client;

import application.ClassifierSettings;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import static org.opencv.highgui.HighGui.WINDOW_AUTOSIZE;
import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

import javafx.scene.image.Image;

public class CascadeClassify {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Rect[] classify(String img_path, String cascadeXmlPath,
                           int minNeighbors, double scaleFactor,
                           Size minSize, Size maxSize) {
        Mat mat_src = Imgcodecs.imread(img_path);                          // 入力画像の読み込み

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
}
