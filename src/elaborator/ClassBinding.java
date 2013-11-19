package elaborator;

public class ClassBinding
{
  public String extendss; // null for non-existing extends
  public java.util.Hashtable<String, ast.type.T> fields;
  public java.util.Hashtable<String, MethodType> methods;

  public ClassBinding(String extendss)
  {
    this.extendss = extendss;
    this.fields = new java.util.Hashtable<String, ast.type.T>();
    this.methods = new java.util.Hashtable<String, MethodType>();
  }

  public ClassBinding(String extendss,
      java.util.Hashtable<String, ast.type.T> fields,
      java.util.Hashtable<String, MethodType> methods)
  {
    this.extendss = extendss;
    this.fields = fields;
    this.methods = methods;
  }

  public void put(String xid, ast.type.T type)
  {
    if (this.fields.get(xid) != null) {
      System.err.println("duplicated class field: " + xid);
//      System.exit(1);
    }else{
    	this.fields.put(xid, type);
    }
  }

  public void put(String mid, MethodType mt)
  {
    if (this.methods.get(mid) != null) {
      System.err.println("duplicated class method: " + mid);
//      System.exit(1);
    }else{
    	this.methods.put(mid, mt);	
    }
  }

  
  public void dump()
  {
    System.out.print("extends: ");
    if (this.extendss != null)
      System.out.println(this.extendss);
    else
      System.out.println("<>");
    System.out.println("\nfields:\n  ");
	  String key;
	  java.util.Enumeration<String> keys = this.fields.keys();
	  while(keys.hasMoreElements()){
		  key = keys.nextElement();
//		  System.out.println("==============================");
//		  System.out.println("Class " + key + ":");
		  
		  System.out.println(key + ": " + this.fields.get(key));  
	  }
//    System.out.println(fields.toString());
    System.out.println("\nmethods:\n  ");
//    System.out.println(methods.toString());
    keys = this.methods.keys();
	  while(keys.hasMoreElements()){
		  key = keys.nextElement();
//		  System.out.println("==============================");
//		  System.out.println("Class " + key + ":");
		  
		  System.out.println(key + ": " + this.methods.get(key));  
	  }
  }
  
  @Override
  public String toString()
  {
    System.out.print("extends: ");
    if (this.extendss != null)
      System.out.println(this.extendss);
    else
      System.out.println("<>");
    System.out.println("\nfields:\n  ");
    System.out.println(fields.toString());
    System.out.println("\nmethods:\n  ");
    System.out.println(methods.toString());

    return "";
  }

}
