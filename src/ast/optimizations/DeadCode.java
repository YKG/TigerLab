package ast.optimizations;

import ast.exp.Block;

// Dead code elimination optimizations on an AST.

public class DeadCode implements ast.Visitor
{
  private ast.classs.T newClass;
  private ast.mainClass.T mainClass;
  public ast.program.T program;
  
  public DeadCode()
  {
    this.newClass = null;
    this.mainClass = null;
    this.program = null;
  }

  // //////////////////////////////////////////////////////
  // 
  public String genId()
  {
    return util.Temp.next();
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.exp.Add e)
  {
  }

  @Override
  public void visit(ast.exp.And e)
  {
  }

  @Override
  public void visit(ast.exp.ArraySelect e)
  {
  }

  @Override
  public void visit(ast.exp.Call e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.False e)
  {
  }

  @Override
  public void visit(ast.exp.Id e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Length e)
  {
  }

  @Override
  public void visit(ast.exp.Lt e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.NewIntArray e)
  {
  }

  @Override
  public void visit(ast.exp.NewObject e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Not e)
  {
  }

  @Override
  public void visit(ast.exp.Num e)
  {
    return;
  }

  @Override
  public void visit(ast.exp.Sub e)
  {
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
    
    return;
  }

  @Override
  public void visit(ast.exp.True e)
  {
  }

  // statements
  @Override
  public void visit(ast.stm.Assign s)
  {
    
    return;
  }

  @Override
  public void visit(ast.stm.AssignArray s)
  {
  }

  @Override
  public void visit(ast.stm.Block s)
  {
  }

  @Override
  public void visit(ast.stm.If s)
  {
   
    return;
  }

  @Override
  public void visit(ast.stm.Print s)
  {
    return;
  }

  @Override
  public void visit(ast.stm.While s)
  {
  }

  // type
  @Override
  public void visit(ast.type.Boolean t)
  {
  }

  @Override
  public void visit(ast.type.Class t)
  {
  }

  @Override
  public void visit(ast.type.Int t)
  {
  }

  @Override
  public void visit(ast.type.IntArray t)
  {
  }

  // dec
  @Override
  public void visit(ast.dec.Dec d)
  {
    return;
  }

  // method
  @Override
  public void visit(ast.method.Method m)
  {
    return;
  }

  // class
  @Override
  public void visit(ast.classs.Class c)
  {
	this.newClass = new ast.classs.Class(c.id, c.extendss, c.decs, c.methods);
	for(ast.method.T m : ((ast.classs.Class)this.newClass).methods){
		m.accept(this);
	}
    return;
  }

  // main class
  @Override
  public void visit(ast.mainClass.MainClass c)
  {
	this.mainClass = new ast.mainClass.MainClass(c.id, c.arg, c.stm);
	((ast.mainClass.MainClass)this.mainClass).stm.accept(this);
    return;
  }

  // program
  @Override
  public void visit(ast.program.Program p)
  {
    
 // You should comment out this line of code:
    
    p.mainClass.accept(this);
    ast.mainClass.T newMainclass = this.mainClass;
    
    java.util.LinkedList<ast.classs.T> newClasses = new java.util.LinkedList<ast.classs.T>();
    for(ast.classs.T c : p.classes){
    	c.accept(this);
    	newClasses.add(this.newClass);
    }
    this.program = new ast.program.Program(newMainclass, newClasses);
    
    if (control.Control.isTracing("ast.DeadCode")){
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
	System.err.println("HELP!!");
}
}
