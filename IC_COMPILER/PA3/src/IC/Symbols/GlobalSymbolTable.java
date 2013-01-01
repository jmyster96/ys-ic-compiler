package IC.Symbols;

public class GlobalSymbolTable extends SymbolTable {

	public GlobalSymbolTable(String name) {
		super(name);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Global Symbol Table";
	}

}
