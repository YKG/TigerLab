class Ex5
{
	public static void main (String[] args)
	{
		System.out.println (new Ex5Foo().foo());
	}
}

class Ex5Foo
{
	public int foo()
	{
		int a;
		int c;

		a = 1;
		c = a + 0;
		a = c * 1;
		a = 0 * c;
		c = 1 + 1;
		a = 0 * a;
		c = 1 * a;

		return c + a;
	}
}
