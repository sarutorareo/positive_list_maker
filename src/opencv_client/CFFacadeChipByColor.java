package opencv_client;

import classifier_ui.CFResult;
import classifier_ui.CFResultChip;
import classifier_ui.CFSettings;
import classifier_ui.CFSettingsChip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import org.opencv.core.Core;
import org.opencv.core.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;

public class CFFacadeChipByColor extends CFFacade {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    double LINE_RATIO = 0.5;

    @Override
    protected CFResult m_createResult(ArrayList<Rectangle>rects, ArrayList<Rectangle>fullRects)
    {
        return new CFResultChip(rects, fullRects);
    }

    @Override
    protected CFSettings m_createSettings(boolean isLoad) throws IOException
    {
        if (isLoad) {
            return CFSettingsChip.load();
        }
        else {
            return new CFSettingsChip();
        }
    }
    @Override
    protected ArrayList<Rectangle> m_doClassify(javafx.scene.image.Image fxImage, String cascadeXmlPath,
                                                int minNeighbors, double scaleFactor,
                                                Size minSize, Size maxSize, boolean removeDuplicate)
    {
        System.out.println(String.format("in CFFacadeChipByColor.m_doClassify cascadeXmlPath = %s, minNeighbors = %d, scaleFactor=%f, minSize=(%f, %f), maxSize=(%f, %f), removeDuplicate=%d",
                cascadeXmlPath, minNeighbors, scaleFactor, minSize.width, minSize.height, maxSize.width, maxSize.height, removeDuplicate? 1:0));

        // ArrayList<Rectangle> result = new ArrayList<Rectangle>();
        // result.add(new Rectangle(minSize.width, minSize.height, minSize.width + 10, minSize.height + 10));

        // 直線を探す
        List<Line> lineList = m_getLines(fxImage);
        ArrayList<ArrayList<Line>> lineGroups = m_regulationLines(lineList);

        // テストのためにisChipColor()を赤く塗る
        ArrayList<Rectangle> result = m_saveTestImage(fxImage, lineGroups);

        result.forEach(r -> {
            System.out.println(String.format("r = %s, %f", r.toString(), (double)r.getWidth() / r.getHeight()));
        });

        return result;
    }

    public boolean isChipColor(Color c) {
        if (c.getRed() > (double)25/255) {
            return false;
        }
        if (c.getGreen() < (double)60/255) {
            return false;
        }
        if (c.getGreen() > (double)94/255) {
            return false;
        }
        if (c.getBlue() > (double)60/255) {
            return false;
        }
        return true;
    }

