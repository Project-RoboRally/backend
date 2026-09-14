{ pkgs, lib, ... }:

{
  packages = with pkgs; [
    git
    gradle_9
    jdk25
  ];

  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk25;
  };

  scripts.build.exec = ''
    gradle build
  '';

  scripts.test.exec = ''
    gradle test
  '';

  scripts.run.exec = ''
    gradle run
  '';

  scripts.format.exec = ''
    gradle spotlessApply
  '';

  enterShell = ''
    gradle install
    echo "Devenv active"
  '';
}
