package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHStructDef;

public class LHClassType {
  private Class<?> clazz;
  private List<Class<?>> dependencyClasses;

  public LHClassType(Class<?> clazz) {
    this.clazz = clazz;
  }

  public boolean isLHPrimitive() {
    return LHLibUtil.isJavaClassLHPrimitive(this.clazz);
  }

  public boolean isStructDef() {
    return this.clazz.isAnnotationPresent(LHStructDef.class);
  }

  public String getStructDefName() {
    return this.clazz.getAnnotation(LHStructDef.class).name();
  }

  public VariableType getPrimitiveType() {
    return LHLibUtil.javaClassToLHVarType(this.clazz);
  }

  public List<Class<?>> getDependencyClasses() {
    if (this.dependencyClasses == null) {
      this.dependencyClasses = collectDependencyClasses();
    }
    return Collections.unmodifiableList(this.dependencyClasses);
  }

  private List<Class<?>> collectDependencyClasses() {
    if (!this.clazz.isAnnotationPresent(LHStructDef.class)) {
      throw new IllegalArgumentException(
          "Missing `@LHStructDef` annotation on class: " + this.clazz.getCanonicalName());
    }

    Set<Class<?>> visited = new HashSet<>();
    List<Class<?>> sortedList = new ArrayList<>();
    Set<Class<?>> tempMarked = new HashSet<>();

    detectCycle(visited, sortedList, tempMarked, new ArrayList<>());

    return sortedList;
  }

  private void detectCycle(
      Set<Class<?>> visited,
      List<Class<?>> sortedList,
      Set<Class<?>> tempMarked,
      List<Class<?>> currentPath) {

    // If we've already visited this locally in a sibling field
    if (visited.contains(clazz)) {
      return;
    }

    currentPath.add(clazz);

    // If we've already visited this class in an ancestor...
    if (tempMarked.contains(clazz)) {
      throw new StructDefCircularDependencyException(buildCircularDependencyExceptionMessage(currentPath));
    }

    tempMarked.add(clazz);

    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
          Class<?> returnType = null;
          Class<?> paramType = null;

          if ("class".equals(pd.getName())) continue;

          if (pd.getReadMethod() != null) {
            returnType = pd.getReadMethod().getReturnType();
          }
          if (pd.getWriteMethod() != null) {
            paramType = pd.getWriteMethod().getParameterTypes()[0];
          }

          Class<?> realClazz = (returnType != null) ? returnType : paramType;
          LHClassType lhClassType = new LHClassType(realClazz);

          if (!lhClassType.isLHPrimitive()) {
            if (!lhClassType.isStructDef()) {
              throw new IllegalArgumentException(
                  "Missing @LHStructDef annotation on non-primitive class used in an LHStructDef: "
                      + realClazz.getName());
            }
            lhClassType.detectCycle(visited, sortedList, tempMarked, currentPath);
          }
      }
    } catch (IntrospectionException e) {
      e.printStackTrace();
      throw new RuntimeException("Blahh");
    }

    // Add the class to the result in topologically sorted order
    currentPath.remove(currentPath.size() - 1); // Remove from current path
    tempMarked.remove(clazz);
    visited.add(clazz);
    sortedList.add(clazz);
  }

  private static String buildCircularDependencyExceptionMessage(List<Class<?>> classList) {
    if (classList.isEmpty()) {
      throw new IllegalStateException(
          "Tried to throw Circular Dependency exception but no classes found in class tree.");
    }

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Circular dependency found involving class: "
        + classList.get(classList.size() - 1).getCanonicalName() + "\n");

    stringBuilder.append("\nDependency tree:\n");

    for (int i = 0; i < classList.size(); i++) {
      Class<?> visitedClass = classList.get(i);
      stringBuilder
          .append("  ".repeat(i))
          .append("- ")
          .append(visitedClass.getCanonicalName())
          .append("\n");
    }
    return stringBuilder.toString();
  }
}
