#!/usr/bin/env bash
# Generates KAZKI placeholder media (covers, moon art, lullaby, icon).
# Raw outputs go to $OUT (outside the project); convert-media.sh converts them into the Android res folders.
set -u
OUT="${OUT:-$TMPDIR/kazki-media}"
mkdir -p "$OUT/covers" "$OUT/logs"

STYLE="Gentle storybook gouache illustration for a Belarusian folk tale for small children. Deep navy night-blue palette with warm golden light, red and cream Belarusian embroidery (vyshyvanka) patterns on clothing, plants and borders, rowan berries, calm cozy bedtime mood, soft painterly texture, centered composition, no text, no letters."

cover() {
  local id="$1"; shift
  (rork ai image "$STYLE Scene: $*" --name "cover_$id" --aspect-ratio 3:4 --opaque -o "$OUT/covers/"; echo "exit $?") > "$OUT/logs/cover_$id.txt" 2>&1 &
}

cover kotok "a brave little cat playing a small gusli harp, a proud rooster in an embroidered shirt and a sly red fox peeking from the forest"
cover zajac_vozyk "a tall grey hare and a small hedgehog standing at the start of a race in a moonlit field"
cover sabaka_vouk "a well-fed farm dog with a red collar and a thin grey wolf talking at the edge of a dark forest"
cover chvasty "forest animals - a fox with a fluffy tail, a hare with a short tail and a bear - gathered around a pile of tails in a glade"
cover zalaty_ptach "a magnificent golden firebird flying over an apple tree with golden apples in a night garden"
cover karalevic "a young prince in a red cloak and a wizard's daughter in an embroidered dress escaping from a dark castle"
cover och_tabakierka "a simple peasant man holding a glowing golden snuffbox, a tiny magical spirit appearing from it"
cover palasuki "a mossy forest spirit with a beard of leaves and a field spirit made of wheat meeting at the border of forest and field under the moon"
cover sciopka "a cheerful clever village boy in a straw hat talking to a puzzled mustached nobleman in a fur coat"
cover razumnaja_dacka "a thoughtful girl with a long braid and a red embroidered headband solving riddles by a wooden village house"
cover muzyka "a young musician boy playing a wooden flute, golden music notes swirling toward a castle under a full moon"
cover andrej "a wise young peasant man in an embroidered shirt standing proudly near a wooden windmill"
cover kot "a calm grey cat sitting bravely while shadows of a bear, wolf and deer hide behind trees"
cover sapciucha "an old village herbal healer woman whispering over a bowl of herbs in a cozy wooden hut with a candle"
cover alionka "a brave young girl in a red embroidered dress walking through a dark enchanted forest holding a lantern"
cover barsuk "a badger and a fox peeking out of their cozy burrows under tree roots at night"

(rork ai image "$STYLE Scene: a sleeping smiling crescent moon decorated with red and cream embroidery patterns, small golden stars, branches of rowan berries on both sides, isolated on plain deep navy #22294A background" --name moon_art --aspect-ratio 4:3 --opaque -o "$OUT/"; echo "exit $?") > "$OUT/logs/moon_art.txt" 2>&1 &

(rork ai audio music "Very calm, slow Belarusian folk lullaby (kalyhanka) for putting small children to sleep. Soft music box and warm acoustic guitar, gentle and steady, no drums, no sudden changes, seamless loop" -d 180 --instrumental -o "$OUT/"; echo "exit $?") > "$OUT/logs/lullaby.txt" 2>&1 &

(rork ai image --icon "A sleeping golden crescent moon with a small star, framed by a geometric red and cream Belarusian embroidery ornament, on a deep navy night-sky background, flat storybook style" -o assets/generated/icon.png && rork ai edit icon-set assets/generated/icon.png --target android -o android-kazki/; echo "exit $?") > "$OUT/logs/icon.txt" 2>&1 &

echo "Started. Logs in $OUT/logs"
