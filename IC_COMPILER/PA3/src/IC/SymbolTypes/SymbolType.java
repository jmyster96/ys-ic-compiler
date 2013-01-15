package IC.SymbolTypes;

public abstract class SymbolType {
	@Override
	public abstract String toString();

	// Important: SymbolTypes are used as HashMap keys.
	// equals() is used in many places.
	@Override
	public abstract boolean equals(Object obj);
	@Override
	public abstract int hashCode();

	// For printing the symbol types table.
	public abstract String getHeader();
	public abstract int getDisplaySortIndex();
	public String additionalStringData() {
		return "";
	}

	// For type comparison
	public abstract boolean isReferenceType();

}