    private List<Line> m_getLines(Image image) {
        ArrayList<Line> list = new ArrayList<Line>();
        int height = (int)image.getHeight();
        int width = (int)image.getWidth();

        boolean isCont = false;
        int startX = -1;
        PixelReader pixelReader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                if (isCont) {
                    if (!isChipColor(color)) {
                        list.add(new Line(startX, y, x - 1, y));
                        isCont = false;
                        startX = -1;
                    }
                }
                else {
                    if (isChipColor(color)) {
                        isCont = true;
                        startX = x;
                    }
                }
            }
        }
        return list;
    }

    private ArrayList<ArrayList<Line>> m_regulationLines(List<Line> orgLineList) {
        // 隙間が少し空いているだけの線はくっつける
        final int SUKIMA = 4;
        final int MIN_LENGTH = 2;
        final int REMOVE_EDGE_LINE_NUM = 5;
        ArrayList<Line>lineList = m_regulationLines_yokoSukima(orgLineList, SUKIMA);

        /*
        // 1～2ドットの長さしかない線は消す
        //TODO 隙間の１ドット～２ドットでくっついているものを分離する
        lineList = m_removeShortLine(lineList, MIN_LENGTH);
        */

        // 塗りつぶしでグルーピング
        ArrayList<ArrayList<Line>> result = m_groupingLines(lineList);
        result.forEach(g -> {
            g.sort(comparing(Line::getStartY));
        });

        // テーブルエッジのバリを取る
        m_removeTableEdge(result, REMOVE_EDGE_LINE_NUM);

        // 微妙な線を分離する
        // result = m_groupingLines_devideSukima(result);

        return result;
    }

    protected void m_removeTableEdge(ArrayList<ArrayList<Line>>  groupList, int removeNum)
    {
        // テーブルエッジの中で一番上にある線を削除
        double topY = m_getTopYTableEdge(groupList);
        ArrayList<ArrayList<Line>> removedGroup = new ArrayList<ArrayList<Line>>();
        for (ArrayList<Line> g : groupList) {
            double groupTop = m_getGroupTop(g);
            if (m_isTableEdge(g) && groupTop == topY) {
                if (m_removeTopLines(g, removeNum)) {
                    removedGroup.add(g);
                }
            }
        }

        // 削除したグループを再編
        removedGroup.forEach(rg -> {
            groupList.remove(rg);
            ArrayList<ArrayList<Line>> newGroups = m_groupingLines(rg);
            groupList.addAll(newGroups);
        });
    }

    protected double m_getTopYTableEdge(ArrayList<ArrayList<Line>>  groupList) {
        double minY = Double.MAX_VALUE;
        for (ArrayList<Line> g : groupList) {
            if (!m_isTableEdge(g)) {
                continue;
            }
            double groupTop = m_getGroupTop(g);
            if (groupTop < minY) {
                minY = groupTop;
            }
        }
        return minY;
    }

    protected boolean m_removeTopLines(ArrayList<Line> group, int num) {
        boolean isRemoved = false;
        for (int i = 0; i < num; i++) {
            if (group.size() == 0) {
                return isRemoved;
            }
            double topY = m_getGroupTop(group);
            int index = 0;
            while(index < group.size()) {
                Line l = group.get(index);
                if (l.getStartY() == topY) {
                    isRemoved = true;
                    group.remove(l);
                }
                else {
                    index++;
                }
            }
        }
        return isRemoved;
    }

    protected boolean m_isTableEdge(ArrayList<Line> group)
    {
        final int TABLE_EDGE_WIDTH = 100;
        int width = m_getGroupWidth(group);
        return width >= TABLE_EDGE_WIDTH;
    }

    protected  ArrayList<Line> m_removeShortLine(List<Line> lineList, int minLength) {
        ArrayList<Line> result = new ArrayList<Line>();
        for (int i = 0; i < lineList.size(); i++) {
            Line l = lineList.get(i);
            if (l.getEndX() - l.getStartX() + 1 >= minLength) {
               result.add(l);
            }
        }
        return result;
    }

    protected ArrayList<Line> m_regulationLines_yokoSukima(List<Line> lineList, int sukima) {
        final int MIN_LENGTH = sukima * 5;
        ArrayList<Line> result = new ArrayList<Line>();
        Line beforeLine = null;
        for (int i = 0; i < lineList.size(); i++) {
            Line l = lineList.get(i);
            if ((beforeLine != null)
                    && (l.getStartX() - beforeLine.getEndX() - 1 <= sukima)
                    && (l.getStartY() == beforeLine.getEndY())
                    && ( (l.getEndX() - l.getStartX() + 1 > MIN_LENGTH)
                        || (beforeLine.getEndX() - beforeLine.getStartX() + 1 > MIN_LENGTH)
                       )
                    ) {
                beforeLine.setEndX(l.getEndX());
            }
            else {
                result.add(l);
                beforeLine = l;
            }
        }
        return result;
    }

    protected ArrayList<ArrayList<Line>> m_groupingLines(ArrayList<Line> orgLineList) {
        ArrayList<Line> lineList = (ArrayList<Line>)orgLineList.clone();
        ArrayList<ArrayList<Line>> result = new ArrayList<ArrayList<Line>>();

        // 再帰的にくっついてるものを集めてグルーピング
        while(lineList.size() > 0) {
            Line line = lineList.get(0);
            ArrayList<Line> group = new ArrayList<Line>();
            m_groupingLinesRec(line, group, lineList);
            result.add(group);
        }

        return result;
    }

    protected boolean m_isSukimaY(double y, ArrayList<Line> group) {
        final int SUKIMA = 2;
        boolean existsY = false;
        for(int i = 0; i < group.size(); i++) {
            Line l = group.get(i);
            if (l.getStartY() == y) {
                existsY = true;
                if ((l.getEndX() - l.getStartX() + 1) > SUKIMA) {
                    return false;
                }
            }
        }
        return existsY;
    }

    protected void m_groupingLinesRec(Line line, ArrayList<Line> group, ArrayList<Line> lineList) {
        group.add(line);
        lineList.remove(line);

        ArrayList<Line> tmpLineList = (ArrayList<Line>)lineList.clone();
        // くっついてるやつを探す
        tmpLineList.forEach(l -> {
            if (m_isOverlaped(line, l) && lineList.contains(l)) {
                m_groupingLinesRec(l, group, lineList);
            }
        });
    }

    protected boolean m_isCovered(Line l1, Line l2) {
        return !((l1.getStartX() > l2.getEndX())
                || (l2.getStartX() > l1.getEndX()));
    }

    protected boolean m_isOverlaped(Line l1, Line l2) {
        return (Math.abs(l1.getStartY() - l2.getStartY()) <= 1) && m_isCovered(l1, l2);
    }

    protected Line m_getLonger(Line l1, Line l2) {
        double len1 = l1.getEndX() - l1.getStartX();
        double len2 = l2.getEndX() - l2.getStartX();
        return (len1 >= len2)? l1: l2;
    }

    protected int m_getGroupWidth(List<Line> lineGroup) {
        assert(lineGroup.size() > 0);
        double left = Integer.MAX_VALUE;
        double right = -1;
        for (Line l : lineGroup) {
            if (l.getStartX() < left) {
                left = l.getStartX();
            }
            if (l.getEndX() > right) {
                right = l.getEndX();
            }
        }

        return (int)(right - left + 1);
    }

    protected double m_getGroupTop(List<Line> lineGroup) {
        assert(lineGroup.size() > 0);
        double top = Integer.MAX_VALUE;
        for (Line l : lineGroup) {
            if (l.getStartY() < top) {
                top = l.getStartY();
            }
        }

        return top;
    }

    protected double m_getGroupBottom(List<Line> lineGroup) {
        assert(lineGroup.size() > 0);
        double bottom = -1;
        for (Line l : lineGroup) {
            if (l.getStartY() > bottom) {
                bottom = l.getStartY();
            }
        }

        return bottom;
    }

    protected int m_getGroupHeight(List<Line> lineGroup) {
        double top = m_getGroupTop(lineGroup);
        double bottom = m_getGroupBottom(lineGroup);
        return (int)(bottom - top + 1);
    }

    protected Line m_getTopLine(List<Line> lineGroup) {
        // 半分より上で一番長いもの
        int half = m_getGroupHeight(lineGroup) / 2 + (int)lineGroup.get(0).getStartY();
        Predicate<Line> isRange = (l)-> { return l.getStartY()  < half; };
        return m_getMaxLine(lineGroup, isRange);
    }
    protected Line m_getBottomLine(List<Line> lineGroup) {
        // 半分より下で一番長いもの
        int half = m_getGroupHeight(lineGroup) / 2 + (int)lineGroup.get(0).getStartY();
        Predicate<Line> isRange = (l)-> { return l.getStartY()  >= half; };
        return m_getMaxLine(lineGroup, isRange);
    }

    protected Line m_getMaxLine(List<Line> lineGroup, Predicate<Line> isRange) {
        assert(lineGroup.size() > 0);
        int groupWidth = m_getGroupWidth(lineGroup);
        double maxLen = -1;
        Line maxLine = null;
        for (int l = 0;  l < lineGroup.size(); l++) {
            Line line = lineGroup.get(l);
            if (!isRange.test(line)) {
                continue;
            }
            if (line.getEndX() - line.getStartX() + 1 >= maxLen) {
                maxLine = line;
                maxLen = line.getEndX() - line.getStartX() + 1;
            }
        }
        if (maxLine != null && (maxLine.getEndX() - maxLine.getStartX() + 1) > groupWidth * LINE_RATIO) {
            return maxLine;
        }
        return null;
    }

    // テストのためにisChipColor()を赤く塗る
    private ArrayList<Rectangle> m_saveTestImage(Image image, ArrayList<ArrayList<Line>>lineGroups) {
        ArrayList<Rectangle> result = new ArrayList<Rectangle>();
        int height = (int)image.getHeight();
        int width = (int)image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color color = pixelReader.getColor(x, y);
                if (isChipColor(color)) {
                    color = Color.ORANGE;
                }
                pixelWriter.setColor(x, y, color);
            }
        }

        System.out.println(String.format("lineGroups.size() = %d", lineGroups.size()));
        for (int group = 0; group < lineGroups.size(); group++) {
            List<Line> g = lineGroups.get(group);
            System.out.println(String.format("g(%d).size() = %d", group, g.size()));
            g.forEach(l -> {
                System.out.println(String.format("l = (%f, %f, %f, %f)", l.getStartX(), l.getStartY(), l.getEndX(), l.getEndY()));
                for(int y = (int)l.getStartY(); y <= (int)l.getEndY(); y++) {
                    for(int x = (int)l.getStartX(); x <= (int)l.getEndX(); x++) {
                        pixelWriter.setColor(x, y, Color.BLUE);
                    }
                }
            });
           /***
            Line l = g.get(0);
            for(int y = (int)l.getStartY(); y <= (int)l.getEndY(); y++) {
                for(int x = (int)l.getStartX(); x <= (int)l.getEndX(); x++) {
                    pixelWriter.setColor(x, y, Color.YELLOW);
                }
            }
            l = g.get(g.size()-1);
            for(int y = (int)l.getStartY(); y <= (int)l.getEndY(); y++) {
                for(int x = (int)l.getStartX(); x <= (int)l.getEndX(); x++) {
                    pixelWriter.setColor(x, y, Color.VIOLET);
                }
            }
            ***/

            // x%以上の長さを持ち、半分より上で最も下を探す
            g.sort(comparing(Line::getStartY));
            final int MIN_LENGTH = 10;
            Line topLine = m_getTopLine(g);
            Line bottomLine = m_getBottomLine(g);
            if (topLine == null || bottomLine == null
                || (topLine.getEndX() - topLine.getStartX() + 1 < MIN_LENGTH)
                || (bottomLine.getEndX() - bottomLine.getStartX() + 1 < MIN_LENGTH)) {
                continue;
            }
            System.out.println(String.format("top / bottom"));
            Line l = topLine;
            for(int y = (int)l.getStartY(); y <= (int)l.getEndY(); y++) {
                for(int x = (int)l.getStartX(); x <= (int)l.getEndX(); x++) {
                    pixelWriter.setColor(x, y, Color.WHITE);
                }
            }
            l = bottomLine;
            for(int y = (int)l.getStartY(); y <= (int)l.getEndY(); y++) {
                for(int x = (int)l.getStartX(); x <= (int)l.getEndX(); x++) {
                    pixelWriter.setColor(x, y, Color.WHITE);
                }
            }

            double left = Math.max(topLine.getStartX(), bottomLine.getStartX());
            double right = Math.min(topLine.getEndX(), bottomLine.getEndX());
            Rectangle rect = new Rectangle(
                left,
                topLine.getStartY(),
                right - left,
                bottomLine.getStartY() - topLine.getStartY() + 1);
            rect.setFill(Color.TRANSPARENT);
            //
            for(int x = (int)rect.getX(); x <= rect.getX() + rect.getWidth(); x++) {
                pixelWriter.setColor(x, (int)rect.getY(), Color.RED);
                pixelWriter.setColor(x, (int)(rect.getY() + rect.getHeight() - 1), Color.RED);
            }
            for(int y = (int)rect.getY(); y <= rect.getY() + rect.getHeight(); y++) {
                pixelWriter.setColor((int)rect.getX(), y,  Color.RED);
                pixelWriter.setColor((int)(rect.getX() + rect.getWidth() - 1), y, Color.RED);
            }
            result.add(rect);
        }

        try {
            utils.ImageUtils.savePng(writableImage, "D:\\MyProgram\\GitHub\\positive_list_maker\\test.png");
        }
        catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
        return result;
    }
}
