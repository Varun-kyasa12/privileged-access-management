import { build } from 'esbuild';
import { mkdir, rm, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const out = resolve(root, 'dist');

await rm(out, { recursive: true, force: true });
await mkdir(resolve(out, 'assets'), { recursive: true });

await build({
  entryPoints: [resolve(root, 'src/main.jsx')],
  bundle: true,
  format: 'esm',
  minify: true,
  sourcemap: false,
  outfile: resolve(out, 'assets/app.js'),
  loader: { '.js': 'jsx', '.jsx': 'jsx', '.css': 'css' },
  define: { 'process.env.NODE_ENV': '"production"' }
});

const html = '<!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="theme-color" content="#102b2b"><title>SafeAccess | PAM Backend</title></head><body><div id="root"></div><script type="module" src="/assets/app.js"></script></body></html>\n';
await writeFile(resolve(out, 'index.html'), html, 'utf8');
console.log('SafeAccess production bundle written to dist/.');
