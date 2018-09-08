package classifier_ui;

import groovy.transform.PackageScope;
import org.opencv.core.Size;
import java.beans.XMLEncoder;
import java.io.*;

abstract public class CFSettings implements Serializable {
    abstract protected String m_getSettingFileName();

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

    @PackageScope
    void save() throws java.io.IOException {
        XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(
                        new FileOutputStream(m_getSettingFileName())));
        encoder.writeObject(this);
        encoder.close();
    }

    abstract public String getCascadeXmlPath();
}
