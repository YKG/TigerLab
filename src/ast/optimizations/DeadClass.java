package ast.optimizations;

import java.util.Hashtable;

import ast.exp.Block;

// Dead class elimination optimizations on an AST.

public class DeadClass implements ast.Visitor
{
  private java.util.Hashtable<String, String> classTable;
  private java.util.HashSet<String> set;
  private java.util.LinkedList<String> worklist;
  private ast.classs.T newClass;
  public ast.program.T program;
  

  public DeadClass()
  {
    this.set = new java.util.HashSet<String>();
    this.worklist = new java.util.LinkedList<String>();
    this.newClass = null;
    this.program = null;
  }

  
  ////////////////////////////////////////////////////////
  private void addClass(String className)
  {
	  this.worklist.add(className);
	  this.set.add(className);
	  
	  String extendss = this.classTable.get(className);
	  while(!extendss.equals("null") && !this.set.contains(extendss)){
		  this.worklist.add(extendss);
		  this.set.add(extendss);
		  extendss = this.classTable.get(extendss);
	  }
  }
  
  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.exp.Add e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.And e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.ArraySelect e)
  {
    e.array.accept(this);
    e.index.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.Call e)
  {
    e.exp.accept(this);
    for (ast.exp.T arg : e.args) {
      arg.accept(this);
    }
    return;
  }

  @Override
  public void visit(ast.exp.False e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Id e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Length e)
  {
    e.array.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.Lt e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.NewIntArray e)
  {
    e.exp.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.NewObject e)
  {
    if (this.set.contains(e.id))
      return;
    addClass(e.id);
    return;
  }

  @Override
  public void visit(ast.exp.Not e)
  {
    e.exp.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.Num e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Sub e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.This e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Times e)
  {
    e.left.accept(this);
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(ast.exp.True e)
  {
    return;
  }

  // statements
  @Override
  public void visit(ast.stm.Assign s)
  {
    s.exp.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.AssignArray s)
  {
    s.index.accept(this);
    s.exp.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.Block s)
  {
    for (ast.stm.T x : s.stms)
      x.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.If s)
  {
    s.condition.accept(this);
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.Print s)
  {
    s.exp.accept(this);
    return;
  }

  @Override
  public void visit(ast.stm.While s)
  {
    s.condition.accept(this);
    s.body.accept(this);
    return;
  }

  // type
  @Override
  public void visit(ast.type.Boolean t)
  {
    return;
  }

  @Override
  public void visit(ast.type.Class t)
  {
	  if (this.set.contains(t.id))
        return;
	  addClass(t.id);
      return;
  }

  @Override
  public void visit(ast.type.Int t)
  {
    return;
  }

  @Override
  public void visit(ast.type.IntArray t)
  {
  }

  // dec
  @Override
  public void visit(ast.dec.Dec d)
  {
	  d.type.accept(this);
      return;
  }

  // method
  @Override
  public void visit(ast.method.Method m)
  {
	for(ast.dec.T d : m.formals){
		d.accept(this);
	}
	for(ast.dec.T d : m.locals){
		d.accept(this);
	}
	
    for (ast.stm.T s : m.stms)
      s.accept(this);
    m.retExp.accept(this);
    return;
  }

  // class
  @Override
  public void visit(ast.classs.Class c)
  {
  }

  // main class
  @Override
  public void visit(ast.mainClass.MainClass c)
  {
    c.stm.accept(this);
    return;
  }

  // program
  @Override
  public void visit(ast.program.Program p)
  {
    this.classTable = new java.util.Hashtable<String, String>();
    for (ast.classs.T classs : p.classes) {
        ast.classs.Class c = (ast.classs.Class) classs;
        this.classTable.put(c.id, c.extendss == null ? "null" : c.extendss);
    }
    
    // we push the class name for mainClass onto the worklist
    ast.mainClass.MainClass mainclass = (ast.mainClass.MainClass) p.mainClass;
    this.set.add(mainclass.id);

    p.mainClass.accept(this);

    while (!this.worklist.isEmpty()) {
      String cid = this.worklist.removeFirst();

      for (ast.classs.T c : p.classes) {
        ast.classs.Class current = (ast.classs.Class) c;

        if (current.id.equals(cid)) {
          c.accept(this);
          break;
        }
      }
    }

    java.util.LinkedList<ast.classs.T> newClasses = new java.util.LinkedList<ast.classs.T>();
    for (ast.classs.T classs : p.classes) {
      ast.classs.Class c = (ast.classs.Class) classs;
      if (this.set.contains(c.id))
        newClasses.add(c);
    }

    
    this.program =
    new ast.program.Program(p.mainClass, newClasses);
    
//    if (control.Control.trace.equals("ast.DeadClass")){
    if (control.Control.isTracing("ast.DeadClass")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
      
    return;
  }

@Override
public void visit(Block e) {
	// TODO Auto-generated method stub
//	System.err.println("HELP!!");
	return;
}
}
