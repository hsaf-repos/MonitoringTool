
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.naming.OperationNotSupportedException;

/**
 * The ManageHistory class is in charge to manage the anomalies history for H-SAF products
 * 
 * NOTES: this is v0.1 relying on FileSystem persistence, improvement are foreseen towards usage of a specific DB for future versions
 */
public class ManageHistory {
   
    // ERROR CODES
    /*
    private final static String NONE = "00";	
    private final static String EMPTY_LIST = "01";
    private final static String GENERATION_INTERRUPT = "02";
    private final static String MINIMUM_SIZE = "03";
    private final static String FREQUENCY_RATE = "04";
    private final static String TOTAL_EXPECTED = "05";
    private final static String EMPTY_FILE = "06";
    */
    // MANDATORY: JUST ONE CHARACTER AS FIELDS SEPARATOR
    private final String FIELD_SEPARATOR = ";";
    private final static String ANOMALIES_DIRECTORY = "/UMARF/safclient/autoUMARF/anomalies";
    // private HashMap<String, String> errorDescription = new HashMap<String, String>();
    private String product = "";

    public ManageHistory() {
        super();
        /*
        errorDescription.put(NONE, "No Anomalies");
        errorDescription.put(EMPTY_LIST, "Empty file list");
        errorDescription.put(GENERATION_INTERRUPT, "Operational Chain generation interrupt");
        errorDescription.put(MINIMUM_SIZE, "Minimum file size not reached");
        errorDescription.put(FREQUENCY_RATE, "Expected generation rate not reached");
        errorDescription.put(TOTAL_EXPECTED, "Total number of expected files not reached");
        errorDescription.put(EMPTY_FILE, "Empty file generated");
         */

    }

    public void setHistoryProduct(String prod) {
        this.product = prod;
    }

    public ArrayList<String> retrieveHistory(String prod) {
        ArrayList<String> anomalies = new ArrayList<String>();
        String filePath = ANOMALIES_DIRECTORY + File.separator + prod + "_anomalies.txt";
        File checkFile = new File(filePath);
        System.out.println("Checking anomalies in file: " + filePath);
        if (checkFile.isFile()) {
            try {
                anomalies = readFile(filePath);
            } catch (IOException ioe) {
                System.out.println("IO Exception in History retrieval: " + ioe.getMessage());
            }
        }
        return anomalies;
    }

    public void storeAnomaly(HsafProductStatus.HSAF_STATUS status) {
        if (!product.isEmpty()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            DateFormat df = new SimpleDateFormat("ddMMyyyyHHmm");
            String errorDate = df.format(now.getTime());
            StringBuilder anomaly = new StringBuilder();
            anomaly.append(status.getStatus());
            anomaly.append(FIELD_SEPARATOR);
            anomaly.append(status.getExtendedStatus());
            anomaly.append(FIELD_SEPARATOR);
            anomaly.append(errorDate);
            try {
                appendToFile(this.product, anomaly.toString());
            } catch (IOException ioe) {
                System.out.println("IO Exception in anomaly storage: " + ioe.getMessage());
            }
        }
    }

    /*
        	public void storeAnomaly(String errorCode) {
        		if (!product.isEmpty()) {
        		    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        		    DateFormat df = new SimpleDateFormat("ddMMyyyyHHmm");
        		    String errorDate = df.format(now.getTime());
        			String desc = errorDescription.get(errorCode);
        			String anomaly=errorCode+";"+desc+";"+errorDate;
        			try {
        				appendToFile(this.product, anomaly);
        			} catch (IOException ioe) {
        				System.out.println("Exception in History Management: "+ioe.getMessage());
        			}
        		}
        		
        	}
    */

    public boolean checkAnomalyStatus(String errorCode) {
        boolean solved = true;
        int i = 1;
        ArrayList<String> anom = retrieveHistory(product);
        if (anom.size() > 0) {
            for (String an : anom) {
                String[] tmpAn = an.split(FIELD_SEPARATOR);
                if (tmpAn[0] != null && tmpAn[0].equals(errorCode)) {
                    if (tmpAn.length < 4) {
                        solved = false;
                        System.out.println(i + ") ANOMALY OPEN (" + tmpAn[2] + "): " + tmpAn[0]);
                        i++;
                    }
                }
            }
        }
        return solved;
    }

    public String checkOpenAnomalies() {
        String rowId = "";
        ArrayList<String> anom = retrieveHistory(product);
        int anomSize = anom.size();
        if (anomSize > 0) {
            int i = 0;
            for (i = 0; i < anomSize; i++) {
                String[] tmpAn = anom.get(i).split(FIELD_SEPARATOR);
                if (tmpAn.length < 4) {
                    rowId = Integer.toString(i);
                }
            }
        }
        return rowId;
    }

    public String getOpenAnomaly(String errorCode) {
        String rowId = "";
        ArrayList<String> anom = retrieveHistory(product);
        int anomSize = anom.size();
        if (anomSize > 0) {
            int i = 0;
            for (i = 0; i < anomSize; i++) {
                String an = anom.get(i);
                String[] tmpAn = an.split(FIELD_SEPARATOR);
                if (tmpAn[0] != null && tmpAn[0].equals(errorCode)) {
                    if (tmpAn.length < 4) {
                        rowId = Integer.toString(i);
                    }
                }
            }
        }
        return rowId;
    }

