import { mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const frontendDir = path.resolve(__dirname, '..');
const targetPath = path.join(frontendDir, 'android', 'app', 'google-services.json');
const encodedConfig = process.env.GOOGLE_SERVICES_JSON_B64;

async function fileExists(filePath) {
  try {
    await readFile(filePath);
    return true;
  } catch {
    return false;
  }
}

async function main() {
  if (!encodedConfig) {
    if (await fileExists(targetPath)) {
      console.log(`google-services.json already exists at ${targetPath}`);
      return;
    }

    console.error('GOOGLE_SERVICES_JSON_B64 is not set and google-services.json does not exist.');
    console.error('Set GOOGLE_SERVICES_JSON_B64 or provide frontend/android/app/google-services.json locally.');
    process.exitCode = 1;
    return;
  }

  let decoded;
  try {
    decoded = Buffer.from(encodedConfig, 'base64').toString('utf8');
  } catch {
    console.error('GOOGLE_SERVICES_JSON_B64 could not be decoded as base64.');
    process.exitCode = 1;
    return;
  }

  try {
    JSON.parse(decoded);
  } catch {
    console.error('Decoded GOOGLE_SERVICES_JSON_B64 is not valid JSON.');
    process.exitCode = 1;
    return;
  }

  await mkdir(path.dirname(targetPath), { recursive: true });
  await writeFile(targetPath, decoded, 'utf8');
  console.log(`Wrote google-services.json to ${targetPath}`);
}

await main();
