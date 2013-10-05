class Factorial { 
	public static void main(String[] a) {
        System.out.println(new Fac().ComputeFac(10, 11));
    }
}
class Fac{
    public int ComputeFac(int num) {
        int num_aux;
        if (num < 1)
            num_aux2 = 1;
        else
            num_aux = num * (this.ComputeFac(num-1));
        num_aux = true;
        return num_aux;
    }
}
