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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassSymbolType)) {
			return false;
		}
		return ((ClassSymbolType) obj).name.equals(this.name);
	}

}
