package com.cheung.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author Cheung
 * @version 1.0.0
 * @date 2018/2/6
 */
@Component
public class ExcelUtil {

	private static String urlIP138 = "http://www.ip138.com:8080/search.asp?action=mobile&mobile=%s";
	private static String urlTaoBao = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm";

	/*private static String urlIP138;
	private static String urlTaoBao;

	@Value("${urlIP138}")
	public void setUrlIP138(String urlIP138) {
		ExcelUtil.urlIP138 = urlIP138;
	}

	@Value("${urlTaoBao}")
	public void setUrlTaoBao(String urlTaoBao) {
		ExcelUtil.urlTaoBao = urlTaoBao;
	}*/

	/**
	 * Excel文件批量查询归属地并导出
	 *
	 * @param filePath 文件路径
	 */
	public static String getAreaByPhoneNum_multiple(String filePath) {

		// 创建线程池
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<String>> results = new ArrayList<Future<String>>();
		for (int i = 0; i < 10; i++) {
			results.add(exec.submit(new SearchThread(filePath)));
		}
		for (Future<String> fs : results) {
			try {
				System.out.println("批量查询完成，结果文件：" + fs.get());
				return fs.get();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				exec.shutdown();
			}
		}
		return null;

	}

	/**
	 * 采用线程的方式,便于对执行过程进行控制
	 */
	public static class SearchThread implements Callable<String> {

		private String filePath;

		public SearchThread(String filePath) {
			this.filePath = filePath;
		}

		@Override
		public String call() throws Exception {

			// 后缀名
			String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);

			/**
			 * 读取excel并批量查询
			 */
			String resultExcelPath = "";
			try {
				FileInputStream fileInputStream = new FileInputStream(filePath);

				if ("xls".equals(suffix.toLowerCase())) {
					HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fileInputStream);// 当前Excel工作簿对象
					for (int m = 0; m < hssfWorkbook.getNumberOfSheets(); m++) {
						HSSFSheet sheet = hssfWorkbook.getSheetAt(m);// 当前sheet
						int rows = sheet.getPhysicalNumberOfRows();// 当前sheet的总物理行数(有数据+无数据)
						if (null != sheet && 0 != rows) {
							HSSFRow titleRow = sheet.getRow(0);// 首行---标题行
							int index = titleRow.getFirstCellNum();// 标题行首个单元格的列数
							int cellCount = titleRow.getLastCellNum();// 标题行尾单元格的列数(有效单元格的个数)

							for (int i = 1; i < rows; i++) {

								if (i % 3000 == 0) {
									System.out.println("sleep~~~");
									// SearchThread.sleep(2000);
									TimeUnit.MILLISECONDS.sleep(2000);// 每读3000行睡2s,避免挂死
								}

								HSSFRow row = sheet.getRow(i);// 当前行(从第2行开始读)
								if (isBlankRow(row, index, cellCount)) {
									continue;// 非空判断
								}

								HSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
								phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String phoneNum = phoneNumCell.getStringCellValue();

								try {
									/**
									 * 方式一:请求ip138接口获取数据(通过解析html获取)
									 */
									String url = String.format(urlIP138, phoneNum);
									Document doc = Jsoup.connect(url).get();
									Elements els = doc.getElementsByClass("tdc2");
									if (null == els || els.size() == 0) {
										System.out.println("请求第三方接口获取数据为null");
										continue;
									}
									String area = els.get(1).text();


									/**
									 * 方式二:请求淘宝api获取数据(通过解析json获取)
									 */
									/*String str = HttpRequestUtil.sendGet(urlTaoBao, "tel=" + phoneNum);
									JSONObject obj = ParseUtil.parseTBStr(str);
									String area = obj.getString("carrier");*/


									System.out.println("第" + i + "条：" + phoneNum + "---" + area);
									if (null == row.getCell(1)) {
										row.createCell(1).setCellType(HSSFCell.CELL_TYPE_STRING);
									}
									row.getCell(1).setCellValue(area);
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}

							}
						}

					}

					resultExcelPath = createResultExcel(filePath, hssfWorkbook, suffix);

				} else if ("xlsx".equals(suffix.toLowerCase())) {
					XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);// 当前Excel工作簿对象
					for (int m = 0; m < xssfWorkbook.getNumberOfSheets(); m++) {
						XSSFSheet sheet = xssfWorkbook.getSheetAt(m);// 当前sheet
						int rows = sheet.getPhysicalNumberOfRows();// 当前sheet的总物理行数(有数据+无数据)
						if (null != sheet && 0 != rows) {
							XSSFRow titleRow = sheet.getRow(0);// 首行---标题行
							int index = titleRow.getFirstCellNum();// 标题行首个单元格的列数
							int cellCount = titleRow.getLastCellNum();// 标题行尾单元格的列数(有效单元格的个数)

							for (int i = 1; i < rows; i++) {

								if (i % 3000 == 0) {
									System.out.println("sleep~~~");
									// SearchThread.sleep(2000);
									TimeUnit.MILLISECONDS.sleep(2000);// 每读3000行睡2s,避免挂死
								}

								XSSFRow row = sheet.getRow(i);// 当前行(从第2行开始读)
								if (isBlankRow(row, index, cellCount)) {
									continue;// 非空判断
								}

								XSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
								phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String phoneNum = phoneNumCell.getStringCellValue();

								try {
									/**
									 * 方式一:请求ip138接口获取数据(通过解析html获取)
									 */
									String url = String.format(urlIP138, phoneNum);
									Document doc = Jsoup.connect(url).get();
									Elements els = doc.getElementsByClass("tdc2");
									if (null == els || els.size() == 0) {
										System.out.println("请求第三方接口获取数据为null");
										continue;
									}
									String area = els.get(1).text();


									/**
									 * 方式二:请求淘宝api获取数据(通过解析json获取)
									 */
									/*String str = HttpRequestUtil.sendGet(urlTaoBao, "tel=" + phoneNum);
									JSONObject obj = ParseUtil.parseTBStr(str);
									String area = obj.getString("carrier");*/

									System.out.println("第" + i + "条：" + phoneNum + "---" + area);
									if (null == row.getCell(1)) {
										row.createCell(1).setCellType(HSSFCell.CELL_TYPE_STRING);
									}
									row.getCell(1).setCellValue(area);
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}

							}
						}

					}

					resultExcelPath = createResultExcel(filePath, xssfWorkbook, suffix);

				}
				fileInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return resultExcelPath;

		}

	}

	/**
	 * 生成结果文件
	 *
	 * @param filePath
	 * @param workbook
	 * @param suffix
	 * @return
	 */
	private static String createResultExcel(String filePath, Workbook workbook, String suffix) {
		try {
			File f = null;
			if (!filePath.contains("-")) {
				// 通过main方法直接批量查询
				f = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1) + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".")) + "-ok." + suffix);
			} else {
				f = new File(filePath.substring(0, filePath.indexOf("-")) + "-ok." + suffix);
			}
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			workbook.write(fos);
			fos.flush();// 刷新此输出流并强制将所有缓冲的输出字节写出
			fos.close();
			// return f.getCanonicalPath();// 返回文件路径
			return f.getName();// 返回文件名称
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 非空行判断一
	 *
	 * @param row       当前行对象
	 * @param index     当前行首单元格的列数
	 * @param cellCount 当前行有效单元格个数
	 * @return
	 */
	private static boolean isBlankRow(HSSFRow row, int index, int cellCount) {
		if (row == null) {
			return true;
		}
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
	private static boolean isBlankRow(XSSFRow row, int index, int cellCount) {
		if (row == null) {
			return true;
		}
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
