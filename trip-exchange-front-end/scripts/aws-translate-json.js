#!/usr/bin/env node
/**
 * Simple script to translate JSON i18n files using AWS Translate.
 *
 * Usage:
 *   AWS_ACCESS_KEY_ID=... AWS_SECRET_ACCESS_KEY=... node scripts/aws-translate-json.js --source en --dir src/assets/i18n
 *
 * It will read <dir>/<source>.json and translate keys into all other locale files
 * found in the directory. Existing non-empty values are preserved unless --force
 * is passed.
 */
const fs = require('fs');
const path = require('path');
const { TranslateClient, TranslateTextCommand } = require('@aws-sdk/client-translate');

function usage() {
  console.log('Usage: AWS_ACCESS_KEY_ID=... AWS_SECRET_ACCESS_KEY=... node scripts/aws-translate-json.js --source en --dir src/assets/i18n [--force] [--region us-east-1]');
}

async function translateText(client, text, sourceLang, targetLang) {
  // AWS Translate TranslateText expects text up to 5000 bytes; for long strings, fall back to sending as-is
  const cmd = new TranslateTextCommand({ SourceLanguageCode: sourceLang, TargetLanguageCode: targetLang, Text: text });
  const res = await client.send(cmd);
  return res.TranslatedText;
}

function flatten(obj, prefix = '') {
  const out = {};
  for (const k of Object.keys(obj)) {
    const v = obj[k];
    const key = prefix ? `${prefix}.${k}` : k;
    if (v && typeof v === 'object' && !Array.isArray(v)) {
      Object.assign(out, flatten(v, key));
    } else {
      out[key] = v;
    }
  }
  return out;
}

function unflatten(flat) {
  const out = {};
  for (const key of Object.keys(flat)) {
    const parts = key.split('.');
    let cur = out;
    for (let i = 0; i < parts.length; i++) {
      const p = parts[i];
      if (i === parts.length - 1) {
        cur[p] = flat[key];
      } else {
        cur[p] = cur[p] || {};
        cur = cur[p];
      }
    }
  }
  return out;
}

async function main() {
  const argv = require('minimist')(process.argv.slice(2));
  const src = argv.source || argv.s;
  const dir = argv.dir || argv.d || 'src/assets/i18n';
  const force = argv.force || false;
  const region = argv.region || process.env.AWS_REGION || 'us-east-1';

  if (!src) {
    usage();
    process.exit(1);
  }

  const client = new TranslateClient({ region });

  const srcPath = path.resolve(dir, `${src}.json`);
  if (!fs.existsSync(srcPath)) {
    console.error('Source file not found:', srcPath);
    process.exit(1);
  }

  const srcJson = JSON.parse(fs.readFileSync(srcPath, 'utf8'));
  const flatSrc = flatten(srcJson);

  // list all .json files in dir
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.json'));

  for (const file of files) {
    const locale = path.basename(file, '.json');
    if (locale === src) continue;
    const targetPath = path.join(dir, file);
    let targetJson = {};
    if (fs.existsSync(targetPath)) {
      try {
        targetJson = JSON.parse(fs.readFileSync(targetPath, 'utf8')) || {};
      } catch (e) {
        console.error('Failed to parse', targetPath, e.message);
        continue;
      }
    }

    const flatTarget = flatten(targetJson);
    let updated = false;

    for (const key of Object.keys(flatSrc)) {
      const srcVal = flatSrc[key];
      if (srcVal == null || srcVal === '') continue;
      if (!force && flatTarget[key] && flatTarget[key] !== '') continue;

      const toTranslate = String(srcVal);
      try {
        const translated = await translateText(client, toTranslate, src, locale);
        flatTarget[key] = translated;
        updated = true;
        console.log(`Translated ${key} -> ${locale}`);
      } catch (e) {
        console.error(`Translate failed for ${key} -> ${locale}:`, e.message || e);
      }
    }

    if (updated) {
      const out = unflatten(flatTarget);
      fs.writeFileSync(targetPath, JSON.stringify(out, null, 2) + '\n', 'utf8');
      console.log('Wrote', targetPath);
    } else {
      console.log('No changes for', targetPath);
    }
  }
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
