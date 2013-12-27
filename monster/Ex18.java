class Ex18
{
	public static void main (String[] args)
	{
		System.out.println (new Ex18Foo().foo());
	}
}

class Ex18Foo
{
	public int foo()
	{
		int [] a;
		int [] b;
		int [] c;
		int [] m;
		int sum;

		m = new int[5];
		c = m;
		a = c;
		b = a;
		sum = a.length + b.length + c.length;

		return sum;
	}
}
