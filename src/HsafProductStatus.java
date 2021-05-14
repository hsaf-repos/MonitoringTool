import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * @author rinaldi
 * 
 */
public class HsafProductStatus {

	// HSAF SEVERITY
	private final static String OK_STATUS = "ok";
	private final static String WARN_STATUS = "warning";
	private final static String ERROR_STATUS = "error";
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS '('z')'");

	public static enum HSAF_STATUS {
		NONE(0, "No Anomalies"), EMPTY_LIST(1, "Empty file list"),
		GENERATION_INTERRUPT(2, "Operational Chain generation interrupt"),
		MINIMUM_SIZE(3, "Minimum file size not reached"), FREQUENCY_RATE(4, "Expected generation rate not reached"),
		TOTAL_EXPECTED(5, "Total number of expected files not reached"), EMPTY_FILE(6, "Empty file generated"),
		CRITICAL(7, "Critical");

		private int code;
		private String extendedStatus;

		private HSAF_STATUS(int code, String extendedStatus) {
			this.code = code;
			this.extendedStatus = extendedStatus;
		}

		protected int getCode() {
			return code;
		}

		protected String getExtendedStatus() {
			return extendedStatus;
		}

		static String getExtendedStatusByCode(int code) {
			return HSAF_STATUS.getEnumByCode(code).getExtendedStatus();
		}

		static HSAF_STATUS getEnumByCode(int code) {
			for (HSAF_STATUS status : HSAF_STATUS.values()) {
				if (status.getCode() == code) {
					return status;
				}
			}
			return HSAF_STATUS.NONE;
		}

		protected String getStatus() {
			return String.format("%02d", code);
		}
	}

	// Current values of checks
	private Boolean[] currStatus = new Boolean[HSAF_STATUS.values().length];

	// Previous values of checks
	private Boolean[] prevStatus = new Boolean[HSAF_STATUS.values().length];

	// Error description List
	private String[] errorStatus = new String[HSAF_STATUS.values().length];

	// Product ID
	private String product;

	// Check criteria
	private int totalExpected;
	private int productionRate;
	private int startTime;
	private int endTime;
	private int dataNum;
	private int minSize;
	private boolean isDateToBeChecked;
	private Calendar now;
	private String cluster;

	// Product history
	private ManageHistory historyProduct = null;

	/**
	 * Initializing constructor.
	 * 
	 * @param product        Product identifier (h1, h2, h3, ...)
	 * @param processingTime Instant of status calculation
	 * @param totalExpected  Total number of products produced in a day (this value
	 *                       is checked as the minimum number of files in a day)
	 * @param productionRate Rate in minutes of the production for the particular
	 *                       product, e.g. every 15mins, 1hr (60), 24hr (3600), etc
	 * @param startTime      Start hour of the check window (if startTime=99 &&
	 *                       endTime=99 check always active)
	 * @param endTime        Stop hour of the check window (if startTime=99 &&
	 *                       endTime=99 check always active)
	 * @param dataNum        The number of data generated every
	 *                       {@code productionRate} minutes, e.g. 3 files every 1hr
	 * @param minSize        The size of files for the specific product [Bytes],
	 *                       e.g. 50000 = 50KB
	 */
	public HsafProductStatus(String product, Calendar processingTime, int totalExpected, int productionRate,
			int startTime, int endTime, int dataNum, int minSize) {
		this.product = product;
		this.now = processingTime;
		this.totalExpected = totalExpected;
		this.productionRate = productionRate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.dataNum = dataNum;
		this.minSize = minSize;
		this.isDateToBeChecked = true;
		if ((startTime == 99) && (endTime == 99)) {
			this.isDateToBeChecked = false;
		}

		// Reset current and previous status array (all check results to false)
		resetStatus();

		// Init history and Previous status array
		historyProduct = new ManageHistory();
		historyProduct.setHistoryProduct(product);
		setPrevStatus();

		// CLUSTER recipient for mail
		// H03A dismissed starting from 11/04/2019
		// H05A dismissed starting from 11/04/2019
		if (product.equals(Products.H01)
				/* || product.equals(Products.H01B) */
				/* || product.equals(Products.H02) */
				|| product.equals(Products.H02B) || product.equals(Products.H03B) || product.equals(Products.H04)
				|| product.equals(Products.H05B) || product.equals(Products.H15) || product.equals(Products.H17)
				|| product.equals(Products.H18)) {
			cluster = Main.PREP_cl;
		} else if (product.equals(Products.H10) || product.equals(Products.H11) || product.equals(Products.H12)
				|| product.equals(Products.H13)) {
			cluster = Main.SNOW_cl;
		} else if (product.equals(Products.H08) || product.equals(Products.H14) || product.equals(Products.H16)
				|| product.equals(Products.H101) || product.equals(Products.H102) || product.equals(Products.H103)) {
			cluster = Main.SOIL_cl;
		}
	}

