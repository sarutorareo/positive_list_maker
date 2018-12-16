package classifier_ui;

import groovy.transform.PackageScope;
import opencv_client.*;

public class ClassifierChip extends Classifier {
    @Override
    protected CFFacade m_createFacade() {
        // return new CFFacadeChip();
        return new CFFacadeChipByColor();
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
