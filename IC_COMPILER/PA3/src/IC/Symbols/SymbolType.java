package IC.Symbols;

public abstract class SymbolType {
	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	public abstract String getHeader();

	public abstract int getDisplaySortIndex();

	public abstract boolean isReferenceType();

}
