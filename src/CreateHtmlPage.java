import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CreateHtmlPage {
	
	public static String HTML_FILE_TEMPLATE = "C:\\Users\\Telespazio\\eclipse-workspace\\MonitoringTool\\resource\\monitoringTool_template.html";
	public static String HTML_FILE = "C:\\Users\\Telespazio\\eclipse-workspace\\MonitoringTool\\resource\\\\monitoringTool.html";

	public static void buildPage(List<Product> productList) throws IOException {
		
		
		File htmlTemplateFile = new File(HTML_FILE_TEMPLATE);
		String htmlString = FileUtils.readFileToString(htmlTemplateFile);
		File newHtmlFile = new File(HTML_FILE);
		for (Product product : productList) {
			
			String prodId = product.getProdId();
			String generationStatus = product.getGenerationRateStatus().toString();
			String dailyGenerationStatus = product.getPreviousDailyGenerationStatus().toString();
			String sizeStatus = product.getSizeStatus().toString();
			String productLog = product.getLogFile().getAbsolutePath();
			
			
			
			String prodIdStr = "$" + product.getProdId() + "$";
			String prodGenerationStatusStr = "$" + product.getProdId() + "_generationStatus$";
			String prodDailyGenStatusStr = "$" + product.getProdId() + "_dailyGenStatus$";
			String prodSizeStatusStr = "$" + product.getProdId() + "_sizeStatus$";
			String prodLogStr = "$" + product.getProdId() + "_logFile$";
			String prodLogResetStr = "$" + product.getProdId() + "_path_to_log_file$";
			
			htmlString = htmlString.replace(prodIdStr, prodId.toUpperCase());
			htmlString = htmlString.replace(prodGenerationStatusStr, generationStatus);
			htmlString = htmlString.replace(prodDailyGenStatusStr, dailyGenerationStatus);
			htmlString = htmlString.replace(prodSizeStatusStr, sizeStatus);
			htmlString = htmlString.replace(prodLogStr, productLog);
			htmlString = htmlString.replace(prodLogResetStr, productLog);
			
			
			String lastUpdateStr = "$lastUpdate$";
			
			SimpleDateFormat lastUpdateSdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			String lastUpdate = lastUpdateSdf.format(new Date());
			htmlString = htmlString.replace(lastUpdateStr, lastUpdate);
			
		}
		FileUtils.writeStringToFile(newHtmlFile, htmlString);			
		
		
	}

}
