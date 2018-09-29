package utils;

public class FileUtil {
    /**
     * ファイル名から拡張子を返します。
     * @param fileName ファイル名
     * @return ファイルの拡張子
     */
    public static String getExtention(String fileName) {
        if (fileName == null)
            return null;
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(point + 1);
        }
        return "";
    }

    /**
     * ファイル名から拡張子を返します。
     * @param fileName 元のファイル名（フルパスでも可）
     * @return 拡張子を除いたファイル名
     */
    public static String getWithoutExtention(String fileName) {
        String ext = getExtention(fileName);
        if (ext == "") {
            return fileName;
        }
        else {
            return fileName.substring(0, fileName.length() - ext.length() - 1);
        }
    }
}
