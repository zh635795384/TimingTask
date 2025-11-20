package com.simatelex.admin.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;

import static org.apache.poi.ss.usermodel.VerticalAlignment.CENTER;
import static org.apache.poi.ss.usermodel.VerticalAlignment.TOP;

/**
 * @Author Nancy Zhao
 * @Date 2023/06/12 15:49
 * @Version 1.0
 **/
public class ExcelStyleUtil {
    //样式1
    public static XSSFCellStyle getCellStyle1(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        cellStyle.setAlignment(HorizontalAlignment.CENTER);    //水平对齐
        cellStyle.setVerticalAlignment(CENTER);
        setBorder1(cellStyle);  //设置边框
        return cellStyle;
    }
    //样式2
    public static XSSFCellStyle getCellStyle2(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        cellStyle.setAlignment(HorizontalAlignment.LEFT);   //水平对齐
        cellStyle.setVerticalAlignment(CENTER); //垂直对齐
        return cellStyle;
    }
    //样式3
    public static XSSFCellStyle getCellStyle3(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);    //水平左对齐
        cellStyle.setVerticalAlignment(CENTER); //垂直居中
        XSSFDataFormat xssfDataFormat = workbook.createDataFormat();
        cellStyle.setDataFormat(xssfDataFormat.getFormat("0.0"));
//        setBorder2(cellStyle);
        cellStyle.setFont(xssfFont);
        return cellStyle;
    }

    //样式4
    public static XSSFCellStyle getCellStyle4(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setRotation((short) 255);    //转换文字方向
        cellStyle.setVerticalAlignment(TOP);    //垂直向上
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        return cellStyle;
    }
    //样式5
    public static XSSFCellStyle getCellStyle5(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setAlignment(HorizontalAlignment.CENTER);    //水平居中
        cellStyle.setVerticalAlignment(CENTER); //垂直居中
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        return cellStyle;
    }
    //样式6
    public static XSSFCellStyle getCellStyle6(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);    //水平左对齐
        cellStyle.setVerticalAlignment(CENTER); //垂直居中
        cellStyle.setFont(xssfFont);
        return cellStyle;
    }
    //样式7
    public static XSSFCellStyle getCellStyle7(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);    //水平左对齐
        cellStyle.setVerticalAlignment(CENTER); //垂直居中
        cellStyle.setWrapText(true);    //自动换行
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setFont(xssfFont);
        return cellStyle;
    }
    //样式8
    public static XSSFCellStyle getCellStyle8(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setAlignment(HorizontalAlignment.CENTER);    //水平居中
        cellStyle.setVerticalAlignment(CENTER); //垂直居中
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        return cellStyle;
    }
    //样式9 处理日期格式
    public static XSSFCellStyle getCellStyle9(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
        return cellStyle;
    }
    public static XSSFCellStyle getCellStyle10(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFDataFormat format = workbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("0.0000"));
        return cellStyle;
    }
    public static XSSFCellStyle getCellStyle11(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFDataFormat format = workbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("0.000000"));
        return cellStyle;
    }

    public static XSSFCellStyle getCellStyle12(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        cellStyle.setAlignment(HorizontalAlignment.CENTER);    //水平对齐
        cellStyle.setVerticalAlignment(CENTER);
        setBorder1(cellStyle);  //设置边框
        return cellStyle;
    }

    public static XSSFCellStyle getCellStyle13(XSSFWorkbook workbook, XSSFFont xssfFont) {
        XSSFCellStyle cellStyle = workbook.createCellStyle(); //设置工作表样式
        cellStyle.setFont(xssfFont); //向样式里添加字体设置
        cellStyle.setAlignment(HorizontalAlignment.CENTER);    //水平对齐
        cellStyle.setVerticalAlignment(CENTER);
        setBorder1(cellStyle);  //设置边框
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    //字体1
    public static XSSFFont getXssfFont1(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 20); //设置字体大小
        xssfFont.setBold(true); //加粗
        return xssfFont;
    }
    //字体2
    public static XSSFFont getXssfFont2(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 12); //设置字体大小
        xssfFont.setBold(true); //加粗
        return xssfFont;
    }
    //字体3
    public static XSSFFont getXssfFont3(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 8); //设置字体大小
        return xssfFont;
    }
    //字体4
    public static XSSFFont getXssfFont4(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 12); //设置字体大小
