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
    gradle.enable = true;
  };

  scripts.build.exec = "./gradlew build";
  scripts.test.exec = "./gradlew test";
  scripts.run.exec = "./gradlew run";
  scripts.format.exec = "./gradlew spotlessApply";

  enterShell = ''
    gradle install
    echo "Devenv active"
  '';
}
