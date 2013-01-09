package IC.Symbols;

public class Symbol {
	String name;
	// Field, Method, Local Variable, Etc
	SymbolKind kind;
	// int, string, etc
	int symbolTypeId;

	public Symbol(String name, SymbolKind kind, int symbolTypeIndex) {
		this.name = name;
		this.kind = kind;
		this.symbolTypeId = symbolTypeIndex;
	}

	public int getTypeId() {
		return symbolTypeId;
	}

	public SymbolKind getKind() {
		return kind;
	}

	public String getName() {
		return name;
	}
}