//        xssfFont.setBold(true); //文字加粗
        return xssfFont;
    }
    //字体5
    public static XSSFFont getXssfFont5(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 10); //设置字体大小
        return xssfFont;
    }
    //字体6
    public static XSSFFont getXssfFont6(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("Times New Roman"); //设置字体
        xssfFont.setFontHeightInPoints((short) 8); //设置字体大小
        return xssfFont;
    }
    //字体7
    public static XSSFFont getXssfFont7(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setBold(true); //文字加粗
        xssfFont.setFontHeightInPoints((short) 8); //设置字体大小
        return xssfFont;
    }
    //字体8
    public static XSSFFont getXssfFont8(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setBold(true); //文字加粗
        xssfFont.setColor(new XSSFColor(new Color(255)));
        xssfFont.setFontHeightInPoints((short) 8); //设置字体大小
        return xssfFont;
    }
    //字体9
    public static XSSFFont getXssfFont9(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setBold(true); //文字加粗
        xssfFont.setColor(new XSSFColor(new Color(0xF00000)));
        xssfFont.setFontHeightInPoints((short) 8); //设置字体大小
        return xssfFont;
    }
    //字体10
    public static XSSFFont getXssfFont10(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 12); //设置字体大小
        xssfFont.setColor(new XSSFColor(new Color(255)));
        xssfFont.setBold(true); //加粗
        return xssfFont;
    }
    //字体11
    public static XSSFFont getXssfFont11(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 12); //设置字体大小
        xssfFont.setColor(new XSSFColor(new Color(0xFF0000)));
        xssfFont.setBold(true); //加粗
        return xssfFont;
    }
    //字体12
    public static XSSFFont getXssfFont12(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 22); //设置字体大小
        xssfFont.setColor(new XSSFColor(new Color(0xFF800000, true)));
        xssfFont.setBold(true); //加粗
        return xssfFont;
    }
    //字体13
    public static XSSFFont getXssfFont13(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 16); //设置字体大小
        return xssfFont;
    }
    //字体14
    public static XSSFFont getXssfFont14(XSSFWorkbook workbook) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName("新细明体"); //设置字体
        xssfFont.setFontHeightInPoints((short) 16); //设置字体大小
        xssfFont.setUnderline(XSSFFont.U_SINGLE);   //加下划线
        return xssfFont;
    }
    //字体15
    public static XSSFFont getXssfFont15(XSSFWorkbook workbook, String fonts, Double fontSize) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName(fonts); //设置字体
        xssfFont.setFontHeightInPoints(fontSize.shortValue()); //设置字体大小
        xssfFont.setBold(true); //加粗
        return xssfFont;
    }
    //字体16
    public static XSSFFont getXssfFont16(XSSFWorkbook workbook, String fonts, Double fontSize) {
        XSSFFont xssfFont = workbook.createFont(); //设置工作表字体
        xssfFont.setFontName(fonts); //设置字体
        xssfFont.setFontHeightInPoints(fontSize.shortValue()); //设置字体大小
        return xssfFont;
    }
    //设置边框
    public static void setBorder1(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THICK); //设置下边框
        cellStyle.setBorderLeft(BorderStyle.THICK); //设置左边框
        cellStyle.setBorderRight(BorderStyle.THICK); //设置右边框
        cellStyle.setBorderTop(BorderStyle.THICK); //设置上边框
        cellStyle.setBottomBorderColor(new XSSFColor(new Color(0))); //设置下边框颜色
        cellStyle.setTopBorderColor(new XSSFColor(new Color(0)));  //设置上边框颜色
        cellStyle.setLeftBorderColor(new XSSFColor(new Color(0))); //设置左边框颜色
        cellStyle.setRightBorderColor(new XSSFColor(new Color(0))); //设置右边框颜色
    }

    public static void setBorder2(XSSFCellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN); //设置下边框
        cellStyle.setBorderLeft(BorderStyle.THIN); //设置左边框
        cellStyle.setBorderRight(BorderStyle.THIN); //设置右边框
        cellStyle.setBorderTop(BorderStyle.THIN); //设置上边框
        cellStyle.setBottomBorderColor(new XSSFColor(new Color(0))); //设置下边框颜色
        cellStyle.setTopBorderColor(new XSSFColor(new Color(0)));  //设置上边框颜色
        cellStyle.setLeftBorderColor(new XSSFColor(new Color(0))); //设置左边框颜色
        cellStyle.setRightBorderColor(new XSSFColor(new Color(0))); //设置右边框颜色
    }

    private static char dig2Char(int dig)
    {
        int acs = dig - 1 + 'A';
        return (char)acs;
    }
    //Excel数字转字母列
    public static String setColumn(Integer column){
        int col = column + 1;
        int system = 26;
        char[] digArr = new char[100];
        int ind = 0;
        while (col > 0)
        {
            int mod = col % system;
            if (mod == 0) mod = system;
            digArr[ind++] = dig2Char(mod);
            col = (col - 1) / 26;
        }
        StringBuffer bf = new StringBuffer(ind);
        for (int i = ind - 1; i >= 0; i--)
        {
            bf.append(digArr[i]);
        }
        return bf.toString();
    }
}
