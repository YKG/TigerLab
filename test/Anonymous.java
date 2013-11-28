class Anonymous { 
	public static void main(String[] a) {
        System.out.println(new AnonymousIntArray().getLength());
    }
}

class AnonymousIntArray {
    public int getLength() {
        int len;

        len = new int[2].length;

        return len;
    }
}
