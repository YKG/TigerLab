import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;
import parser.Parser;
import sun.reflect.generics.tree.Tree;
import control.CommandLine;
import control.Control;

public class Tiger
{
  public static void main(String[] args)
  {
    InputStream fstream;
    Parser parser;

    // ///////////////////////////////////////////////////////
    // handle command line arguments
    CommandLine cmd = new CommandLine();
    String fname = cmd.scan(args);

    // /////////////////////////////////////////////////////
    // to test the pretty printer on the "test/Fac.java" program
    if (control.Control.testFac) {
      System.out.println("Testing the Tiger compiler on Fac.java starting:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      ast.Fac.prog.accept(pp);

      // elaborate the given program, this step is necessary
      // for that it will annotate the AST with some
      // informations used by later phase.
      elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
      ast.Fac.prog.accept(elab);

      // Compile this program to C.
      System.out.println("code generation starting");
   // code generation
      switch (control.Control.codegen) {
      case Bytecode:
        System.out.println("bytecode codegen");            
        codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
        ast.Fac.prog.accept(trans);
        codegen.bytecode.program.T bytecodeAst = trans.program;
        codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
        bytecodeAst.accept(ppbc);
        break;
      case C:
        System.out.println("C codegen");
        codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
        ast.Fac.prog.accept(transC);
        codegen.C.program.T cAst = transC.program;
        codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
        cAst.accept(ppc);
        break;
      case Dalvik:
//        codegen.dalvik.TranslateVisitor transDalvik = new codegen.dalvik.TranslateVisitor();
//        ast.Fac.prog.accept(transDalvik);
//        codegen.dalvik.program.T dalvikAst = transDalvik.program;
//        codegen.dalvik.PrettyPrintVisitor ppDalvik = new codegen.dalvik.PrettyPrintVisitor();
//        dalvikAst.accept(ppDalvik);
        break;
      case X86:
        // similar
        break;
      default:
        break;
      }
      System.out.println("Testing the Tiger compiler on Fac.java finished.");
      System.exit(1);
    }

    if (fname == null) {
      cmd.usage();
      return;
    }
    Control.fileName = fname;

    // /////////////////////////////////////////////////////
    // it would be helpful to be able to test the lexer
    // independently.
    if (control.Control.testlexer) {
      System.out.println("Testing the lexer. All tokens:");
      try {
        fstream = new BufferedInputStream(new FileInputStream(fname));
        Lexer lexer = new Lexer(fname, fstream);
        Token token = lexer.nextToken();
        while (token.kind != Kind.TOKEN_EOF) {
          System.out.println(token.toString());
          token = lexer.nextToken();
        }
        fstream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    // /////////////////////////////////////////////////////////
    // normal compilation phases.
    ast.program.T theAst = null;

    // parsing the file, get an AST.
    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      parser = new Parser(fname, fstream);

      theAst = parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // pretty printing the AST, if necessary
    if (control.Control.dumpAst) {
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      theAst.accept(pp);
    }

    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    theAst.accept(elab);

    // code generation
    switch (control.Control.codegen) {
    case Bytecode:
      codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
      theAst.accept(trans);
      codegen.bytecode.program.T bytecodeAst = trans.program;
      codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
      bytecodeAst.accept(ppbc);
      break;
    case C:
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      theAst.accept(transC);
      codegen.C.program.T cAst = transC.program;
      codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
      cAst.accept(ppc);
      break;
    case Dalvik:
//      codegen.dalvik.TranslateVisitor transDalvik = new codegen.dalvik.TranslateVisitor();
//      theAst.accept(transDalvik);
//      codegen.dalvik.program.T dalvikAst = transDalvik.program;
//      codegen.dalvik.PrettyPrintVisitor ppDalvik = new codegen.dalvik.PrettyPrintVisitor();
//      dalvikAst.accept(ppDalvik);
      break;
    case X86:
      // similar
      break;
    default:
      break;
    }
    
    // Lab3, exercise 6: add some glue code to
    // call gcc to compile the generated C or x86
    // file, or call java to run the bytecode file,
    // or dalvik to run the dalvik bytecode.
    // Your code here:
    
    if(System.getProperty("os.name").toLowerCase().indexOf("win") < 0){
    	System.err.println("Sorry! The following glue code works ONLY on Windows.");
    	return;
    }
    
    try{
    	String exeFileName = fname.substring(0, fname.lastIndexOf('.')) + ".exe";
    	String command = "gcc ../runtime/runtime.c " + fname+".c" + " -o " + exeFileName;
//    	System.out.println("exec: " + command);
    	Process p = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});
    	p.waitFor();
    	BufferedReader in = new BufferedReader(  
                new InputStreamReader(p.getInputStream()));  
		String line = null;  
		while ((line = in.readLine()) != null) {
			System.out.println(line);  
		}
		BufferedReader err = new BufferedReader(  
                new InputStreamReader(p.getErrorStream()));  
		line = null;  
		while ((line = err.readLine()) != null) {
			System.out.println(line);  
		}
		
    	
//    	System.out.println("exec: " + exeFileName);
    	p = Runtime.getRuntime().exec(new String[]{"cmd", "/c", ".\\"+exeFileName});
    	p.waitFor();
    	in = new BufferedReader(  
                new InputStreamReader(p.getInputStream()));  
		line = null;
		while ((line = in.readLine()) != null) {
			System.out.println(line);  
		}
		err = new BufferedReader(  
                new InputStreamReader(p.getErrorStream()));
		line = null;
		while ((line = err.readLine()) != null) {
			System.out.println(line);  
		}
    }catch(Exception e){
    	e.printStackTrace();
    }
    
    
    return;
  }
}
