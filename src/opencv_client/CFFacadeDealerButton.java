package opencv_client;

import classifier_ui.CFResult;
import classifier_ui.CFResultDealerButton;
import classifier_ui.CFSettings;
import classifier_ui.CFSettingsDealerButton;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.ArrayList;

public class CFFacadeDealerButton extends CFFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    protected CFResult m_createResult(ArrayList<Rectangle>rects, ArrayList<Rectangle>fullRects) throws Exception
    {
        return new CFResultDealerButton(rects, fullRects);
    }

    @Override
    protected CFSettings m_createSettings(boolean isLoad) throws IOException
    {
        if (isLoad) {
            return CFSettingsDealerButton.load();
        }
        else {
            return new CFSettingsDealerButton();
        }
    }
}
