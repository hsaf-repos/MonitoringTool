import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class provides a set of static methods to parse the status report file.
 * <p>
 * FORMAT of REPORT FILE:
 * 
 * <pre>
 * &lt;product size in bytes> &lt;gen.month> &lt;gen.day> &lt;gen.hour>:&lt;gen.minutes> &lt;product file name>
 * </pre>
 * 
 * Example:
 * 
 * <pre>
 * 501113 Feb 18 08:10 h08_20140218_072100_metopb_07368_ZAMG.buf.gz
 * </pre>
 * 
 * @author Vik
 */
public class ProductReportParser {

    // STATUS REPORT FORMAT SPECIFICATION
    private static final String FIELD_SEPARATOR = " ";
    private static final int FIELDS_NUMBER = 5;
    private static final int SIZE_IN_BYTE = 0;
    private static final int GENERATION_MOUTH = 1;
    private static final int GENERATION_DAY = 2;
    private static final int GENERATION_TIME = 3;
    private static final int PRODUCT_FILE = 4;

    // PRODUCT FILE NAME FORMAT SPECIFICATION
    private static final String FILE_FIELD_SEPARATOR = "_";
    private static final int FILE_FULL_DATA_CHARS = 40;
    private static final int FILE_PRODUCT = 0;
    private static final int FILE_DATE = 1;
    private static final int FILE_TIME = 2;
    private static final int FILE_SAT_ID = 3;

    public static ProductReport parseReport(String product, String line) {
        String productFile = "";
        Date generationTime = null;
        Date sensingTime = null;
        String sizeInBytes = "";
        String satelliteId = "Not available";
        boolean isQualityCheck = false;
        try {
            String[] tokens = line.trim().split(FIELD_SEPARATOR);

            // Check correct line format
            if (tokens.length != FIELDS_NUMBER) {
            	System.out.println("Wrong line format: Lenght field != " + FIELDS_NUMBER);
                return null;
            }

            // In case of a fake token an exception is thrown
            sizeInBytes = String.valueOf(Long.parseLong(tokens[SIZE_IN_BYTE]));

            // Extract Satellite Id & Product file name
            productFile = tokens[PRODUCT_FILE];
            String[] tokensProductFile = productFile.trim().split(FILE_FIELD_SEPARATOR);
            if (productFile.length() >= FILE_FULL_DATA_CHARS) {
                satelliteId = tokensProductFile[FILE_SAT_ID];
            }

            // Check if a quality check file
            if (productFile.contains("_QC_")) {
                isQualityCheck = true;
            }

            // Extract Sensing Time
            if (!tokensProductFile[FILE_PRODUCT].equalsIgnoreCase(product)) {
            	System.out.println("Wrong Token product files: " + product);
                return null;
            }
            String dateTmp = tokensProductFile[FILE_DATE];
            String year = dateTmp.substring(0, 4);
            String month = dateTmp.substring(4, 6);
            String day = dateTmp.substring(6);
            String timeTmp = tokensProductFile[FILE_TIME];
            String hour = timeTmp.substring(0, 2);
            String mins = "00";
            String secs = "00";
            if (!(product.equalsIgnoreCase(Products.H10) || product.equalsIgnoreCase(Products.H11)
                    || product.equalsIgnoreCase(Products.H12) || product.equalsIgnoreCase(Products.H13))) {
                // time part present
                if ((timeTmp.length() > 4) && (timeTmp.length() < 7)) {
                    mins = timeTmp.substring(2, 4);
                    secs = timeTmp.substring(4, 6);
                } else {
                    mins = timeTmp.substring(2, 4);
                }
            } else {
                // daily product, no time part
                hour = "00";
            }
            sensingTime = new SimpleDateFormat("yyyyMMdd HHmmss",Locale.ENGLISH).parse(year + month + day + " " + hour + mins + secs);

            // Extract Generation Time
            String genTmp = year + tokens[GENERATION_MOUTH] + tokens[GENERATION_DAY] + " " + tokens[GENERATION_TIME] + "00";
            generationTime = new SimpleDateFormat("yyyyMMMdd HH:mmss",Locale.ENGLISH).parse(genTmp);
            
            System.out.println("---- DEBUG genertation Time prodId: " + product + "  genTmp: " + genTmp);
            
        } catch (Throwable e) {
        	System.out.println("Exception in parsing report file: " + product);
        	e.printStackTrace();
            return null;
        }
        return new ProductReport(productFile, generationTime, sensingTime, sizeInBytes, satelliteId, isQualityCheck);
    }

}
