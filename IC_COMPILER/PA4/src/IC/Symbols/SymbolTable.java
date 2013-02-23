package IC.Symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.ASTNode;
import IC.Parser.CourtesyErrorReporter;
import IC.SymbolTypes.SymbolTypeTable;

public abstract class SymbolTable {
	private SymbolTable parent;
	private List<SymbolTable> children = new ArrayList<SymbolTable>();
	private String name;
	protected Map<String, Symbol> symbols = new HashMap<String, Symbol>();
	// Used for typing
	private List<Symbol> symbolsByOrder = new ArrayList<Symbol>();

	private SymbolTypeTable typeTable;
	private final ASTNode releventAstNode;

	public SymbolTable(String name, ASTNode relevantAstNode, SymbolTypeTable typeTable) {
		this.name = name;
		this.releventAstNode = relevantAstNode;
		this.typeTable = typeTable;
	}

	public void insert(Symbol newSymbol) throws SymbolTableException {
		if (symbols.containsKey(newSymbol.name)) {
			throw new SymbolTableException(
					"A symbol with this name already exists in this scope: "
							+ newSymbol.name);
		}
		symbols.put(newSymbol.name, newSymbol);
		symbolsByOrder.add(newSymbol);
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

		for (Symbol symbol : symbolsByOrder) {
			builder.append("    ");
			builder.append(symbol.kind);
			builder.append(": ");
			switch (symbol.kind) {
			case CLASS:
				builder.append(symbol.name);
				break;
			case FIELD:
			case LOCAL_VARIABLE:
			case PARAMETER:
				builder.append(this.getTypeTable().getSymbolById(
						symbol.symbolTypeId));
				builder.append(" ");
				builder.append(symbol.name);

				break;
			case STATIC_METHOD:
			case VIRTUAL_METHOD:
				builder.append(symbol.name);
				builder.append(" ");
				builder.append(this.getTypeTable().getSymbolById(
						symbol.symbolTypeId));
				break;
			default:
				break;

			}
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

	public ASTNode getReleventAstNode() {
		return releventAstNode;
	}
}
