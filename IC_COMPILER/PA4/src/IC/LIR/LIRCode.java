package IC.LIR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import IC.AST.ASTNode;
import IC.AST.ICClass;
import IC.Parser.CourtesyErrorReporter;

public abstract class LIRCode {

	private List<String> codeLines = new ArrayList<String>();

	protected String targetRegister;
	protected boolean wasImmediateOrMemory;
	protected String immediateOrMemory;

	public String getTargetRegister() {
		return targetRegister;
	}

	public void addCommand(String cmdName, String leftOperand,
			String destOperand, String comment) {
		this.codeLines.add("  " + cmdName + " " + leftOperand + ", "
				+ destOperand + " # " + comment);
	}

	public List<String> getCommands() {
		return codeLines;
	}

	public void addCommandsFromOtherLr(LIRCode other, ASTNode node) {
		// addComment("Translating node of type: "
		// + node.getClass().getSimpleName());
		this.codeLines.addAll(other.getCommands());
		// addComment("Finished translating node of type: "
		// + node.getClass().getSimpleName());
	}

	private void p(String str) {
		// System.out.println(str);
	}

	public void addLabel(String command) {
		this.codeLines.add(command);
		p();
	}

	private void p() {
		p(codeLines.get(codeLines.size() - 1));
	}

	public void addJumpCommand(String string, String label) {
		this.codeLines.add("  " + string + " " + label);
		p();
	}

	public void addCommandFormat(String format, String... args) {
		this.codeLines.add("  " + String.format(format, (Object[]) args));
		p();
	}

	public void insertCommand(int i, String string) {
		this.codeLines.add(i, string);
		p(codeLines.get(i));
	}

	public void addComment(String string) {
		this.codeLines.add("  # " + string);
		p();
	}

	public void addDispatchVector(ICClass clazz, List<String> methodNames) {
		this.codeLines.add(String.format("_DV_%s: [%s]", clazz.getName(),
				CourtesyErrorReporter.joinStrings(methodNames)));
	}

	public void alignComments() {
		int maxCommentIndex = 3;
		for (String line : codeLines) {
			if (line.indexOf("#") > maxCommentIndex) {
				maxCommentIndex = line.indexOf("#");
				System.out.println(line + maxCommentIndex);
			}
		}
		for (int i = 0; i < codeLines.size(); i++) {
			codeLines.set(i, normalizeLine(codeLines.get(i), maxCommentIndex));
		}
	}

	private String normalizeLine(String line, int maxCommentIndex) {
		int indexOfHash = line.indexOf("#");
		if (indexOfHash > 3 && indexOfHash < maxCommentIndex) {
			// Need to add (maxCommentIndex - indexOfHash) spaces before #
			String part1 = line.substring(0, indexOfHash);
			String part2 = getStringPadding(maxCommentIndex - indexOfHash, ' ');
			String part3 = line.substring(indexOfHash);

			return part1 + part2 + part3;
		}
		return line;
	}

	private String getStringPadding(int l, char c) {
		char[] charArray = new char[l];
		Arrays.fill(charArray, c);
		String part2 = new String(charArray);
		return part2;
	}

	public void addEmptyLine() {
		codeLines.add("");
	}

	public void addMethodComment(String string) {
		codeLines.add("# " + string);
		codeLines.add("# " + getStringPadding(string.length(), '-'));
	}

	public boolean wasImmediateOrMemory() {
		return wasImmediateOrMemory;
	}

	public String getImmediateOrMemory() {
		return immediateOrMemory;
	}
	public void setImmediateOrMemory(String immediateOrMemory) {
		this.immediateOrMemory = immediateOrMemory;
		this.wasImmediateOrMemory = true;
	}
}
