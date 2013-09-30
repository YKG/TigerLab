package lexer;

import java.io.InputStream;
import java.util.HashMap;

import lexer.Token.Kind;
import util.Todo;

public class Lexer {
	String fname; // the input file name to be compiled
	InputStream fstream; // input stream for the above file

	HashMap<String, Kind> m;
	int lineNum;
	int colNum;
	
	public Lexer(String fname, InputStream fstream) {
		this.fname = fname;
		this.fstream = fstream;
		this.lineNum = 1;
		this.colNum = 1;
		this.m = new HashMap<String, Kind>();
		m.put("+", Kind.TOKEN_ADD);
		m.put("&&", Kind.TOKEN_AND);
		m.put("=", Kind.TOKEN_ASSIGN);
		m.put("boolean", Kind.TOKEN_BOOLEAN);
		m.put("class", Kind.TOKEN_CLASS);
		m.put(",", Kind.TOKEN_COMMER);
		m.put(".", Kind.TOKEN_DOT);
		m.put("else", Kind.TOKEN_ELSE);
//		TOKEN_EOF, // EOF
		m.put("extends", Kind.TOKEN_EXTENDS);
		m.put("false", Kind.TOKEN_FALSE);
//		TOKEN_ID, // Identifier
		m.put("if", Kind.TOKEN_IF);
		m.put("int", Kind.TOKEN_INT);
		m.put("{", Kind.TOKEN_LBRACE);
		m.put("[", Kind.TOKEN_LBRACK);
		m.put("length", Kind.TOKEN_LENGTH);
		m.put("(", Kind.TOKEN_LPAREN);
		m.put("<", Kind.TOKEN_LT);
		m.put("main", Kind.TOKEN_MAIN);
		m.put("new", Kind.TOKEN_NEW);
		m.put("!", Kind.TOKEN_NOT);
//		TOKEN_NUM, // IntegerLiteral
		m.put("out", Kind.TOKEN_OUT);
		m.put("println", Kind.TOKEN_PRINTLN);
		m.put("public", Kind.TOKEN_PUBLIC);
		m.put("}", Kind.TOKEN_RBRACE);
		m.put("]", Kind.TOKEN_RBRACK);
		m.put("return", Kind.TOKEN_RETURN);
		m.put(")", Kind.TOKEN_RPAREN);
		m.put(";", Kind.TOKEN_SEMI);
		m.put("static", Kind.TOKEN_STATIC);
		m.put("String", Kind.TOKEN_STRING);
		m.put("-", Kind.TOKEN_SUB);
		m.put("System", Kind.TOKEN_SYSTEM);
		m.put("this", Kind.TOKEN_THIS);
		m.put("*", Kind.TOKEN_TIMES);
		m.put("true", Kind.TOKEN_TRUE);
		m.put("void", Kind.TOKEN_VOID);
		m.put("while", Kind.TOKEN_WHILE);
	}

	// When called, return the next token (refer to the code "Token.java")
	// from the input stream.
	// Return TOKEN_EOF when reaching the end of the input stream.
	private Token nextTokenInternal() throws Exception {
		int leftBlockComment = 0;
		int c;
		int state = 0;
		String s = "";
		while (true) {
			this.fstream.mark(1);
			c = this.fstream.read();
			colNum++;
			if (-1 == c)// may return other value. DO NOT FORGET.
			{
				if(leftBlockComment > 0)
					new Todo();
				return new Token(Kind.TOKEN_EOF, lineNum);
			}
			
			if(leftBlockComment < 0){
				new Todo();
			}
			
			if(leftBlockComment > 0){
				int ss = state;
				if(state == 6){
					if('*' == c){
						state = 7;
					}else if('/' == c){
						state = 8;
					}else{
					}
				}else if(state == 7){
					if('*' == c){
					}else if('/' == c){
						state = 8;
						leftBlockComment--;
						if(leftBlockComment == 0){
							state = 0;
						}else if(leftBlockComment > 0){
							state = 6;
						}
					}else{
						state = 6;
					}
				}else if(state == 8){
					if('*' == c){
						leftBlockComment++;
						state = 6;
					}else if('/' == c){
					}else{
						state = 6;
					}
				}
				
				continue;
			}
			
			
			
			if (state == 0) {// EMPTY				
				if(m.get("" + (char)c) != null)
					return new Token(m.get("" + (char)c), lineNum, colNum - 1, ""+(char)c);
				
				if (Character.isDigit(c)) {
					state = 1;
					s += (char)c;
				} else if (Character.isLowerCase(c) || Character.isUpperCase(c)) {
					state = 2;
					s += (char)c;
				}else if(' ' == c || '\t' == c || '\n' == c || '\r' == c){
					if('\n' == c) {
						lineNum++;
						colNum = 1;
					}
				}else if('&' == c){
					state = 3;
				}else if('/' == c){
					state = 4;
				}else{	// Illegal character.
//					new Todo();
					System.out.println("Illegal character: '" + (char)c + "' at line " + lineNum + ", col " + colNum);
					System.exit(1);
					return null;					
				}
			} else if (state == 1) {// NUM
				if (Character.isDigit(c)) {
					s += (char)c;
				} else {
					colNum--;
					this.fstream.reset();
					return new Token(Kind.TOKEN_NUM, lineNum, colNum - s.length(), s);
				}
			} else if (state == 2) {// ID
				if (Character.isDigit(c) || Character.isLowerCase(c)
						|| Character.isUpperCase(c) || '_' == c) {
					s += (char)c;
				} else{
					colNum--;
					this.fstream.reset();
					return new Token(m.get(s) == null ? Kind.TOKEN_ID : m.get(s), lineNum, colNum - s.length(), s);
				}
			} else if(state == 3){
				if('&' == c) return new Token(Kind.TOKEN_AND, lineNum, colNum - s.length(), s);
				else{
					new Todo();// for signal &
					return null;
				}
			} else if(state == 4){
				if('/' == c){
					state = leftBlockComment > 0 ? 6 : 5;
				}else if('*' == c){
					leftBlockComment++;
					state = 6;
				}else{
					new Todo();// for signal /
					return null;
				}
			} else if(state == 5){
				if('\n' == c){
					state = 0;
					colNum = 1;
					this.fstream.reset();
				}
			}
			

		}
	}

	public Token nextToken() {
		Token t = null;

		try {
			t = this.nextTokenInternal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (control.Control.lex)
			System.out.println(t.toString());
		return t;
	}
	
	public String getFname(){
		return this.fname;
	}
}
