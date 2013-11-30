package codegen.C;

import java.util.LinkedHashSet;

import codegen.C.exp.Block;
import control.Control;

public class PrettyPrintVisitor implements Visitor
{
  private int indentLevel;
  private java.io.BufferedWriter writer;
  private java.util.LinkedHashSet<String> localRefs;  
  private String methodId;
  private java.util.HashMap<String, String> classes_gc_map;

  // //////////////////////////////////////////////////////
  // 
  public String genId()
  {
    return util.Temp.next();
  }
////////////////////////////////////////////////////////
  
  public PrettyPrintVisitor()
  {
    this.indentLevel = 2;
  }

  private void indent()
  {
    this.indentLevel += 2;
  }

  private void unIndent()
  {
    this.indentLevel -= 2;
  }

  private void printSpaces()
  {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s)
  {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s)
  {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(codegen.C.exp.Add e)
  {
	  e.left.accept(this);
	  this.say(" + ");
	  e.right.accept(this);
	  return;
  }

  @Override
  public void visit(codegen.C.exp.And e)
  {
	  e.left.accept(this);
	  this.say(" && ");
	  e.right.accept(this);
	  return;
  }

  @Override
  public void visit(codegen.C.exp.ArraySelect e)
  {
	  e.array.accept(this);
	  this.say("[");
	  e.index.accept(this);
	  this.say(" + 4]");
	  return;
  }

  @Override
  public void visit(codegen.C.exp.Call e)
  {
	if(this.localRefs.contains(e.assign)){
		e.assign = "__GC_frame." + e.assign;
	}
		
    this.say("(" + e.assign + "=");
    e.exp.accept(this);
    this.say(", ");
    this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
    int size = e.args.size();
    if (size == 0) {
      this.say("))");
      return;
    }
    for (codegen.C.exp.T x : e.args) {
      this.say(", ");
      x.accept(this);
    }
    this.say("))");
    return;
  }

  @Override
  public void visit(codegen.C.exp.Id e)
  {
	if(this.localRefs.contains(e.id)){
		e.id = "__GC_frame." + e.id;
	}
	
    this.say(e.id);
  }

  @Override
  public void visit(codegen.C.exp.Length e)
  {
	  this.say("*(((int *)");
	  e.array.accept(this);
	  this.say(")+2)");
	  return;
  }

  @Override
  public void visit(codegen.C.exp.Lt e)
  {
    e.left.accept(this);
    this.say(" < ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(codegen.C.exp.NewIntArray e)
  {
	if(this.localRefs.contains(e.name)){
		e.name = "__GC_frame." + e.name;
	}
	
	  this.say("(" + e.name + " = (int *)(Tiger_new_array (");
	  e.exp.accept(this);
	  this.say(")), " + e.name + ")");
	  return;
  }

  @Override
  public void visit(codegen.C.exp.NewObject e)
  {
	if(this.localRefs.contains(e.name)){
		e.name = "__GC_frame." + e.name;
	}
	if(this.localRefs.contains(e.id)){
		e.id = "__GC_frame." + e.id;		/* YKG. Is it reachable? */
	}
    this.say("(" + e.name + " = (struct " + e.id + "*)(Tiger_new (&" + e.id
        + "_vtable_, sizeof(struct " + e.id + "))), " + e.name + ")");
    return;
  }

  @Override
  public void visit(codegen.C.exp.Not e)
  {
	  this.say("!");
	  e.exp.accept(this);
	  return;
  }

  @Override
  public void visit(codegen.C.exp.Num e)
  {
    this.say(Integer.toString(e.num));
    return;
  }

  @Override
  public void visit(codegen.C.exp.Sub e)
  {
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(codegen.C.exp.This e)
  {
    this.say("this");
  }

  @Override
  public void visit(codegen.C.exp.Times e)
  {
    e.left.accept(this);
    this.say(" * ");
    e.right.accept(this);
    return;
  }

  // statements
  @Override
  public void visit(codegen.C.stm.Assign s)
  {
	if(this.localRefs.contains(s.id)){
		s.id = "__GC_frame." + s.id;
	}
		
    this.printSpaces();
    this.say(s.id + " = ");
    s.exp.accept(this);
    this.sayln(";");
    return;
  }

  @Override
  public void visit(codegen.C.stm.AssignArray s)
  {
		if(this.localRefs.contains(s.id)){
			s.id = "__GC_frame." + s.id;
		}

	  this.printSpaces();
	  this.say(s.id + "[");
	  s.index.accept(this);
	  this.say(" + 4] = ");
	  s.exp.accept(this);
	  this.sayln(";");
	  return;
  }

  @Override
  public void visit(codegen.C.stm.Block s)
  {
	  this.unIndent();
	  this.printSpaces();
	  this.sayln("{");
	  this.indent();
	  for(codegen.C.stm.T stm : s.stms){
		  stm.accept(this);
	  }
	  this.unIndent();
	  this.printSpaces();
	  this.sayln("}");
	  this.indent();
	  return;	  
  }

  @Override
  public void visit(codegen.C.stm.If s)
  {
    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.thenn.accept(this);
    this.unIndent();
//    this.sayln("");
    this.printSpaces();
    this.sayln("else");
    this.indent();
    s.elsee.accept(this);
//    this.sayln("");
    this.unIndent();
    return;
  }

  @Override
  public void visit(codegen.C.stm.Print s)
  {
    this.printSpaces();
    this.say("System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
    return;
  }

  @Override
  public void visit(codegen.C.stm.While s)
  {
    this.printSpaces();
    this.say("while (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.body.accept(this);
    this.unIndent();
//	    this.sayln("");    
    return;	  
  }

  // type
  @Override
  public void visit(codegen.C.type.Class t)
  {
    this.say("struct " + t.id + " *");
  }

  @Override
  public void visit(codegen.C.type.Int t)
  {
    this.say("int");
  }

  @Override
  public void visit(codegen.C.type.IntArray t)
  {
	  this.say("int *");
  }

  // dec
  @Override
  public void visit(codegen.C.dec.Dec d)
  {
	  d.type.accept(this);
	  this.say(" " + d.id);
	  return;
  }

  /*
   * YKG. The longest length of a variable name in C is 31 characters.
   * I use util.Temp, does the int ranges sufficient?
   * 
   * (non-Javadoc)
   * @see codegen.C.Visitor#visit(codegen.C.method.Method)
   */
  // method
  @Override
  public void visit(codegen.C.method.Method m)
  {
	this.methodId = this.genId();
	this.localRefs.clear();	
	// arguments_gc_map
    String arguments_gc_map = "";
    for (codegen.C.dec.T d : m.formals) {
    	if(((codegen.C.dec.Dec) d).type instanceof codegen.C.type.Int){
    		arguments_gc_map += "0"; 
    	}else{ // int [], Class/Struct
    		arguments_gc_map += "1";
    	}
    }
    String locals_gc_map  = "";    
    for (codegen.C.dec.T d : m.locals) {
    	codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
    	if(dec.type instanceof codegen.C.type.Int){
    		locals_gc_map += "0"; 
    	}else{ // int [], Class/Struct
    		locals_gc_map += "1";
    		this.localRefs.add(dec.id);
    		
    	}
    }
    this.sayln("char *" + this.methodId + "_args_gc_map = \"" + arguments_gc_map + "\";");
//    this.sayln("char *" + this.methodId + "_locals_gc_map = \"" + locals_gc_map + "\";");
    
    // struct f_gc_frame
    this.sayln("struct " + this.methodId +"_gc_frame{");
    this.sayln("  void *__gc_prev;                      // dynamic chain, pointing to f's caller's GC frame");
    this.sayln("  char *arguments_gc_map;         // should be assigned the value of \"f_arguments_gc_map\"");
    this.sayln("  int *arguments_base_address;    // address of the first argument");
//    this.sayln("  char *locals_gc_map;            // should be assigned the value of \"f_locals_gc_map\"");
    this.sayln("  int localRefCount;");
    for (codegen.C.dec.T d : m.locals) {
        codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
        if(this.localRefs.contains(dec.id)){
	        this.say("  ");
	        dec.type.accept(this);
	        this.say(" " + dec.id + ";\n");
        }
    }    
    this.sayln("};");
    
    m.retType.accept(this);    
    this.say(" " + m.classId + "_" + m.id + "(");
    int size = m.formals.size();
    for (codegen.C.dec.T d : m.formals) {
      codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
      size--;
      dec.type.accept(this);
      this.say(" " + dec.id);
      if (size > 0)
        this.say(", ");
    }
    this.sayln(")");
    this.sayln("{");

    // init __GC_frame. /* YKG. Yeah, this variable name may be conflict with other variables, because of the NAME. */
    this.sayln("  // put the GC stack frame onto the call stack");
    this.sayln("  // note that this frame contains the three original locals in f");
    this.sayln("  struct " + this.methodId + "_gc_frame __GC_frame;  ");
    
    for (codegen.C.dec.T d : m.locals) {
      codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
      if(!this.localRefs.contains(dec.id)){
	      this.say("  ");
	      dec.type.accept(this);
	      this.say(" " + dec.id + ";\n");
      }
    }
    this.sayln("");
    
    this.sayln("  memset(&__GC_frame, 0, sizeof(__GC_frame));");
//    this.sayln("  fprintf(stderr, \"@@@prev: %x " + m.id + "\\n\", prev);");
    this.sayln("  fprintf(stderr, \"========================================>>>>>>>>>>>>>>>>>> method " + m.id + " start\\n\");");
    
    this.sayln("  __GC_frame.__gc_prev = prev;");
    this.sayln("  prev = &__GC_frame; ");
    this.sayln("  __GC_frame.arguments_gc_map = " + this.methodId + "_args_gc_map;");
    this.sayln("  __GC_frame.arguments_base_address = (int *)&this;");
//    this.sayln("  __GC_frame.locals_gc_map = " + this.methodId + "_locals_gc_map;");
    this.sayln("  __GC_frame.localRefCount = " + this.localRefs.size() + ";");
    this.sayln("");
    
    for (codegen.C.stm.T s : m.stms)
      s.accept(this);
    
    this.sayln("  prev = __GC_frame.__gc_prev;"); /* YKG. It takes me TWO DAYS debugging!!!! */
    
    this.sayln("  fprintf(stderr, \"========================================<<<<<<<<<<<<<<<<<<< method " + m.id + " leave\\n\");");
    this.say("  return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("}");
    return;
  }

  @Override
  public void visit(codegen.C.mainMethod.MainMethod m)
  {
    this.sayln("int Tiger_main ()");
    this.sayln("{");
    for (codegen.C.dec.T dec : m.locals) {
      this.say("  ");
      codegen.C.dec.Dec d = (codegen.C.dec.Dec) dec;
      d.type.accept(this);
      this.say(" ");
      this.sayln(d.id + ";");
    }
    m.stm.accept(this);
    this.sayln("}\n");
    return;
  }

  // vtables
  @Override
  public void visit(codegen.C.vtable.Vtable v)
  {
    this.sayln("struct " + v.id + "_vtable");
    this.sayln("{");
    this.sayln("  char * " + v.id + "_gc_map;");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      t.ret.accept(this);
      this.sayln(" (*" + t.id + ")();");
    }
//    this.sayln("};\n");
    this.sayln("} " + v.id + "_vtable_" + ";\n");
    return;
  }

  private void outputVtable(codegen.C.vtable.Vtable v)
  {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
//    this.sayln(v.id + "_vtable_ = "); // YKG. WRONG! Global assign statement is not allowed!
    this.sayln("{");
    this.sayln("  \"" + this.classes_gc_map.get(v.id) + "\",");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.sayln("};\n");
    return;
  }

  // class
  @Override
  public void visit(codegen.C.classs.Class c)
  {
	this.localRefs.clear();
	String class_gc_map = "";
	
    this.sayln("struct " + c.id);
    this.sayln("{");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    this.sayln("  int isObjOrArray;");
    this.sayln("  int length;");
    this.sayln("  char * forwarding;");
    for (codegen.C.Tuple t : c.decs) {
      this.say("  ");
      t.type.accept(this);
      this.say(" ");
      this.sayln(t.id + ";");
      
      if(t.type instanceof codegen.C.type.Int){
    	  class_gc_map += "0";  
      }else{
    	  class_gc_map += "1";
      }
    }
    this.sayln("};");
    
    classes_gc_map.put(c.id, class_gc_map);
    return;
  }

  // program
  @Override
  public void visit(codegen.C.program.Program p)
  {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = null;
      if (Control.outputName != null)
        outputName = Control.outputName;
      else if (Control.fileName != null)
        outputName = Control.fileName + ".c";
      else
        outputName = "a.c";

      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    this.localRefs = new java.util.LinkedHashSet<String>(); /* YKG. Prevent potential NULL Pointer Exception. */
    this.classes_gc_map = new java.util.HashMap<String, String>();

    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!\n");
    
    this.sayln("// include");
    this.sayln("#include <stdio.h>");
    this.sayln("#include <stdlib.h>\n");
    this.sayln("#include <string.h>\n");
    
    this.sayln("// structures");
    for (codegen.C.classs.T c : p.classes) {
      c.accept(this);
    }

    this.sayln("// vtables structures");
    for (codegen.C.vtable.T v : p.vtables) {
      v.accept(this);
    }
    this.sayln("");
    
    this.sayln("// methods");
    this.sayln("extern void * prev;");
    for (codegen.C.method.T m : p.methods) {
      m.accept(this);
    }
    this.sayln("");

    this.sayln("// vtables");
    for (codegen.C.vtable.T v : p.vtables) {
      outputVtable((codegen.C.vtable.Vtable) v);
    }
    this.sayln("");

    this.sayln("// main method");
    p.mainMethod.accept(this);
    this.sayln("");

    this.say("\n\n");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

@Override
public void visit(Block block) {
	this.say("(");
	block.exp.accept(this);
	this.say(")");
}

}
