package classifier_ui;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class CFSettingsPlayer extends CFSettings {
    private static final String FILE_NAME = "classifier_settings_player.xml";
    @Override
    protected String m_getSettingFileName() {
        return FILE_NAME;
    }

    @Override
    public String getCascadeXmlPath()
    {
        if (featureTypeIndex == 0) {
             return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_player\\cascade_haar\\cascade.xml";
        }
        else {
            return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_player\\cascade_lbp\\cascade.xml";
        }
    }

    static public CFSettings load() throws java.io.IOException {
        XMLDecoder dec = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(FILE_NAME)));
        CFSettingsPlayer cs = (CFSettingsPlayer)dec.readObject();
        dec.close();
        return cs;
    }
}