	// Reset all status
	private void resetStatus() {
		// Init Status Array
		for (int i = 0; i < HSAF_STATUS.values().length; i++) {
			currStatus[i] = false;
			prevStatus[i] = false;
			errorStatus[i] = "";
		}
	}

	private void setPrevStatus() {
		// for (int i = 0; i < HSAF_STATUS.values().length; i++) {
		// prevStatus[i] =
		// !historyProduct.checkAnomalyStatus(HSAF_STATUS.getEnumByCode(i).getStatus());
		// }
		prevStatus[HSAF_STATUS.GENERATION_INTERRUPT.getCode()] = !historyProduct
				.checkAnomalyStatus(HSAF_STATUS.GENERATION_INTERRUPT.getStatus());
		prevStatus[HSAF_STATUS.FREQUENCY_RATE.getCode()] = !historyProduct
				.checkAnomalyStatus(HSAF_STATUS.FREQUENCY_RATE.getStatus());
		prevStatus[HSAF_STATUS.TOTAL_EXPECTED.getCode()] = !historyProduct
				.checkAnomalyStatus(HSAF_STATUS.TOTAL_EXPECTED.getStatus());
	}

	private boolean isGenDateInNoCheckZone(Calendar startCal, Calendar endCal, Calendar generationCal) {
		boolean result = false;
		DateFormat df = new SimpleDateFormat("HHmmss");
		String startStr = df.format(startCal.getTime());
		long start = Long.parseLong(startStr);
		String endDStr = df.format(endCal.getTime());
		long end = Long.parseLong(endDStr);
		String genStr = df.format(generationCal.getTime());
		long gen = Long.parseLong(genStr);
		if (gen <= start || gen >= end) {
			result = true;
		}
		return result;
	}

	// 01 - EMPTY LIST
	private void checkOnEmptyList(int numberOfFiles, Calendar now, Calendar startCal, Calendar endCal) {
		if (numberOfFiles == 0) {
			if (isDateToBeChecked == true) {
				if (now.before(startCal) || now.after(endCal)) {
					currStatus[HSAF_STATUS.GENERATION_INTERRUPT.getCode()] = true;
				}
			}
		}
	}

	// 02 - GENERATION INTERRUPT
	private void checkOnGenerationInterrupt(int numberOfFiles, Calendar now, Calendar startCal, Calendar endCal) {
		if (numberOfFiles == 0) {
			if (isDateToBeChecked == true) {
				if (!(now.before(startCal) || now.after(endCal))) {
					currStatus[HSAF_STATUS.GENERATION_INTERRUPT.getCode()] = true;
				}
			} else {
				currStatus[HSAF_STATUS.GENERATION_INTERRUPT.getCode()] = true;
			}
		}
	}

	// 03 - MINIMUM SIZE
	private void checkOnMinimumSize(ArrayList<String> fileSizes) {
		for (String size : fileSizes) {
			int sizeFile = Integer.parseInt(size);
			if (sizeFile < minSize) {
				currStatus[HSAF_STATUS.MINIMUM_SIZE.getCode()] = true;
			}
		}
	}

