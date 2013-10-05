package ast.exp;

public class This extends T
{
  public This()
  {
  }

  public This(int lineNum, int colNum)
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
