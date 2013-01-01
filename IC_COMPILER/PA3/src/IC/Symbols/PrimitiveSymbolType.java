package IC.Symbols;

import IC.AST.PrimitiveType;

public class PrimitiveSymbolType extends SymbolType {

	PrimitiveType type;

	public PrimitiveSymbolType(PrimitiveType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PrimitiveSymbolType)) {
			return false;
		}
		return ((PrimitiveSymbolType) obj).type.getName().equals(
				this.type.getName());
	}

}
