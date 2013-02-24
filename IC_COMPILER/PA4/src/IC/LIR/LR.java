package IC.LIR;

import java.util.ArrayList;
import java.util.List;

import IC.AST.ASTNode;
import IC.AST.Expression;
import IC.AST.PrettyPrinter;
import IC.Parser.CourtesyErrorReporter;

public abstract class LR {

	private List<String> codeLines = new ArrayList<String>();

	protected String resultRegister;

	public String getResultRegister() {
		return resultRegister;
	}

	public void addCommand(String cmdName, String leftOperand,
			String destOperand) {
		this.codeLines.add(cmdName + " " + leftOperand + ", " + destOperand);
		p();
	}

	public void addCommand(String cmdName, String leftOperand,
			String destOperand, String comment) {
		this.codeLines.add(cmdName + " " + leftOperand + ", " + destOperand
				+ "\t\t# " + comment);
	}

	private static String buildCommand(String cmdName, String... parts) {
		StringBuilder cmd = new StringBuilder();
		cmd.append(cmdName);
		cmd.append(" ");
		if (parts.length > 0) {
			cmd.append(parts[0]);
		}
		for (int i = 1; i < parts.length; ++i) {
			cmd.append(", ");
			cmd.append(parts[i]);
		}
		return cmd.toString();
	}

	public List<String> getCommands() {
		return codeLines;
	}

	private static PrettyPrinter printer = new PrettyPrinter("", null);

	public void addCommandsFromOtherLr(LR other, ASTNode node) {
		if (other != null) {
			this.codeLines.addAll(other.getCommands());
		} else {
			this.codeLines.add("-- Missing translation of: " + node);
			p();
		}
		// p(CourtesyErrorReporter.joinStrings(commands, "\n"));
	}

	private void p(String str) {
		// System.out.println(str);
	}

	public void addCommand(String command) {
		this.codeLines.add(command);
		p();
	}

	private void p() {
		p(codeLines.get(codeLines.size() - 1));
	}

	public void addCommand(String string, String label) {
		this.codeLines.add(string + " " + label);
		p();
	}

	public void addCommandFormat(String format, String... args) {
		this.codeLines.add(String.format(format, (Object[]) args));
		p();
	}

	public void insertCommand(int i, String string) {
		this.codeLines.add(i, string);
		p(codeLines.get(i));
	}

}
