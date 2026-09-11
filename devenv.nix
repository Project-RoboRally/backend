{ pkgs, lib, ... }:

{
  packages = with pkgs; [
    git
    gradle
    jdk25
  ];

  languages.java.enable = true;

  scripts.build.exec = ''
    gradle build
  '';

  scripts.test.exec = ''
    gradle test
  '';

  scripts.run.exec = ''
    gradle run
  '';

  enterShell = ''
    gradle install
    echo "Devenv active"
  '';
}
