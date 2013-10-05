package ast.exp;

public class NewIntArray extends T
{
  public T exp;

  public NewIntArray(T exp)
  {
    this.exp = exp;
  }
  
  public NewIntArray(T exp, int lineNum, int colNum)
  {
    this.exp = exp;
    this.lineNum = lineNum;
    this.colNum = colNum;
  }
  
  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
    return;
  }
}
