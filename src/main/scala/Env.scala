import scala.util.Try
import scala.io.Source

object Env:
  def get(key: String, default: String = ""): String =
    sys.env
      .get(key)
      .orElse(
        sys.env.get(s"${key}_FILE").flatMap(path => Try(Source.fromFile(path).getLines().mkString).toOption)
      )
      .getOrElse(default)
