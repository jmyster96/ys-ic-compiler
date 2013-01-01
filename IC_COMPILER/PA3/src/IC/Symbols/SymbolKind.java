package IC.Symbols;

public enum SymbolKind {
	CLASS("Class"), FIELD("Field"), VIRTUAL_METHOD("Virtual method"), STATIC_METHOD(
			"Static method"), LOCAL_VARIABLE("Local variable"), PARAMETER(
			"Parameter");

	String printable;

	SymbolKind(String printable) {
		this.printable = printable;
	}
	
	@Override
	public String toString() {
		return this.printable;
	}
}
