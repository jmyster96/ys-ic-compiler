package IC.Symbols;

public class Symbol {
	String name;
	// Field, Method, Local Variable, Etc
	SymbolKind kind;
	// int, string, etc
	SymbolType type;

	public Symbol(String name, SymbolKind kind, SymbolType type) {
		this.name = name;
		this.kind = kind;
		this.type = type;
	}
}
