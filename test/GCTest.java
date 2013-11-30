class GCTest { 
	public static void main(String[] a) {
        System.out.println(new GC().doit(101));
    }
}

class GC {
    public int foo(){
        int i;

        i = new int[64].length;

        return i;
    }

    public int doit(int n) {
        int sum;
        int[] a;
        int i;
        GC gc;

        gc = new GC();
        i = 0;
        while(i < 10){
            sum = sum  + gc.foo();
            i = i + 1;
        }
        a = new int[64];
        sum = a.length + sum;

        return sum;
    }
}