    public void storeResolutionDate(String rowId) {
        if (!product.isEmpty() && !rowId.isEmpty()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            DateFormat df = new SimpleDateFormat("ddMMyyyyHHmm");
            String resDate = df.format(now.getTime());
            ArrayList<String> anomalies = retrieveHistory(product);
            int rowInt = Integer.parseInt(rowId);
            String an = anomalies.get(rowInt);
            String[] tmpAn = an.split(FIELD_SEPARATOR);
            if (tmpAn.length < 4) {
                // OPEN ANOMALY FOUND; STORING DATE RESOLUTION
                String resolution = an + FIELD_SEPARATOR + resDate;
                // System.out.println("ANOMALY LINE "+rowInt+" : "+ an);
                System.out.println("RESOLUTION: " + resolution);
                anomalies.set(rowInt, resolution);
            }
            try {
                cleanFile(this.product);
                for (String anom : anomalies) {
                    appendToFile(this.product, anom);
                }
            } catch (IOException ioe) {
                System.out.println("IO Exception in resolution date storage: " + ioe.getMessage());
            }
        }
    }

    public void storeResolutionComment(String resolutionComment, String resDate) {
        if (!product.isEmpty()) {
            resolutionComment = resolutionComment.replaceAll("[^a-zA-Z0-9 \\.,:_-]", " ");
            ArrayList<String> anomalies = retrieveHistory(product);
            int rowId = -1;
            int sizean = anomalies.size();
            Date resDateD = null;
            try {
                // resDateD = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(resDate);
                resDateD = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(resDate);
                DateFormat df = new SimpleDateFormat("ddMMyyyyHHmm");
                String resDateStr = df.format(resDateD.getTime());
                System.out.println("STORE RESOLUTION COMMENT - RESDATE: " + resDateStr);
                for (int i = 0; i < sizean; i++) {
                    String anom = anomalies.get(i);
                    String[] tmpAn = anom.split(FIELD_SEPARATOR);
                    if (tmpAn.length >= 4) {
                        if (tmpAn[3].equals(resDateStr)) {
                            rowId = i;
                        }
                    }
                }
                // Resolution date was found into anomalies file
                if (rowId >= 0) {
                    String resAno = anomalies.get(rowId);
                    System.out.println("ROW " + rowId + " : " + resAno);
                    String[] tmpAnom = resAno.split(FIELD_SEPARATOR);
                    StringBuffer resolution = new StringBuffer();
                    if (tmpAnom.length > 4) {
                        resolution.append(tmpAnom[0]).append(FIELD_SEPARATOR);
                        resolution.append(tmpAnom[1]).append(FIELD_SEPARATOR);
                        resolution.append(tmpAnom[2]).append(FIELD_SEPARATOR);
                        resolution.append(tmpAnom[3]).append(FIELD_SEPARATOR);
                        resolution.append(resolutionComment);
                    } else if (tmpAnom.length == 4) {
                        // RESOLUTION NOT PRESENT, FILL IT
                        resolution.append(resAno).append(FIELD_SEPARATOR);
                        resolution.append(resolutionComment);
                    } else {
                        //
                        throw new OperationNotSupportedException("Try to add comment to an open anomaly. Line <" + rowId + ">");
                    }
                    anomalies.set(rowId, resolution.toString());
                    cleanFile(this.product);
                    for (String anom : anomalies) {
                        appendToFile(this.product, anom);
                    }
                }
            } catch (ParseException ex) {
                System.out.println("Parse problem: " + ex.getMessage());
            } catch (IOException ioe) {
                System.out.println("IO Exception in resolution comment storage: " + ioe.getMessage());
            } catch (OperationNotSupportedException e) {
                System.out.println("Exception adding resolution comment: " + e.getMessage());
            }
        }
    }

    private ArrayList<String> readFile(String filename) throws IOException {
        ArrayList<String> dataProduct = new ArrayList<String>();
        System.out.println("File path: " + filename);
        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(filename));
            try {
                String line = null; // not declared within while loop
                /*
                 * readLine is a bit quirky :
                 * it returns the content of a line MINUS the newline.
                 * it returns null only for the END of the stream.
                 * it returns an empty String if two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    if (line != null
                            && !line.isEmpty()
                            && !line.contains("\n")
                            && (line.startsWith(HsafProductStatus.HSAF_STATUS.GENERATION_INTERRUPT.getStatus())
                                    || line.startsWith(HsafProductStatus.HSAF_STATUS.TOTAL_EXPECTED.getStatus()) || line
                                        .startsWith(HsafProductStatus.HSAF_STATUS.FREQUENCY_RATE.getStatus()))) {
                        dataProduct.add(line);
                    } else {
                        // SKIP FAKE LINE
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            System.out.println("Could not read file " + filename + ": " + ex.getMessage());
        }
        return dataProduct;
    }

    private void cleanFile(String prod) throws IOException {
        String fileName = ANOMALIES_DIRECTORY + File.separator + prod + "_anomalies.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        try {
            out.write("");
            out.close();
        } finally {
            out.close();
        }
    }

    private void appendToFile(String prod, String anomaly) throws IOException {
        String fileName = ANOMALIES_DIRECTORY + File.separator + prod + "_anomalies.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
        try {
            out.write(anomaly);
            out.write(System.getProperty("line.separator"));
            out.close();
        } finally {
            out.close();
        }
    }

}
