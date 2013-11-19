package elaborator;

public class MethodTable
{
  private java.util.Hashtable<String, ast.type.T> table;
  private java.util.Hashtable<String, ast.dec.T> tableLocalVar;
  
  public MethodTable()
  {
    this.table = new java.util.Hashtable<String, ast.type.T>();
    this.tableLocalVar = new java.util.Hashtable<String, ast.dec.T>();
  }

  // Duplication is not allowed
  public void put(java.util.LinkedList<ast.dec.T> formals,
      java.util.LinkedList<ast.dec.T> locals)
  {
    for (ast.dec.T dec : formals) {
      ast.dec.Dec decc = (ast.dec.Dec) dec;
      if (this.table.get(decc.id) != null) {
        System.err.println("duplicated parameter: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.type);
    }

    for (ast.dec.T dec : locals) {
      ast.dec.Dec decc = (ast.dec.Dec) dec;
      if (this.table.get(decc.id) != null) {
        System.err.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, decc.type);
      this.tableLocalVar.put(decc.id, decc);
    }
  }

  // Duplication is not allowed
  public void put(java.util.LinkedList<ast.dec.T> formals,
      java.util.LinkedList<ast.dec.T> locals, ClassTable ref)
  {
    for (ast.dec.T dec : formals) {
      ast.dec.Dec decc = (ast.dec.Dec) dec;
      if (this.table.get(decc.id) != null) {
        System.err.println("duplicated parameter: " + decc.id);
//        System.exit(1);
      }else{
          ast.dec.Dec d = decc;
          if(d.type instanceof ast.type.Class){
        	 String cname = ((ast.type.Class)(d.type)).id;
        	 if(ref.get(cname) == null){
        		 System.err.println("Error: " + dec.lineNum + ": unknown type '" +  cname + "'");
        		 /* YKG. Remember to mark the error */
//        		 this.table.put(decc.id, null);
//        	 }else{
//        		 this.table.put(decc.id, d.type);
        	 }
//          }else{
//        	 this.table.put(decc.id, decc.type);  
          }
    	  this.table.put(decc.id, decc.type); /* YKG. left him go */
      }
    }

    for (ast.dec.T dec : locals) {
      ast.dec.Dec decc = (ast.dec.Dec) dec;
      if (this.table.get(decc.id) != null) {
        System.err.println("duplicated variable: " + decc.id);
//        System.exit(1);
      }else{
    	  ast.dec.Dec d = decc;
          if(d.type instanceof ast.type.Class){
        	 String cname = ((ast.type.Class)(d.type)).id;
        	 if(ref.get(cname) == null){
        		 System.err.println("Error: " + dec.lineNum + ": unknown type '" +  cname + "'");
        		 /* YKG. Remember to mark the error */
        	 }
          }
    	  this.table.put(decc.id, decc.type);
	      this.tableLocalVar.put(decc.id, decc);
      }
    }
  }
  
  // return null for non-existing keys
  public ast.type.T get(String id)
  {
    return this.table.get(id);
  }

  public void dump()
  {
//System.out.println("--------------- MethodTable dump -- begin ----");		  
  System.out.println("-------------------------------------");		  
//	System.out.println(this.table.toString());
	  String key;
	  java.util.Enumeration<String> keys = this.table.keys();
	  while(keys.hasMoreElements()){
		  key = keys.nextElement();
		  System.out.println(key + ": " + this.table.get(key));  
	  }
System.out.println("=============== MethodTable dump end ===========");	  
//    new Todo();
  }

  public void clear()
  {
	  this.table.clear();
	  this.tableLocalVar.clear();
  }
  
  public void set(String id)
  {
	  this.tableLocalVar.remove(id);
  }
  
  
  public void dumpTableLocalVar()
  {
//	System.out.println(this.tableLocalVar.toString());  
  }
  public void printWarning()
  {
	  String key;
	  java.util.Enumeration<String> keys = this.tableLocalVar.keys();
	  while(keys.hasMoreElements()){
		  key = keys.nextElement();
		  System.err.println("Warning: variable \"" + key + "\" declared at line " 
				  + this.tableLocalVar.get(key).lineNum + " never used");
	  }
  }
  

  
  @Override
  public String toString()
  {
    return this.table.toString();
  }
}
