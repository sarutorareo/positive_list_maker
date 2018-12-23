package opencv_client;

import javafx.scene.shape.Line;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.*;

public class CFFacadeChipByColorTest {
    @org.junit.jupiter.api.Test
    void test_isChipColor() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();
        Color c = new Color((double)1/255, (double)78/255, (double)25/255, 1);
        assertEquals(true, cc.isChipColor(c));

        c = new Color((double)255/255, (double)78/255, (double)25/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)8/255, (double)91/255, (double)34/255, 1);
        assertEquals(true, cc.isChipColor(c));

        c = new Color((double)31/255, (double)102/255, (double)52/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)0/255, (double)51/255, (double)16/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)0/255, (double)61/255, (double)16/255, 1);
        assertEquals(true, cc.isChipColor(c));

        c = new Color((double)0/255, (double)61/255, (double)255/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)8/255, (double)91/255, (double)255/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)8/255, (double)91/255, (double)60/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)8/255, (double)91/255, (double)59/255, 1);
        assertEquals(true, cc.isChipColor(c));

        c = new Color((double)2/255, (double)119/255, (double)39/255, 1);
        assertEquals(false, cc.isChipColor(c));

        c = new Color((double)2/255, (double)97/255, (double)31/255, 1);
        assertEquals(false, cc.isChipColor(c));
    }

    @org.junit.jupiter.api.Test
    void test_regulationLines_yokoSukima() {
        int SUKIMA = 2;
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        List<Line> lineList = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(12, 0, 32, 0));

        List<Line> result = cc.m_regulationLines_yokoSukima(lineList, SUKIMA);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getStartX());
        assertEquals(0, result.get(0).getStartY());
        assertEquals(32, result.get(0).getEndX());
        assertEquals(0, result.get(0).getEndY());

        lineList = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 20, 0));
        lineList.add(new Line(23, 0, 24, 0));
        result = cc.m_regulationLines_yokoSukima(lineList, SUKIMA);
        assertEquals(1, result.size());

        lineList = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(14, 0, 20, 0));
        result = cc.m_regulationLines_yokoSukima(lineList, SUKIMA);
        assertEquals(2, result.size());

        lineList = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(12, 1, 20, 1));
        result = cc.m_regulationLines_yokoSukima(lineList, SUKIMA);
        assertEquals(2, result.size());

        lineList = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 2, 0));
        lineList.add(new Line(5, 0, 7, 0));
        result = cc.m_regulationLines_yokoSukima(lineList, SUKIMA);
        assertEquals(2, result.size());
    }

    @org.junit.jupiter.api.Test
    void test_groupingLines() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        ArrayList<Line> lineList = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(0, 1, 10, 1));
        lineList.add(new Line(0, 10, 10, 10));

        ArrayList<ArrayList<Line>> result = cc.m_groupingLines(lineList);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(0, result.get(0).get(0).getStartX());
        assertEquals(10, result.get(0).get(0).getEndX());
        assertEquals(0, result.get(0).get(0).getStartY());
        assertEquals(1, result.get(1).size());
        assertEquals(0, result.get(1).get(0).getStartX());
        assertEquals(10, result.get(1).get(0).getStartY());
    }

    @org.junit.jupiter.api.Test
    void test_m_groupingLinesRec() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();
        ArrayList<Line> lineList = new ArrayList<Line>();
        ArrayList<Line> group = new ArrayList<Line>();
        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(0, 1, 10, 1));

        cc.m_groupingLinesRec(lineList.get(0), group, lineList);

        assertEquals(0, lineList.size());
        assertEquals(2, group.size());

        lineList.clear();
        group.clear();

        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(0, 10, 10, 10));
        lineList.add(new Line(0, 1, 10, 1));

        cc.m_groupingLinesRec(lineList.get(0), group, lineList);

        assertEquals(1, lineList.size());
        assertEquals(2, group.size());

        lineList.clear();
        group.clear();

        lineList.add(new Line(0, 0, 10, 0));
        lineList.add(new Line(0, 1, 10, 1));
        lineList.add(new Line(1, 1, 11, 1));

        cc.m_groupingLinesRec(lineList.get(0), group, lineList);

        assertEquals(0, lineList.size());
        assertEquals(3, group.size());
    }

    @org.junit.jupiter.api.Test
    void test_getGroupWidth_Height() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        List<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 9, 10, 9));
        group.add(new Line(0, 1, 9, 1));
        group.add(new Line(0, 2, 10, 2));
        group.add(new Line(20, 3, 29, 3));

        int width = cc.m_getGroupWidth(group);
        assertEquals(30, width);
        int height = cc.m_getGroupHeight(group);
        assertEquals(9, height);
    }
    @org.junit.jupiter.api.Test
    void test_sort() {
        List<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 1, 9, 1));
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 3, 9, 3));
        group.add(new Line(0, 2, 10, 2));

        group.sort(comparing(Line::getStartY));
        assertEquals(0, group.get(0).getStartY());

        group.sort(comparing(Line::getStartY).reversed());
        assertEquals(3, group.get(0).getStartY());
    }

    @org.junit.jupiter.api.Test
    void test_getTopLine() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        // 半分より上で一番長いのを採用する
        List<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 1, 9, 1));
        group.add(new Line(0, 2, 11, 2));
        group.add(new Line(0, 3, 9, 3));

        group.sort(comparing(Line::getStartY));
        Line result = cc.m_getTopLine(group);
        assertEquals(0, result.getStartY());

        // 長さが50%未満なら採用しない
        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 1, 4, 1));
        group.add(new Line(0, 2, 10, 2));
        group.add(new Line(0, 3, 9, 3));

        group.sort(comparing(Line::getStartY));
        result = cc.m_getTopLine(group);
        assertEquals(0, result.getStartY());
    }

    @org.junit.jupiter.api.Test
    void test_getBottomLine() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        List<Line> group = new ArrayList<Line>();
        // 一番長いものを採用
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 1, 13, 1));
        group.add(new Line(0, 2, 9, 2));
        group.add(new Line(0, 3, 10, 3));

        group.sort(comparing(Line::getStartY));
        Line result = cc.m_getBottomLine(group);
        assertEquals(3, result.getStartY());

        // 長さが50%未満なら採用しない
        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 1, 7, 1));
        group.add(new Line(0, 2, 4, 2));
        group.add(new Line(0, 3, 9, 3));

        group.sort(comparing(Line::getStartY));
        result = cc.m_getBottomLine(group);
        assertEquals(3, result.getStartY());
    }


    @org.junit.jupiter.api.Test
    void test_getLonger() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        Line l1 = new Line(0, 0, 10, 0);
        Line l2 = new Line(0, 0, 9, 0);

        assertEquals(l1, cc.m_getLonger(l1, l2));

        l1 = new Line(0, 0, 9, 0);
        l2 = new Line(0, 0, 10, 0);

        assertEquals(l2, cc.m_getLonger(l1, l2));
    }

    @org.junit.jupiter.api.Test
    void test_isCovered() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        Line l1 = new Line(0, 0, 10, 0);
        Line l2 = new Line(0, 1, 10, 1);

        assertEquals(true, cc.m_isCovered(l1, l2));

        l1 = new Line(0, 0, 10, 0);
        l2 = new Line(11, 0, 20, 0);

        assertEquals(false, cc.m_isCovered(l1, l2));

        l2 = new Line(0, 0, 10, 0);
        l1 = new Line(11, 0, 20, 0);

        assertEquals(false, cc.m_isCovered(l1, l2));

        l1 = new Line(0, 0, 10, 0);
        l2 = new Line(10, 0, 20, 0);

        assertEquals(true, cc.m_isCovered(l1, l2));

        /*
        l1 = new Line(0, 0, 10, 0);
        l2 = new Line(10, 0, 20, 0);

        assertEquals(true, cc.m_isCovered(l1, l2));
        */
    }

}
