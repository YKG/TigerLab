package ast.exp;

public class True extends T
{
  public True()
  {
  }

  public True(int lineNum, int colNum)
  {
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
