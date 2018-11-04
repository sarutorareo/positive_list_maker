package classifier_ui;

import java.awt.image.BufferedImage;

public class ImageCleaner {
    public BufferedImage cleaning(BufferedImage readImage) {
        BufferedImage writeImage = m_cleanVirtSingleDot(readImage);
//        bSubImage = m_cleanSingleDotLine(bSubImage);
        return writeImage;
    }

    private BufferedImage m_cleanVirtSingleDot(BufferedImage readImage) {
        int w = readImage.getWidth();
        int h = readImage.getHeight();

        BufferedImage write = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < w; x++) {
            boolean isSingleDot = m_isSingleDot(readImage, x, h);
            for (int y = 0; y < h; y++) {
                int c = readImage.getRGB(x, y);
                if (isSingleDot && (c != -1)) {
                    c = -1;
                    for (int x2 = x; x2 < w; x2++) {
                        int c2 = readImage.getRGB(x2, y);
                        if (c2 == -1) {
                            break;
                        }
                        readImage.setRGB(x2, y, c);
                    }
                }
                write.setRGB(x, y, c);
            }
        }
        return write;
    }
    private boolean m_isSingleDot(BufferedImage readImage, int x, int height) {
        boolean isSingleDot = false;
        for (int y = 0; y < height; y++) {
            int c = readImage.getRGB(x, y);
            if (c != -1) {
                // 2個目なら
                if (isSingleDot) {
                    return false;
                }
                // 1個目なら
                else {
                    isSingleDot = true;
                }
            }
        }
        return isSingleDot;
    }
}
