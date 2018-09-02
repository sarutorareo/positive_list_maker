package opencv_client;

import classifier_ui.CFResult;
import classifier_ui.CFSettings;
import classifier_ui.CFSettingsPlayer;
import javafx.scene.shape.Rectangle;
import org.opencv.core.*;
import classifier_ui.CFResultPlayer;

import java.io.IOException;
import java.util.ArrayList;

public class CFFacadePlayer extends CFFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    protected CFResult m_createResult(ArrayList<Rectangle>rects, ArrayList<Rectangle>fullRects)
    {
        return new CFResultPlayer(rects, fullRects);
    }
    @Override
    protected CFSettings m_createSettings(boolean isLoad) throws IOException
    {
        if (isLoad) {
            return CFSettingsPlayer.load();
        }
        else {
            return new CFSettingsPlayer();
        }
    }
}
