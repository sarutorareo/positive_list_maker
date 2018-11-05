package classifier_ui;

import groovy.transform.PackageScope;
import opencv_client.CFFacade;
import opencv_client.CFFacadeChip;
import opencv_client.CFFacadePlayer;

public class ClassifierChip extends Classifier {
    @Override
    protected CFFacade m_createFacade() {
        return new CFFacadeChip();
    }

    @Override
    protected CFResult m_createDefaultCFResult() {
        return new CFResultChip();
    }

    @Override
    @PackageScope
    String getDataDir() {
        return "./train_chip/pos";
    }

}
