package cfg.optimizations;

import java.util.HashSet;

import cfg.optimizations.LivenessVisitor.Liveness_Kind_t;

public class ReachingDefinition implements cfg.Visitor
{
  // gen, kill for one statement
  private java.util.HashSet<cfg.stm.T> oneStmGen;
  private java.util.HashSet<cfg.stm.T> oneStmKill;

  // gen, kill for one transfer
  private java.util.HashSet<cfg.stm.T> oneTransferGen;
  private java.util.HashSet<cfg.stm.T> oneTransferKill;

  // gen, kill for statements
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmGen;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmKill;

  public java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmGen2;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmKill2;

  
  // gen, kill for transfers
  private java.util.HashMap<cfg.transfer.T, java.util.HashSet<cfg.stm.T>> transferGen;
  private java.util.HashMap<cfg.transfer.T, java.util.HashSet<cfg.stm.T>> transferKill;

  // gen, kill for blocks
  private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockGen;
  private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockKill;

  // in, out for blocks
  private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockIn;
  private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockOut;

  // in, out for statements
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmIn;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmOut;

  // liveIn, liveOut for transfer
  public java.util.HashMap<cfg.transfer.T, java.util.HashSet<cfg.stm.T>> transferIn;
  public java.util.HashMap<cfg.transfer.T, java.util.HashSet<cfg.stm.T>> transferOut;

  
  private java.util.HashMap<util.Label, java.util.HashSet<cfg.block.T>> predBlocks;
  private java.util.HashMap<String, java.util.HashSet<cfg.stm.T>> defSet;
  
  enum ReachingDef_Kind_t
  {
    None, DefSet, StmGenKill, AllInOne ,BlockGenKill, BlockInOut, StmInOut,
  }

  private ReachingDef_Kind_t kind = ReachingDef_Kind_t.None;
  
  public ReachingDefinition()
  {
    this.oneStmGen = new java.util.HashSet<>();
    this.oneStmKill = new java.util.HashSet<>();

    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();

    this.stmGen = new java.util.HashMap<>();
    this.stmKill = new java.util.HashMap<>();
    
    this.transferGen = new java.util.HashMap<>();
    this.transferKill = new java.util.HashMap<>();

    this.blockGen = new java.util.HashMap<>();
    this.blockKill = new java.util.HashMap<>();

    this.blockIn = new java.util.HashMap<>();
    this.blockOut = new java.util.HashMap<>();

    this.stmIn = new java.util.HashMap<>();
    this.stmOut = new java.util.HashMap<>();

    this.transferIn = new java.util.HashMap<>();
    this.transferOut = new java.util.HashMap<>();
    
    this.defSet = new java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>();
    this.stmGen2 = new java.util.HashMap<>();
    this.stmKill2 = new java.util.HashMap<>();
  }

  // /////////////////////////////////////////////////////
  // utilities

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

  private String getDst(cfg.stm.T s)
  {
	if(s instanceof cfg.stm.AssignArray || s instanceof cfg.stm.Print)
		return "";
  	if(this.stmKill.get(s).isEmpty() || this.stmKill.get(s).size() > 1)
  		System.err.println("ERROR: EMPTY or > 1 !? ");
  	Object []dsts = this.stmKill.get(s).toArray();
  	String dst = (String)dsts[0];
  	
  	return dst;
  }
  
  private void calculateDefSet(cfg.block.Block b)
  {
    for (cfg.stm.T s : b.stms) {
    	String dst = getDst(s);
    	if(dst.length() <= 0) continue;

    	if(this.defSet.containsKey(dst))
    		this.defSet.get(dst).add(s);
    	else{
    		java.util.HashSet<cfg.stm.T> newSet = new java.util.HashSet<cfg.stm.T>();
    		newSet.add(s);
    		this.defSet.put(dst, newSet);
    	}
    }
	  
      if(control.Control.isTracing("reaching.step0")){
		  System.out.println(b.toString());
		  System.out.println(this.defSet);
	  }
  }
  
  
  private java.util.HashSet<cfg.stm.T> hashSetCopy(java.util.HashSet<cfg.stm.T> set)
  {
	  java.util.HashSet<cfg.stm.T> tmpSet = new java.util.HashSet<cfg.stm.T>();
	  for(cfg.stm.T s : set)
		  tmpSet.add(s);
	  return tmpSet;
  }
  private void calculateStmGenKill(cfg.block.Block b)
  {
    for (cfg.stm.T s : b.stms) {
    	String dst = getDst(s);
    	if(dst.length() <= 0){
    		this.stmGen2.put(s, new java.util.HashSet<cfg.stm.T>());
    		this.stmKill2.put(s, new java.util.HashSet<cfg.stm.T>());
    		continue;
    	}

    	java.util.HashSet<cfg.stm.T> genSet = new java.util.HashSet<cfg.stm.T>();
    	genSet.add(s);
    	this.stmGen2.put(s, genSet);
    	java.util.HashSet<cfg.stm.T> killSet = hashSetCopy(this.defSet.get(dst));
    	killSet.remove(s);
    	this.stmKill2.put(s, killSet);
    }
    
    if(control.Control.isTracing("reaching.step1")){
    	System.out.println("====== reaching.step1 ======");
    	System.out.println(b.toString());
        for (cfg.stm.T s : b.stms) {
        	System.out.println(s);
        	System.out.println("\tgen :" + this.stmGen2.get(s));
        	System.out.println("\tkill:" + this.stmKill2.get(s));
        }
	}
  }
  
