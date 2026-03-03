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
            body(cls := "min-h-screen bg-stone-950 flex items-center justify-center p-6",
                div(cls := "w-full max-w-md",
                    div(cls := "bg-stone-900 border border-stone-700 rounded-2xl shadow-2xl overflow-hidden",
                        div(cls := "border-b border-stone-700 px-8 py-6",
                            div(cls := "flex items-center gap-3",
                                h1(cls := "text-xl font-semibold text-white tracking-tight text-center", "Lichess Invites")
                            )
                        ),
                        div(cls := "px-8 py-7 space-y-5",
                            p(cls := "text-stone-300 leading-relaxed",
                                "This service allows Lichess team members to create an account to access internal applications."
                            ),
                            div(cls := "rounded-xl bg-stone-950 border border-stone-700 px-5 py-4 space-y-3",
                                p(cls := "text-stone-500 text-xs font-semibold uppercase tracking-widest", "Get started"),
                                p(cls := "text-stone-300",
                                    "Log in with Lichess to verify your membership. The ",
                                    code(cls := "text-amber-400 bg-stone-800 px-1.5 py-0.5 rounded text-xs font-mono", "web:mod"),
                                    " scope is required to verify you have the `Lichess team` role."
                                ),
                                a(href := "/login",
                                    cls := "inline-flex items-center gap-2 bg-amber-500 hover:bg-amber-400 text-stone-950 font-bold px-4 py-2.5 rounded-lg transition-colors duration-150",
                                    "Log in with Lichess"
                                )
                            ),
                            div(cls := "pt-1 border-t border-stone-700",
                                p(cls := "text-stone-500 pt-2",
                                    "Already have an account? ",
                                    a(href := authentikUrl,
                                        cls := "text-amber-400 hover:text-amber-300 underline underline-offset-2 transition-colors duration-150",
                                        "Go to the Authentik portal"
                                    )
                                )
                            )
                        )
                    ),
                )
            )
        )
