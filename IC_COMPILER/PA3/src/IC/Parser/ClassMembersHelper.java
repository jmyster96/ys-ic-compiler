package IC.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import IC.AST.Field;
import IC.AST.Method;

/**
 * A helper class for the Parser, that allows accumulating Fields and Methods
 * in a statically-typed manner. Used while parsing class members.
 */
class ClassMembersHelper {
  final List<Field> fields = new ArrayList<Field>(); 
  final List<Method> methods = new ArrayList<Method>();

  public void add(Field field) {
    fields.add(field);
  }

  public void add(Method method) {
    methods.add(method);
  }

  public List<Field> getFields() {
    return Collections.unmodifiableList(fields);
  }

  public List<Method> getMethods() {
    return Collections.unmodifiableList(methods);
  }
}