{ pkgs, lib, config, inputs, ... }:

{
  packages = [
    pkgs.git
    pkgs.yamlfmt
  ];

  languages = {
    java = {
      enable = true;
      jdk.package = pkgs.openjdk25;
    };
    scala = {
      enable = true;
      sbt.enable = true;
    };
  };
}
