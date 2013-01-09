package IC.Symbols;

public class GlobalSymbolTable extends SymbolTable {

	public GlobalSymbolTable(String name, SymbolTypeTable typeTable) {
		super(name, typeTable);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Global Symbol Table";
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + "\n" + getTypeTable().toString();
	}
}
