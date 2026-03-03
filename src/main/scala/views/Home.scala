package views

import scalatags.Text.all._
import scalatags.Text.tags2.title as titleTag

object Home:
    def render(authentikUrl: String) =
        html(
            head(
                titleTag("Lichess Invites"),
                link(rel := "stylesheet", href := "/static/style.css")
            ),
            body(
                cls := "bg-white",
                h1("Lichess SSO Invites"),
                p(
                    "This application allows members of the Lichess team to create an account to access internal apps. If you're not a member of the Lichess team, please contact a team member to request access."
                ),
                p(
                    "To get started, please ",
                    a(href := "/login", "log in with your Lichess account"),
                    ". The `web:mod` scope is required to verify that you are a member of the Lichess team."
                ),
                p(
                    "If you already have an account, you can go directly to",
                    a(href := authentikUrl, " your Authentik dashboard"),
                )
            )
        )
