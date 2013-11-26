package codegen.C.exp;

import codegen.C.Visitor;

public class NewObject extends T
{
  public String id;
  // Lab4, exercise 1: this field
  // is used to name the allocation.
  public String name;

  public NewObject(String id)
  {
    this.id = id;
  }
  
  public NewObject(String id, String name)
  {
    this.id = id;
    this.name = name;
  }
  
  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}
