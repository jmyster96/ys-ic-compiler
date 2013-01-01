package IC.Symbols;

public class Symbol {
	String name;
	// Field, Method, Local Variable, Etc
	SymbolKind kind;
	// int, string, etc
	int symbolTypeIndex;

	public Symbol(String name, SymbolKind kind, int symbolTypeIndex) {
		this.name = name;
		this.kind = kind;
		this.symbolTypeIndex = symbolTypeIndex;
	}
}
