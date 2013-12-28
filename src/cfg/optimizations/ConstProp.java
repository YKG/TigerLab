package cfg.optimizations;

public class ConstProp implements cfg.Visitor
{
  public cfg.program.T program;
  
  // in, out for statements
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmIn;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmOut;
  
  private boolean changed;
//  private cfg.stm.T currentStm;
  private cfg.operand.T newOperand;
//  private cfg.stm.T newStm;
  
  public ConstProp()
  {
    this.program = null;
  } 

  /////////////////////////////////////////////////////////
  private boolean check(cfg.operand.T operand, java.util.HashSet<cfg.stm.T> set)
  {
	  if(operand instanceof cfg.operand.Int) return false;
	  
	  cfg.stm.T target = null;
	  int count = 0;
	  for(cfg.stm.T s : set){
		  if(s.getDst().equals(((cfg.operand.Var)operand).id)){
			  count++;
			  if(count > 1) return false;
			  target = s;
		  }
	  }
	  if(!(target instanceof cfg.stm.Move)) return false;
	  if(((cfg.stm.Move)target).src instanceof cfg.operand.Int) {
		  this.newOperand = ((cfg.stm.Move)target).src;
		  this.changed = true;
		  return true;
	  }
	  return false;
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
	  if(check(s.left, this.stmIn.get(s)))
		  s.left = this.newOperand;
	  if(check(s.right, this.stmIn.get(s)))
		  s.right = this.newOperand;
  }

  @Override
  public void visit(cfg.stm.InvokeVirtual s)
  {
	  for(int i = 0; i < s.args.size(); i++)
		  if(check(s.args.get(i), this.stmIn.get(s)))
			  s.args.set(i, this.newOperand);
  }

  @Override
  public void visit(cfg.stm.Lt s)
  {
	  if(check(s.left, this.stmIn.get(s)))
		  s.left = this.newOperand;
	  if(check(s.right, this.stmIn.get(s)))
		  s.right = this.newOperand;
  }

  @Override
  public void visit(cfg.stm.Move s)
  {
	  if(check(s.src, this.stmIn.get(s)))
		  s.src = this.newOperand;
  }

  @Override
  public void visit(cfg.stm.NewObject s)
  {
  }

  @Override
  public void visit(cfg.stm.Print s)
  {
	  if(check(s.arg, this.stmIn.get(s)))
		  s.arg = this.newOperand;
  }

  @Override
  public void visit(cfg.stm.Sub s)
  {
	  if(check(s.left, this.stmIn.get(s)))
		  s.left = this.newOperand;
	  if(check(s.right, this.stmIn.get(s)))
		  s.right = this.newOperand;
  }

  @Override
  public void visit(cfg.stm.Times s)
  {
	  if(check(s.left, this.stmIn.get(s)))
		  s.left = this.newOperand;
	  if(check(s.right, this.stmIn.get(s)))
		  s.right = this.newOperand;
  }

  // transfer
  @Override
  public void visit(cfg.transfer.If s)
  {
	  if(check(s.operand, this.stmIn.get(s)))
		  s.operand = this.newOperand;
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
	  if(control.Control.isTracing("constprop.loop")){
		  System.out.println("========= block before constprop========");
		  System.out.println(b.toString());
	  }
	  
	  for(cfg.stm.T s : b.stms){
//		  System.out.println("s before: " + s);
		  s.accept(this);
//		  if(!this.newStm.equals(s)){
//			  this.changed = true;
//			  s = this.newStm; /* YKG. In place replacement, Is it OK? */
//		  }
		  
		  
//		  System.out.println("s after: " + s + " " + this.changed);
	  }
	  
	  if(control.Control.isTracing("constprop.loop")){
		  System.out.println("========= block after constprop========");
		  System.out.println(b.toString());
	  }
  }

  // method
  @Override
  public void visit(cfg.method.Method m)
  {
	  if(control.Control.isTracing("constprop")){
		  System.out.println("========= block before constprop========");
		  for(cfg.block.T b : m.blocks)
			  System.out.println(b.toString());
	  }
	  
//	  java.util.LinkedList<cfg.block.T> newBlocks = new java.util.LinkedList<cfg.block.T>();
	  this.changed = true;
	  while(this.changed){
		  this.changed = false;
		  for(cfg.block.T b : m.blocks){
			  b.accept(this);
//			  newBlocks.add(this.newBlock);
		  }
	  }
//	  this.newBlocks = newBlocks;
	  
	  if(control.Control.isTracing("constprop")){
		  System.out.println("========= block after constprop========");
		  for(cfg.block.T b : m.blocks)
			  System.out.println(b.toString());
	  }
  }

  @Override
  public void visit(cfg.mainMethod.MainMethod m)
  {
	  if(control.Control.isTracing("constprop")){
		  System.out.println("========= block before constprop========");
		  for(cfg.block.T b : m.blocks)
			  System.out.println(b.toString());
	  }
	  
//	  java.util.LinkedList<cfg.block.T> newBlocks = new java.util.LinkedList<cfg.block.T>();
	  this.changed = true;
	  while(this.changed){
		  this.changed = false;
		  for(cfg.block.T b : m.blocks){
			  b.accept(this);
//			  newBlocks.add(this.newBlock);
		  }
	  }
//	  this.newBlocks = newBlocks;
	  
	  if(control.Control.isTracing("constprop")){
		  System.out.println("========= block after constprop========");
		  for(cfg.block.T b : m.blocks)
			  System.out.println(b.toString());
	  }
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
    p.mainMethod.accept(this);
    
//    this.newMainMethod = new cfg.mainMethod.MainMethod(
//    		(cfg.mainMethod.MainMethod)p.mainMethod, this.newMainblocks);
//    java.util.LinkedList<E>
    for(cfg.method.T method : p.methods)
    	method.accept(this);
    
  }

@Override
public void visit(cfg.stm.And s) {
	  if(check(s.left, this.stmIn.get(s)))
		  s.left = this.newOperand;
	  if(check(s.right, this.stmIn.get(s)))
		  s.right = this.newOperand;
}

@Override
public void visit(cfg.stm.ArraySelect s) {
	  if(check(s.index, this.stmIn.get(s)))
		  s.index = this.newOperand;
}

@Override
public void visit(cfg.stm.AssignArray s) {
	  if(check(s.exp, this.stmIn.get(s)))
		  s.exp = this.newOperand;
	  if(check(s.index, this.stmIn.get(s)))
		  s.index = this.newOperand;
}

@Override
public void visit(cfg.stm.Length s) {
}

@Override
public void visit(cfg.stm.NewIntArray s) {
	  if(check(s.array, this.stmIn.get(s)))
		  s.array = this.newOperand;
}

@Override
public void visit(cfg.stm.Xor s) {
	  if(check(s.exp, this.stmIn.get(s)))
		  s.exp = this.newOperand;
}

}
