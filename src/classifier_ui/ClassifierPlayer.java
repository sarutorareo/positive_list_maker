package classifier_ui;

import groovy.transform.PackageScope;
import opencv_client.CFFacade;
import opencv_client.CFFacadePlayer;

public class ClassifierPlayer extends Classifier {
    @Override
    protected CFFacade m_createFacade() {
        return new CFFacadePlayer();
    }

    @Override
    protected CFResult m_createDefaultCFResult() {
        return new CFResultPlayer();
    }

    @Override
    @PackageScope
    String getDataDir() {
        return "./train_player/pos";
    }

}
