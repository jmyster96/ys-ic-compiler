package IC.SymbolTypes;

import IC.AST.PrimitiveType;

public class PrimitiveSymbolType extends SymbolType {

	public enum PrimitiveSymbolTypes {
		INT("int"), BOOLEAN("boolean"), NULL("null"), STRING("string"), VOID(
				"void");

		String name;

		private PrimitiveSymbolTypes(String name) {
			this.name = name;
		}

		String getName() {
			return this.name;
		}
	}

	final PrimitiveSymbolTypes type;

	public PrimitiveSymbolType(PrimitiveSymbolTypes type) {
		this.type = type;
	}

	public PrimitiveSymbolType(PrimitiveType type) {
		switch (type.getDataType()) {
		case BOOLEAN:
			this.type = PrimitiveSymbolTypes.BOOLEAN;
			break;
		case INT:
			this.type = PrimitiveSymbolTypes.INT;
			break;
		case STRING:
			this.type = PrimitiveSymbolTypes.STRING;
			break;
		case VOID:
			this.type = PrimitiveSymbolTypes.VOID;
			break;
		default:
			throw new NullPointerException("Unexpected primitive type in line "
					+ type.getLine());
		}
	}

	@Override
	public String toString() {
		return type.getName();
	}

	@Override
	public String getHeader() {
		return "Primitive type";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PrimitiveSymbolType)) {
			return false;
		}
		return ((PrimitiveSymbolType) obj).type == this.type;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public int getDisplaySortIndex() {
		return 1;
	}

	@Override
	public boolean isReferenceType() {
		return type == PrimitiveSymbolTypes.STRING
				|| type == PrimitiveSymbolTypes.NULL;
	}
}
