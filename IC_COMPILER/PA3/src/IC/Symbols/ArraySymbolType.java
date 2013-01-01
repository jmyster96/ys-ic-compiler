package IC.Symbols;

public class ArraySymbolType extends SymbolType {

	SymbolType baseType;
	int dimension;

	public ArraySymbolType(SymbolType baseType, int dimension) {
		this.baseType = baseType;
		this.dimension = dimension;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(baseType);
		for (int i = 0; i < dimension; ++i) {
			builder.append("[]");
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArraySymbolType)) {
			return false;
		}
		ArraySymbolType other = (ArraySymbolType) obj;
		return other.baseType.equals(this.baseType)
				&& other.dimension == this.dimension;
	}

}
