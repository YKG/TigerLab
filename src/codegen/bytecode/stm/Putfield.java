package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Putfield extends T {
  public String className;
  public String fieldName;
  public codegen.bytecode.type.T type;

  public Putfield(String className, String fieldName, codegen.bytecode.type.T type)
  {
	  this.className = className;
	  this.fieldName = fieldName;
	  this.type = type;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}
