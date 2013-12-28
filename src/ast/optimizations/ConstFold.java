package ast.optimizations;

import ast.exp.Num;


// Algebraic simplification optimizations on an AST.

public class ConstFold implements ast.Visitor
{
//  private ast.classs.T newClass;
//  private ast.mainClass.T mainClass;
  public ast.program.T program;
  
//  private ast.stm.T newStm;
  private ast.exp.T newExp;
  
  public ConstFold()
  {
//    this.newClass = null;
//    this.mainClass = null;
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
	  e.left.accept(this);
	  ast.exp.T left = this.newExp;
	  e.right.accept(this);
	  ast.exp.T right = this.newExp;
	  if(left instanceof ast.exp.Num && right instanceof ast.exp.Num)
		  this.newExp = new ast.exp.Num(((ast.exp.Num)left).num + ((ast.exp.Num)right).num);
	  else
		  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.And e)
  {
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.ArraySelect e)
  {
	e.index.accept(this);
	e.index = this.newExp;
	this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Call e)
  {
	  e.exp.accept(this);
	  e.exp = this.newExp;
	  for(int i = 0; i < e.args.size(); i++){
		  e.args.get(i).accept(this);
		  e.args.set(i, this.newExp);
	  }
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.False e)
  {
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Id e)
  {
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Length e)
  {
	  e.array.accept(this);
	  e.array = this.newExp;
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Lt e)
  {
	  e.left.accept(this);
	  e.left = this.newExp;
	  e.right.accept(this);
	  e.right = this.newExp;
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.NewIntArray e)
  {
	  e.exp.accept(this);
	  e.exp = this.newExp;
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.NewObject e)
  {
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Not e)
  {
	  e.exp.accept(this);
	  e.exp = this.newExp;
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Num e)
  {
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Sub e)
  {
	  /* YKG. If the result is negative, it will not be an EXP any more! GRAMMA is not good. */
	  e.left.accept(this);
	  ast.exp.T left = this.newExp;
	  e.right.accept(this);
	  ast.exp.T right = this.newExp;
	  if(left instanceof ast.exp.Num && right instanceof ast.exp.Num)
		  this.newExp = new ast.exp.Num(((ast.exp.Num)left).num - ((ast.exp.Num)right).num);
	  else
		  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.This e)
  {
	  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.Times e)
  {
	  e.left.accept(this);
	  ast.exp.T left = this.newExp;
	  e.right.accept(this);
	  ast.exp.T right = this.newExp;
	  if(left instanceof ast.exp.Num && right instanceof ast.exp.Num)
		  this.newExp = new ast.exp.Num(((ast.exp.Num)left).num * ((ast.exp.Num)right).num);
	  else
		  this.newExp = e;
  }

  @Override
  public void visit(ast.exp.True e)
  {
	  this.newExp = e;
  }

  // statements
  @Override
  public void visit(ast.stm.Assign s)
  {
    s.exp.accept(this);
    s.exp = this.newExp;
  }

  @Override
  public void visit(ast.stm.AssignArray s)
  {
	  s.exp.accept(this);
	  s.exp = this.newExp;
	  s.index.accept(this);
	  s.index = this.newExp;
  }

  @Override
  public void visit(ast.stm.Block ss)
  {
	  for(ast.stm.T s : ss.stms)
		s.accept(this);
  }

  @Override
  public void visit(ast.stm.If s)
  {
	  s.condition.accept(this);
	  s.condition = this.newExp;
	  s.elsee.accept(this);
	  s.thenn.accept(this);
  }

  @Override
  public void visit(ast.stm.Print s)
  {
	  s.exp.accept(this);
	  s.exp = this.newExp;
  }

  @Override
  public void visit(ast.stm.While s)
  {
	  s.condition.accept(this);
	  s.condition = this.newExp;
	  s.body.accept(this);
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
  }

  // method
  @Override
  public void visit(ast.method.Method m)
  {
	  for(ast.stm.T s : m.stms)
		  s.accept(this);
  }

  // class
  @Override
  public void visit(ast.classs.Class c)
  {
    for(ast.method.T m : c.methods)
    	m.accept(this);
    return;
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
	    if (control.Control.isTracing("ast.ConstFold")){
	        System.out.println("before ConstFold optimization:");
	        ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
	        p.accept(pp);
	      }
    
 // You should comment out this line of code:
    this.program = p;
    
    p.mainClass.accept(this);
    for(ast.classs.T c : p.classes)
    	c.accept(this);
    
    if (control.Control.isTracing("ast.ConstFold")){
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      System.out.println("after ConstFold optimization:");
      this.program.accept(pp);
    }
    return;
  }

@Override
public void visit(ast.exp.Block e) {
	e.exp.accept(this);
	e.exp = this.newExp;
	this.newExp = e;
}
}
