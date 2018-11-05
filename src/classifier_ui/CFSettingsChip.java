package classifier_ui;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class CFSettingsChip extends CFSettings {
    private static final String FILE_NAME = "classifier_settings_chip.xml";

    public CFSettingsChip() {
        super();
    }

    @Override
    protected String m_getSettingFileName() {
        return FILE_NAME;
    }

    static public CFSettings load() throws java.io.IOException {
        XMLDecoder dec = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(FILE_NAME)));
        CFSettingsChip cs = (CFSettingsChip)dec.readObject();
        dec.close();
        return cs;
    }

    @Override
    public String getCascadeXmlPath()
    {
        if (featureTypeIndex == 0) {
            return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_chip\\cascade_haar\\cascade.xml";
        }
        else {
            return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_chip\\cascade_lbp\\cascade.xml";
        }
    }
}
