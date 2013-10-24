class Factorial { 
	public static void main(String[] a) {
        System.out.println(new Fac().ComputeFac(10));
    }
}
class M extends M{
	public int foo(int a){
		int b;
		b = a + b;
		return b;
	}
}

class A extends B{
	public int foo(int a){
		int b;
		b = a + b;
		return b;
	}
}
class B extends C{
	public int foo(int a){
		int b;
		b = a + b;
		return b;
	}
}
class C extends A{
	public int foo(int a){
		int b;
		b = a + b;
		return b;
	}
}
class Fac {
    public int ComputeFac(int num) {
        int num_aux;
        if (num < 1)
            num_aux = 1;
        else
            num_aux = num * (this.ComputeFac(num-1));
        return num_aux;
    }
}
