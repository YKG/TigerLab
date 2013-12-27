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
		int a;
		int b;
		int c;
		int m;

		m = 1;
		c = m;
		a = c;
		b = a;
		c = a + b;
		
		return c;
	}
}