	// 04 - FREQUENCY RATE
	private void checkOnFrequencyRate(Calendar now, Calendar startCal, Calendar endCal, Calendar generationCal) {
		long nowInMillis = now.getTimeInMillis();
		long startCalInMillis = startCal.getTimeInMillis();
		long endCalInMillis = endCal.getTimeInMillis();
		long generationInMillis = generationCal.getTimeInMillis();
		long delayInMillis = 0; // Default value, means no check to be performed
		// current day (now)
		// |xxxxx|---------------------------|xxxx|
		// ^ ^ ^ ^
		// 23|0 |<-startCal endCal->| 23|0
		//
		// ^
		// |xxxxx| <- no check area
		// no check area ->|xxxxx|
		//
		// "No check" area is set
		if (isDateToBeChecked == true) {
			// IF "now" is inside the check area
			if (!(now.after(endCal) || now.before(startCal))) {
				long diffDays;
				if (now.get(Calendar.YEAR) > generationCal.get(Calendar.YEAR)) {
					diffDays = (generationCal.getActualMaximum(Calendar.DAY_OF_YEAR) + now.get(Calendar.DAY_OF_YEAR))
							- generationCal.get(Calendar.DAY_OF_YEAR);
				} else {
					diffDays = now.get(Calendar.DAY_OF_YEAR) - generationCal.get(Calendar.DAY_OF_YEAR);
				}
				long h24InMillis = (24 * 60 * 60 * 1000);
				long noCheckRangeInMillis = h24InMillis - (endCalInMillis - startCalInMillis);
				// if the generation time of the product is within the range of
				// non-production
				// the frequency rate must be calculated from "stratCal" and not
				// from the generation
				// time of product.
				if (isGenDateInNoCheckZone(startCal, endCal, generationCal)) {
					delayInMillis = (nowInMillis - startCalInMillis);
				} else {
					delayInMillis = (nowInMillis - generationInMillis) - (diffDays * noCheckRangeInMillis);
				}
				// IF "now" is outside the check area
			} else {
				// DO NOTHING
				delayInMillis = 0;
			}
			// "No check" area is not set
		} else {
			delayInMillis = nowInMillis - generationInMillis;
		}
		long delayMins = delayInMillis / (60 * 1000);
		if (delayMins > productionRate) {
			currStatus[HSAF_STATUS.FREQUENCY_RATE.getCode()] = true;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS '('z')'");
			String generationDateStr = df.format(generationCal.getTime());
			String msg = "Expected generation rate <" + dataNum + "/" + productionRate + "min> " +
			// "Observed generation rate <" +dataNum + "/" + delayMins
			// +"min> " +
					"Last product generated on <" + generationDateStr + ">";
			errorStatus[HSAF_STATUS.FREQUENCY_RATE.getCode()] = msg;
			System.out.println(msg);
		}
	}

	// 05 - TOTAL EXPECTED
	private void checkOnTotalExpected(int numberOfFiles) {
		if (numberOfFiles < totalExpected) {
			currStatus[HSAF_STATUS.TOTAL_EXPECTED.getCode()] = true;
			String msg = "Expected products <" + totalExpected + "> " + "Generated products <" + numberOfFiles + ">";
			errorStatus[HSAF_STATUS.TOTAL_EXPECTED.getCode()] = msg;
			System.out.println(msg);
		}
	}

	// 06 - EMPTY FILE
	private void checkOnEmptyFile(ArrayList<String> fileSizes) {
		for (String size : fileSizes) {
			int sizeFile = Integer.parseInt(size);
			if (sizeFile == 0) {
				currStatus[HSAF_STATUS.EMPTY_FILE.getCode()] = true;
			}
		}
	}

	/**
	 * CheckStatus checks error policies against criteria for each product return
	 * the processing time
	 */

