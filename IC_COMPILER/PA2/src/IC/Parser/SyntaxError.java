package IC.Parser;

public class SyntaxError extends Exception {
	private int line;
	private String value;

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int line, String value) {
		super(message);
		this.line = line + 1;
		this.value = value;
	}

	public String toString() {
		if (this.value == null) {
			return this.line + ": Syntax error: " + this.getMessage();
		} else {
			return this.line + ": Syntax error: " + this.getMessage() + " '" + this.value + "'";
		}
	}
}
