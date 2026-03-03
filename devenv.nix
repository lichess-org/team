{ pkgs, lib, config, inputs, ... }:

{
  # https://devenv.sh/basics/
  env.GREET = "devenv";

  # https://devenv.sh/packages/
  packages = [ pkgs.git ];

  languages.java.enable = true;
  languages.java.jdk.package = pkgs.openjdk25;
  
  languages.scala.enable = true;
  languages.scala.sbt.enable = true;
}
