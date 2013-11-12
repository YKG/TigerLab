class IntArrayLength { 
	public static void main(String[] a) {
        System.out.println(new Arr().len());
    }
}

class Arr {
	public int len(){
		int[] arr;
		arr = new int[13];
		return arr.length;
	}
}
