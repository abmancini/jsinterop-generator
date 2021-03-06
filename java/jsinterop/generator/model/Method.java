/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package jsinterop.generator.model;

import static com.google.common.collect.Lists.transform;
import static jsinterop.generator.model.EntityKind.CONSTRUCTOR;
import static jsinterop.generator.model.EntityKind.METHOD;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Models java methods. */
public class Method extends Entity implements HasTypeParameters, Visitable<Method> {

  /** Models parameters of java methods. */
  public static class Parameter implements Visitable<Parameter> {
    public static Parameter from(Parameter parameter) {
      return new Parameter(
          parameter.getName(), parameter.getType(), parameter.isVarargs(), parameter.isOptional());
    }

    private TypeReference type;
    private boolean varargs;
    private final boolean optional;
    private String name;

    public Parameter(String name, TypeReference type, boolean varargs, boolean optional) {
      this.name = name;
      this.type = type;
      this.varargs = varargs;

      this.optional = optional;
    }

    public TypeReference getType() {
      return type;
    }

    public String getName() {
      return name;
    }

    public boolean isVarargs() {
      return varargs;
    }

    public boolean isOptional() {
      return optional;
    }

    public void setType(TypeReference type) {
      this.type = type;
    }

    @Override
    public Parameter doVisit(ModelVisitor visitor) {
      if (visitor.visit(this)) {
        setType(visitor.accept(type));
      }

      visitor.endVisit(this);

      return this;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    public String getJniSignature() {
      TypeReference jniType = isVarargs() ? new ArrayTypeReference(getType()) : getType();
      return jniType.getJniSignature();
    }
  }

  public static Method from(Method method) {
    Method m = new Method();

    Entity.copyEntityProperties(method, m);

    m.setKind(method.getKind());
    m.setReturnType(method.getReturnType());

    m.setBody(method.getBody());

    method.getParameters().stream().map(Parameter::from).forEach(m::addParameter);

    method.getTypeParameters().forEach(m::addTypeParameter);

    return m;
  }

  public static Method newConstructor() {
    return new Method(true);
  }

  private static final Function<Parameter, String> toParameterTypeName =
      parameter -> {
        TypeReference type =
            parameter.isVarargs() ? new ArrayTypeReference(parameter.type) : parameter.type;
        return type.getJavaTypeFqn();
      };

  private List<Parameter> parameters = new LinkedList<>();
  private TypeReference returnType;
  private List<TypeReference> typeParameters = new LinkedList<>();
  private Statement body;
  private boolean isDefault;

  public Method(boolean isConstructor) {
    setKind(isConstructor ? CONSTRUCTOR : METHOD);
  }

  public Method() {
    this(false);
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }

    List<String> parameterTypes = transform(parameters, toParameterTypeName);
    List<String> otherParameterTypes = transform(((Method) o).parameters, toParameterTypeName);

    return Objects.equals(parameterTypes, otherParameterTypes);
  }

  @Override
  public int hashCode() {
    List<String> parameterTypes = transform(parameters, toParameterTypeName);
    return Objects.hash(super.hashCode(), parameterTypes);
  }

  public List<Parameter> getParameters() {
    return ImmutableList.copyOf(parameters);
  }

  public void clearParameters() {
    parameters.clear();
  }

  public void addParameter(Parameter parameter) {
    this.parameters.add(parameter);
  }

  public TypeReference getReturnType() {
    return returnType;
  }

  public void setReturnType(TypeReference returnType) {
    this.returnType = returnType;
  }

  @Override
  public void addTypeParameter(TypeReference typeParameter) {
    typeParameters.add(typeParameter);
  }

  @Override
  public List<TypeReference> getTypeParameters() {
    return typeParameters;
  }

  public void setTypeParameters(List<TypeReference> typeParameters) {
    this.typeParameters = typeParameters;
  }

  public Statement getBody() {
    return body;
  }

  public void setBody(Statement body) {
    this.body = body;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public String toString() {
    return getName() + "(" + Joiner.on(", ").join(getParameters()) + ")";
  }

  @Override
  public Method doVisit(ModelVisitor visitor) {
    if (visitor.visit(this)) {
      if (getKind() == METHOD) {
        setReturnType(visitor.accept(returnType));
      }

      visitor.accept(parameters);

      if (body != null) {
        visitor.accept(body);
      }
    }

    visitor.endVisit(this);

    return this;
  }

  public String getJniSignatureWithoutReturn() {
    String parameters =
        getParameters().stream().map(Parameter::getJniSignature).collect(Collectors.joining());

    String methodName = getKind() == EntityKind.CONSTRUCTOR ? "%constructor%" : getName();

    return methodName + "(" + parameters + ")";
  }
}
