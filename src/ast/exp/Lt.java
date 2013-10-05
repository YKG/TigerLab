package ast.exp;

public class Lt extends T
{
  public T left;
  public T right;

  public Lt(T left, T right)
  {
    this.left = left;
    this.right = right;
  }
  
  public Lt(T left, T right, int lineNum, int colNum)
  {
    this.left = left;
    this.right = right;
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
