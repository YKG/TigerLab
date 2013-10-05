package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser
{
  Lexer lexer;
  Token current;
  boolean varDecl2Stat;
  String id_stat;
  boolean ok;

  
  public Parser(String fname, java.io.InputStream fstream)
  {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
    varDecl2Stat = false;
    ok = true;
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.

  private void advance()
  {
    current = lexer.nextToken();
  }

  private void eatToken(Kind kind)
  {
    if (kind == current.kind)
      advance();
    else {
    	error(kind.toString());
//      System.out.println("Expects: " + kind.toString());
//      System.out.println("But got: " + current.toString());
//      System.exit(1);
    	
//      advance();
//      eatToken(kind);
    }
  }

  private void error(String diagnosis)
  {
	  this.ok = false;
//    System.out.println("Syntax error: compilation aborting...\n");
    System.out.print(lexer.getFname() + ":" + current.lineNum + ":" + current.colNum + ":");
    System.out.println("expect " + diagnosis + ", but got: " + current.kind.toString());
    System.out.println(lexer.getCurrentLine());
//    System.out.print(lexer.getFname() + ": Expects an expression, ");
//    System.out.println("But got: " + current.toString());    
//    System.exit(1);
    advance();
    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private java.util.LinkedList<ast.exp.T> parseExpList()
  {
	java.util.LinkedList<ast.exp.T> list = new java.util.LinkedList<ast.exp.T>();
    if (current.kind == Kind.TOKEN_RPAREN)
      return list;
    list.add(parseExp());
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      list.add(parseExp());
    }
    return list;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private ast.exp.T parseAtomExp()
  {
	  ast.exp.T exp = null;
		boolean retry = false;

		do {
			retry = false;
			switch (current.kind) {
			case TOKEN_LPAREN:
				advance();
				exp = new ast.exp.Block(parseExp(), current.lineNum, current.colNum);
				eatToken(Kind.TOKEN_RPAREN);
				return exp;
			case TOKEN_NUM:
				exp = new ast.exp.Num(Integer.parseInt(current.lexeme), current.lineNum, current.colNum);
				advance();
				return exp;
			case TOKEN_TRUE:
				exp = new ast.exp.True(current.lineNum, current.colNum);
				advance();
				return exp;
			case TOKEN_FALSE:
				exp = new ast.exp.False(current.lineNum, current.colNum);
				advance();
				return exp;
			case TOKEN_THIS:
				exp = new ast.exp.This(current.lineNum, current.colNum);
				advance();
				return exp;
			case TOKEN_ID:
				exp = new ast.exp.Id(current.lexeme, current.lineNum, current.colNum);
				advance();
				return exp;
			case TOKEN_NEW: {
				int line = current.lineNum;
				int col = current.colNum;
				advance();
				switch (current.kind) {
				case TOKEN_INT:
					advance();
					eatToken(Kind.TOKEN_LBRACK);
					exp = parseExp();
					eatToken(Kind.TOKEN_RBRACK);
					return new ast.exp.NewIntArray(exp, line, col);
				case TOKEN_ID:
					String id = current.lexeme;
					advance();
					eatToken(Kind.TOKEN_LPAREN);
					eatToken(Kind.TOKEN_RPAREN);
					return new ast.exp.NewObject(id, line, col);
				default:
					error("TOKEN_INT or TOKEN_ID");
					retry = true;
//					return;
				}
			}
			default:
				error("An expression");
				retry = true;
//				return;
			}
		} while (retry);
		return null;
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private ast.exp.T parseNotExp()
  {
	int line = current.lineNum; 
	int col = current.colNum;
	ast.exp.T exp = null;	  
    exp = parseAtomExp();
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          return new ast.exp.Length(exp, line, col);
        }
        String id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        exp = new ast.exp.Call(exp, id, parseExpList(), line, col);
        eatToken(Kind.TOKEN_RPAREN);
      } else {
        advance();
        exp = new ast.exp.ArraySelect(exp, parseExp(), line, col);
        eatToken(Kind.TOKEN_RBRACK);
      }
    }
    return exp;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private ast.exp.T parseTimesExp()
  {
	int line = current.lineNum;
	int col = current.colNum;
	boolean not = false;
	ast.exp.T exp = null;
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
      not = !not;	/* YKG. Remember to keep the source code */
    }
    exp = parseNotExp();
    return not ? new ast.exp.Not(exp, line, col) : exp;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private ast.exp.T parseAddSubExp()
  {
	int line = current.lineNum;
	int col = current.colNum;	  
	ast.exp.T exp = null;
    exp = parseTimesExp();	  	  
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      exp = new ast.exp.Times(exp, parseTimesExp(), line, col);
    }
    return exp;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private ast.exp.T parseLtExp()
  {
	int line = current.lineNum;
	int col = current.colNum;	 	  
	ast.exp.T exp = null;
    exp = parseAddSubExp();	  
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      if(current.kind == Kind.TOKEN_ADD){
    	  advance();
    	  exp = new ast.exp.Add(exp, parseAddSubExp(), line, col);
      }else{
    	  advance();
    	  exp = new ast.exp.Sub(exp, parseAddSubExp(), line, col);
      }
    }
    return exp;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private ast.exp.T parseAndExp()
  {
	int line = current.lineNum;
	int col = current.colNum;		  
	ast.exp.T exp = null;
    exp = parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      exp = new ast.exp.Lt(exp, parseLtExp(), line, col);
    }
    return exp;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private ast.exp.T parseExp()
  {
	int line = current.lineNum;
	int col = current.colNum;		  
	ast.exp.T exp = null;
    exp = parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      exp = new ast.exp.And(exp, parseAndExp(), line, col);
    }
    return exp;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  private ast.stm.T parseStatement()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
	  ast.exp.T exp;
	  ast.stm.T stm;
	  if(current.kind == Kind.TOKEN_LBRACE){
		  advance();
		  java.util.LinkedList<ast.stm.T> list = null;
		  if(current.kind != Kind.TOKEN_RBRACE){
			  list = parseStatements();
		  }
		  eatToken(Kind.TOKEN_RBRACE);
		  return new ast.stm.Block(list);
	  }else if(current.kind == Kind.TOKEN_IF){
		  eatToken(Kind.TOKEN_IF);
		  eatToken(Kind.TOKEN_LPAREN);
		  exp = parseExp();
		  eatToken(Kind.TOKEN_RPAREN);
		  stm = parseStatement();
		  eatToken(Kind.TOKEN_ELSE);
		  ast.stm.T stm2 = parseStatement();
		  return new ast.stm.If(exp, stm, stm2);
	  }else if(current.kind == Kind.TOKEN_WHILE){
		  eatToken(Kind.TOKEN_WHILE);
		  eatToken(Kind.TOKEN_LPAREN);
		  exp = parseExp();
		  eatToken(Kind.TOKEN_RPAREN);
		  stm = parseStatement();
		  return new ast.stm.While(exp, stm);
	  }else if(current.kind == Kind.TOKEN_SYSTEM){
		  eatToken(Kind.TOKEN_SYSTEM);
		  eatToken(Kind.TOKEN_DOT);
		  eatToken(Kind.TOKEN_OUT);
		  eatToken(Kind.TOKEN_DOT);
		  eatToken(Kind.TOKEN_PRINTLN);
		  eatToken(Kind.TOKEN_LPAREN);
		  exp = parseExp();
		  eatToken(Kind.TOKEN_RPAREN);
		  eatToken(Kind.TOKEN_SEMI);
		  return new ast.stm.Print(exp);
	  }else if(current.kind == Kind.TOKEN_ID || varDecl2Stat){
		  if(!varDecl2Stat){
			  id_stat = current.lexeme;
			  advance();
		  }else{
			  varDecl2Stat = false;
		  }
		  if(current.kind == Kind.TOKEN_ASSIGN){
			  eatToken(Kind.TOKEN_ASSIGN);
			  exp = parseExp();
			  eatToken(Kind.TOKEN_SEMI);  
			  return new ast.stm.Assign(id_stat, exp);
		  }else{
			  eatToken(Kind.TOKEN_LBRACK);			  
			  ast.exp.T index = parseExp();
			  eatToken(Kind.TOKEN_RBRACK);			  
			  eatToken(Kind.TOKEN_ASSIGN);
			  ast.exp.T rightExp = parseExp();
			  eatToken(Kind.TOKEN_SEMI);
			  return new ast.stm.AssignArray(id_stat, index, rightExp);
		  }
	  }
	  return null;	  
	  
//	new util.Todo();
  }

  // Statements -> Statement Statements
  // ->
  private java.util.LinkedList<ast.stm.T> parseStatements()
  {
    java.util.LinkedList<ast.stm.T> list = new java.util.LinkedList<ast.stm.T>();
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID || varDecl2Stat) {
      list.add(parseStatement());
    }
    return list;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private ast.type.T parseType()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a type.
	  if(current.kind == Kind.TOKEN_INT){
		  advance();
		  if(current.kind == Kind.TOKEN_LBRACK){
			  advance();
			  eatToken(Kind.TOKEN_RBRACK);
			  return new ast.type.IntArray();
		  }else{
			  return new ast.type.Int();
		  }
	  }else if(current.kind == Kind.TOKEN_BOOLEAN){
		  advance();
		  return new ast.type.Boolean();
	  }else if(current.kind == Kind.TOKEN_ID){
		  id_stat = current.lexeme;
		  advance();
		  if(current.kind == Kind.TOKEN_ASSIGN || current.kind == Kind.TOKEN_LBRACK){
		    	varDecl2Stat = true;
		  }else{
			  return new ast.type.Class(id_stat);
		  }
	  }
	  return null;
	  
//    new util.Todo();
  }

  // VarDecl -> Type id ;
  private ast.dec.T parseVarDecl()
  {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
	ast.type.T type = parseType();
	if(!varDecl2Stat){
		String id = current.lexeme;
		int lineNum = current.lineNum;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_SEMI);
		return new ast.dec.Dec(type, id, lineNum);
	}    
    return null;
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private java.util.LinkedList<ast.dec.T> parseVarDecls()
  {
    java.util.LinkedList<ast.dec.T> list = new java.util.LinkedList<ast.dec.T>();
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      ast.dec.T dec = parseVarDecl();
      if(varDecl2Stat){
    	  break;
      }else{
    	  list.add(dec);
      }
    }
    return list;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private java.util.LinkedList<ast.dec.T> parseFormalList()
  {
    ast.type.T type;
	java.util.LinkedList<ast.dec.T> list = new java.util.LinkedList<ast.dec.T>();
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      type = parseType();
      list.add(new ast.dec.Dec(type, current.lexeme, current.lineNum));
      eatToken(Kind.TOKEN_ID);
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        type = parseType();
        list.add(new ast.dec.Dec(type, current.lexeme, current.lineNum));
        eatToken(Kind.TOKEN_ID);
      }
    }
    return list;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private ast.method.T parseMethod()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
	
	  eatToken(Kind.TOKEN_PUBLIC);
	  ast.type.T retType = parseType();
	  String id = current.lexeme;
	  eatToken(Kind.TOKEN_ID);
	  eatToken(Kind.TOKEN_LPAREN);
	  java.util.LinkedList<ast.dec.T> formalList = parseFormalList();
	  eatToken(Kind.TOKEN_RPAREN);
	  eatToken(Kind.TOKEN_LBRACE);
	  java.util.LinkedList<ast.dec.T> varList = new java.util.LinkedList<ast.dec.T>();
	  varList = parseVarDecls();
	  java.util.LinkedList<ast.stm.T> stmList = new java.util.LinkedList<ast.stm.T>();
	  stmList = parseStatements();	  
	  eatToken(Kind.TOKEN_RETURN);
	  ast.exp.T retExp = parseExp();
	  eatToken(Kind.TOKEN_SEMI);
	  eatToken(Kind.TOKEN_RBRACE);
	  return new ast.method.Method(retType, id, formalList, varList, stmList, retExp);
	  
