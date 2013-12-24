package cfg.block;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import cfg.Visitor;

public class Block extends T
{
  public util.Label label;
  public java.util.LinkedList<cfg.stm.T> stms;
  public cfg.transfer.T transfer;
  
  public Block(util.Label label,
      java.util.LinkedList<cfg.stm.T> stms,
      cfg.transfer.T transfer)
  {
    this.label = label;
    this.stms = stms;
    this.transfer = transfer;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o==null)
      return false;
    
    if (!(o instanceof Block))
      return false;
    
    Block ob = (Block)o;
    return this.label.equals(ob.label);
  }
  
  @Override
  public String toString()
  {
    cfg.BlockPrintVisitor bp = new cfg.BlockPrintVisitor();
    this.accept(bp);
    return bp.sb.toString();
  }
  
  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}
