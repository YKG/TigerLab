// compile it using
// $ java Tiger -codegen C -auot ../test/Triger_gc2.java
// and run it using
// $ ./a.out @tiger -heapSize 1400 -gcLog true @
class Triger_gc2 {
    public static void main(String[] args) {
        System.out.println(new Test2().foo(3));
    }
}

class Test2 {
    public int foo(int j) {
        int i;
        int[] data;
        AllocateArray2 aa;
        i = 0;
        while (i < j) {
            data = new int[100];
            aa = new AllocateArray2();
            i = i + aa.allocate(data);//trigger gc
        }
        return 1;
    }
}

class AllocateArray2 {
    int[] array;
    public int allocate(int[] data) {
        array = new int[100];
        array = new int[100];
        array = new int[100];
        array = new int[100];
        return 1;
    }
}
