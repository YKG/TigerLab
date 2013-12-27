class Ex17
{
	public static void main (String[] args)
	{
		System.out.println (new Ex17Foo().foo());
	}
}

class Ex17Foo
{
	public int foo()
	{
		// int sum;
		// int x;
		// int y;
		// int z;

		// sum = 0;
		// x = 1;
		// y = x;
		// x = sum;
		// sum = x + y;
		// y = x + sum;
		// z = x + y;
		// return sum;

		int a;
		int c;

		a = 5;
		c = 1;
		while(c < a){
			c = c + c;
		}
		a = c - a;
		c = 0;
		return c + a;
	}
}
