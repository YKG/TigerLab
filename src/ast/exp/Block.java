package ast.exp;

public class Block extends T
{
  public T exp;

  public Block(T exp)
  {
    this.exp = exp;
  }
  
  public Block(T exp, int lineNum, int colNum){
	  this(exp);
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
