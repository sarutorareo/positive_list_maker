package classifier_ui;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class CFSettingsDealerButton extends CFSettings {
    private static final String FILE_NAME = "classifier_settings_dealerButton.xml";
    @Override
    protected String m_getSettingFileName() {
        return FILE_NAME;
    }

    @Override
    public String getCascadeXmlPath()
    {
        if (featureTypeIndex == 0) {
             return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_dealerButton\\cascade_haar\\cascade.xml";
        }
        else {
            return "D:\\MyProgram\\GitHub\\positive_list_maker\\train_dealerButton\\cascade_lbp\\cascade.xml";
        }
    }

    static public CFSettings load() throws java.io.IOException {
        XMLDecoder dec = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(FILE_NAME)));
        CFSettingsDealerButton cs = (CFSettingsDealerButton)dec.readObject();
        dec.close();
        return cs;
    }
}
