package views

import scalatags.Text.all.*
import scalatags.Text.tags2.title as titleTag

object Home:
  def render(authentikUrl: String, version: String) =
    html(
      head(
        titleTag("Lichess Invites"),
        meta(charset := "utf-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1"),
        link(rel := "stylesheet", href := "/static/style.css")
      ),
      body(
        div(
          cls := "card-wrap",
          div(
            cls := "card",
            div(cls := "card-header", h1("Lichess Invites")),
            div(
              cls := "card-body",
              p(
                "This service allows Lichess team members to create an account to access internal applications."
              ),
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
              ),
              div(
                cls := "card-footer",
                p(
                  "Already have an account? ",
                  a(href := authentikUrl, "Go to your Authentik dashboard")
                )
              )
            )
          ),
          p(
            cls := "page-footer",
            if version.nonEmpty then a(href := s"https://github.com/lichess-org/invites/$version", version)
            else "dev"
          )
        )
      )
    )
