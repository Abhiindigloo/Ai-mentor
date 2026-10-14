import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

const workflow=await readFile(new URL('../.github/workflows/ci.yml',import.meta.url),'utf8');
const build=await readFile(new URL('../android/app/build.gradle.kts',import.meta.url),'utf8');
const setup=workflow.match(/uses: android-actions\/setup-android@v3\n\s+with:\n\s+packages: '([^']+)'/);
assert.ok(setup,'Android SDK setup must explicitly override the obsolete default packages');
const packages=setup[1].split(/\s+/);
const sdk=build.match(/compileSdk = (\d+)/)[1];
assert.ok(!packages.includes('tools'),'The removed Android SDK tools package must not be requested');
assert.ok(packages.includes('platform-tools'));
assert.ok(packages.includes(`platforms;android-${sdk}`));
assert.ok(packages.includes(`build-tools;${sdk}.0.0`));
console.log('PASS: Android CI explicitly installs supported SDK packages matching compileSdk.');
