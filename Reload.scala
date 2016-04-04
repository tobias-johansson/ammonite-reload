

implicit class Reload(load: ammonite.repl.frontend.Load) {

  import java.nio.file._
  import java.nio.file.StandardWatchEventKinds._
  import scala.collection.JavaConversions._

  def reload(path: ammonite.ops.Path) = {

    load.module(path)

    val file = path.toNIO.toAbsolutePath
    val dir  = file.getParent

    val ws = FileSystems.getDefault().newWatchService()
    dir.register(ws, ENTRY_MODIFY)

    val watcher = new Watcher(ws) {
      def observe(kind: WatchEvent.Kind[Path], path: Path) = {
        val changed = dir.resolve(path)
        if (changed == file) {
          val toLoad = ammonite.ops.Path(changed.toFile)
          println(s"Reloading module $toLoad")
          load.module(toLoad)
        }
      }
    }

    watcher.start()
    watcher
  }

  abstract class Watcher(ws: WatchService) extends Thread {
    private[this] var running = true

    def observe(kind: WatchEvent.Kind[Path], path: Path): Unit

    override def run() =
      while (running) {
        try {
          val key = ws.take()
          key.pollEvents().foreach {
            case event if event.kind == OVERFLOW =>
              running = false
            case event: WatchEvent[Path] =>
              observe(event.kind, event.context)
              running = key.reset
            case _ =>
          }
        } catch {
          case _ => running = false
        }
      }

    def cancel(): Unit = {
      running = false
      ws.close()
    }
  }
}
