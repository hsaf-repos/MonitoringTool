import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Product {

	private static final double MINUTE_TO_MILLSECONDS_MULTIPLIER = 60000;

	private static final long BYTE_TO_KBYTES = 100;

	private static double HOUR_TO_MINUTE = 60;

	private String prodId;
	private String folderName;
	private double prodRateOnMonitorInterval;
	private double dailyProdRate;
	private double minSize;
	private double monitorTimeIntervalMinutes;
	private Status generationRateStatus;
	private Status sizeStatus;
	private File logFile;

	private Map<String, BasicFileAttributes> processedFileList;

	private long lastUpdate;

	private double dailyGenerated;

	private Status dailyGenerationStatus;

	public Product(String prodId, String folderName, String prodRateMin, String dailyProdRate, String minSize,
			String monitorTimeInterval) {
		super();
		this.prodId = prodId;
		this.folderName = folderName;
		this.prodRateOnMonitorInterval = Double.parseDouble(prodRateMin);
		this.dailyProdRate = Double.parseDouble(dailyProdRate);
		this.processedFileList = new HashMap<String, BasicFileAttributes>();
		this.minSize = Double.parseDouble(minSize);
		this.dailyGenerated = 0;
		this.setMonitorTimeInterval(Double.parseDouble(monitorTimeInterval) * MINUTE_TO_MILLSECONDS_MULTIPLIER);
		this.logFile = new File(Main.LOG_FILE_PATH + "/" + this.prodId + ".log");
		this.setGenerationRateStatus(Status.OK);
		this.setDailyGenerationStatus(Status.OK);
		this.setSizeStatus(Status.OK);
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public double getProdRateMin() {
		return prodRateOnMonitorInterval;
	}

	public void setProdRateMin(String prodRateMin) {
		prodRateOnMonitorInterval = Double.parseDouble(prodRateMin);
	}

	public double getDailyProdRate() {
		return dailyProdRate;
	}

	public void setDailyProdRate(String dailyProdRate) {
		this.dailyProdRate = Double.parseDouble(dailyProdRate);
	}

	public double getNumOfFiles() {

		return this.processedFileList.size();
	}

	public void addFileToTheList(String name, BasicFileAttributes fileAttribute) {

		this.processedFileList.put(name, fileAttribute);

	}

	public void checkStatus() {

		FileWriter logFileWriter = null;
		try {
			logFileWriter = new FileWriter(this.logFile, true);
			logFileWriter.append(">> START Date " + new Date() + "\n");

			long currentTime = System.currentTimeMillis();

			long deltaTimeMinutes = (long) ((currentTime - this.lastUpdate) / MINUTE_TO_MILLSECONDS_MULTIPLIER);

			// Get the product status before the check
			Status currentGenerationRateStatus = this.getGenerationRateStatus();
			Status currentDailyGenerationStatus = this.getPreviousDailyGenerationStatus();
			Status currentSizeStatus = this.getSizeStatus();

			if (deltaTimeMinutes > this.getMonitorTimeInterval()) {

				checkGenerationRate(logFileWriter, deltaTimeMinutes);
				checkMinimumSize(logFileWriter);
			}

			Calendar calendar = Calendar.getInstance();

			if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {

				checkDailyProduction(logFileWriter);
				this.dailyGenerated = 0;
			}

			logFileWriter.append("<< END Date " + new Date() + "\n\n");
			logFileWriter.close();

			EmailServer emailServer = new EmailServer();
			boolean alarm = false;
			String msgStr = "";

			// Send an email if a transition from OK to NOK occurred in one of the three
			// status
			if (this.generationRateStatus == Status.NOK && currentGenerationRateStatus == Status.OK) {
				msgStr = "Generation Rate Status:" + this.generationRateStatus + " Generated " + this.processedFileList
						+ " in " + deltaTimeMinutes + " minutes ( expected " + this.prodRateOnMonitorInterval + " )";
				alarm = true;
			}
			if (this.dailyGenerationStatus == Status.NOK && currentDailyGenerationStatus == Status.OK) {
				msgStr = msgStr + "/n Daily Rate Status:" + this.dailyGenerationStatus + " Generated = "
						+ this.dailyGenerated + " - Expected = " + this.dailyProdRate;
				alarm = true;
			}
			if (this.sizeStatus == Status.NOK && currentSizeStatus == Status.OK) {
				msgStr = msgStr + "/n Size Status:" + this.sizeStatus;
				alarm = true;
			}
			if (alarm)
				emailServer.sendmailHsaf(msgStr, this.prodId,"");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}

	private void checkDailyProduction(FileWriter logFileWriter) {

		Status currentStatus = Status.OK;
		if (this.dailyGenerated < this.dailyProdRate || this.dailyGenerationStatus == Status.NOK) {
			currentStatus = Status.NOK;
		}

		this.setDailyGenerationStatus(currentStatus);
	}

	private void checkMinimumSize(FileWriter logFileWriter) throws IOException {

		String listOfFilesFailed = "";
		Status currentStatus = Status.OK;
		for (Map.Entry<String, BasicFileAttributes> entry : this.processedFileList.entrySet()) {
			if (entry.getValue().size() < this.minSize) {

				currentStatus = Status.NOK;
				System.out.println();
				listOfFilesFailed = listOfFilesFailed + entry.getKey() + "   " + String.valueOf(entry.getValue().size())
						+ "\n";
			}
		}

		if (this.sizeStatus == Status.NOK)
			currentStatus = Status.NOK;

		this.setSizeStatus(currentStatus);

		logFileWriter.append("* Size Status " + this.getGenerationRateStatus().toString() + " \n");
		logFileWriter.append(listOfFilesFailed);
	}

	private void checkGenerationRate(FileWriter logFileWriter, long deltatimeMinutes) throws IOException {

		double numOfFiles = this.processedFileList.size();
		Status currentStatus;

		// IF the production rate computed between two status update is less than the
		// expected one
		// NOTE: the comparison is done between expected product per minutes and the
		// generated products per minutes
		if (numOfFiles != 0 && (numOfFiles / deltatimeMinutes) < (this.prodRateOnMonitorInterval
				/ this.monitorTimeIntervalMinutes)) {
			currentStatus = Status.NOK;
			this.setGenerationRateStatus(currentStatus);
			logFileWriter.append("* Generation rate Status " + this.getGenerationRateStatus().toString() + " *\n");

			for (Map.Entry<String, BasicFileAttributes> entry : this.processedFileList.entrySet()) {
				logFileWriter.append(entry.getKey() + "   " + entry.getValue().creationTime().toString() + " "
						+ entry.getValue().size() / BYTE_TO_KBYTES + "\n");
			}
		} else {
			currentStatus = Status.OK;
			if (this.generationRateStatus == Status.NOK)
				currentStatus = Status.NOK;

			// Set product status for the next run
			this.setGenerationRateStatus(currentStatus);
		}

	}

	public void setDailyGenerationStatus(Status dailyGenerationStatus) {
		this.dailyGenerationStatus = dailyGenerationStatus;
	}

	public Status getGenerationRateStatus() {
		return generationRateStatus;
	}

	public void setGenerationRateStatus(Status status) {
		this.generationRateStatus = status;
	}

	public File getLogFile() {
		return logFile;
	}

	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}

	public double getMonitorTimeInterval() {
		return monitorTimeIntervalMinutes;
	}

	public void setMonitorTimeInterval(double monitorTimeInterval) {
		this.monitorTimeIntervalMinutes = monitorTimeInterval;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Status getSizeStatus() {
		return sizeStatus;
	}

	public void setSizeStatus(Status sizeStatus) {
		this.sizeStatus = sizeStatus;
	}

	public void setDailyGenerated(double dailyGeneratedCounter) {
		this.dailyGenerated = dailyGeneratedCounter;

	}

	public double getDailyGeneratedProd() {
		return this.dailyGenerated;
	}

	public Status getPreviousDailyGenerationStatus() {
		return this.dailyGenerationStatus;
	}

}
