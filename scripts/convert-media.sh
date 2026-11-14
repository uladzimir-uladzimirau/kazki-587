#!/usr/bin/env bash
# Converts generated media from $TMPDIR/kazki-media into Android resources.
set -euo pipefail
SRC="${TMPDIR:-/home/user/.tmp}/kazki-media"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RES="$ROOT/android-kazki/app/src/main/res"
mkdir -p "$RES/drawable-nodpi" "$RES/raw"

for f in "$SRC"/covers/cover_*.png; do
  name="$(basename "$f" .png)"
  magick "$f" -resize 720x960^ -gravity center -extent 720x960 -quality 82 "$RES/drawable-nodpi/$name.webp"
done
magick "$SRC/moon_art.png" -resize 900x -quality 85 "$RES/drawable-nodpi/moon_art.webp"
ffmpeg -y -loglevel error -i "$SRC/lullaby_music_box.mp3" -ac 2 -b:a 96k "$RES/raw/lullaby.mp3"
echo "done"
