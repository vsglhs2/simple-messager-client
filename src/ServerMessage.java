import java.util.ArrayList;

public class ServerMessage {
	public String header = null;
	public String subHeader = null;
	public String headerType = null;
	public ArrayList<String> args = null;
	private int argSize = -1;
	
	public static boolean isHeader(String header) {
		return (header.equals("System") || header.equals("Error") || header.equals("MMessage"));
	}
	
	public boolean isFulfilled() {
		return (
			header != null &&
			subHeader != null &&
			headerType != null &&
			(
				argSize > 0 && args != null && args.size() == argSize ||
				argSize == 0 && args == null
			)
		);
	}
	
	public boolean put(String part) {
		if (header == null) {
			header = part;
		} else if (subHeader == null) {
			subHeader = part;
		} else if (subHeader == null) {
			subHeader = part;
		} else if (headerType == null) {
			headerType = part;
		} else if (args == null) {
			argSize = Integer.valueOf(part);
			if (argSize > 0) args = new ArrayList<>();
		} else if (args.size() != argSize && args != null) {
			args.add(part);
		} else {
			return false;
		}
		
		return true;
	}
}