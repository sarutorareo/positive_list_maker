package application;

import org.opencv.core.Size;

import java.io.*;

public class ClassifierSettings implements Serializable {
    public int fetureTypeIndex = 0;
    public int minNeighbors = 0;
    public double scaleFactor = 1.01;
    private double minSizeWidth = 1;
    private double minSizeHeight = 1;
    private double maxSizeWidth = 9999;
    private double maxSizeHeight = 9999;

    public Size getMinSize() {
        return new Size(minSizeWidth, minSizeHeight);
    }
    public Size getMaxSize() {
        return new Size(maxSizeWidth, maxSizeHeight);
    }

    public void setMinSize(Size minSize) {
        minSizeWidth = minSize.width;
        minSizeHeight = minSize.height;
    }
    public void setMaxSize(Size maxSize) {
        maxSizeWidth = maxSize.width;
        maxSizeHeight = maxSize.height;
    }

    public void save() throws java.io.IOException {
        FileOutputStream outFile = new FileOutputStream("classifier_settings.dat");
        ObjectOutputStream outObject = new ObjectOutputStream(outFile);
        outObject.writeObject(this);
    }
    static public ClassifierSettings load() throws java.io.IOException, java.lang.ClassNotFoundException {
        FileInputStream inFile = new FileInputStream("classifier_settings.dat");
        ObjectInputStream inObject = new ObjectInputStream(inFile);
        return (ClassifierSettings)inObject.readObject();
    }
}
