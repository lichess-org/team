{ pkgs, lib, config, inputs, ... }:

{
  # https://devenv.sh/basics/
  env.GREET = "devenv";

  # https://devenv.sh/packages/
  packages = [ pkgs.git ];

  languages.javascript.enable = true;
  languages.javascript.package = pkgs.nodejs-slim_24;
  languages.javascript.npm.enable = true;
  languages.javascript.pnpm.enable = true;
  languages.javascript.pnpm.install.enable = true;
  languages.javascript.corepack.enable = true;

  languages.java.enable = true;
  languages.java.jdk.package = pkgs.openjdk25;
  
  languages.scala.enable = true;
  languages.scala.sbt.enable = true;
}
