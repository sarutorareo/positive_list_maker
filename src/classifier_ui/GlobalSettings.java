package classifier_ui;

import groovy.transform.PackageScope;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class GlobalSettings implements Serializable {
    private static final String FILE_NAME = "global_settings.xml";

    private int m_selectedTargetIndex = 0;

    public int getSelectedTargetIndex() {
        return m_selectedTargetIndex;
    }
    public void setSelectedTargetIndex(int val) {
        m_selectedTargetIndex = val;
    }

    static public GlobalSettings load() throws java.io.IOException {
        XMLDecoder dec = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(FILE_NAME)));
        GlobalSettings cs = (GlobalSettings)dec.readObject();
        dec.close();
        return cs;
    }

    @PackageScope
    void save() throws java.io.IOException {
        XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(
                        new FileOutputStream(FILE_NAME)));
        encoder.writeObject(this);
        encoder.close();
    }
}