	private void computeCurrStatus(String prod, ArrayList<String> files, Date generationDate,
			ArrayList<String> fileSizes) {
		int numberOfFiles = files.size();
		Calendar startCal = (Calendar) now.clone();
		Calendar endCal = (Calendar) now.clone();
		Calendar generationCal = (Calendar) now.clone();
		if (generationDate != null) {
			generationCal.setTime(generationDate);
			// startCal.setTime(generationDate);
			startCal.set(Calendar.HOUR_OF_DAY, startTime);
			startCal.set(Calendar.MINUTE, 0);
			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MILLISECOND, 0);
			// endCal.setTime(generationDate);
			if (endTime == 0) {
				endCal.set(Calendar.HOUR_OF_DAY, 23);
				endCal.set(Calendar.MINUTE, 59);
				endCal.set(Calendar.SECOND, 59);
			} else {
				endCal.set(Calendar.HOUR_OF_DAY, endTime);
				endCal.set(Calendar.MINUTE, 0);
				endCal.set(Calendar.SECOND, 0);
			}
			endCal.set(Calendar.MILLISECOND, 0);
		}
		// 01 - Empty List
		checkOnEmptyList(numberOfFiles, now, startCal, endCal);
		// 02 - Generation Interrupt
		checkOnGenerationInterrupt(numberOfFiles, now, startCal, endCal);
		// 03 - Minimum Size
		checkOnMinimumSize(fileSizes);
		// 04 - Frequency Rate
		checkOnFrequencyRate(now, startCal, endCal, generationCal);
		// 05 -Total Expected
		checkOnTotalExpected(numberOfFiles);
		// 06 - Empty File
		checkOnEmptyFile(fileSizes);
	}

	public HashMap<String, String> checkStatus(String prod, ArrayList<String> files, ArrayList<String> fileSizes) {
		HashMap<String, String> status = new HashMap<String, String>();
		status.put("status", OK_STATUS);
		status.put("value", HSAF_STATUS.NONE.getStatus());
		status.put("critical", "no");
		Date generationDate = null;
		if (files.size() - dataNum >= 0) {
			String line = files.get(files.size() - dataNum);
			ProductReport productReport = ProductReportParser.parseReport(prod, line);
			generationDate = productReport.getGenerationTime();
		}

		computeCurrStatus(prod, files, generationDate, fileSizes);
		// EXIT WITH A SINGLE ERROR - ERROR HIERARCHY:
		//
		// ERROR
		// 1. GENERATION_INTERRUPT
		// 2.a FREQ RATE && 2.b TOTAL_EXPECTED => CRITICAL
		// WARNING
		// 3. Empty list
		// 4. Empty file
		// 5. Minimum size
		// OK
		// 6. None
		if (currStatus[HSAF_STATUS.GENERATION_INTERRUPT.getCode()] == true) {
			status.put("status", ERROR_STATUS);
			status.put("value", HSAF_STATUS.GENERATION_INTERRUPT.getStatus());
		} else if (currStatus[HSAF_STATUS.FREQUENCY_RATE.getCode()] == true
				&& currStatus[HSAF_STATUS.TOTAL_EXPECTED.getCode()] == true) {
			status.put("status", ERROR_STATUS);
			status.put("value", HSAF_STATUS.FREQUENCY_RATE.getStatus());
			status.put("critical", "yes");
		} else if (currStatus[HSAF_STATUS.FREQUENCY_RATE.getCode()] == true) {
			status.put("status", ERROR_STATUS);
			status.put("value", HSAF_STATUS.FREQUENCY_RATE.getStatus());
		} else if (currStatus[HSAF_STATUS.TOTAL_EXPECTED.getCode()] == true) {
			status.put("status", ERROR_STATUS);
			status.put("value", HSAF_STATUS.TOTAL_EXPECTED.getStatus());
		} else if (currStatus[HSAF_STATUS.EMPTY_LIST.getCode()] == true) {
			status.put("status", WARN_STATUS);
			status.put("value", HSAF_STATUS.EMPTY_LIST.getStatus());
		} else if (currStatus[HSAF_STATUS.EMPTY_FILE.getCode()] == true) {
			status.put("status", WARN_STATUS);
			status.put("value", HSAF_STATUS.EMPTY_FILE.getStatus());
		} else if (currStatus[HSAF_STATUS.MINIMUM_SIZE.getCode()] == true) {
			status.put("status", WARN_STATUS);
			status.put("value", HSAF_STATUS.MINIMUM_SIZE.getStatus());
		} else {
			status.put("status", OK_STATUS);
			status.put("value", HSAF_STATUS.NONE.getStatus());
		}

		boolean emailOnGenerationInterrupt = false;
		boolean emailOnFrequencyRate = false;
		boolean emailOnTotalExpected = false;
		// UPDATE HISTORY FILE:
		if (currStatus[HSAF_STATUS.GENERATION_INTERRUPT.getCode()] == true) {
			emailOnGenerationInterrupt = updateHistoryFile(HSAF_STATUS.GENERATION_INTERRUPT);
		} else {
			emailOnGenerationInterrupt = updateHistoryFile(HSAF_STATUS.GENERATION_INTERRUPT);
			emailOnFrequencyRate = updateHistoryFile(HSAF_STATUS.FREQUENCY_RATE);
			emailOnTotalExpected = updateHistoryFile(HSAF_STATUS.TOTAL_EXPECTED);
		}
		// SEND EMAIL TO WORN THE END USER
		if (emailOnGenerationInterrupt || emailOnFrequencyRate || emailOnTotalExpected) {
			String alarmReport = "";
			int errorCode = Integer.parseInt(status.get("value"));
			// EMAIL WITH CRITICAL ERROR
			if (status.get("critical").equals("yes")) {
				alarmReport = "This is an automatic email notification - please do not reply to this message. \r\n"
						+ "\r\n" + "H-SAF Monitoring tool has found the following critical anomaly for the product "
						+ product + ": \r\n" + "\r\n" + "- " + HSAF_STATUS.FREQUENCY_RATE.getExtendedStatus() + "\r\n"
						+ "  " + errorStatus[HSAF_STATUS.FREQUENCY_RATE.getCode()] + "\r\n" + "\r\n" + "- "
						+ HSAF_STATUS.TOTAL_EXPECTED.getExtendedStatus() + "\r\n" + "  "
						+ errorStatus[HSAF_STATUS.TOTAL_EXPECTED.getCode()];
				// EMAIL WITH SIMPLE ERROR
			} else {
				alarmReport = "This is an automatic email notification - please do not reply to this message. \r\n"
						+ "\r\n" + "H-SAF Monitoring tool has found the following anomaly for the product " + product
						+ ": \r\n" + "\r\n" + "- " + HSAF_STATUS.getExtendedStatusByCode(errorCode) + "\r\n" + "  "
						+ errorStatus[errorCode];
			}
			EmailServer emailServer = new EmailServer();
			emailServer.sendmailHsaf(prod, alarmReport, cluster);
			System.out.println("EMAIL TO <" + cluster + ">:\n" + alarmReport);
		}
		logStatus();
		return status;
	}

	private boolean updateHistoryFile(HSAF_STATUS status) {
		boolean result = false;
		// OPEN ANOMALY on history file
		if (prevStatus[status.getCode()] == false && currStatus[status.getCode()] == true) {
			result = true;
			historyProduct.storeAnomaly(status);
			// CLOSE ANOMALY on history file
		} else if (prevStatus[status.getCode()] == true && currStatus[status.getCode()] == false) {
			result = false;
			String rowId = historyProduct.getOpenAnomaly(status.getStatus());
			historyProduct.storeResolutionDate(rowId);
		} else {
			result = false;
			System.out.println("No action on history file for product <" + product + ">: " + status.getCode() + ";"
					+ currStatus[status.getCode()]);
		}
		return result;
	}

	public void logStatus() {
		StringBuilder curr = new StringBuilder();
		StringBuilder prev = new StringBuilder();
		curr.append("CURRENT  STATUS (").append(product).append("): [ ");
		prev.append("PREVIOUS STATUS (").append(product).append("): [ ");
		for (int i = 1; i < HSAF_STATUS.values().length; i++) {
			curr.append(HSAF_STATUS.getEnumByCode(i).getStatus()).append("=").append(currStatus[i]);
			curr.append(" ");
			prev.append(HSAF_STATUS.getEnumByCode(i).getStatus()).append("=").append(prevStatus[i]);
			prev.append(" ");
		}
		curr.append("]");
		prev.append("]");
		System.out.println("PROCESSING TIME: " + df.format(now.getTime()));
		System.out.println(curr.toString());
		System.out.println(prev.toString());
	}

	public Boolean[] getCurrentStatus() {
		// TODO Auto-generated method stub
		return this.currStatus;
	}

}
