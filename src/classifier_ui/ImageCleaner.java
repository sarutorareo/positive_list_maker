package classifier_ui;

import utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImageCleaner {
    public BufferedImage cleaning(BufferedImage readImage) {
//        BufferedImage writeImage = m_cleanVirtSingleDot(readImage);
//        bSubImage = m_cleanSingleDotLine(bSubImage);
        BufferedImage writeImage = m_cleanSideEdgePoint(readImage);
        return writeImage;
    }

    private BufferedImage m_cleanSideEdgePoint(BufferedImage readImage) {
        final int EDGE_WIDTH = 3;
        int w = readImage.getWidth();
        int h = readImage.getHeight();

        BufferedImage writeImage = ImageUtils.copyImage(readImage);

        // 右端
        int boundLeft = Math.max(0, w - EDGE_WIDTH);
        int boundRight = w-1;
        m_cleanSideRangePoint(readImage, writeImage, w, h, boundLeft, boundRight);
        // 左端
        boundLeft = 0;
        boundRight = Math.min(w-1, EDGE_WIDTH - 1);
        m_cleanSideRangePoint(readImage, writeImage, w, h, boundLeft, boundRight);

        return writeImage;
    }

    private void m_cleanSideRangePoint(BufferedImage readImage, BufferedImage writeImage, int w, int h, int boundLeft, int boundRight) {
        boolean[][] bArray = new boolean[w][h];
        for (int x = boundLeft; x <= boundRight ; x++) {
            for (int y = 0; y < h; y++) {
                // -1が白, 0が黒
                int c = readImage.getRGB(x, y);
                if ((c != -1) && !bArray[x][y]) {
                    boolean[][] bCurrentArray = new boolean[w][h];
                    if (!m_isDigitPoint(bCurrentArray, readImage, x, y, boundLeft, boundRight)) {
                        m_paint(bCurrentArray, writeImage);
                    }
                    m_marge(bArray, bCurrentArray);
                }
            }
        }
    }

    private void m_marge(boolean[][] bArrayDst, boolean[][] bArraySrc) {
        for (int x = 0; x < bArraySrc.length; x++) {
            for (int y = 0; y < bArraySrc[x].length; y++) {
                if (bArraySrc[x][y]) {
                    bArrayDst[x][y] = bArraySrc[x][y];
                }
            }
        }
    }

    private void m_paint(boolean[][] bArray, BufferedImage image) {
        for (int x = 0; x < bArray.length; x++) {
            for (int y = 0; y < bArray[x].length; y++) {
                if (bArray[x][y]) {
                    image.setRGB(x, y, -1);
                }
            }
        }
    }

    private void m_isDigitPoint_rec(boolean[][] bArray, BufferedImage readImage, int x, int y, int edgeLeft, int edgeRight) {
        if ( (x < 0 || x >= bArray.length)
            || (y < 0 || y >= bArray[x].length)) {
            return;
        }
        if (bArray[x][y]) {
            return;
        }

        int c = readImage.getRGB(x, y);
        if (c == -1) {
            return;
        }
        bArray[x][y] = true;

        if ( (x < edgeLeft) || (x > edgeRight) ) {
            return;
        }

        // 左
        m_isDigitPoint_rec(bArray, readImage, x-1, y, edgeLeft, edgeRight);
        // 右
        m_isDigitPoint_rec(bArray, readImage, x+1, y, edgeLeft, edgeRight);
        // 上
        m_isDigitPoint_rec(bArray, readImage, x, y-1, edgeLeft, edgeRight);
        // 下
        m_isDigitPoint_rec(bArray, readImage, x, y+1, edgeLeft, edgeRight);
    }

    private boolean m_isDigitPoint(boolean[][] bArray, BufferedImage readImage, int x, int y, int edgeLeft, int edgeRight)
    {
        m_isDigitPoint_rec(bArray, readImage, x, y, edgeLeft, edgeRight);
        for (int resultX = 0; resultX < bArray.length; resultX++) {
            for (int resultY = 0; resultY < bArray[0].length; resultY++) {
                if (bArray[resultX][resultY] &&
                    (resultX < edgeLeft || resultX > edgeRight)
                        ) {
                    return true;
                }
            }
        }
        return false;
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
