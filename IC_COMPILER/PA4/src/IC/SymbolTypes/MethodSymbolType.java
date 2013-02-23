package IC.SymbolTypes;

import java.util.ArrayList;
import java.util.List;

import IC.Parser.CourtesyErrorReporter;

public class MethodSymbolType extends SymbolType {
	List<SymbolType> formalsTypes = new ArrayList<SymbolType>();
	private SymbolType returnType;
	private final boolean isStatic;

	public MethodSymbolType(boolean isStatic, List<SymbolType> formalsTypes,
			SymbolType returnType) {
		this.isStatic = isStatic;
		this.formalsTypes = formalsTypes;
		this.returnType = returnType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(isStatic ? "static " : "");
		builder.append("{");
		List<String> formalsTypeNames = new ArrayList<String>();
		for (SymbolType typeName : formalsTypes) {
			formalsTypeNames.add(typeName.toString());
		}
		builder.append(CourtesyErrorReporter.joinStrings(formalsTypeNames));
		builder.append(" -> ");
		builder.append(getReturnType());
		builder.append("}");
		return builder.toString();
	}

	private static boolean areListsEqual(List<?> list1, List<?> list2) {
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

	@Override
	public String getHeader() {
		return "Method type";
	}

	@Override
	public int getDisplaySortIndex() {
		return 4;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MethodSymbolType)) {
			return false;
		}
		MethodSymbolType other = (MethodSymbolType) obj;
		return other.isStatic == this.isStatic
				&& other.getReturnType().equals(this.getReturnType())
				&& areListsEqual(other.formalsTypes, this.formalsTypes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isStatic ? 1 : 0);
		result = prime * result + getReturnType().hashCode();
		for (SymbolType formalType : formalsTypes) {
			result = prime * result + formalType.hashCode();
		}
		return result;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public SymbolType getReturnType() {
		return returnType;
	}

	public List<SymbolType> getFormalsTypes() {
		return formalsTypes;
	}

	@Override
	public boolean isReferenceType() {
		return false;
	}
}
