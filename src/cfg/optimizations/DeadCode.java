package cfg.optimizations;

import cfg.PrettyPrintVisitor;
import cfg.block.Block;
import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;

public class DeadCode implements cfg.Visitor
{
  public cfg.program.T program;
  public cfg.mainMethod.T newMainMethod;
  public cfg.method.T newMethod;
  public cfg.block.T newBlock;
  
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;
  
  public DeadCode()
  {
    this.program = null;
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
	  if(control.Control.isTracing("deadcode")){
		  System.out.println("====== new block ======");
		  System.out.println(b.toString());
	  }
	  java.util.LinkedList<cfg.stm.T> stmts = new java.util.LinkedList<cfg.stm.T>();
	  for(cfg.stm.T s : b.stms){
		  if(!this.stmLiveIn.get(s).equals(this.stmLiveOut.get(s))){
			  stmts.add(s);
			  
			  if(control.Control.isTracing("deadcode"))
				  System.out.println("Keep: " + s);
		  }
		  else{
			  if(control.Control.isTracing("deadcode"))
				  System.out.println("Delete: " + s);
		  }
		  if(control.Control.isTracing("deadcode")){
			  System.out.println("\tkill: " + this.stmLiveIn.get(s));
			  System.out.println("\tliveOut: " + this.stmLiveOut.get(s));
		  }
	  }
	  this.newBlock = new cfg.block.Block(b.label, stmts, b.transfer);
	  
	  if(control.Control.isTracing("deadcode")){
		  System.out.println("------- after:");
		  System.out.println(this.newBlock.toString());
	  }
  }

  // method
  @Override
  public void visit(cfg.method.Method m)
  {
	  java.util.LinkedList<cfg.block.T> newBlocks = new java.util.LinkedList<cfg.block.T>();
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
		  newBlocks.add(this.newBlock);
	  }
	  this.newMethod = new cfg.method.Method(m.retType, m.id, m.classId, m.formals, m.locals, newBlocks, m.entry, m.exit, m.retValue);
  }

  @Override
  public void visit(cfg.mainMethod.MainMethod m)
  {
	  java.util.LinkedList<cfg.block.T> newBlocks = new java.util.LinkedList<cfg.block.T>();
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
		  newBlocks.add(this.newBlock);
	  }
	  this.newMainMethod = new cfg.mainMethod.MainMethod(m.locals, newBlocks);
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
    this.program = p;
    
    java.util.LinkedList<cfg.method.T> newMethods = new java.util.LinkedList<cfg.method.T>();
    p.mainMethod.accept(this);
    for(cfg.method.T m : p.methods){
    	m.accept(this);
    	newMethods.add(this.newMethod);
    }
    
    this.program = new cfg.program.Program(p.classes, p.vtables, newMethods, this.newMainMethod);
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
public void visit(Not not) {
	// TODO Auto-generated method stub
	
}

}
