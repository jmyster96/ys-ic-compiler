package IC.Symbols;

public class ClassSymbolType extends SymbolType {

	private static int NO_BASE_CLASS = -1;

	String name;
	int baseClassTypeId = NO_BASE_CLASS;

	public ClassSymbolType(String name) {
		this.name = name;
	}

	public boolean hasBaseClass() {
		return baseClassTypeId != NO_BASE_CLASS;
	}

	public void setBaseClassTypeId(int id) {
		baseClassTypeId = id;
	}

	@Override
	public String toString() {
		return name
				+ (hasBaseClass() ? ", Superclass ID: " + baseClassTypeId : "");
	}

	public String getHeader() {
		return "Class";
	}

	@Override
	public int getDisplaySortIndex() {
		return 2;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + baseClassTypeId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassSymbolType other = (ClassSymbolType) obj;
		if (baseClassTypeId != other.baseClassTypeId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
