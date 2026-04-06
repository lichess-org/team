package views

import scalatags.Text.all.*
import scalatags.Text.tags2.title as titleTag

object Home:
  val siteName = "Lichess Team"

  def render(version: String) =
    html(
      head(
        titleTag(siteName),
        meta(charset := "utf-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1"),
        link(rel := "stylesheet", href := "/static/style.css")
      ),
      body(
        div(
          cls := "card-wrap",
          div(
            cls := "card",
            div(cls := "card-header", h1(siteName)),
            div(
              cls := "card-body",
              div(
                cls := "section",
                p(cls := "section-label", "Get started"),
                p(
                  "Log in with Lichess to verify your membership. The ",
                  code("web:mod"),
                  " scope is required to verify you have the ",
                  code("Lichess team"),
                  " role."
                ),
                a(href := "/login", cls := "btn-primary", "Log in with Lichess")
              )
            )
          ),
          p(
            cls := "page-footer",
            if version.nonEmpty then a(href := s"https://github.com/lichess-org/team/$version", version)
            else "dev"
          )
        )
      )
    )
