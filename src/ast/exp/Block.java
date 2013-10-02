package ast.exp;

public class Block extends T
{
  public T exp;

  public Block(T exp)
  {
    this.exp = exp;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
    return;
  }
}
