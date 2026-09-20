#!/usr/bin/env bash
# Deploy this working tree to se2-h over SSH.
# The server is firewalled from GitHub; this never git pull/clone on the host
# and never copies GitHub tokens or SSH keys for github.com.
#
# Build happens on the laptop (Java 25 + Gradle). Only the Boot JAR is uploaded.
#
# Usage (from laptop, in the backend repo):
#   ./scripts/deploy-se2-h.sh
#
# Optional env (SSH host, not an http URL):
#   DEPLOY_HOST=se2-h.compute.dtu.dk ./scripts/deploy-se2-h.sh
#   DEPLOY_USER=sXXXXXX ./scripts/deploy-se2-h.sh   # skips the username prompt

set -euo pipefail

ssh_host() {
  # ssh wants se2-h.compute.dtu.dk, not http://se2-h.compute.dtu.dk/
  local host="${1:-}"
  host="${host#http://}"
  host="${host#https://}"
  host="${host%%/*}"
  host="${host%%:*}"
  printf '%s' "$host"
}

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -z "${DEPLOY_USER:-}" ]]; then
  read -rp "SSH username on se2-h: " DEPLOY_USER
fi
if [[ -z "${DEPLOY_USER}" ]]; then
  echo "Username is required." >&2
  exit 1
fi
DEPLOY_HOST="$(ssh_host "${DEPLOY_HOST:-se2-h.compute.dtu.dk}")"
REMOTE="${DEPLOY_USER}@${DEPLOY_HOST}"
STAGING="roborally-backend-upload"
DEST="/opt/roborally"
JAVA_BIN="/usr/bin/java"

cd "$ROOT"

if [[ ! -f settings.gradle.kts && ! -f settings.gradle ]]; then
  echo "Run this from the backend repo (settings.gradle.kts not found)." >&2
  exit 1
fi
if [[ ! -x ./gradlew ]]; then
  echo "./gradlew is missing or not executable." >&2
  exit 1
fi

echo "Building Boot JAR"
./gradlew :app:bootJar --no-daemon

JAR="$(find app/build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | tail -n 1)"
if [[ -z "${JAR}" || ! -f "${JAR}" ]]; then
  echo "No Boot JAR found under app/build/libs (ignored *-plain.jar)." >&2
  exit 1
fi
echo "Using ${JAR}"

echo "Uploading JAR -> ${REMOTE}:~/${STAGING}/roborally.jar"
ssh "$REMOTE" "mkdir -p \"\$HOME/${STAGING}\""
rsync -az --delete "${JAR}" "${REMOTE}:~/${STAGING}/roborally.jar"

echo "Installing and restarting on ${DEPLOY_HOST}"
# -t so sudo can ask for your se2-h password. Nothing GitHub-related is sent.
ssh -t "$REMOTE" "set -euo pipefail
  ${JAVA_BIN} -version
  sudo mkdir -p '${DEST}'
  sudo cp \"\$HOME/${STAGING}/roborally.jar\" '${DEST}/roborally.jar'
  sudo chown roborally:roborally '${DEST}/roborally.jar'
  sudo systemctl restart roborally.service
  sudo systemctl --no-pager --full status roborally.service
"

echo "Deployed. Backend: http://${DEPLOY_HOST}:8080"
echo "Logs: ssh ${REMOTE} 'sudo journalctl -u roborally.service -n 50 --no-pager'"
