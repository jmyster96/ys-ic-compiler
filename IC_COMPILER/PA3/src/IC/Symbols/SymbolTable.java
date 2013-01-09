package IC.Symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import sun.util.logging.resources.logging;

import IC.Parser.CourtesyErrorReporter;
import IC.Semantic.SemanticError;

public abstract class SymbolTable {
	private SymbolTable parent;
	private List<SymbolTable> children = new ArrayList<SymbolTable>();
	private String name;
	private Map<String, Symbol> symbols = new HashMap<String, Symbol>();

	private SymbolTypeTable typeTable;

	public SymbolTable(String name, SymbolTypeTable typeTable) {
		this.name = name;
		this.typeTable = typeTable;
	}

	public void insert(Symbol newSymbol) throws SymbolTableException {
		if (symbols.containsKey(newSymbol.name)) {
			throw new SymbolTableException(
					"A symbol with this name already exists in this scope: "
							+ newSymbol.name);
		}
		symbols.put(newSymbol.name, newSymbol);
	}

	public Symbol lookup(String name) throws SymbolTableException {
		if (symbols.containsKey(name)) {
			return symbols.get(name);
		}
		if (parent == null) {
			throw new SymbolTableException("Couldn't find a symbol with name: "
					+ name);
		}
		return parent.lookup(name);
	}

	public SymbolTable lookupScope(String name) throws SymbolTableException {
		for (SymbolTable child : children) {
			if (child.name.equals(name)) {
				return child;
			}
		}
		if (parent == null) {
			throw new SymbolTableException("Couldn't find a symbol with name: "
					+ name);
		}
		return parent.lookupScope(name);
	}

	public void addChild(SymbolTable child) {
		getChildren().add(child);
		child.parent = this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getSymbolTableTypeString());
		builder.append(": ");
		builder.append(getName());
		builder.append("\n");

		for (Map.Entry<String, Symbol> tableEntry : symbols.entrySet()) {
			builder.append("    ");
			builder.append(tableEntry.getValue().kind);
			builder.append(": ");
			builder.append(tableEntry.getKey());
			builder.append(": ");
			builder.append(this.getTypeTable().getSymbolById(
					tableEntry.getValue().symbolTypeId));
			builder.append("\n");
		}
		if (getChildren().size() > 0) {
			builder.append("Children tables: ");
			builder.append(CourtesyErrorReporter
					.joinStrings(getChildrenNames()));
			builder.append("\n");

			builder.append("\n");
			for (SymbolTable child : getChildren()) {
				builder.append(child.toString());
			}
		} else {
			builder.append("\n");
		}
		return builder.toString();
	}

	private Collection<?> getChildrenNames() {
		Collection<String> names = new ArrayList<String>();
		for (SymbolTable child : getChildren()) {
			names.add(child.getName());
		}
		return names;
	}

	protected String getSymbolTableTypeString() {
		return "Symbol Table";
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public List<SymbolTable> getChildren() {
		return children;
	}

	public SymbolTypeTable getTypeTable() {
		return typeTable;
	}

	public SymbolTable getParent() {
		return parent;
	}
}
