package com.cheung.utils;

import com.cheung.model.SearchModel;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Excel处理工具类
 *
 * @author Cheung
 * @version 1.0.0
 * @date 2018/2/6
 */
@Component
public class ExcelUtil {

	@Deprecated
	private static final String URL_IP138 = "http://www.ip138.com:8080/singleSearch.asp?action=mobile&mobile=%s";
	@Deprecated
	private static final String URL_TAOBAO = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm";

	/**
	 * 初始化上传的文件
	 *
	 * @param request
	 * @return 结果文件
	 * @throws Exception
	 */
	public static String init(HttpServletRequest request) throws Exception {
		// 设置文件上传路径
		String path = request.getSession().getServletContext().getRealPath(File.separator);
		String dirPath = path + "upload";
		File dirFile = new File(dirPath);
		if (!dirFile.exists()) {
			dirFile.mkdirs();// 创建文件夹
		}

		// 定义一个文件上传工厂
		FileItemFactory factory = new DiskFileItemFactory();
		// 负责专门处理上传文件的数据
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> itemList = upload.parseRequest(request);
		if (null != itemList) {
			Iterator<FileItem> iterator = itemList.iterator();
			while (iterator.hasNext()) {
				FileItem fileItem = iterator.next();
				// 判断文件上传的形式
				if (fileItem.isFormField()) {
					// 普通表单域提交的 --> 忽略掉.
					continue;
				} else {
					// 文件上传表单域提交的 --> 仅处理.
					/*
					 * 需要单独copy一份来保存
					 * 解决文件名可能重复,造成文件覆盖的问题
					 */
					System.out.println("接收到文件: " + fileItem.getName());
					String oriFile = fileItem.getName();// 原文件名
					String suffix = oriFile.substring(oriFile.lastIndexOf("."), oriFile.length());// 后缀名
					// 最终保存的文件名
					String resName = System.currentTimeMillis() + "-" + oriFile.substring(0, oriFile.lastIndexOf(".")) + suffix;

					fileItem.write(new File(dirFile, resName));// 保存

					return dirPath + File.separator + resName;// 最终保存的文件的路径
				}
			}
		}
		return null;
	}