  private void calculateBlockGenKill(cfg.block.Block b)
  {
	java.util.HashSet<cfg.stm.T> genSet = new java.util.HashSet<cfg.stm.T>();
	java.util.HashSet<cfg.stm.T> killSet = new java.util.HashSet<cfg.stm.T>();
    for (cfg.stm.T s : b.stms) {
    	String dst = getDst(s);
    	if(dst.length() <= 0){
//    		java.util.HashSet<cfg.stm.T> newGenSet = new java.util.HashSet<cfg.stm.T>();
//    		newGenSet.addAll(genSet);
//    		this.stmIn.get(s).addAll(newGenSet);
    		continue;
    	}
    	
    	genSet.removeAll(this.stmKill2.get(s));
    	genSet.addAll(this.stmGen2.get(s));
    	killSet.addAll(this.stmKill2.get(s));
    }
    
    this.blockGen.put(b, genSet);
    this.blockKill.put(b, killSet);
    
    if(control.Control.isTracing("reaching.step2")){
    	System.out.println("====== reaching.step2 ======");
    	System.out.println(b.toString());
    	System.out.println("\tgen :" + this.blockGen.get(b));
    	System.out.println("\tkill:" + this.blockKill.get(b));
	}
  }
  
  private void calculatePredBlocks(java.util.LinkedList<cfg.block.T> blocks)
  {
	  boolean fixpointFlag = false;
	  this.predBlocks = new java.util.HashMap<util.Label, java.util.HashSet<cfg.block.T>>();
	  
	  int count = 1;
	  for(cfg.block.T block : blocks){
		  cfg.block.Block b = (cfg.block.Block)block;
		  this.predBlocks.put(b.label, new java.util.HashSet<cfg.block.T>());
	  }
	  while(!fixpointFlag){
		  fixpointFlag = true;
		  for(cfg.block.T block : blocks){
			  cfg.block.Block b = (cfg.block.Block)block;
			  if(b.transfer instanceof cfg.transfer.If){
				  if(!this.predBlocks.get(((cfg.transfer.If)(b.transfer)).truee).contains(b)){
					  this.predBlocks.get(((cfg.transfer.If)(b.transfer)).truee).add(b);
					  fixpointFlag = false;
				  }
				  if(!this.predBlocks.get(((cfg.transfer.If)(b.transfer)).falsee).contains(b)){
					  this.predBlocks.get(((cfg.transfer.If)(b.transfer)).falsee).add(b);
					  fixpointFlag = false;
				  }
			  }else if(b.transfer instanceof cfg.transfer.Goto){
				  if(!this.predBlocks.get(((cfg.transfer.Goto)(b.transfer)).label).contains(b)){
					  this.predBlocks.get(((cfg.transfer.Goto)(b.transfer)).label).add(b);
					  fixpointFlag = false;
				  }
			  }else{
				// return. Do not need to do any processing.  
			  }
		  }
		  
		  System.err.println("count: " + count);
		  count++;
		  if(count > 100) System.exit(1);
	  }
  }
  
  private void initBlockInOut(java.util.LinkedList<cfg.block.T> blocks)
  {
	  for(cfg.block.T b : blocks)
	  {
//		  this.blockIn.put(b, this.blockKill.get(b));
		  this.blockOut.put(b, this.blockGen.get(b));
		  this.blockIn.put(b, new java.util.HashSet<cfg.stm.T>());
	  }
  }
  
  private void calculateBlockInOut(java.util.LinkedList<cfg.block.T> blocks)
  {
	  if(control.Control.isTracing("reaching.step3")){
		  System.out.println("====== reaching.step3 ======");
	  }
	  
	  calculatePredBlocks(blocks);
	  initBlockInOut(blocks);
	  int count = 0;
	  boolean fixpointFlag = false;
	  while(!fixpointFlag)
	  {
		  fixpointFlag = true;
		  if(control.Control.isTracing("reaching.step3")){
			  System.out.println("----- round: " + count++);
			  for(cfg.block.T block : blocks){
				  cfg.block.Block b = (cfg.block.Block)block;
				  System.out.println(b);
				  System.out.println("\t in :" + this.blockIn.get(b));
				  System.out.println("\t out:" + this.blockOut.get(b));
			  }
		  }
		  
		  for(cfg.block.T block : blocks){
			  cfg.block.Block b = (cfg.block.Block)block;
			  
			  java.util.HashSet<cfg.stm.T> newBlockIn = new java.util.HashSet<cfg.stm.T>();
			  java.util.HashSet<cfg.stm.T> newBlockOut = new java.util.HashSet<cfg.stm.T>();
			  if(this.predBlocks.get(b.label) == null)
				  System.err.println("why?");
			  for(cfg.block.T prev : this.predBlocks.get(b.label)){
				  newBlockIn.addAll(this.blockOut.get(prev));
			  }
			  newBlockOut.addAll(newBlockIn);
			  newBlockOut.removeAll(this.blockKill.get(b));
			  newBlockOut.addAll(this.blockGen.get(b));
			  if(!this.blockIn.get(b).equals(newBlockIn)) fixpointFlag = false;
			  if(!this.blockOut.get(b).equals(newBlockOut)) fixpointFlag = false;
			  this.blockIn.put(b, newBlockIn);
			  this.blockOut.put(b, newBlockOut);
		  }
		  

	  }
	  

  }
  
