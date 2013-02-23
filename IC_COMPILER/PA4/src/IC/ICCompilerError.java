package IC;

public class ICCompilerError extends Exception {
	private static final long serialVersionUID = 5344529355531210154L;
	protected int line;
	protected String value;

	public ICCompilerError(String message) {
		super(message);
	}

	public ICCompilerError(String message, int line) {
		super(message);
		this.line = line;
	}

	public ICCompilerError(String message, int line, String value) {
		super(message);
		this.line = line;
		this.value = value;
	}

	public String toString() {
		String msg = "Line " + this.line + ": Error: "
				+ this.getMessage();
		if (value != null) {
			msg += "; " + value;
		}
		return msg;
	}

	public int getLine() {
		return this.line;
	}
}
