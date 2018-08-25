package application;

import com.sun.xml.internal.txw2.output.XmlSerializer;
import org.opencv.core.Size;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class ClassifierSettings implements Serializable {
    private static final String FILE_NAME = "classifier_settings.xml";

    public int featureTypeIndex = 0;
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
        XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(
                        new FileOutputStream(FILE_NAME)));
        encoder.writeObject(this);
        encoder.close();
    }
    static public ClassifierSettings load() throws java.io.IOException {
        XMLDecoder dec = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(FILE_NAME)));
        ClassifierSettings cs = (ClassifierSettings)dec.readObject();
        dec.close();
        return cs;
    }

    public String getCascadeXmlPath()
    {
        if (featureTypeIndex == 0) {
             return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_player\\cascade_haar\\cascade.xml";
        }
        else {
            return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_player\\cascade_lbp\\cascade.xml";
        }
    }
}
