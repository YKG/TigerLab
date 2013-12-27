package cfg.stm;

import cfg.Visitor;

public class NewIntArray extends T
{
  public String dst;
  // type of the destination variable
  public cfg.operand.T array;

  public NewIntArray(String dst, cfg.operand.T array)
  {
    this.dst = dst;
    this.array = array;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
  
  public String getDst()
  {
	  return dst;
  }
}
