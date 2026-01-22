#!/usr/bin/env zsh
# scripts/generate_shared_key.sh
# Usage:
#   ./scripts/generate_shared_key.sh         # generate 32-byte (256-bit) key and print
#   ./scripts/generate_shared_key.sh -b 16   # generate 16-byte (128-bit) key
#   ./scripts/generate_shared_key.sh -k 128  # generate 128-bit key (16 bytes)
#   ./scripts/generate_shared_key.sh -c      # copy result to macOS clipboard (pbcopy)
#   ./scripts/generate_shared_key.sh -o file # write Base64 key to file (mode 600)
# Defaults to AES-256 (32 bytes) if not specified.

bytes=32
copy_to_clipboard=false
outfile=""
show_help=false

while getopts ":b:k:coh" opt; do
  case $opt in
    b) bytes=$OPTARG ;;
    k) # key size in bits
       if ! [[ $OPTARG =~ ^[0-9]+$ ]]; then
         echo "Error: -k expects numeric bit size (128/192/256)" >&2; exit 2
       fi
       bytes=$((OPTARG / 8))
       ;;
    c) copy_to_clipboard=true ;;
    o) outfile=$OPTARG ;;
    h) show_help=true ;;
    \?) echo "Invalid option: -$OPTARG" >&2; exit 2 ;;
    :) echo "Option -$OPTARG requires an argument." >&2; exit 2 ;;
  esac
done

if $show_help; then
  sed -n '1,200p' "$0"
  exit 0
fi

if ! [[ $bytes =~ ^[0-9]+$ ]] || [ "$bytes" -le 0 ]; then
  echo "Invalid byte count: $bytes" >&2
  exit 2
fi

# Generate random bytes then Base64 encode
if command -v openssl >/dev/null 2>&1; then
  key_base64=$(openssl rand "$bytes" | base64)
else
  # fallback to /dev/urandom and base64
  key_base64=$(dd if=/dev/urandom bs=1 count="$bytes" 2>/dev/null | base64)
fi

echo
echo "Base64 key:"
echo "$key_base64"
echo

# Optionally write to file (secure mode 600)
if [ -n "$outfile" ]; then
  printf '%s' "$key_base64" > "$outfile"
  chmod 600 "$outfile"
  echo "Saved to: $outfile (mode 600)"
fi

# Optionally copy to clipboard on macOS
if $copy_to_clipboard && command -v pbcopy >/dev/null 2>&1; then
  printf '%s' "$key_base64" | pbcopy
  echo "Copied to clipboard (pbcopy)."
elif $copy_to_clipboard; then
  echo "pbcopy not available; skipping clipboard copy."
fi

# Verify decoded byte length using best available tool (python3, openssl or base64)
try_decode_len() {
  # $1 is the key
  local k="$1"
  # If no arg provided, read from stdin
  if [ -z "$k" ]; then
    read -r k
  fi

  if command -v python3 >/dev/null 2>&1; then
    echo -n "$k" | python3 -c "import base64,sys; data=sys.stdin.read().strip(); print(len(base64.b64decode(data)))" 2>/dev/null
    return $?
  fi

  if command -v openssl >/dev/null 2>&1; then
    echo -n "$k" | openssl base64 -d -A 2>/dev/null | wc -c
    return $?
  fi

  if base64 --help >/dev/null 2>&1; then
    # GNU coreutils base64 supports --decode
    echo -n "$k" | base64 --decode 2>/dev/null | wc -c
    return $?
  fi

  # macOS base64 uses -D for decode
  echo -n "$k" | base64 -D 2>/dev/null | wc -c
  return $?
}

decoded_len=$(printf '%s' "$key_base64" | try_decode_len)
# If try_decode_len printed the length itself, capture it; else try again
if [ -z "$decoded_len" ]; then
  decoded_len=$(printf '%s' "$key_base64" | try_decode_len)
fi

if [[ "$decoded_len" =~ ^[0-9]+$ ]]; then
  echo "Decoded byte length: $decoded_len"
  case $decoded_len in
    16) echo "This is AES-128 (16 bytes)." ;;
    24) echo "This is AES-192 (24 bytes)." ;;
    32) echo "This is AES-256 (32 bytes)." ;;
    *)  echo "Non-standard AES key byte length." ;;
  esac
else
  echo "Could not determine decoded length (no suitable decode tool found)." >&2
fi
