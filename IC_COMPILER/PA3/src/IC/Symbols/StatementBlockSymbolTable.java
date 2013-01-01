package IC.Symbols;

public class StatementBlockSymbolTable extends SymbolTable {

	public StatementBlockSymbolTable() {
		super("statement block");
	}

	public void setParentName(String parentName) {
		this.name = "statement block in " + parentName;
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Statement Block Symbol Table";
	}


}
