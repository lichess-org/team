object App extends cask.MainRoutes {
  @cask.get("/")
  def home() = "Hello, World!"

  initialize()
}
