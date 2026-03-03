package views

import scalatags.Text.all._
import scalatags.Text.tags2.title as titleTag

object Home:
    def render(authentikUrl: String) =
        html(
            head(
                titleTag("Lichess Invites"),
                meta(charset := "utf-8"),
                meta(name := "viewport", content := "width=device-width, initial-scale=1"),
                link(rel := "stylesheet", href := "/static/style.css")
            ),
            body(cls := "",
                div(cls := "",
                    div(cls := "",
                        div(cls := "",
                            div(cls := "",
                                h1(cls := "", "Lichess Invites")
                            )
                        ),
                        div(cls := "",
                            p(cls := "",
                                "Allows Lichess team members to create an account for internal apps. ",
                                "If you're not on the team, contact a team member to request access."
                            ),
                            div(cls := "",
                                p(cls := "", "Get started"),
                                p(cls := "",
                                    "Log in with Lichess to verify your membership. The ",
                                    code(cls := "", "web:mod"),
                                    " scope is required."
                                ),
                                a(href := "/login",
                                    cls := "",
                                    "Log in with Lichess"
                                )
                            ),
                            div(cls := "",
                                p(cls := "",
                                    "Already have an account? ",
                                    a(href := authentikUrl,
                                        cls := "",
                                        "Go to the Authentik portal"
                                    )
                                )
                            )
                        )
                    ),
                )
            )
        )
