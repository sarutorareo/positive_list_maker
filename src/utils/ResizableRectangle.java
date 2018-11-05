package utils;

import classifier_ui.EnCorner;
import javafx.scene.Cursor;
import javafx.scene.shape.Rectangle;

public class ResizableRectangle extends Rectangle {
    EnCorner m_draggingCorner = EnCorner.NONE;
    public EnCorner getDraggingCorner() {
        return m_draggingCorner;
    }
    public void setDraggingCorner(EnCorner c) {
        m_draggingCorner = c;
    }

    public ResizableRectangle(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public EnCorner getCorner(int x, int y)
    {
        int CORNER_SIZE = 5;
        if (x < 0 || y < 0 || x > this.getWidth() || y > this.getHeight()) {
            return EnCorner.NONE;
        }

        if ((x < CORNER_SIZE) && (y < CORNER_SIZE)) {
            return EnCorner.NW;
        }
        if ((x >= this.getWidth() - CORNER_SIZE) && (y < CORNER_SIZE)) {
            return EnCorner.NE;
        }
        if ((x >= this.getWidth() - CORNER_SIZE) && (y >= this.getHeight() - CORNER_SIZE)) {
            return EnCorner.SE;
        }
        if ((x < CORNER_SIZE) && (y >= this.getHeight() - CORNER_SIZE)) {
            return EnCorner.SW;
        }

        return EnCorner.NONE;
    }

    public void setCursorByPoint(int x, int y) {
        EnCorner c = getCorner(x, y);
        Cursor csr;
        switch(c) {
            case NW:
                csr = Cursor.NW_RESIZE;
                break;
            case NE:
                csr = Cursor.NE_RESIZE;
                break;
            case SE:
                csr = Cursor.SE_RESIZE;
                break;
            case SW:
                csr = Cursor.SW_RESIZE;
                break;
            case NONE:
            default:
                csr = Cursor.MOVE;
                break;
        }
        setCursor(csr);
    }

    public void resizeByDragging(double mouseX, double mouseY, double parentWidth, double parentHeight)
    {
    }
}
