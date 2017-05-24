def apply(f: Int => Int, v: Int) = f(v)

def example(x: Int): Int = {
  x+4
}

apply(example,2)