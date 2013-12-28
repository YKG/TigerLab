package cfg.optimizations;

public class LivenessVisitor implements cfg.Visitor
{
  private boolean fixpointFlag;
  private java.util.LinkedList<cfg.block.T> reverseTopoSortBlocks;
  private java.util.Hashtable<util.Label, cfg.block.T> blockTable;
	
  // gen, kill for one statement
  private java.util.HashSet<String> oneStmGen;
  private java.util.HashSet<String> oneStmKill;

  // gen, kill for one transfer
  private java.util.HashSet<String> oneTransferGen;
  private java.util.HashSet<String> oneTransferKill;

  // gen, kill for statements
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmGen;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmKill;

  // gen, kill for transfers
  private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferGen;
  private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferKill;

  // gen, kill for blocks
  private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockGen;
  private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockKill;

  // liveIn, liveOut for blocks
  private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockLiveIn;
  private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockLiveOut;

  // liveIn, liveOut for statements
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

  // liveIn, liveOut for transfer
  public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveIn;
  public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveOut;

  private java.util.HashSet<cfg.block.T> visited;
  
  // As you will walk the tree for many times, so
  // it will be useful to recored which is which:
  enum Liveness_Kind_t
  {
    None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
  }

  private Liveness_Kind_t kind = Liveness_Kind_t.None;

  public LivenessVisitor()
  {
	this.visited = new java.util.HashSet<cfg.block.T>();
	this.reverseTopoSortBlocks = new java.util.LinkedList<cfg.block.T>();  
	  
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

    this.blockLiveIn = new java.util.HashMap<>();
    this.blockLiveOut = new java.util.HashMap<>();

    this.stmLiveIn = new java.util.HashMap<>();
    this.stmLiveOut = new java.util.HashMap<>();

    this.transferLiveIn = new java.util.HashMap<>();
    this.transferLiveOut = new java.util.HashMap<>();

    this.kind = Liveness_Kind_t.None;
  }

  // /////////////////////////////////////////////////////
  // utilities

//  private java.util.HashSet<String> getOneStmGenAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneStmGen;
//    this.oneStmGen = new java.util.HashSet<>();
//    return temp;
//  }
//
//  private java.util.HashSet<String> getOneStmKillAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneStmKill;
//    this.oneStmKill = new java.util.HashSet<>();
//    return temp;
//  }
//
//  private java.util.HashSet<String> getOneTransferGenAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneTransferGen;
//    this.oneTransferGen = new java.util.HashSet<>();
//    return temp;
//  }
//
//  private java.util.HashSet<String> getOneTransferKillAndClear()
//  {
//    java.util.HashSet<String> temp = this.oneTransferKill;
//    this.oneTransferKill = new java.util.HashSet<>();
//    return temp;
//  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(cfg.operand.Int operand)
  {
    return;
  }

  @Override
  public void visit(cfg.operand.Var operand)
  {
    this.oneStmGen.add(operand.id);
    return;
  }

