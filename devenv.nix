{ pkgs, lib, config, inputs, ... }:

{
  packages = [
    pkgs.git
    pkgs.yamlfmt
  ];

  languages.java.enable = true;
  languages.java.jdk.package = pkgs.openjdk25;
  
  languages.scala.enable = true;
  languages.scala.sbt.enable = true;
}
