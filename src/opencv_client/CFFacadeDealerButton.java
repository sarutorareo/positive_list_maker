package opencv_client;

import classifier_ui.CFResult;
import classifier_ui.CFResultDealerButton;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Core;

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
}
