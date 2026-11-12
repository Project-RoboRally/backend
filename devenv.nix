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

  services.postgres = {
    enable = true;
    package = pkgs.postgresql_16;
    listen_addresses = "127.0.0.1";
    port = 5432;
    initialDatabases = [ { name = "roborally"; } ];
    initialScript = ''
      CREATE ROLE roborally WITH LOGIN SUPERUSER PASSWORD 'roborally-password!2026';
    '';
  };

  scripts.build.exec = "./gradlew build";
  scripts.test.exec = "./gradlew test";
  scripts.run.exec = "./gradlew run";
  scripts.format.exec = "./gradlew spotlessApply";

  enterShell = ''
    echo "Devenv active"
  '';
}
