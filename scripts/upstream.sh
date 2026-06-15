#!/usr/bin/env bash
# get base dir regardless of execution location
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
. "$(dirname "$SOURCE")/init.sh"

git submodule update --init --progress

if [[ "$1" == up* ]]; then
  (
    cd "$basedir/$upstreamDir/" || exit
    git fetch && git reset --hard "origin/$upstreamBranch"
    cd ../
    git add "$upstreamDir"
  )
fi

upstreamVer=$(gethead "$upstreamDir")
basedir

tag="tag-$(echo -e "$upstreamVer" | shasum | awk '{print $1}')"
echo "$tag" >"$basedir"/current-upstream

cd "${upstreamDir}/" || exit

function tag() {
  (
    cd "$1" || exit
    if [ "$2" == "1" ]; then
      git tag -d "$tag" 2>/dev/null
    fi
    echo -e "$(date)\n\n$version" | git tag -a "$tag" -F - 2>/dev/null
  )
}
echo "Tagging as $tag"
echo -e "$version"

forcetag=0
if [ "$(cat "$basedir"/current-upstream)" != "$tag" ]; then
  forcetag=1
fi

tag "." $forcetag

