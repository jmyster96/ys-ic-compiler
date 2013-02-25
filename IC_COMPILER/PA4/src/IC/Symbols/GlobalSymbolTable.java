package IC.Symbols;

import IC.AST.ASTNode;
import IC.AST.Program;
import IC.SymbolTypes.SymbolTypeTable;

public class GlobalSymbolTable extends SymbolTable {

	public GlobalSymbolTable(String programName, Program program,
			SymbolTypeTable typeTable) {
		super(programName, program, typeTable);
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
	
	@Override
	public SymbolTable lookupScope(String name) throws SymbolTableException {
		try {
			Symbol clazz = lookup(name);
			return clazz.getScope();
		} catch (SymbolTableException e) {
			// Do nothing
		}
		return super.lookupScope(name);
	}
}