  // statements
  @Override
  public void visit(cfg.stm.Add s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(cfg.stm.InvokeVirtual s)
  {
    this.oneStmKill.add(s.dst);
    this.oneStmGen.add(s.obj);
    for (cfg.operand.T arg : s.args) {
      arg.accept(this);
    }
    return;
  }

  @Override
  public void visit(cfg.stm.Lt s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(cfg.stm.Move s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.src.accept(this);
    return;
  }

  @Override
  public void visit(cfg.stm.NewObject s)
  {
    this.oneStmKill.add(s.dst);
    return;
  }

  @Override
  public void visit(cfg.stm.Print s)
  {
    s.arg.accept(this);
    return;
  }

  @Override
  public void visit(cfg.stm.Sub s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(cfg.stm.Times s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  // transfer
  @Override
  public void visit(cfg.transfer.If s)
  {
    // Invariant: accept() of operand modifies "gen"
    s.operand.accept(this);
    return;
  }

  @Override
  public void visit(cfg.transfer.Goto s)
  {
    return;
  }

  @Override
  public void visit(cfg.transfer.Return s)
  {
    // Invariant: accept() of operand modifies "gen"
    s.operand.accept(this);
    return;
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

  // utility functions:
  private void calculateStmTransferGenKill(cfg.block.Block b)
  {
    for (cfg.stm.T s : b.stms) {
      this.oneStmGen = new java.util.HashSet<>();
      this.oneStmKill = new java.util.HashSet<>();
      s.accept(this);
      this.stmGen.put(s, this.oneStmGen);
      this.stmKill.put(s, this.oneStmKill);
      if (control.Control.isTracing("liveness.step1")) {
        System.out.print("\ngen, kill for statement:");
        System.out.print(s.toString());
        System.out.print("\ngen is:");
        for (String str : this.oneStmGen) {
          System.out.print(str + ", ");
        }
        System.out.print("\nkill is:");
        for (String str : this.oneStmKill) {
          System.out.print(str + ", ");
        }
      }
    }
    this.oneStmGen = new java.util.HashSet<>();
    this.oneStmKill = new java.util.HashSet<>();
    b.transfer.accept(this);
    this.oneTransferGen = this.oneStmGen;
    this.oneTransferKill = this.oneStmKill;
    this.transferGen.put(b.transfer, this.oneTransferGen);
    this.transferKill.put(b.transfer, this.oneTransferKill);
    if (control.Control.isTracing("liveness.step1")) {
      System.out.print("\ngen, kill for transfer:");
      System.out.print(b.transfer.toString());
      System.out.print("\ngen is:");
      for (String str : this.oneTransferGen) {
        System.out.print(str + ", ");
      }
      System.out.println("\nkill is:");
      for (String str : this.oneTransferKill) {
        System.out.print(str + ", ");
      }
      System.out.println();
    }
    return;
  }
  
  private void calculateBlockGenKill(cfg.block.Block b)
  {
	    this.oneTransferGen = this.transferGen.get(b.transfer);
	    this.oneTransferKill = this.transferKill.get(b.transfer);

	    java.util.HashSet<String> oneBlockGen = new java.util.HashSet<String>();
	    java.util.HashSet<String> oneBlockKill = new java.util.HashSet<String>();

	    if (control.Control.isTracing("liveness.step2")) {
		    System.out.println("=============new block step2=========");
		    System.out.println(b.toString());
		    System.out.print("init gen:");
	        for (String str : this.oneTransferGen) {
	            System.out.print(str + ", ");
	        }
	        System.out.println();
	    }
	    
	    oneBlockGen.addAll(this.oneTransferGen);
	    
	    if (control.Control.isTracing("liveness.step2")) {
	    	System.out.print("init blockgen:");
	    	for (String str : oneBlockGen) {
	    		System.out.print(str + ", ");
	    	}
	    }
	    
	    oneBlockKill.addAll(this.oneTransferKill);
	    
	    if (control.Control.isTracing("liveness.step2")) {
	          System.out.println("\ngen, kill for block :");
	          System.out.print("blockgen now: ");
	          for (String str : oneBlockGen) {
	              System.out.print(str + ", ");
	          }
	          System.out.print("\nblockKill now: ");
	          for (String str : oneBlockKill) {
	            System.out.print(str + ", ");
	          }
	          System.out.println();
	      }
	int size = b.stms.size();
    for (int i = size - 1; i >= 0; i--) {
      cfg.stm.T s = b.stms.get(i);
      this.oneStmGen = this.stmGen.get(s);
      this.oneStmKill = this.stmKill.get(s);
      
      oneBlockGen.removeAll(this.oneStmKill);
      oneBlockGen.addAll(this.oneStmGen);
      oneBlockKill.addAll(this.oneStmKill);
      if (control.Control.isTracing("liveness.step2")) {
          System.out.print("gen, kill for statement:");
          System.out.print(s.toString());          
          System.out.print("gen is:");
          for (String str : this.oneStmGen) {
            System.out.print(str + ", ");
          }
          System.out.print("\nblockgen now: ");
          for (String str : oneBlockGen) {
              System.out.print(str + ", ");
          }
          System.out.print("\nkill is:");
          for (String str : this.oneStmKill) {
              System.out.print(str + ", ");
          }
          System.out.print("\nblockKill now: ");
          for (String str : oneBlockKill) {
            System.out.print(str + ", ");
          }
          System.out.println();
      }
    }
    this.blockGen.put(b, oneBlockGen);
    this.blockKill.put(b, oneBlockKill);
    
    if (control.Control.isTracing("liveness.step2")) {
      System.out.print("\ngen, kill for block final:");
      System.out.print("\ngen is:");
      for (String str : oneBlockGen) {
        System.out.print(str + ", ");
      }
      System.out.print("\nkill is:");
      for (String str : oneBlockKill) {
        System.out.print(str + ", ");
      }
      System.out.println();
    }
    return;
  }

  private void reverseTopSort(java.util.LinkedList<cfg.block.T> blocks)
  {
	  this.blockTable = new java.util.Hashtable<util.Label, cfg.block.T>();
	  for(cfg.block.T b : blocks){
		  this.blockTable.put(((cfg.block.Block)b).label, b);
	  }
	  this.visited.clear();
	  dfs(blocks.get(0));
  }
  
  private void dfs(cfg.block.T block)
  {
	  if(this.visited.contains(block)) return;
	  this.visited.add(block);
	  cfg.block.Block b = (cfg.block.Block)block;
	  if(b.transfer instanceof cfg.transfer.If){
		  dfs(this.blockTable.get(((cfg.transfer.If)(b.transfer)).truee));
		  dfs(this.blockTable.get(((cfg.transfer.If)(b.transfer)).falsee));
	  }else if(b.transfer instanceof cfg.transfer.Goto){
		  dfs(this.blockTable.get(((cfg.transfer.Goto)(b.transfer)).label));
	  }else{
		// return. Do not need to do any processing.  
	  }
	  this.reverseTopoSortBlocks.add(block);
  }
  
  private void initBlockInOut(java.util.LinkedList<cfg.block.T> blocks)
  {
	  for(cfg.block.T b : blocks){
		  this.blockLiveIn.put(b, new java.util.HashSet<String>());
		  this.blockLiveOut.put(b, new java.util.HashSet<String>());
	  }
  }
  
  private void calculateBlockInOut(cfg.block.Block b)
  {
	  java.util.HashSet<String> liveIn = new java.util.HashSet<String>();
	  java.util.HashSet<String> liveOut = new java.util.HashSet<String>();
	  if(b.transfer instanceof cfg.transfer.If){
		  liveOut.addAll(this.blockLiveIn.get(this.blockTable.get(((cfg.transfer.If)(b.transfer)).truee)));
		  liveOut.addAll(this.blockLiveIn.get(this.blockTable.get(((cfg.transfer.If)(b.transfer)).falsee)));
	  }else if(b.transfer instanceof cfg.transfer.Goto){
		  liveOut.addAll(this.blockLiveIn.get(this.blockTable.get(((cfg.transfer.Goto)(b.transfer)).label)));
	  }else{
//		  this.blockLiveOut.put(b, new HashSet<String>()); /* null? YKG. */
	  }
	  liveIn.addAll(liveOut);
	  liveIn.removeAll(this.blockKill.get(b));
	  liveIn.addAll(this.blockGen.get(b));

	  if(!liveIn.equals(this.blockLiveIn.get(b))) this.fixpointFlag = false;
	  if(!liveOut.equals(this.blockLiveOut.get(b))) this.fixpointFlag = false;
	  this.blockLiveIn.put(b, liveIn);
	  this.blockLiveOut.put(b, liveOut);
	
    if (control.Control.isTracing("liveness.step3")) {
    	System.out.println("======== new block step3 ============");
        System.out.print("\nliveIn, liveOut for block:");
        System.out.println(b.toString());
        System.out.print("\nliveIn is:");
        for (String str : liveIn) {
          System.out.print(str + ", ");
        }
        System.out.print("\nliveOut is:");
        for (String str : liveOut) {
          System.out.print(str + ", ");
        }
        System.out.println();
      }
      return;
  }
  
  private void calculateStmTransferInOut(cfg.block.Block b)
  {
	    if (control.Control.isTracing("liveness.step4")) {
	      System.out.println("======== new block ============");
		  System.out.println(b.toString());
		}
	
	if(this.transferKill.get(b.transfer).size() > 0){
		System.err.println("ERROR! HOW CAN IT BE?");
	}
	
	java.util.HashSet<String> liveIn = new java.util.HashSet<String>();
	liveIn.addAll(this.blockLiveOut.get(b));
	
	java.util.HashSet<String> transOut = new java.util.HashSet<String>();
	transOut.addAll(liveIn);
	this.transferLiveOut.put(b.transfer, transOut);
	java.util.HashSet<String> transIn = new java.util.HashSet<String>();
	transIn.addAll(transOut);
	transIn.addAll(this.transferGen.get(b.transfer));
	this.transferLiveIn.put(b.transfer, transIn);
	liveIn.addAll(transIn);
	for (int i = b.stms.size() - 1; i >= 0; i--) {
		cfg.stm.T s = b.stms.get(i);
	    this.oneStmGen = this.stmGen.get(s);
	    this.oneStmKill = this.stmKill.get(s);
	    
	    java.util.HashSet<String> stmLiveOut = new java.util.HashSet<String>();
	    stmLiveOut.addAll(liveIn);
	    this.stmLiveOut.put(s, stmLiveOut);
	    
	    java.util.HashSet<String> stmLiveIn = new java.util.HashSet<String>();
	    stmLiveIn.addAll(stmLiveOut);
	    if(stmLiveIn.containsAll((this.stmKill.get(s)))){
		    stmLiveIn.removeAll(this.stmKill.get(s));
		    stmLiveIn.addAll(this.stmGen.get(s));
	    }
	    this.stmLiveIn.put(s, stmLiveIn);
	    
	    liveIn.clear();
	    liveIn.addAll(stmLiveIn);
	    
	    if (control.Control.isTracing("liveness.step4")) {
		  System.out.print("\nliveIn, liveOut for statement:");
		  System.out.print(s.toString());
		  System.out.print("\nin is:");
		  for (String str : stmLiveIn) {
		    System.out.print(str + ", ");
		  }
		  System.out.print("\nout is:");
		  for (String str : stmLiveOut) {
		    System.out.print(str + ", ");
		  }
		  System.out.println();
		}
	}
    return;
  }
  
  // block
  @Override
  public void visit(cfg.block.Block b)
  {
    switch (this.kind) {
    case StmGenKill:
        calculateStmTransferGenKill(b);
        break;
    case BlockGenKill:
        calculateBlockGenKill(b);
        break;
    case BlockInOut:
        calculateBlockInOut(b);
        break;
    case StmInOut:
    	calculateStmTransferInOut(b);
        break;
    default:
      // Your code here:
      return;
    }
  }

  // method
  @Override
  public void visit(cfg.method.Method m)
  {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (cfg.block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    // Your code here:
    this.kind = Liveness_Kind_t.BlockGenKill;
    for (cfg.block.T block : m.blocks) {
      block.accept(this);
    }
    
    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:
    this.kind = Liveness_Kind_t.BlockInOut;
    initBlockInOut(m.blocks);
    reverseTopSort(m.blocks);
    this.fixpointFlag = false;
    while(!this.fixpointFlag){
    	this.fixpointFlag = true;
	    for (cfg.block.T block : this.reverseTopoSortBlocks) {
	      block.accept(this);
	    }
    }
    
    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:
    this.kind = Liveness_Kind_t.StmInOut;
    for (cfg.block.T block : m.blocks) {
      block.accept(this);
    }
  }

  @Override
  public void visit(cfg.mainMethod.MainMethod m)
  {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (cfg.block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    // Your code here:
    this.kind = Liveness_Kind_t.BlockGenKill;
    for (cfg.block.T block : m.blocks) {
      block.accept(this);
    }
    
    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:
    this.kind = Liveness_Kind_t.BlockInOut;
    initBlockInOut(m.blocks);
    reverseTopSort(m.blocks);
    this.fixpointFlag = false;
    while(!this.fixpointFlag){
    	this.fixpointFlag = true;
	    for (cfg.block.T block : this.reverseTopoSortBlocks) {
	      block.accept(this);
	    }
    }
    
    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:
    this.kind = Liveness_Kind_t.StmInOut;
    for (cfg.block.T block : m.blocks) {
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
public void visit(cfg.stm.And s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
}

@Override
public void visit(cfg.stm.ArraySelect s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.array.accept(this);
    s.index.accept(this);
    return;
}

@Override
public void visit(cfg.stm.AssignArray s) {
	// ?? YKG. It's hard to decide.	
//	this.oneStmKill.add(s.id);
	this.oneStmGen.add(s.id);
	s.index.accept(this);
	s.exp.accept(this);
}

@Override
public void visit(cfg.stm.Length s) {
	this.oneStmKill.add(s.dst);
	s.array.accept(this);
}

@Override
public void visit(cfg.stm.NewIntArray s) {
	this.oneStmKill.add(s.dst);
	s.array.accept(this);
}

@Override
public void visit(cfg.stm.Xor s) {
	this.oneStmKill.add(s.dst);
	s.exp.accept(this);
}

}
