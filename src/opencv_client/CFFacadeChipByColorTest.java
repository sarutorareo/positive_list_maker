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

        c = new Color((double)2/255, (double)95/255, (double)29/255, 1);
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

        ArrayList<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 1, 10, 1));
        group.add(new Line(0, 10, 10, 10));

        ArrayList<ArrayList<Line>> result = cc.m_groupingLines(group);
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
    void test_isSukimaY() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        ArrayList<Line> group = new ArrayList<Line>();
        ArrayList<Line> devided1 = new ArrayList<Line>();
        ArrayList<Line> devided2 = new ArrayList<Line>();
        group.add(new Line(0, 0, 10, 0));
        group.add(new Line(0, 1, 1, 1));

        assertEquals(false, cc.m_isSukimaY(0, group));
        assertEquals(true, cc.m_isSukimaY(1, group));
        assertEquals(false, cc.m_isSukimaY(0.5, group));
    }

    @org.junit.jupiter.api.Test
    void test_isTableEdge() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        ArrayList<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 0, 99, 0));
        group.add(new Line(0, 1, 99, 1));
        group.add(new Line(0, 2, 99, 1));
        group.add(new Line(0, 3, 99, 1));

        assertEquals(true, cc.m_isTableEdge(group));

        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 98, 0));
        group.add(new Line(0, 1, 98, 1));
        group.add(new Line(0, 2, 98, 1));
        group.add(new Line(0, 3, 98, 1));

        assertEquals(false, cc.m_isTableEdge(group));
    }

    @org.junit.jupiter.api.Test
    void test_removeTableEdge() {
        final int REMOVE_LINE_NUM = 3;
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        // 一つ目
        ArrayList<ArrayList<Line>> groupList = new ArrayList<ArrayList<Line>>();
        ArrayList<Line>group = new ArrayList<Line>();
        group.add(new Line(0, 0, 150, 0));
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        groupList.add(group);

        cc.m_removeTableEdge(groupList, REMOVE_LINE_NUM);

        assertEquals(1, groupList.size());
        assertEquals(1, groupList.get(0).size());

        // 一つ目、削除した結果グループが別れる
        groupList = new ArrayList<ArrayList<Line>>();
        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 150, 0));
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(100, 3, 150, 3));
        group.add(new Line(10, 3, 20, 3));
        group.add(new Line(10, 4, 20, 4));
        groupList.add(group);

        cc.m_removeTableEdge(groupList, REMOVE_LINE_NUM);

        assertEquals(2, groupList.size());
        assertEquals(1, groupList.get(0).size());
        assertEquals(2, groupList.get(1).size());

        // 二つ目の高さは消えない
        groupList = new ArrayList<ArrayList<Line>>();
        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 150, 0));
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        groupList.add(group);

        group = new ArrayList<Line>();
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        group.add(new Line(0, 4, 150, 4));
        groupList.add(group);

        cc.m_removeTableEdge(groupList, REMOVE_LINE_NUM);
        assertEquals(2, groupList.size());
        assertEquals(1, groupList.get(1).size());
        assertEquals(4, groupList.get(0).size());

        // 3つ目だけど同じ高さなら消える
        groupList = new ArrayList<ArrayList<Line>>();
        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 150, 0));
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        groupList.add(group);

        group = new ArrayList<Line>();
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        group.add(new Line(0, 4, 150, 4));
        groupList.add(group);

        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 150, 0));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        group.add(new Line(0, 4, 150, 4));
        groupList.add(group);

        cc.m_removeTableEdge(groupList, REMOVE_LINE_NUM);
        assertEquals(3, groupList.size());
        assertEquals(1, groupList.get(1).size());
        assertEquals(4, groupList.get(0).size());
        assertEquals(1, groupList.get(2).size());
    }

    @org.junit.jupiter.api.Test
    void test_removeTableLines() {
        final int REMOVE_LINE_NUM = 3;
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        // 先頭から指定した行数を消す
        ArrayList<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 0, 150, 0));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        group.add(new Line(0, 4, 150, 4));

        cc.m_removeTopLines(group, REMOVE_LINE_NUM);
        assertEquals(1, group.size());

        // 同じ座標に複数の線がある場合は全部消す
        group = new ArrayList<Line>();
        group.add(new Line(0, 1, 150, 1));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 2, 150, 2));
        group.add(new Line(0, 3, 150, 3));
        group.add(new Line(0, 4, 150, 4));

        cc.m_removeTopLines(group, REMOVE_LINE_NUM);
        assertEquals(1, group.size());
    }

    @org.junit.jupiter.api.Test
    void test_getTopYTableEdge() {
        CFFacadeChipByColor cc = new CFFacadeChipByColor();

        // 一つ目
        ArrayList<ArrayList<Line>> groupList = new ArrayList<ArrayList<Line>>();
        ArrayList<Line> group = new ArrayList<Line>();
        group.add(new Line(0, 10, 100, 10));
        groupList.add(group);

        assertEquals(10, cc.m_getTopYTableEdge(groupList));

        // 2つ目
        group = new ArrayList<Line>();
        group.add(new Line(0, 20, 100, 20));
        group.add(new Line(0, 1, 100, 1));
        groupList.add(group);

        // ３つ目が一番高いけどTableEdgeじゃない
        group = new ArrayList<Line>();
        group.add(new Line(0, 0, 90, 0));
        groupList.add(group);
        assertEquals(1, cc.m_getTopYTableEdge(groupList));

        // TableEdgeが一つもない
        groupList = new ArrayList<ArrayList<Line>>();
        group = new ArrayList<Line>();
        group.add(new Line(0, 10, 10, 10));
        groupList.add(group);
        assertEquals(Double.MAX_VALUE, cc.m_getTopYTableEdge(groupList));
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
