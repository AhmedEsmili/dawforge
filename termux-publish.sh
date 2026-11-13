#!/data/data/com.termux/files/usr/bin/bash
# Run inside the unzipped dawforge folder.
set -e
pkg install -y git gh
[ -n "$(git config --global user.name)" ]  || git config --global user.name  "$(read -p 'Git name: ' n; echo $n)"
[ -n "$(git config --global user.email)" ] || git config --global user.email "$(read -p 'Git email: ' m; echo $m)"
gh auth status || gh auth login
git init -b main
git add .
git commit -m "DAWForge phase 1: engine, sequencer, piano roll"
gh repo create dawforge --public --source=. --push
echo "Waiting for cloud build..."
sleep 15
gh run watch --exit-status
mkdir -p ~/storage/downloads/dawforge-apk 2>/dev/null || true
gh run download -n dawforge-debug-apk -D ~/storage/downloads/dawforge-apk || gh run download -n dawforge-debug-apk -D ./apk
echo "Done. Install the APK from Downloads/dawforge-apk."
