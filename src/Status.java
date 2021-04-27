
public enum Status {

	OK, NOK;

	public static String getStatusValue(Boolean statusBool) {
		if (statusBool == true)
			return "NOK";
		else
			return "OK";

	}
}
