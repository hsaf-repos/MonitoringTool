import java.util.HashSet;
import java.util.Set;

/**
 * H-SAF products acronyms.
 * 
 * @author Vik
 */
public class Products {
	public final static String H01 = "h01";
	// public final static String H01B = "h01B";
	// H02 dismissed starting from 17/10/2019
	// public final static String H02 = "h02";
	public final static String H02B = "h02B";
	// H03A dismissed starting from 11/04/2019
	// public final static String H03 = "h03";
	public final static String H03B = "h03B";
	public final static String H04 = "h04";
	// H05A dismissed starting from 11/04/2019
	// public final static String H05 = "h05";
	public final static String H05B = "h05B";
	public final static String H08 = "h08";
	public final static String H10 = "h10";
	public final static String H11 = "h11";
	public final static String H12 = "h12";
	public final static String H13 = "h13";
	public final static String H14 = "h14";
	public final static String H15 = "h15";
	public final static String H16 = "h16";
	public final static String H17 = "h17";
	public final static String H18 = "h18";
	public final static String H101 = "h101";
	public final static String H102 = "h102";
	public final static String H103 = "h103";
	
	public static Set<String> getListOfProducts() 
	{
		Set<String> listOfProducts = new HashSet<String>();
		
		listOfProducts.add(H01);
		listOfProducts.add(H02B);
		listOfProducts.add(H03B);
		listOfProducts.add(H04);
		listOfProducts.add(H05B);
		listOfProducts.add(H08);
		listOfProducts.add(H10);
		listOfProducts.add(H11);
		listOfProducts.add(H12);
		listOfProducts.add(H13);
		listOfProducts.add(H14);
		listOfProducts.add(H15);
		listOfProducts.add(H16);
		listOfProducts.add(H17);
		listOfProducts.add(H18);
		listOfProducts.add(H101);
		listOfProducts.add(H102);
		listOfProducts.add(H103);
		
		
		return listOfProducts;
		
	}

}
