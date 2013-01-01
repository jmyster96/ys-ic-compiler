package IC.Symbols;

public class MethodSymbolTable extends SymbolTable {

	public MethodSymbolTable(String name) {
		super(name);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Method Symbol Table";
	}

}
