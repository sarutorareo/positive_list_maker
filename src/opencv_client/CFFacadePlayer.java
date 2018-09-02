package opencv_client;

import classifier_ui.CFResult;
import javafx.scene.shape.Rectangle;
import org.opencv.core.*;
import classifier_ui.CFResultPlayer;

import java.util.ArrayList;

public class CFFacadePlayer extends CFFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    protected CFResult m_createResult(ArrayList<Rectangle>rects, ArrayList<Rectangle>fullRects) throws Exception
    {
        return new CFResultPlayer(rects, fullRects);
    }
}
