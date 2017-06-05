def line(a: Int, b: Int, x: Int): Int = a * x + b

def curriedLine(a: Int)(b: Int)(x: Int): Int = a * x + b

def defaultLine(x: Int): Int = curriedLine(1)(2)(x)

curriedLine(1)(2)(0)
line(1,2,0)


def example(a:Int => Int)(b:Int => Int): Int = b(5) + a(1)

def sum(a:Int):Int = a+a

def mult(b:Int):Int = b*b

example(mult)(sum)


def sum(a:Int,b:Int,c:Int): Int = a+b+c

sum(1,2,3)
val x = sum(_:Int,2,3)
x(1)



