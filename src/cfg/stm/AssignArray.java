package cfg.stm;

import cfg.Visitor;

public class AssignArray extends T
{
  public String id;
  // type of the destination variable
  public cfg.operand.T index;
  public cfg.operand.T exp;

  public AssignArray(String id, cfg.operand.T index, cfg.operand.T exp)
  {
    this.id = id;
    this.index = index;
    this.exp = exp;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
  
  public String getDst()
  {
	  return "";
  }
}
