package IC.LIR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import IC.AST.Field;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.VirtualMethod;

public class ClassLayout {
	Map<Method, Integer> methodToOffset = new HashMap<Method, Integer>();
	// DVPtr = 0
	Map<String, Integer> fieldToOffset = new HashMap<String, Integer>();
	Map<String, String> methodNameToLabel = new HashMap<String, String>();
	List<Pair<String, String>> methodNamesAndLabelsInOrder = new ArrayList<Pair<String, String>>();
	ClassLayout baseClassLayout;

	int numFields = 0;

	public ClassLayout(ICClass clazz) {
		this(clazz, null);
	}

	public ClassLayout(ICClass clazz, ClassLayout baseClassLayout) {
		this.baseClassLayout = baseClassLayout;
		if (baseClassLayout != null) {
			for (Entry<String, String> nameToLabelInBase : baseClassLayout.methodNameToLabel
					.entrySet()) {
				methodNameToLabel.put(nameToLabelInBase.getKey(),
						nameToLabelInBase.getValue());
			}
		}
		for (Method method : clazz.getMethods()) {
			if (method instanceof VirtualMethod) {
				methodNameToLabel.put(
						method.getName(),
						String.format("_%s_%s", clazz.getName(),
								method.getName()));
			}
		}
		if (baseClassLayout != null) {
			for (Pair<String, String> methodNameAndLabel : baseClassLayout.methodNamesAndLabelsInOrder) {
				methodNamesAndLabelsInOrder.add(new Pair<String, String>(
						methodNameAndLabel.fst, methodNameToLabel
								.get(methodNameAndLabel.fst)));
			}
		}
		for (Method method : clazz.getMethods()) {
			if (method instanceof VirtualMethod
					&& !isMethodOverride(baseClassLayout, method)) {
				methodNamesAndLabelsInOrder.add(new Pair<String, String>(method
						.getName(), methodNameToLabel.get(method.getName())));
			}
		}

		int methodOffset = 0;
		int fieldOffset = 1;
		if (baseClassLayout != null) {
			methodOffset = baseClassLayout.methodToOffset.size();
			fieldOffset = baseClassLayout.fieldToOffset.size() + 1;
			numFields = baseClassLayout.numFields;
		}
		for (Method method : clazz.getMethods()) {
			if (!(method instanceof VirtualMethod)) {
				continue;
			}
			if (isMethodOverride(baseClassLayout, method)) {
				methodToOffset.put(method,
						getMethodOffsetInBase(baseClassLayout, method));
			} else {
				methodToOffset.put(method, methodOffset);
				methodOffset++;
			}
		}
		if (baseClassLayout != null) {
			for (Entry<String, Integer> fieldOffsetInBase : baseClassLayout.fieldToOffset
					.entrySet()) {
				fieldToOffset.put(fieldOffsetInBase.getKey(),
						fieldOffsetInBase.getValue());
			}
		}
		for (Field field : clazz.getFields()) {
			fieldToOffset.put(field.getName(), fieldOffset);
			fieldOffset++;
			numFields++;
		}
	}

	private int getMethodOffsetInBase(ClassLayout baseClassLayout, Method method) {
		int i = 0;
		for (Pair<String, String> nameAndLabel : baseClassLayout.methodNamesAndLabelsInOrder) {
			if (nameAndLabel.fst.equals(method.getName())) {
				return i;
			}
			i++;
		}
		printErrorMessage("Method in not overridden. Internal incosistency.");
		return -1;
	}

	private void printErrorMessage(String errorMessage) {
		try {
			throw new Exception(errorMessage);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private boolean isMethodOverride(ClassLayout baseClassLayout, Method method) {
		return baseClassLayout != null
				&& baseClassLayout.methodNameToLabel.containsKey(method
						.getName());
	}

	public List<String> getDispatchVector() {
		List<String> dv = new ArrayList<String>();
		for (Pair<String, String> nameAndLabel : methodNamesAndLabelsInOrder) {
			dv.add(nameAndLabel.snd);
		}
		return dv;
	}

	public int getNumField() {
		return fieldToOffset.size()
				+ (baseClassLayout != null ? baseClassLayout.getNumField() : 0);
	}

	public int getFieldOffset(String name) {
		if (!fieldToOffset.containsKey(name)) {
			printErrorMessage("Compiler unexpected error: a scope is supposed to be present.");
		}
		return fieldToOffset.get(name);
	}

	class Pair<X, Y> {
		final X fst;
		final Y snd;

		Pair(X fst, Y snd) {
			this.fst = fst;
			this.snd = snd;
		}
	}
}
