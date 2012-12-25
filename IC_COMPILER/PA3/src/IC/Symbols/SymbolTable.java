package IC.Symbols;

import java.util.List;
import java.util.Map;

public abstract class SymbolTable {
	SymbolTable parent;
	List<SymbolTable> children;
	String name;
	Map<String, Symbol> symbols;
	
	void insert(Symbol newSymbol) {
		if (symbols.containsKey(newSymbol.name)) {
			// TODO: error
		}
		symbols.put(newSymbol.name, newSymbol);
	}
	
	Symbol lookup(String name) {
		if (symbols.containsKey(name)) {
			return symbols.get(name);
		}
		if (parent == null) {
			// TODO: error
			return null;
		}
		return parent.lookup(name);
	}

	public void addChild(SymbolTable child) {
		children.add(child);
		child.parent = this;
	}
}
