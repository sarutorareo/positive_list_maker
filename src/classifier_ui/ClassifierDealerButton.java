package classifier_ui;

import groovy.transform.PackageScope;
import opencv_client.CFFacade;
import opencv_client.CFFacadeDealerButton;

public class ClassifierDealerButton extends Classifier {
    @Override
    protected CFFacade m_createFacade() {
        return new CFFacadeDealerButton();
    }
    @Override
    protected CFResult m_createDefaultCFResult() {
        return new CFResultDealerButton();
    }

    @Override
    @PackageScope
    String getDataDir() {
        return "./train_dealerButton/pos";
    }
}
