package tess4j_client;

public class StrRectangle extends javafx.scene.shape.Rectangle {
    public String m_char;

    public String getChar() {
        return m_char;
    }
    public void setChar(String val) {
        m_char = val;
    }

    public StrRectangle(String chr, double x, double y, double width, double height) {
        super(x, y, width, height);
        m_char = chr;
    }

    public String toBoxString(int bottom) {
        return String.format("%s %d %d %d %d", m_char, (int)getX(), (int)(bottom - (getY() + getHeight())),
                (int)(getX() + getWidth()), (int)(bottom - getY()));
    }
}
