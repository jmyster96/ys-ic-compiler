package IC.Symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.Parser.CourtesyErrorReporter;

public abstract class SymbolTable {
	SymbolTable parent;
	List<SymbolTable> children = new ArrayList<SymbolTable>();
	String name;
	Map<String, Symbol> symbols = new HashMap<String, Symbol>();
	SymbolTypeTable typeTable;

	public SymbolTable(String name, SymbolTypeTable typeTable) {
		this.name = name;
		this.typeTable = typeTable;
	}

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getSymbolTableTypeString());
		builder.append(": ");
		builder.append(name);
		builder.append("\n");

		for (Map.Entry<String, Symbol> tableEntry : symbols.entrySet()) {
			builder.append("    ");
			builder.append(tableEntry.getValue().kind);
			builder.append(": ");
			builder.append(tableEntry.getKey());
			builder.append(": ");
			builder.append(this.typeTable.getSymbolById(tableEntry.getValue().symbolTypeIndex));
			builder.append("\n");
		}
		if (children.size() > 0) {
			builder.append("Children tables: ");
			builder.append(CourtesyErrorReporter
					.joinStrings(getChildrenNames()));
			builder.append("\n");

			builder.append("\n");
			for (SymbolTable child : children) {
				builder.append(child.toString());
			}
		} else {
			builder.append("\n");
		}
		return builder.toString();
	}

	private Collection<?> getChildrenNames() {
		Collection<String> names = new ArrayList<String>();
		for (SymbolTable child : children) {
			names.add(child.name);
		}
		return names;
	}

	protected String getSymbolTableTypeString() {
		return "Symbol Table";
	}
}
