package IC.SymbolTypes;

public class ArraySymbolType extends SymbolType {

	SymbolType baseType;

	public ArraySymbolType(SymbolType baseType) {
		this.baseType = baseType;
	}

	@Override
	public String toString() {
		return baseType + "[]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArraySymbolType other = (ArraySymbolType) obj;
		if (baseType == null) {
			if (other.baseType != null)
				return false;
		} else if (!baseType.equals(other.baseType))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baseType == null) ? 0 : baseType.hashCode());
		return result;
	}

	@Override
	public String getHeader() {
		return "Array type";
	}

	@Override
	public int getDisplaySortIndex() {
		return 3;
	}

	public SymbolType getBaseType() {
		return baseType;
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

}
