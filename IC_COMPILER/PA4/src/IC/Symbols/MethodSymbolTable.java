package IC.Symbols;

import IC.AST.ASTNode;
import IC.AST.Method;
import IC.SymbolTypes.SymbolTypeTable;

public class MethodSymbolTable extends SymbolTable {

	public MethodSymbolTable(Method method, SymbolTypeTable typeTable) {
		super(method.getName(), method, typeTable);
	}

	@Override
	protected String getSymbolTableTypeString() {
		return "Method Symbol Table";
	}

}