	/**
	 * 读取文件,获取数据
	 *
	 * @param filePath 文件路径
	 * @return 文件数据
	 */
	public static Map<Integer, Map<Integer, String>> getFileData(String filePath) {

		Map<Integer, Map<Integer, String>> workbookData = new LinkedHashMap<>();

		/*
		 * 读取excel,获取data
		 */
		FileInputStream in = null;
		try {
			in = new FileInputStream(filePath);
			// 后缀名
			String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);

			if ("xls".equals(suffix.toLowerCase())) {
				HSSFWorkbook workbook = new HSSFWorkbook(in);// 当前Excel工作簿
				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					HSSFSheet sheet = workbook.getSheetAt(i);// 当前sheet
					if (null == sheet || sheet.getPhysicalNumberOfRows() <= 0) {
						workbookData.put(i, null);
						continue;
					}

					LinkedHashMap<Integer, String> sheetData = new LinkedHashMap<>(sheet.getPhysicalNumberOfRows());

					for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
						HSSFRow row = sheet.getRow(j);// 当前行(从第2行开始读)
						if (isBlankRow(row)) {
							sheetData.put(j, null);
							continue;
						}

						HSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
						phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
						sheetData.put(j, phoneNumCell.getStringCellValue());
					}
					workbookData.put(i, sheetData);
				}
			} else if ("xlsx".equals(suffix.toLowerCase())) {
				XSSFWorkbook workbook = new XSSFWorkbook(in);// 当前Excel工作簿
				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					XSSFSheet sheet = workbook.getSheetAt(i);// 当前sheet
					if (null == sheet || sheet.getPhysicalNumberOfRows() <= 0) {
						workbookData.put(i, null);
						continue;
					}

					LinkedHashMap<Integer, String> sheetData = new LinkedHashMap<>(sheet.getPhysicalNumberOfRows());

					for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
						XSSFRow row = sheet.getRow(j);// 当前行(从第2行开始读)
						if (isBlankRow(row)) {
							sheetData.put(j, null);
							continue;
						}

						XSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
						phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
						sheetData.put(j, phoneNumCell.getStringCellValue());
					}
					workbookData.put(i, sheetData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return workbookData;
	}

	/**
	 * 反写数据到指定文件
	 *
	 * @param filePath   文件路径
	 * @param searchData 数据
	 * @return
	 */
	public static String writeData(String filePath, Map<Integer, Map<Integer, SearchModel>> searchData) {

		/*
		 * 读
		 */
		FileInputStream in = null;
		FileOutputStream os = null;
		try {
			in = new FileInputStream(filePath);
			// 后缀名
			String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);

			Workbook workbook = null;
			if ("xls".equals(suffix.toLowerCase())) {
				workbook = new HSSFWorkbook(in);// 当前Excel工作簿
				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					Sheet sheet = workbook.getSheetAt(i);// 当前sheet
					if (null == sheet || sheet.getPhysicalNumberOfRows() <= 0) {
						continue;// 忽略空表
					}

					// 当前sheet表数据
					Map<Integer, SearchModel> searchModels = searchData.get(i);
					for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
						Row row = sheet.getRow(j);// 当前行(从第2行开始读)
						if (isBlankRow(row)) {
							continue;// 忽略空行
						}
						// 当前行数据
						SearchModel searchModel = searchModels.get(j);

						// 匹配数据
						Cell phoneNumCell = row.getCell(0);// 第一列(电话号码)
						phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
						if (searchModel.getPhoneNum().equals(phoneNumCell.getStringCellValue())) {
							if (null == row.getCell(1)) {
								row.createCell(1).setCellType(HSSFCell.CELL_TYPE_STRING);
							}
							row.getCell(1).setCellValue(searchModel.getArea());// 赋值
						}
					}
				}
			} else if ("xlsx".equals(suffix.toLowerCase())) {
				workbook = new XSSFWorkbook(in);// 当前Excel工作簿
				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					Sheet sheet = workbook.getSheetAt(i);// 当前sheet
					if (null == sheet || sheet.getPhysicalNumberOfRows() <= 0) {
						continue;// 忽略空表
					}

					// 当前sheet表数据
					Map<Integer, SearchModel> searchModels = searchData.get(i);
					for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
						Row row = sheet.getRow(j);// 当前行(从第2行开始读)
						if (isBlankRow(row)) {
							continue;// 忽略空行
						}
						// 当前行数据
						SearchModel searchModel = searchModels.get(j);

						// 匹配数据
						Cell phoneNumCell = row.getCell(0);// 第一列(电话号码)
						phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
						if (searchModel.getPhoneNum().equals(phoneNumCell.getStringCellValue())) {
							if (null == row.getCell(1)) {
								row.createCell(1).setCellType(HSSFCell.CELL_TYPE_STRING);
							}
							row.getCell(1).setCellValue(searchModel.getArea());// 赋值
						}
					}
				}
			}
			/*
			 * 写
			 */
			File f = new File(filePath.substring(0, filePath.lastIndexOf("-")) + "-success." + suffix);
			f.createNewFile();
			os = new FileOutputStream(f);
			workbook.write(os);
			os.flush();// 刷新此输出流并强制将所有缓冲的输出字节写出
			return f.getName();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != os) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}

	/**
	 * Excel文件批量查询归属地并导出
	 *
	 * @param filePath 文件路径
	 */
	@Deprecated
	public static String multipleSearch(String filePath) {
		/*
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
		return null;*/

		// 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
		ExecutorService exec = Executors.newCachedThreadPool();
		// 所要执行的线程任务
		SearchThread task = new SearchThread(filePath);
		// 执行
		Future<String> future = exec.submit(task);

		try {
			System.out.println("批量查询完成，结果文件：" + future.get());
			return future.get();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。
			// 一定要调用这个方法，不然executorService.isTerminated()永远不为true
			exec.shutdown();
			while (true) {//等待所有任务都结束了继续执行
				try {
					if (exec.isTerminated()) {
						System.out.println("所有的子线程都结束了！");
						break;
					}
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 采用线程的方式,便于对执行过程进行控制
	 */
	@Deprecated
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
					HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fileInputStream);// 当前Excel工作簿
					for (int m = 0; m < hssfWorkbook.getNumberOfSheets(); m++) {
						HSSFSheet sheet = hssfWorkbook.getSheetAt(m);// 当前sheet
						int rows = sheet.getPhysicalNumberOfRows();// 当前sheet的总物理行数(有数据+无数据)
						if (null != sheet && 0 != rows) {
							for (int i = 1; i < rows; i++) {

								if (i % 3000 == 0) {
									System.out.println("sleep~~~");
									// SearchThread.sleep(2000);
									TimeUnit.MILLISECONDS.sleep(2000);// 每读3000行睡2s,避免挂死
								}

								HSSFRow row = sheet.getRow(i);// 当前行(从第2行开始读)
								if (isBlankRow(row)) {
									continue;// 非空判断
								}

								HSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
								phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String phoneNum = phoneNumCell.getStringCellValue();

								try {
									/**
									 * 方式一:请求ip138接口获取数据(通过解析html获取)
									 */
									String url = String.format(URL_IP138, phoneNum);
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
									/*String str = HttpRequestUtil.sendGet(URL_TAOBAO, "tel=" + phoneNum);
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
					XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);// 当前Excel工作簿
					for (int m = 0; m < xssfWorkbook.getNumberOfSheets(); m++) {
						XSSFSheet sheet = xssfWorkbook.getSheetAt(m);// 当前sheet
						int rows = sheet.getPhysicalNumberOfRows();// 当前sheet的总物理行数(有数据+无数据)
						if (null != sheet && 0 != rows) {
							for (int i = 1; i < rows; i++) {

								if (i % 3000 == 0) {
									System.out.println("sleep~~~");
									// SearchThread.sleep(2000);
									TimeUnit.MILLISECONDS.sleep(2000);// 每读3000行睡2s,避免挂死
								}

								XSSFRow row = sheet.getRow(i);// 当前行(从第2行开始读)
								if (isBlankRow(row)) {
									continue;// 非空判断
								}

								XSSFCell phoneNumCell = row.getCell(0);// 第一列(电话号码)
								phoneNumCell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String phoneNum = phoneNumCell.getStringCellValue();

								try {
									/**
									 * 方式一:请求ip138接口获取数据(通过解析html获取)
									 */
									String url = String.format(URL_IP138, phoneNum);
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
									/*String str = HttpRequestUtil.sendGet(URL_TAOBAO, "tel=" + phoneNum);
									JSONObject obj = ParseUtil.parseTBStr(str);
									String area = obj.getString("carrier");*/

									System.out.println("第" + i + "条：" + phoneNum + "---" + area);
									if (null == row.getCell(1)) {
										row.createCell(1).setCellType(HSSFCell.CELL_TYPE_STRING);
									}
									row.getCell(1).setCellValue(area);
								} catch (Exception e) {
									e.printStackTrace();
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
	@Deprecated
	private static String createResultExcel(String filePath, Workbook workbook, String suffix) {
		try {
			File f = null;
			if (!filePath.contains("-")) {
				// 通过main方法直接批量查询
				f = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1) + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".")) + "-success." + suffix);
			} else {
				f = new File(filePath.substring(0, filePath.lastIndexOf("-")) + "-success." + suffix);
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
	 * 非空行判断
	 *
	 * @param row
	 * @return
	 */
	private static boolean isBlankRow(Row row) {
		if (null == row) {
			return true;
		}
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i);
			cell.setCellType(Cell.CELL_TYPE_STRING);
			if (null != row.getCell(i) && !StringUtils.isBlank(row.getCell(i).getStringCellValue().trim())) {
				return false;
			}
		}
		return true;
	}

}
