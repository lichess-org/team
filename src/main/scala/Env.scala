import java.nio.file.{ Files, Path }
import scala.util.Try

object Env:
  def get(key: String, default: String = ""): String =
    sys.env
      .get(key)
      .orElse(
        sys.env.get(s"${key}_FILE").flatMap(path => Try(Files.readString(Path.of(path))).toOption)
      )
      .getOrElse(default)
