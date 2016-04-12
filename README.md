ammonite-reload
===============

Reload scala files in Ammonite on file changes


Usage
-----

Load as predef `$ amm -f Reload.scala` or as module `@ load.module("Reload.scala")`

This adds the function
```scala
@ load.reload
def reload(path: ammonite.ops.Path): Reload.this.Watcher
```

### Loading

Assuming we have an empty file `Test.scala`, we use `reload` to set up automatic reload of a module
```scala
@ import ammonite.ops._
import ammonite.ops._
@ load.reload(cwd/"Test.scala")
res1: Reload#Watcher = Watcher(/home/tobias/Projects/personal/ammonite-reload/Test.scala)
```

Changin the contents of `Test.scala` to
```scala
val foo = List(1, 2, 3)
show(foo)
```
causes the module to reload in Ammonite
```scala
@  
<Reloading module /home/tobias/Projects/personal/ammonite-reload/Test.scala>
List(1, 2, 3)
@ foo
res2: List[Int] = List(1, 2, 3)
```

### Canceling

The watcher for a file can be canceled by calling `cancel()` on it
```scala
@ res1.cancel()
<Stopping Watcher(/home/tobias/Projects/personal/ammonite-reload/Test.scala)>
```
