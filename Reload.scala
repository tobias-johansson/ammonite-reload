

implicit class Reload(load: ammonite.repl.frontend.Load) {

  import java.nio.file._
  import java.nio.file.StandardWatchEventKinds._
  import scala.collection.JavaConversions._
  import scala.util.Try

  def reload(path: ammonite.ops.Path) = {

    load.module(path)

    val file = path.toNIO.toAbsolutePath

    val watcher = Watcher(file) { path =>
      val toLoad = ammonite.ops.Path(path)
      println("")
      println(s"<Reloading module $toLoad>")
      Try(load.module(toLoad))
      print(colors().prompt() + prompt() + colors().reset())
    }

    watcher.start()
    watcher
  }

  object Watcher {
    def apply(path: Path)(obs: Path => Unit) =
      new Watcher(path) {
        def observe(kind: WatchEvent.Kind[Path], path: Path) = obs(path)
      }
  }

  abstract class Watcher(file: Path) extends Thread {
    private[this] var running = true
    val ws = FileSystems.getDefault().newWatchService()

    def observe(kind: WatchEvent.Kind[Path], file: Path): Unit

    override def run() = {
      val dir = file.getParent
      dir.register(ws, ENTRY_MODIFY)
      def isOurFile(p: Path) = dir.resolve(p) == file
      while (running) {
        val attempt = Try {
          val key = ws.take()
          key.pollEvents().foreach {
            case event if event.kind == OVERFLOW =>
              running = false
            case event: WatchEvent[Path]
            if isOurFile(event.context) =>
              observe(event.kind, dir.resolve(event.context))
            case _ =>
          }
          key.reset
        }
        running = attempt.isSuccess
      }
      println(s"<Stopping $this>")
    }

    def cancel(): Unit = {
      running = false
      ws.close()
    }

    override def toString(): String =
      s"Watcher($file)"
  }
}
