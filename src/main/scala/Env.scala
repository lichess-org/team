import scala.io.Source
import scala.util.Using

object Env:
  def get(key: String, default: String = ""): String =
    sys.env
      .get(key)
      .orElse(
        sys.env.get(s"${key}_FILE").flatMap(path => Using(Source.fromFile(path))(_.mkString).toOption)
      )
      .getOrElse(default)
