package IC.Symbols;

import java.util.ArrayList;
import java.util.List;

import IC.Parser.CourtesyErrorReporter;

public class MethodSymbolType extends SymbolType {
	List<SymbolType> formalsTypes = new ArrayList<SymbolType>();
	SymbolType returnType;

	public MethodSymbolType(List<SymbolType> formalsTypes, SymbolType returnType) {
		this.formalsTypes = formalsTypes;
		this.returnType = returnType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		List<String> formalsTypeNames = new ArrayList<String>();
		for (SymbolType typeName : formalsTypes) {
			formalsTypeNames.add(typeName.toString());
		}
		builder.append(CourtesyErrorReporter.joinStrings(formalsTypeNames));
		builder.append(" -> ");
		builder.append(returnType);
		builder.append("}");
		return builder.toString();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof MethodSymbolType)) {
			return false;
		}
		MethodSymbolType other = (MethodSymbolType) obj;
		return other.returnType.equals(this.returnType)
				&& listsEqual(other.formalsTypes, this.formalsTypes);
	}

	private boolean listsEqual(List<?> list1, List<?> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}
		for (int i = 0; i < list1.size(); ++i) {
			if (!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}
		return true;
	}

}
