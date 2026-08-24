const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

async function generate() {
  const rootDir = path.resolve(__dirname, '..');
  const assetsDir = path.join(rootDir, 'assets');
  const resDir = path.join(rootDir, 'android', 'app', 'src', 'main', 'res');

  const fgSvgPath = path.join(rootDir, 'ic_launcher_foreground.svg');
  const monoSvgPath = path.join(rootDir, 'ic_launcher_monochrome.svg');

  const fgSvgRaw = fs.readFileSync(fgSvgPath, 'utf8');
  const monoSvgRaw = fs.readFileSync(monoSvgPath, 'utf8');

  // 1. Prepare SVG variants:
  // - Full icon SVG (with #0159A5 background)
  // - Transparent Foreground SVG (without background rect)
  // - Transparent Monochrome SVG (without background rect, white paths)
  // - Background SVG (solid #0159A5)

  // Strip background rect from foreground SVG for clean adaptive layers
  const fgTransparentSvg = fgSvgRaw.replace(/<rect[^>]*fill="#0159A5"[^>]*\/>/i, '');
  // Strip background rect from monochrome SVG
  const monoTransparentSvg = monoSvgRaw.replace(/<rect[^>]*fill="#0159A5"[^>]*\/>/i, '');
  // Solid background SVG
  const bgSvg = `<svg width="108" height="108" viewBox="0 0 108 108" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="108" height="108" fill="#0159A5"/></svg>`;
  // Full composite SVG (background + foreground)
  const fullSvg = fgSvgRaw;

  // Round clip mask for round legacy icon
  const getRoundSvg = (size) => `<svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}"><circle cx="${size / 2}" cy="${size / 2}" r="${size / 2}" fill="white"/></svg>`;

  console.log('Generating Expo assets in assets/...');

  // assets/icon.png (1024x1024)
  await sharp(Buffer.from(fullSvg))
    .resize(1024, 1024)
    .png()
    .toFile(path.join(assetsDir, 'icon.png'));

  // assets/android-icon-foreground.png (1024x1024, transparent)
  await sharp(Buffer.from(fgTransparentSvg))
    .resize(1024, 1024)
    .png()
    .toFile(path.join(assetsDir, 'android-icon-foreground.png'));

  // assets/android-icon-background.png (1024x1024, solid)
  await sharp(Buffer.from(bgSvg))
    .resize(1024, 1024)
    .png()
    .toFile(path.join(assetsDir, 'android-icon-background.png'));

  // assets/android-icon-monochrome.png (1024x1024, transparent)
  await sharp(Buffer.from(monoTransparentSvg))
    .resize(1024, 1024)
    .png()
    .toFile(path.join(assetsDir, 'android-icon-monochrome.png'));

  // assets/favicon.png (48x48)
  await sharp(Buffer.from(fullSvg))
    .resize(48, 48)
    .png()
    .toFile(path.join(assetsDir, 'favicon.png'));

  console.log('Generating Android res mipmap files in android/app/src/main/res/...');

  const densities = [
    { name: 'mipmap-mdpi', adaptiveSize: 108, legacySize: 48 },
    { name: 'mipmap-hdpi', adaptiveSize: 162, legacySize: 72 },
    { name: 'mipmap-xhdpi', adaptiveSize: 216, legacySize: 96 },
    { name: 'mipmap-xxhdpi', adaptiveSize: 324, legacySize: 144 },
    { name: 'mipmap-xxxhdpi', adaptiveSize: 432, legacySize: 192 },
  ];

  for (const d of densities) {
    const targetFolder = path.join(resDir, d.name);
    if (!fs.existsSync(targetFolder)) {
      fs.mkdirSync(targetFolder, { recursive: true });
    }

    // 1. ic_launcher_background.webp
    await sharp(Buffer.from(bgSvg))
      .resize(d.adaptiveSize, d.adaptiveSize)
      .webp({ lossless: true })
      .toFile(path.join(targetFolder, 'ic_launcher_background.webp'));

    // 2. ic_launcher_foreground.webp
    await sharp(Buffer.from(fgTransparentSvg))
      .resize(d.adaptiveSize, d.adaptiveSize)
      .webp({ lossless: true })
      .toFile(path.join(targetFolder, 'ic_launcher_foreground.webp'));

    // 3. ic_launcher_monochrome.webp
    await sharp(Buffer.from(monoTransparentSvg))
      .resize(d.adaptiveSize, d.adaptiveSize)
      .webp({ lossless: true })
      .toFile(path.join(targetFolder, 'ic_launcher_monochrome.webp'));

    // 4. ic_launcher.webp (Square legacy icon with squircle / square corners)
    await sharp(Buffer.from(fullSvg))
      .resize(d.legacySize, d.legacySize)
      .webp({ lossless: true })
      .toFile(path.join(targetFolder, 'ic_launcher.webp'));

    // 5. ic_launcher_round.webp (Round legacy icon)
    const baseImg = await sharp(Buffer.from(fullSvg))
      .resize(d.legacySize, d.legacySize)
      .png()
      .toBuffer();

    const roundMask = Buffer.from(getRoundSvg(d.legacySize));

    await sharp(baseImg)
      .composite([{ input: roundMask, blend: 'dest-in' }])
      .webp({ lossless: true })
      .toFile(path.join(targetFolder, 'ic_launcher_round.webp'));

    console.log(`Generated all icons for ${d.name} (adaptive ${d.adaptiveSize}px, legacy ${d.legacySize}px)`);
  }

  console.log('Icon generation complete!');
}

generate().catch((err) => {
  console.error(err);
  process.exit(1);
});