//    new util.Todo();
    
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private java.util.LinkedList<ast.method.T> parseMethodDecls()
  {
    java.util.LinkedList<ast.method.T> list = new java.util.LinkedList<ast.method.T>();
    while (current.kind == Kind.TOKEN_PUBLIC) {
      list.add(parseMethod());
    }
    return list;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private ast.classs.T parseClassDecl()
  {
	ast.classs.T classs = null;
    eatToken(Kind.TOKEN_CLASS);
    String id = current.lexeme;
    int line = current.lineNum; 
    eatToken(Kind.TOKEN_ID);
    String extendss = null;
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      extendss = current.lexeme;
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    java.util.LinkedList<ast.dec.T> varList = new java.util.LinkedList<ast.dec.T>();
    varList = parseVarDecls();
    java.util.LinkedList<ast.method.T> methodList = new java.util.LinkedList<ast.method.T>();
    methodList = parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    classs = new ast.classs.Class(id, extendss, varList, methodList, line);
    return classs;
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private java.util.LinkedList<ast.classs.T> parseClassDecls()
  {
	java.util.LinkedList<ast.classs.T> list = new java.util.LinkedList<ast.classs.T>();
    while (current.kind == Kind.TOKEN_CLASS) {
      list.add(parseClassDecl());
    }
    return list;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private ast.mainClass.T parseMainClass()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
	  eatToken(Kind.TOKEN_CLASS);
	  String id = current.lexeme;
	  eatToken(Kind.TOKEN_ID);
	  eatToken(Kind.TOKEN_LBRACE);
	  
	  eatToken(Kind.TOKEN_PUBLIC);
	  eatToken(Kind.TOKEN_STATIC);
	  eatToken(Kind.TOKEN_VOID);
	  eatToken(Kind.TOKEN_MAIN);
	  eatToken(Kind.TOKEN_LPAREN);
	  eatToken(Kind.TOKEN_STRING);
	  eatToken(Kind.TOKEN_LBRACK);
	  eatToken(Kind.TOKEN_RBRACK);
	  String arg = current.lexeme;
	  eatToken(Kind.TOKEN_ID);
	  eatToken(Kind.TOKEN_RPAREN);
		
	  eatToken(Kind.TOKEN_LBRACE);
	  ast.stm.T stm = parseStatement();
	  eatToken(Kind.TOKEN_RBRACE);
	  eatToken(Kind.TOKEN_RBRACE);
	  return new ast.mainClass.MainClass(id, arg, stm);
//    new util.Todo();
  }

  // Program -> MainClass ClassDecl*
  private ast.program.Program parseProgram()
  {
    ast.mainClass.T main = parseMainClass();
    java.util.LinkedList<ast.classs.T> classes = parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return new ast.program.Program(main, classes);
  }

  public ast.program.T parse()
  {
	ast.program.Program p = parseProgram();
	if(!this.ok)
		return null;
    return p;
  }
}
