/**
 * A Token is a pair of a value (string or word) and its type
 *
 * @author javiergs
 * @version 1.0
 */
public class TheToken {
	
	private String value;
	private String type;
	
	public TheToken(String value, String type) {
		this.value = value;
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Token(value=" + value + ", type=" + type + ")";
	}
}

