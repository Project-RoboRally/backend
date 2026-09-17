{ pkgs, lib, ... }:

{
  packages = with pkgs; [
    git
  ];

  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk25;
    gradle = {
      enable = true;
      package = pkgs.gradle_9;
    };
  };

  scripts.build.exec = "./gradlew build";
  scripts.test.exec = "./gradlew test";
  scripts.run.exec = "./gradlew run";
  scripts.format.exec = "./gradlew spotlessApply";

  enterShell = ''
    echo "Devenv active"
  '';
}
