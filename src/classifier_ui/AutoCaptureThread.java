package classifier_ui;

import javafx.application.Platform;

public class AutoCaptureThread extends Thread {
    private ClassifierViewFormController m_cvfController;
    private boolean m_loopFlag = true;
    private int m_loopCount = 0;
    final private int SLEEP_M_SEC = 500;

    public AutoCaptureThread(ClassifierViewFormController cvfController) {
        m_cvfController = cvfController;
    }
    public synchronized void stopLoop() {
        m_loopFlag = false;
    }
    public void run() {
        m_loopCount = 0;
        while (m_loopFlag) {
            System.out.println("roop " + m_loopCount);

            ClassifyResult cr = m_cvfController.captureImageAndClassify();
            Platform.runLater(new Runnable() {
                public void run() {
                    m_cvfController.setResult(cr);
                }
            });
            try {
                sleep(SLEEP_M_SEC);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            m_loopCount++;
        }
    }
}
