class GCTest { 
	public static void main(String[] a) {
        System.out.println(new GC().doit(101));
    }
}

class GC {
    public int foo(){
        int i;
        int n;

        i = 0;
        n = 0;
        while(n < 100){
            i = new int[64].length;
            n = n + 1;
        }

        return i;
    }

    public int doit(int n) {
        int sum;
        int[] a;
        int i;
        GC gc;

        sum = 0;
        gc = new GC();
        i = 0;
        while(i < 1){
            sum = sum  + gc.foo();
            i = i + 1;
        }
        a = new int[64];
        sum = a.length + sum;

        return sum;
    }
}
