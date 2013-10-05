package ast.exp;

public class False extends T
{
  public False()
  {
  }

  public False(int lineNum, int colNum)
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
