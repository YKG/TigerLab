class Ex4
{
	public static void main (String[] args)
	{
		System.out.println (new Ex4Foo().foo());
	}
}

class Ex4Foo
{
	public int foo()
	{
		int a;
		int c;

		if(true){
			c = 1;
		}else{
			c = 2;
		}
		c = c + 10;
		while(false){			
			a = 5;
			c = 1;	
		}
		while(true){
			c = c + c;
		}
		if(false){
			a = 1;
		}else{
			a = 2;
		}
		a = c - a;
		c = 0;
		return c + a;
	}
}
