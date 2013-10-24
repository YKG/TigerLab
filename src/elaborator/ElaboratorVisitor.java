package elaborator;

import ast.PrettyPrintVisitor;

public class ElaboratorVisitor implements ast.Visitor
{
  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public ast.type.T type; // type of the expression being elaborated
  private boolean ok = true;

  public ElaboratorVisitor()
  {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
  }

  @SuppressWarnings("unused")
private void error()
  {
    System.out.println("type mismatch");
//    System.exit(1);
  }

  private void error(String msg)
  {
	  System.out.println(msg);
//	  this.ok = false;
  }
  
  @SuppressWarnings("unused")
  private void error(ast.exp.T left, ast.exp.T right)
  {
    PrettyPrintVisitor pp = new PrettyPrintVisitor();
    System.out.print("Line: " + left.lineNum + " : " + left.colNum + " : '");
    left.accept(pp);
    System.out.print("' and '");
    left.accept(pp);
    System.out.println("' must have the same type");
    this.ok = false;
//    System.exit(1);
  }
  
  private void error(int lineNum, String id)
  {
    System.out.println("Error: " + lineNum +": '" + id + "' was not declared");
    this.ok = false;
//    System.exit(1);
  }
  
  private void error(ast.exp.T exp, String expType, String type)
  {
    System.out.print("Error: " + exp.lineNum +": '");
    PrettyPrintVisitor pp = new PrettyPrintVisitor();
    exp.accept(pp);
    System.out.println("' is " + expType + ", but expect " + type);
    this.ok = false;
//    System.out.println("'" + "at line " + exp.lineNum + " col " + exp.colNum
//    		+ " should be an " + type + " but its type is " + expType);
//    System.exit(1);
  }
  
  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.exp.Add e)
  {
	  e.left.accept(this);
	  if(this.type != null && !this.type.toString().equals("@int"))
		  error(e.left, this.type.toString(), "@int");
	  e.right.accept(this);
	  if(this.type != null && !this.type.toString().equals("@int"))
		  error(e.right, this.type.toString(), "@int");
	  this.type = new ast.type.Int();
	  return;
  }

  @Override
  public void visit(ast.exp.And e)
  {
	  e.left.accept(this);
	  if(this.type != null && !this.type.toString().equals("@boolean"))
		  error(e.left, this.type.toString(), "@boolean");
	  e.right.accept(this);
	  if(this.type != null && !this.type.toString().equals("@boolean"))
		  error(e.right, this.type.toString(), "@boolean");	  
	  this.type = new ast.type.Boolean();
	  return;
  }

  @Override
  public void visit(ast.exp.ArraySelect e)
  {
	  e.array.accept(this);
	  if(this.type != null && !this.type.toString().equals("@int[]"))
		  error(e.array, this.type.toString(), "@int[]");
	  e.index.accept(this);
	  if(this.type != null && !this.type.toString().equals("@int"))
		  error(e.index, this.type.toString(), "@int");
	  this.type = new ast.type.Int();
	  return;
  }

  @Override
  public void visit(ast.exp.Block e)
  {
	  e.exp.accept(this);
	  return;
  }

  @Override
  public void visit(ast.exp.Call e)
  {
    ast.type.T leftty;
    ast.type.Class ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty != null) {
    	if(leftty instanceof ast.type.Class){
	      ty = (ast.type.Class) leftty;
	      e.type = ty.id;

		    MethodType mty = this.classTable.getm(ty.id, e.id);
		    if(mty != null){
			    java.util.LinkedList<ast.type.T> argsty = new java.util.LinkedList<ast.type.T>();
			    java.util.LinkedList<Boolean> okList = new java.util.LinkedList<Boolean>();
			    for (ast.exp.T a : e.args) {
			      a.accept(this);
			      okList.addLast(new Boolean(this.type != null));
			      argsty.addLast(this.type);
			    }
			    if (mty.argsType.size() != argsty.size()){
			      error("Error: " + e.lineNum + ": " + ty.id + "." + e.id + " args don't match");
			    }else{
				    for (int i = 0; i < argsty.size(); i++) {
				      ast.dec.Dec dec = (ast.dec.Dec) mty.argsType.get(i);
				      Boolean b = okList.get(i);
				      if(b.booleanValue() && dec.type != null){
				    	  if (dec.type.toString().equals(argsty.get(i).toString()))
						    ;
					      else{
					    	  if((dec.type instanceof ast.type.Class) &&
					    			  this.classTable.isA(dec.type.toString(), argsty.get(i).toString())){
					    		  ;
					    	  }else{
					    		  error(e.args.get(i), argsty.get(i).toString(), dec.type.toString());  
					    	  }
					      }
				      }
				    }
				    this.type = mty.retType;
				    e.at = argsty;
				    e.rt = this.type;
				    return;
			    }
		    }else{
		    	error("Error: " + e.exp.lineNum + ": Class " + ty.id + " do not have the method: " + e.id);
		    }
    	}else{
    		error(e.exp, this.type.toString(), "@Class");
    	}
    }
    this.type = null;
    return;
 }

  @Override
  public void visit(ast.exp.False e)
  {
	  this.type = new ast.type.Boolean();	  
	  return;
  }

  @Override
  public void visit(ast.exp.Id e)
  {
    // first look up the id in method table
    ast.type.T type = this.methodTable.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null)
      error(e.lineNum, e.id);
    this.type = type;
    // record this type on this node for future use.
    e.type = type;	/* YKG. BE CAREFUL */
    
    /*
     * added by YKG. Check local vars used or not.
     */
