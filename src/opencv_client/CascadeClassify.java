package opencv_client;

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
/*
    public static void main(String[] args) {
        String path_in = "d:/temp/test_in.bmp";
        String path_out = "d:/temp/test_out.bmp";

        Mat mat_src = new Mat();
        Mat mat_dst = new Mat();

        mat_src = Imgcodecs.imread(path_in);                          // 入力画像の読み込み
        HighGui.namedWindow("wnd_in", WINDOW_AUTOSIZE);
        HighGui.imshow("wnd_in", mat_src);
        Imgproc.cvtColor(mat_src, mat_dst, Imgproc.COLOR_BGR2GRAY); // カラー画像をグレー画像に変換
        Imgcodecs.imwrite(path_out, mat_dst);                         // 出力画像を保存
        HighGui.namedWindow("wnd_out", WINDOW_AUTOSIZE);
        HighGui.imshow("wnd_out", mat_dst);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
    }
*/

    public static void main(String[] args) {
        String path_in = "d:/temp/stars.png";

        Mat mat_src = new Mat();
        Mat mat_dst = new Mat();

        mat_src = Imgcodecs.imread(path_in);                          // 入力画像の読み込み
        HighGui.namedWindow("wnd_in", WINDOW_AUTOSIZE);
        HighGui.imshow("wnd_in", mat_src);

        Imgproc.cvtColor(mat_src, mat_dst, Imgproc.COLOR_BGR2GRAY); // カラー画像をグレー画像に変換

        /* 正面顔検出器のロード */
//        CascadeClassifier face_cascade = new CascadeClassifier( "C:\\OpenCV\\build\\etc\\haarcascades\\haarcascade_frontalface_default.xml" );
        CascadeClassifier face_cascade = new CascadeClassifier( "D:\\MyProgram\\GitHub\\OpenCV\\training\\train_player\\cascade\\cascade.xml");

        MatOfRect faces = new MatOfRect();
        int minNeighbors = 3;
        double scaleFactor = 1.2;
        face_cascade.detectMultiScale(mat_src, faces, scaleFactor, minNeighbors, CASCADE_SCALE_IMAGE, new Size(1, 1), new Size(3000, 3000));
        System.out.println("faces.size = " + faces.size().toString());
        Rect[] rects = faces.toArray();
        for(int i = 0; i < rects.length; i++) {
            Rect r = rects[i];
            Imgproc.rectangle(mat_src, new Point(r.x, r.y), new Point(r.x+r.width, r.y + r.height), new Scalar(0, 0, 255));
        }

        /*
        for (x,y,w,h) in faces:
        cv.rectangle(img,(x,y),(x+w,y+h),(255,0,0),2)

        roi_gray = gray[y:y+h, x:x+w]
        roi_color = img[y:y+h, x:x+w]
        // me
        eyes = eye_cascade.detectMultiScale(roi_gray)
        for (ex,ey,ew,eh) in eyes:
        cv.rectangle(roi_color,(ex,ey),(ex+ew,ey+eh),(0,255,0),2)
        */

        HighGui.namedWindow("wnd_out", WINDOW_AUTOSIZE);
        HighGui.imshow("wnd_out", mat_dst);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
    }

    public Rect[] classify(String img_path ) {
        Mat mat_src = Imgcodecs.imread(img_path);                          // 入力画像の読み込み

        HighGui.namedWindow("wnd_in", WINDOW_AUTOSIZE);

        /* 正面顔検出器のロード */
        CascadeClassifier face_cascade = new CascadeClassifier( "D:\\MyProgram\\GitHub\\OpenCV\\training\\train_player\\cascade\\cascade.xml");

        MatOfRect faces = new MatOfRect();
        int minNeighbors = 3;
        double scaleFactor = 1.2;
        face_cascade.detectMultiScale(mat_src, faces, scaleFactor, minNeighbors, CASCADE_SCALE_IMAGE, new Size(1, 1), new Size(3000, 3000));
        System.out.println("faces.size = " + faces.size().toString());

        Rect[] rects = faces.toArray();
        for(int i = 0; i < rects.length; i++) {
            Rect r = rects[i];
            Imgproc.rectangle(mat_src, new Point(r.x, r.y), new Point(r.x+r.width, r.y + r.height), new Scalar(0, 0, 255));
        }

        /*
        HighGui.imshow("wnd_in", mat_src);
        HighGui.waitKey(0);
        */
        HighGui.destroyAllWindows();

        return faces.toArray();
    }
}
