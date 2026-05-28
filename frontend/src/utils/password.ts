const LOWER = "abcdefghijkmnpqrstuvwxyz";
const UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
const DIGITS = "23456789";
const SYMBOLS = "!@#$%^&*-_=+";

function pick(chars: string): string {
  const idx = crypto.getRandomValues(new Uint32Array(1))[0] % chars.length;
  return chars[idx];
}

/** Generates a random password (default 16 chars) with at least one of each character class. */
export function generatePassword(length = 16): string {
  const all = LOWER + UPPER + DIGITS + SYMBOLS;
  const required = [pick(LOWER), pick(UPPER), pick(DIGITS), pick(SYMBOLS)];
  const rest = Array.from({ length: Math.max(length, required.length) - required.length }, () => pick(all));
  const chars = [...required, ...rest];

  // Shuffle so required characters aren't always at the front.
  for (let i = chars.length - 1; i > 0; i--) {
    const j = crypto.getRandomValues(new Uint32Array(1))[0] % (i + 1);
    [chars[i], chars[j]] = [chars[j], chars[i]];
  }
  return chars.join("");
}