  private void calculateStmTransInOut(cfg.block.Block b)
  {
	  if(control.Control.isTracing("reaching.step4")){
		  System.out.println("====== reaching.step4 ======");
		  System.out.println(b.toString());
	  }
	  
	  java.util.HashSet<cfg.stm.T> currentOut = new java.util.HashSet<cfg.stm.T>();

	  currentOut.addAll(this.blockIn.get(b));
	  for(cfg.stm.T s : b.stms){
		  // dst == null pass?
		  java.util.HashSet<cfg.stm.T> newStmIn = new java.util.HashSet<cfg.stm.T>();
		  java.util.HashSet<cfg.stm.T> newStmOut = new java.util.HashSet<cfg.stm.T>();
		  newStmIn.addAll(currentOut);
		  newStmOut.addAll(newStmIn);
		  newStmOut.removeAll(this.stmKill2.get(s));
		  newStmOut.addAll(this.stmGen2.get(s));
		  
		  this.stmIn.put(s, newStmIn);
		  this.stmOut.put(s, newStmOut);
		  
		  currentOut.clear();
		  currentOut.addAll(newStmOut);
		  
		  if(control.Control.isTracing("reaching.step4")){
			  System.out.println(s);
			  System.out.println("\t in :" + newStmIn);
			  System.out.println("\t out:" + newStmOut);
		  }
	  }
	  this.transferIn.put(b.transfer, this.blockOut.get(b));
	  this.transferOut.put(b.transfer, this.blockOut.get(b));
  }
  
  
  // block
  @Override
  public void visit(cfg.block.Block b)
  {
	  switch(this.kind){
	  case DefSet:
		  	calculateDefSet(b);
			break;
	  case StmGenKill:
		  	calculateStmGenKill(b);
			break;
	  case BlockGenKill:
		  	calculateBlockGenKill(b);
			break;		
	  case StmInOut:
		  calculateStmTransInOut(b);
			break;
	  default:
	        return;  
	  }
	  
  }

  // method
  @Override
  public void visit(cfg.method.Method m)
  {
    // Five steps:
    // Step 0: for each argument or local variable "x" in the
    // method m, calculate x's definition site set def(x).
    // Your code here:
	  this.kind = ReachingDef_Kind_t.DefSet;
	  this.defSet = new java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>();
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
	  
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
	  this.kind = ReachingDef_Kind_t.StmGenKill;
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
	  
    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block sequentially.
    // Your code here:
	  this.kind = ReachingDef_Kind_t.BlockGenKill;
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
    // Step 3: calculate the "in" and "out" sets for each block
    // Note that to speed up the calculation, you should use
    // a topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:
	  this.kind = ReachingDef_Kind_t.BlockInOut;
	  calculateBlockInOut(m.blocks);
    // Step 4: calculate the "in" and "out" sets for each
    // statement and transfer
    // Your code here:
	  this.kind = ReachingDef_Kind_t.StmInOut;
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
  }

  @Override
  public void visit(cfg.mainMethod.MainMethod m)
  {
	// Five steps:
	// Step 0: for each argument or local variable "x" in the
	// method m, calculate x's definition site set def(x).
	// Your code here:
	  this.kind = ReachingDef_Kind_t.DefSet;
	  this.defSet = new java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>();
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
	  
	// Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
	  this.kind = ReachingDef_Kind_t.StmGenKill;
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
	  
    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block sequentially.
    // Your code here:
	  this.kind = ReachingDef_Kind_t.BlockGenKill;
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
	  }
    // Step 3: calculate the "in" and "out" sets for each block
    // Note that to speed up the calculation, you should use
    // a topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:
	  this.kind = ReachingDef_Kind_t.BlockInOut;
	  calculateBlockInOut(m.blocks);
    // Step 4: calculate the "in" and "out" sets for each
    // statement and transfer
    // Your code here:
	  this.kind = ReachingDef_Kind_t.StmInOut;
	  for(cfg.block.T block : m.blocks){
		  block.accept(this);
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
	    p.mainMethod.accept(this);
	    for (cfg.method.T mth : p.methods) {
	      mth.accept(this);
	    }
	    return;
  }

@Override
public void visit(cfg.stm.And and) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(cfg.stm.ArraySelect arraySelect) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(cfg.stm.AssignArray assignArray) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(cfg.stm.Length length) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(cfg.stm.NewIntArray newIntArray) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(cfg.stm.Xor s) {
	// TODO Auto-generated method stub
	
}

}
