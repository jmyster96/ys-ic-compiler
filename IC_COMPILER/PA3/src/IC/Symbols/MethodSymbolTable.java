package IC.Symbols;

import IC.SymbolTypes.SymbolTypeTable;

public class MethodSymbolTable extends SymbolTable {

	public MethodSymbolTable(String name, SymbolTypeTable typeTable) {
		super(name, typeTable);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Method Symbol Table";
	}

}
