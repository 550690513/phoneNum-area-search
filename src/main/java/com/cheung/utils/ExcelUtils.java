package com.cheung.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;

/**
 * Created by Cheung on 2018/2/6.
 */
public class ExcelUtils {

	/**
	 * Excel文件批量查询归属地并导出
	 * @param filePath 文件路径
	 */
	public static void getAreaByPhoneNum_multiple(String filePath) {
		searchThread searchThread = new searchThread(filePath);
		searchThread.start();
	}

	/**
	 * 采用线程的方式,便于对执行过程进行控制
	 */
	private static class searchThread extends Thread {

		private String filePath;

		public searchThread(String filePath) {
			this.filePath = filePath;
		}

		public void run() {

			// 后缀名
			String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);

			/**
			 * 1.读取excel并批量查询
 			 */
			HSSFWorkbook workbook = null;
			try {
				FileInputStream fileInputStream = new FileInputStream(filePath);
				if (fileInputStream == null) throw new RuntimeException("文件不存在");

				if ("xls".equals(suffix.toLowerCase())) {
					workbook = new HSSFWorkbook(fileInputStream);// 当前Excel工作簿对象
					for (int m = 0; m < workbook.getNumberOfSheets(); m++) {
						HSSFSheet sheet = workbook.getSheetAt(m);// 当前sheet
						int rows = sheet.getPhysicalNumberOfRows();// 当前sheet的总物理行数(有数据+无数据)
						if (null != sheet && 0 != rows) {
							HSSFRow titleRow = sheet.getRow(0);// 首行---标题行
							int index = titleRow.getFirstCellNum();// 标题行首个单元格的列数
							int cellCount = titleRow.getLastCellNum();// 标题行尾单元格的列数(有效单元格的个数)

							for (int i = 1; i < rows; i++) {

								if (i % 3000 == 0) {
									System.out.println("sleep~~~");
									searchThread.sleep(2000);// 每读3000行睡2s,避免挂死
								}

								HSSFRow row = sheet.getRow(i);// 当前行(从第2行开始读)
								if (isBlankRow(row, index, cellCount)) continue;// 非空判断

								HSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
								phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String phoneNum = phoneNumCell.getStringCellValue();
								String url = "http://www.ip138.com:8080/search.asp?action=mobile&mobile=%s";
								url = String.format(url, phoneNum);
								try {
									Document doc = Jsoup.connect(url).get();
									Elements els = doc.getElementsByClass("tdc2");
									String area = els.get(1).text();
									System.out.println("第" + i + "条：" + phoneNum + "---" + area);
									if (null == row.getCell(1)) row.createCell(1).setCellType(HSSFCell.CELL_TYPE_STRING);
									row.getCell(1).setCellValue(area);
								} catch (IOException e) {
									e.printStackTrace();
								}

							}
						}

					}
				} else if ("xlsx".equals(suffix.toLowerCase())) {

				}
				fileInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}


			/**
			 * 2.输出为新的excel
			 */
			try {
				File f = new File(filePath.substring(0, filePath.lastIndexOf(".")) + "-ok.xls");
				f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);
				workbook.write(fos);
				fos.flush();// 刷新此输出流并强制将所有缓冲的输出字节写出
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}


	/**
	 * 非空行判断一
	 *
	 * @param row       当前行对象
	 * @param index     当前行首单元格的列数
	 * @param cellCount 当前行有效单元格个数
	 * @return
	 */
	public static boolean isBlankRow(HSSFRow row, int index, int cellCount) {
		if (row == null) return true;
		// 遍历每一列
		for (int i = index; i < cellCount; i++) {
			HSSFCell cell = row.getCell(i);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			if (row.getCell(i) != null && !"".equals(row.getCell(i).getStringCellValue().trim())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 非空行判断二
	 *
	 * @param row       当前行对象
	 * @param index     当前行首单元格的列数
	 * @param cellCount 当前行有效单元格个数
	 * @return
	 */
	public static boolean isBlankRow(XSSFRow row, int index, int cellCount) {
		if (row == null) return true;
		for (int i = index; i < cellCount; i++) {
			XSSFCell cell = row.getCell(i);
			cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			if (row.getCell(i) != null || !"".equals(row.getCell(i).getStringCellValue().trim())) {
				return false;
			}
		}
		return true;
	}

}
