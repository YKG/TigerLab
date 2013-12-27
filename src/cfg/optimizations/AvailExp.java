package cfg.optimizations;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;

public class AvailExp implements cfg.Visitor
{
  
  public AvailExp()
  {
    
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(cfg.operand.Int operand)
  {
  }

  @Override
  public void visit(cfg.operand.Var operand)
  {
  }

  // statements
  @Override
  public void visit(cfg.stm.Add s)
  {
  }

  @Override
  public void visit(cfg.stm.InvokeVirtual s)
  {
  }

  @Override
  public void visit(cfg.stm.Lt s)
  {
  }

  @Override
  public void visit(cfg.stm.Move s)
  {
  }

  @Override
  public void visit(cfg.stm.NewObject s)
  {
  }

  @Override
  public void visit(cfg.stm.Print s)
  {
  }

  @Override
  public void visit(cfg.stm.Sub s)
  {
  }

  @Override
  public void visit(cfg.stm.Times s)
  {
  }

  // transfer
  @Override
  public void visit(cfg.transfer.If s)
  {
  }

  @Override
  public void visit(cfg.transfer.Goto s)
  {
    return;
  }

  @Override
  public void visit(cfg.transfer.Return s)
  {
  }

  // type
  @Override
  public void visit(cfg.type.Class t)
  {
  }

  @Override
  public void visit(cfg.type.Int t)
  {
  }

  @Override
  public void visit(cfg.type.IntArray t)
  {
  }

  // dec
  @Override
  public void visit(cfg.dec.Dec d)
  {
  }

  // block
  @Override
  public void visit(cfg.block.Block b)
  {
  }

  // method
  @Override
  public void visit(cfg.method.Method m)
  {
  }

  @Override
  public void visit(cfg.mainMethod.MainMethod m)
  {
  }

  // vtables
  @Override
  public void visit(cfg.vtable.Vtable v)
  {
  }

  // class
  @Override
  public void visit(cfg.classs.Class c)
  {
  }

  // program
  @Override
  public void visit(cfg.program.Program p)
  {
  }

@Override
public void visit(And and) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(ArraySelect arraySelect) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(AssignArray assignArray) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(Length length) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(NewIntArray newIntArray) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(cfg.stm.Xor s) {
	// TODO Auto-generated method stub
	
}

}
