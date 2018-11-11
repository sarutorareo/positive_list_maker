package utils;

import classifier_ui.EnEdge;
import javafx.scene.Cursor;
import javafx.scene.shape.Rectangle;

public class ResizableRectangle extends Rectangle {
    EnEdge m_draggingCorner = EnEdge.NONE;
    public EnEdge getDraggingCorner() {
        return m_draggingCorner;
    }
    public void setDraggingCorner(EnEdge c) {
        m_draggingCorner = c;
    }

    public ResizableRectangle(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public EnEdge getEdge(int x, int y, boolean isCorner)
    {
        int CORNER_SIZE = 5;

        if (x < 0 || y < 0 || x > this.getWidth() || y > this.getHeight()) {
            return EnEdge.NONE;
        }

        if (isCorner) {
            if ((x < CORNER_SIZE) && (y < CORNER_SIZE)) {
                return EnEdge.NW;
            }
            if ((x >= this.getWidth() - CORNER_SIZE) && (y < CORNER_SIZE)) {
                return EnEdge.NE;
            }
            if ((x >= this.getWidth() - CORNER_SIZE) && (y >= this.getHeight() - CORNER_SIZE)) {
                return EnEdge.SE;
            }
            if ((x < CORNER_SIZE) && (y >= this.getHeight() - CORNER_SIZE)) {
                return EnEdge.SW;
            }
            if (x < (this.getWidth() / 2)) {
                if (y < (this.getHeight() / 2)) {
                    return EnEdge.NW;
                }
                else {
                    return EnEdge.SW;
                }
            }
            else {
                if (y < (this.getHeight() / 2)) {
                    return EnEdge.NE;
                }
                else {
                    return EnEdge.SE;
                }
            }
        }
        else {
            // Rectangleが横長である事を前提として、横優先
            if (x < CORNER_SIZE) {
                return EnEdge.W;
            }
            if (x >= (this.getWidth() - CORNER_SIZE)) {
                return EnEdge.E;
            }
            if (y < CORNER_SIZE) {
                return EnEdge.N;
            }
            if (y >= (this.getHeight() - CORNER_SIZE)) {
                return EnEdge.S;
            }
        }

        return EnEdge.NONE;
    }

    public void setCursorByPoint(int x, int y, boolean isCorner) {
        EnEdge c = getEdge(x, y, isCorner);
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
            case N:
                csr = Cursor.N_RESIZE;
                break;
            case W:
                csr = Cursor.W_RESIZE;
                break;
            case S:
                csr = Cursor.S_RESIZE;
                break;
            case E:
                csr = Cursor.E_RESIZE;
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
