#!/data/data/com.termux/files/usr/bin/bash
# Usage: bash push-and-build.sh "commit message"
set -e
git add -A
git commit -m "${1:-update}" || true
git push
sleep 15
gh run watch --exit-status "$(gh run list --limit 1 --json databaseId -q '.[0].databaseId')"
rm -rf ~/storage/downloads/dawforge-apk
gh run download -n dawforge-debug-apk -D ~/storage/downloads/dawforge-apk
echo "APK ready in Downloads/dawforge-apk"
