package opencv_client;

import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CascadeClassifierFacadeTest {
    @org.junit.jupiter.api.Test
    void test_isSamePosSize() {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        Rectangle rectA = new Rectangle(0, 0, 100, 100);
        Rectangle rectB = new Rectangle(0, 0, 100, 100);
        assertEquals(true, cc.isSamePosSize(rectA, rectB));
        rectA = new Rectangle(0, 0, 100, 100);
        rectB = new Rectangle(1, 0, 100, 100);
        assertEquals(false, cc.isSamePosSize(rectA, rectB));
        rectA = new Rectangle(0, 1, 100, 100);
        rectB = new Rectangle(0, 0, 100, 100);
        assertEquals(false, cc.isSamePosSize(rectA, rectB));
        rectA = new Rectangle(0, 0, 101, 100);
        rectB = new Rectangle(0, 0, 100, 100);
        assertEquals(false, cc.isSamePosSize(rectA, rectB));
        rectA = new Rectangle(0, 0, 100, 100);
        rectB = new Rectangle(0, 0, 100, 99);
        assertEquals(false, cc.isSamePosSize(rectA, rectB));
    }

    @org.junit.jupiter.api.Test
    void test_isDuplicated() {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        Rectangle rectA = new Rectangle(0, 0, 100, 100);
        Rectangle rectB = new Rectangle(0, 0, 100, 100);
        assertEquals(true, cc.isDuplicated(rectA, rectB));

        rectA = new Rectangle(0, 0, 100, 100);
        rectB = new Rectangle(100, 100, 100, 100);
        assertEquals(false, cc.isDuplicated(rectA, rectB));

        rectA = new Rectangle(0, 0, 100, 100);
        rectB = new Rectangle(90, 90, 100, 100);
        assertEquals(false, cc.isDuplicated(rectA, rectB));

        rectA = new Rectangle(0, 0, 200, 100);
        rectB = new Rectangle(100, 0, 200, 100);
        assertEquals(false, cc.isDuplicated(rectA, rectB));
        rectA = new Rectangle(0, 0, 200, 100);
        rectB = new Rectangle(99, 0, 200, 100);
        assertEquals(true, cc.isDuplicated(rectA, rectB));
        rectA = new Rectangle(0, 0, 200, 100);
        rectB = new Rectangle(0, 50, 200, 100);
        assertEquals(false, cc.isDuplicated(rectA, rectB));
        rectA = new Rectangle(0, 0, 200, 100);
        rectB = new Rectangle(0, 49, 200, 100);
        assertEquals(true, cc.isDuplicated(rectA, rectB));
    }

    @org.junit.jupiter.api.Test
    void test_duplicatedRectangle() {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        Rectangle rectA = new Rectangle(0, 0, 100, 100);
        Rectangle rectB = new Rectangle(0, 0, 100, 100);
        assertEquals(true, cc.isSamePosSize(rectA, cc.duplicatedRectangle(rectA, rectB)));

        rectA = new Rectangle(0, 0, 100, 100);
        rectB = new Rectangle(100, 100, 100, 100);
        assertEquals(null, cc.duplicatedRectangle(rectA, rectB));

        rectA = new Rectangle(0, 0, 100, 100);
        rectB = new Rectangle(99, 99, 100, 100);
        assertEquals(true, cc.isSamePosSize(new Rectangle(99, 99, 1, 1),
                cc.duplicatedRectangle(rectA, rectB)));

        rectB = new Rectangle(0, 0, 100, 100);
        rectA = new Rectangle(10, 20, 80, 60);
        assertEquals(true, cc.isSamePosSize(new Rectangle(10, 20, 80, 60),
                cc.duplicatedRectangle(rectA, rectB)));
    }

    @org.junit.jupiter.api.Test
    void test_existsDuplicatedRectangle() {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        ArrayList<Rectangle> orgList = new ArrayList();
        assertEquals(false, cc.existsDuplicatedRectangle(orgList, new Rectangle(0,0, 100, 100)));

        orgList.add(new Rectangle(0,0, 100, 100));
        assertEquals(true, cc.existsDuplicatedRectangle(orgList, new Rectangle(0,0, 100, 100)));

        assertEquals(true, cc.existsDuplicatedRectangle(orgList, new Rectangle(10,10, 100, 100)));

        assertEquals(false, cc.existsDuplicatedRectangle(orgList, new Rectangle(90,90, 100, 100)));
    }

    @org.junit.jupiter.api.Test
    void test_removeDuplicatedRectangles() {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        ArrayList<Rectangle> orgList = new ArrayList();
        orgList.add(new Rectangle(0,0, 100, 100));
        orgList.add(new Rectangle(0,0, 100, 100));

        ArrayList<Rectangle> newList = cc.removeDuplicatedRects(orgList);
        assertEquals(1, newList.size());

        orgList.add(new Rectangle(100,100, 100, 100));
        newList = cc.removeDuplicatedRects(orgList);
        assertEquals(2, newList.size());

        orgList.add(new Rectangle(20,20, 100, 100));
        newList = cc.removeDuplicatedRects(orgList);
        assertEquals(2, newList.size());
    }
}