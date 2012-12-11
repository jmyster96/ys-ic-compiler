package IC.Parser;

public class LexicalError extends Exception {
	private int line;
	private String value;

	public LexicalError(String message) {
		super(message);
	}

	public LexicalError(String message, int line, String value) {
		super(message);
		this.line = line + 1;
		this.value = value;
	}

	public String toString() {
		if (this.value == null) {
			return this.line + ": Lexical error: " + this.getMessage();
		} else {
			return this.line + ": Lexical error: " + this.getMessage() + " '" + this.value + "'";
		}
	}
	
	public int getLine() {
	   return this.line;
	}
}