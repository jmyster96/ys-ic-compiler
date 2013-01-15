package IC.Semantic;

import IC.SymbolTypes.ClassSymbolType;
import IC.SymbolTypes.PrimitiveSymbolType;
import IC.SymbolTypes.SymbolType;
import IC.SymbolTypes.SymbolTypeTable;
import IC.SymbolTypes.PrimitiveSymbolType.PrimitiveSymbolTypes;

public class TypeComparer {
	private static final PrimitiveSymbolType NULL_TYPE = new PrimitiveSymbolType(
			PrimitiveSymbolTypes.NULL);
	SymbolTypeTable typeTable;
	
	public TypeComparer(SymbolTypeTable typeTable) {
		this.typeTable = typeTable;
	}
	
	public boolean isTypeLessThanOrEquals(SymbolType type1, SymbolType type2) {
		if (type1.equals(type2)) {
			return true;
		}
		if (type1.equals(NULL_TYPE) && type2.isReferenceType()) {
			return true;
		}
		if (type1 instanceof ClassSymbolType) {
			ClassSymbolType classType = (ClassSymbolType) type1;
			if (doesClassExtend(classType, type2)) {
				return true;
			}
		}
		return false;
	}

	private boolean doesClassExtend(ClassSymbolType subType,
			SymbolType superType) {
		while (subType.hasBaseClass()) {
			ClassSymbolType baseClass = (ClassSymbolType) typeTable
					.getSymbolById(subType.getBaseClassTypeId());
			if (baseClass.equals(superType)) {
				return true;
			} else {
				subType = baseClass;
			}
		}
		return false;
	}

}
