package opencv_client;

import classifier_ui.*;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.ArrayList;

public class CFFacadeChip extends CFFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    protected CFResult m_createResult(ArrayList<Rectangle>rects, ArrayList<Rectangle>fullRects)
    {
        return new CFResultChip(rects, fullRects);
    }
    @Override
    protected CFSettings m_createSettings(boolean isLoad) throws IOException
    {
        if (isLoad) {
            return CFSettingsChip.load();
        }
        else {
            return new CFSettingsChip();
        }
    }
}