//    this.methodTable.dumpTableLocalVar();
    if(type != null && !e.isField){
//    	System.out.println("DELETE_LOCAL: " + e.id);
    	this.methodTable.set(e.id);
    }
//    this.methodTable.dumpTableLocalVar();
    return;
  }

  @Override
  public void visit(ast.exp.Length e)
  {
	  e.array.accept(this);
	  if(this.type != null && !this.type.toString().equals("@int[]"))
		  error(e.array, this.type.toString(), "@int[]");
	  this.type = new ast.type.Int();
	  return;
  }

  @Override
  public void visit(ast.exp.Lt e)
  {
    e.left.accept(this);
    if(this.type != null && !this.type.toString().equals("@int"))
    	error(e.left, this.type.toString(), "@int");    
    e.right.accept(this);
    if(this.type != null && !this.type.toString().equals("@int"))
    	error(e.right, this.type.toString(), "@int");
    this.type = new ast.type.Boolean();
    return;
  }

  @Override
  public void visit(ast.exp.NewIntArray e)
  {
	  e.exp.accept(this);
	  if(this.type != null && !this.type.toString().equals("@int"))
		  error(e.exp, this.type.toString(), "@int");
	  this.type = new ast.type.IntArray();
	  return;
  }

  @Override
  public void visit(ast.exp.NewObject e)
  {
	if(this.classTable.get(e.id) == null){
		error(e.lineNum, e.id);
		this.type = null; /* YKG. Set type to null ONLY when the result type is undetermined */
	}else{
		this.type = new ast.type.Class(e.id);
	}
    return;
  }

  @Override
  public void visit(ast.exp.Not e)
  {
	  e.exp.accept(this);
	  if(this.type != null && !this.type.toString().equals("@boolean"))
		  error(e.exp, this.type.toString(), "@boolean");
	  this.type = new ast.type.Boolean();
	  return;
  }

  @Override
  public void visit(ast.exp.Num e)
  {
    this.type = new ast.type.Int();
    return;
  }

  @Override
  public void visit(ast.exp.Sub e)
  {
    e.left.accept(this);
    if(this.type != null && !this.type.toString().equals("@int"))
    	error(e.left, this.type.toString(), "@int");    
    e.right.accept(this);
    if(this.type != null && !this.type.toString().equals("@int"))
    	error(e.right, this.type.toString(), "@int");
    this.type = new ast.type.Int();	/* YKG. Let outside see it OK? */
    return;
  }

  @Override
  public void visit(ast.exp.This e)
  {
    this.type = new ast.type.Class(this.currentClass);
    return;
  }

  @Override
  public void visit(ast.exp.Times e)
  {
	  /* YKG. Check left.type == "@int" && right.type == "@int" ? */
    e.left.accept(this);
    if(this.type != null && !this.type.toString().equals("@int"))
    	error(e.left, this.type.toString(), "@int");    
    e.right.accept(this);
    if(this.type != null && !this.type.toString().equals("@int"))
    	error(e.right, this.type.toString(), "@int");
//    if (!this.type.toString().equals(leftty.toString()))
//      error(e.left, e.right);
    this.type = new ast.type.Int();	/* YKG. Let outside see it OK? */
    return;
  }

  @Override
  public void visit(ast.exp.True e)
  {
	  this.type = new ast.type.Boolean();
	  return;
  }

  // statements
  @Override
  public void visit(ast.stm.Assign s)
  {
	boolean isMethodField = false;
    // first look up the id in method table
    ast.type.T type = this.methodTable.get(s.id);
    // if search failed, then s.id must
    if (type == null){
      type = this.classTable.get(this.currentClass, s.id);
    }else{
    	isMethodField = true;
    }
    if (type == null)
      error(s.exp.lineNum, s.id); /* YKG. WRONG COL NUM */
    
    /*
     * added by YKG. Check local vars used or not.
     */
//    this.methodTable.dumpTableLocalVar();
    if(isMethodField){
//    	System.out.println("DELETE_LOCAL: " + s.id);
    	this.methodTable.set(s.id);
    }
//    this.methodTable.dumpTableLocalVar();
    
    s.exp.accept(this);
    if(this.type != null && type != null 
    		&& !this.type.toString().equals(type.toString()))
    	error(s.exp, this.type.toString(), type.toString());
//    s.type = type;
    return;
  }

  @Override
  public void visit(ast.stm.AssignArray s)
  {
	  	boolean isMethodField = false;
	    // first look up the id in method table
	    ast.type.T type = this.methodTable.get(s.id);
	    // if search failed, then s.id must
	    if (type == null){
	      type = this.classTable.get(this.currentClass, s.id);
		}else{
		  isMethodField = true;
		}
	    if (type == null)
	      error(s.index.lineNum, s.id);

	    /*
	     * added by YKG. Check local vars used or not.
	     */
//	    this.methodTable.dumpTableLocalVar();
	    if(isMethodField){
//	    	System.out.println("DELETE_LOCAL: " + s.id);
	    	this.methodTable.set(s.id);
	    }
//	    this.methodTable.dumpTableLocalVar();	    
	    
	    if(this.type != null && !type.toString().equals("@int[]"))
	    	error(s.index, type.toString(), "@int[]"); /* YKG. WRONG COL NUM */
	    s.index.accept(this);
	    if(this.type != null && !this.type.toString().equals("@int"))
	    	error(s.index, this.type.toString(), "@int");
	    s.exp.accept(this);
	    if(this.type != null && !this.type.toString().equals("@int"))
	    	error(s.exp, this.type.toString(), "@int");
	    return;
  }

  @Override
  public void visit(ast.stm.Block s)
  {
	  for(ast.stm.T stm : s.stms)
		  stm.accept(this);
	  this.type = null;
	  return;
  }

  @Override
  public void visit(ast.stm.If s)
  {
    s.condition.accept(this);
    if (this.type != null && !this.type.toString().equals("@boolean"))
      error(s.condition, this.type.toString(), "@boolean");
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.Print s)
  {
    s.exp.accept(this);
    if (this.type != null && !this.type.toString().equals("@int"))
      error(s.exp, this.type.toString(), "@int");
    return;
  }

  @Override
  public void visit(ast.stm.While s)
  {
	    s.condition.accept(this);
	    if (this.type != null && !this.type.toString().equals("@boolean"))
	      error(s.condition, this.type.toString(), "@boolean");
	    s.body.accept(this);
	    return;	  
  }

  // type
  @Override
  public void visit(ast.type.Boolean t)
  {
	  System.out.println("@boolean");
  }

  @Override
  public void visit(ast.type.Class t)
  {
	  System.out.println("@Class<" + t.id + ">");
  }

  @Override
  public void visit(ast.type.Int t)
  {
    System.out.println("@int");
  }

  @Override
  public void visit(ast.type.IntArray t)
  {
	  System.out.println("@int[]");
  }

  // dec
  @Override
  public void visit(ast.dec.Dec d)
  {
	  System.out.println("#declare");
//	  d.type.accept(this);
  }

  // method
  @Override
  public void visit(ast.method.Method m)
  {
	this.methodTable.clear();
    // construct the method table
//    this.methodTable.put(m.formals, m.locals);
    this.methodTable.put(m.formals, m.locals, this.classTable);
//System.out.print("method: " + m.id + "\n\t");
    if (control.Control.elabMethodTable){
      System.out.println("dump Method: " + this.currentClass + "." + m.id);
      this.methodTable.dump();
    }
    
    for (ast.stm.T s : m.stms)
      s.accept(this);
    
    /* YKG. print 'unused' waring */
    this.methodTable.printWarning(); /* YKG. SHOULD NOT BE after the next line */
    m.retExp.accept(this);
    if(this.ok && !this.type.toString().equals(m.retType.toString())){
    	System.out.println("Error: " + m.retExp.lineNum + ": Type mismatch: "
    			+ "cannot convert from " + this.type.toString() 
    			+" to " + m.retType.toString());
    }
    return;
  }

  // class
  @Override
  public void visit(ast.classs.Class c)
  {
    this.currentClass = c.id;
//	if(c.id.equals(c.extendss)){
//		System.out.println("Error: " + c.lineNum + ": Class '" + c.id + "' extends self");
//		/* YKG. Remember to mark the error */
//	}
	ClassBinding cb = this.classTable.get(c.id);
	String father = cb.extendss;
	while(father != null){
//		System.out.println("Trace: " + c.id +" extends: " + father);
		if(father.equals(c.id)){
			System.out.println("Error: circular inheritance class: " + c.id);
			break;
		}
		father = this.classTable.get(father).extendss;
	}
	if(father != null){
		return;
	}
	

	for (ast.dec.T dec : c.decs) {
      ast.dec.Dec d = (ast.dec.Dec) dec;
      if(d.type instanceof ast.type.Class){
    	 String cname = ((ast.type.Class)(d.type)).id;
    	 if(this.classTable.get(cname) == null){
    		 System.out.println("Error: " + dec.lineNum + ": unknown type '" +  cname + "'");
    		 /* YKG. Remember to mark the error */
    	 }
      }
	}
	    
//System.out.println("Class: " + this.currentClass);
    for (ast.method.T m : c.methods)
      m.accept(this);

    return;
  }

  // main class
  @Override
  public void visit(ast.mainClass.MainClass c)
  {
    this.currentClass = c.id;
    // "main" has an argument "arg" of type "String[]", but
    // one has no chance to use it. So it's safe to skip it...

    c.stm.accept(this);
    return;
  }

  // ////////////////////////////////////////////////////////
  // step 1: build class table
  // class table for Main class
  private void buildMainClass(ast.mainClass.MainClass main)
  {
    this.classTable.put(main.id, new ClassBinding(null));
  }

  // class table for normal classes
  private void buildClass(ast.classs.Class c)
  {
	this.classTable.put(c.id, new ClassBinding(c.extendss));

	for (ast.dec.T dec : c.decs) {
      ast.dec.Dec d = (ast.dec.Dec) dec;
      this.classTable.put(c.id, d.id, d.type); /* YKG. Another situation: d.type UNDEFINED */
    }
    for (ast.method.T method : c.methods) {
      ast.method.Method m = (ast.method.Method) method;
      this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals)); /* YKG. retType match */
    }
  }

  // step 1: end
  // ///////////////////////////////////////////////////

  // program
  @Override
  public void visit(ast.program.Program p)
  {
    // ////////////////////////////////////////////////
    // step 1: build a symbol table for class (the class table)
    // a class table is a mapping from class names to class bindings
    // classTable: className -> ClassBinding{extends, fields, methods}
    buildMainClass((ast.mainClass.MainClass) p.mainClass);
    for (ast.classs.T c : p.classes) {
      buildClass((ast.classs.Class) c);
    }

    // we can double check that the class table is OK!
    if (control.Control.elabClassTable) {
      this.classTable.dump();
    }
//System.out.println("######################");
    // ////////////////////////////////////////////////
    // step 2: elaborate each class in turn, under the class table
    // built above.
    p.mainClass.accept(this);
    for (ast.classs.T c : p.classes) {
      c.accept(this);
    }

  }
}
