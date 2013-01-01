package IC.Symbols;

public class ClassSymbolTable extends SymbolTable {

	public ClassSymbolTable(String name) {
		super(name);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Class Symbol Table";
	}


}
