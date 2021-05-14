import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;



public class Main {

	public static String PRODUCTS_PATH;
	public static String CONFING_FILE;
	public static String LAST_UPDATE_FILE_SUFFIX;
	public static String LOG_FILE_PATH;

	public static int PRODUCT_ID = 0;
	public static int PRODUCT_FOLDER_NAME = 1;
	public static int PROD_RATE_MIN = 2;
	public static int DAILY_PROD_RATE = 3;
	public static int MIN_SIZE = 4;
	public static int MONITOR_TIME_INTERVAL_MINUTE = 5;

	public final static String SOIL_cl = "SOIL";
	public final static String SNOW_cl = "SNOW";
	public final static String PREP_cl = "PREP";

	private final static String REPORTS_DIRECTORY = "/UMARF/safclient/autoUMARF/reports";

	private final static String OK_STATUS = "ok";
	private final static String WARN_STATUS = "warning";
	private final static String ERROR_STATUS = "error";

	public static long lastUpdate;

	public static void main(String[] args) {

		String os = System.getProperty("os.name");

		if (os.contains("Windows")) {
			// Section to be enabled for testing
			PRODUCTS_PATH = "C:\\Users\\telespazio\\Documents\\HSAF\\eclipse-workspace\\MonitoringTool\\input";
			CONFING_FILE = "C:\\Users\\telespazio\\Documents\\HSAF\\eclipse-workspace\\MonitoringTool\\resource\\monitor.config";
			LAST_UPDATE_FILE_SUFFIX = "C:\\Users\\telespazio\\Documents\\HSAF\\eclipse-workspace\\MonitoringTool\\resource\\lastUpdate";
			LOG_FILE_PATH = "C:\\Users\\Telespazio\\eclipse-workspace\\MonitoringTool\\log";

		} else {

			// Section to be enabled when deployed on target machine
			PRODUCTS_PATH = "/ext_storage/HSAF_FTP/products";
			CONFING_FILE = "/var/hsaf/monitoringtool/monitor.config";
			LAST_UPDATE_FILE_SUFFIX = "/var/www/html/lastUpdate";
			LOG_FILE_PATH = "/var/www/html/logs/";
		}

		for (String product : Products.getListOfProducts()) {

			String status = OK_STATUS;
			ArrayList<String> fileSizes = new ArrayList<String>();
			ArrayList<String> statusLines = parseStatusFile(product);

			Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

//			for (String line : statusLines) {

			try {
				HsafProductStatus hsafProductStatus = ProductCriteria.getProduct(product, now);
				HashMap<String, String> result = hsafProductStatus.checkStatus(product, statusLines, fileSizes);
				
				updateStatusFile(product,hsafProductStatus, result);

			} catch (Exception e) {
				e.printStackTrace();
			}
//			}
		}
		System.out.println("--------------- "+ new Date() +" CYCLE DONE ---------------");
	}

	private static void updateStatusFile(String product, HsafProductStatus hsafProductStatus, HashMap<String, String> result) throws IOException {
		// Update last update tine for this product
		FileWriter myWriter = null;
		
		myWriter = new FileWriter(LAST_UPDATE_FILE_SUFFIX + "." + product);
		
		myWriter.write(Long.toString(System.currentTimeMillis()) + ",");
		// Update daily generation for product

		Boolean[] currStatus = hsafProductStatus.getCurrentStatus();
		String currentOverallStatus = result.get("status");
	
		// Overall status :  "ok", "warning" or "error";
		myWriter.write(currentOverallStatus +",");
		myWriter.write(Status.getStatusValue(currStatus [HsafProductStatus.HSAF_STATUS.GENERATION_INTERRUPT.getCode()]) +",");		
		myWriter.write(Status.getStatusValue(currStatus [HsafProductStatus.HSAF_STATUS.FREQUENCY_RATE.getCode()])+",");
		myWriter.write(Status.getStatusValue(currStatus [HsafProductStatus.HSAF_STATUS.TOTAL_EXPECTED.getCode()])+",");
		myWriter.write(Status.getStatusValue(currStatus [HsafProductStatus.HSAF_STATUS.EMPTY_LIST.getCode()]) +",");
		myWriter.write(Status.getStatusValue(currStatus [HsafProductStatus.HSAF_STATUS.EMPTY_FILE.getCode()]) +",");
		myWriter.write(Status.getStatusValue(currStatus [HsafProductStatus.HSAF_STATUS.MINIMUM_SIZE.getCode()]) +",");

		// Update Status for product
		myWriter.close();
	}

	private static ArrayList<String> parseStatusFile(String product) {
		System.out.println("Parsing reports for " + product);
		ArrayList<String> statusFileLines = new ArrayList<String>();
		File reportDir = new File(REPORTS_DIRECTORY);
		try {
			if (reportDir.isDirectory()) {
				final String statusName = String.format("%s.status", product);
				FilenameFilter filenameFilter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.equalsIgnoreCase(statusName);
					}
				};
				File[] statusFiles = reportDir.listFiles(filenameFilter);
				if (statusFiles.length != 0) {
					statusFileLines = readLines(statusFiles[0].getAbsolutePath());
				} else {
					System.out.println("No status file found for " + product);
				}
			}
		} catch (IOException e) {
			System.out.println("Exception in parsing report file: " + e.getMessage());
		}
		return statusFileLines;
	}

	private static ArrayList<String> readLines(String filename) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		System.out.println("Reading status file: " + filename);
		// use buffering, reading one line at a time
		// FileReader always assumes default encoding is OK!
		BufferedReader input = new BufferedReader(new FileReader(filename));
		try {
			String line = null; // not declared within while loop
			/*
			 * readLine is a bit quirky : it returns the content of a line MINUS the
			 * newline. it returns null only for the END of the stream. it returns an empty
			 * String if two newlines appear in a row.
			 */
			while ((line = input.readLine()) != null) {
				lines.add(line);
			}
		} finally {
			input.close();
		}
		return lines;
	}



}
