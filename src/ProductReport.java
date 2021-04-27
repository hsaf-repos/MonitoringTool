import java.util.Date;

class ProductReport {

	private String productFile    = "";
	private Date   generationTime = null;
	private Date   sensingTime    = null;
	private String sizeInBytes    = "";
	private String satelliteId    = "Not available";
	private boolean isQualityCheckFile = false;


	public ProductReport(String productFile, Date generationTime, Date sensingTime,
			String sizeInBytes, String satelliteId, boolean isQualityCheckFile){
		this.productFile = productFile;
		this.generationTime = generationTime;
		this.sensingTime = sensingTime;
		this.sizeInBytes = sizeInBytes;
		this.satelliteId = satelliteId;
		this.isQualityCheckFile = isQualityCheckFile;
	}

	
	public String getProductFile() {
		return this.productFile;
	}

	public String getSatelliteId() {
		return this.satelliteId;
	}
	
	public String getSizeInBytes() {
		return this.sizeInBytes;
	}

	public Date getSensingTime() {
		return this.sensingTime;
	}

	public Date getGenerationTime() {
		return this.generationTime;
	}
	
	public boolean isQualityCheckFile() {
		return this.isQualityCheckFile;
	}
}
