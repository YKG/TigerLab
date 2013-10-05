package ast.exp;

public class Not extends T
{
  public T exp;

  public Not(T exp)
  {
    this.exp = exp;
  }

  public Not(T exp, int lineNum, int colNum)
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
