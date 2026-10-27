import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react-swc'
import { resolve } from 'path'
import { realpathSync } from 'fs'

// import path from "path";

// https://vite.dev/config/
//TODO: Thresholds to be increased after adding more tests
const COVERAGE_THRESHOLDS = {
  lines: 75,
  functions: 65,
  branches: 65,
  statements: 75,
};

// Resolve symlinks so we get the real path (works both locally and in Bazel runfiles)
const ROOT = realpathSync(process.cwd())
// When running under Bazel, individual files are symlinks into the source tree.
// Vite's /@fs/ guard blocks access to symlink targets outside ROOT, so we must
// also allow the real source directory (the symlink target of vite.config.ts).
const REAL_SOURCE_DIR = realpathSync(new URL(import.meta.url).pathname.replace(/\/vite\.config\.[^/]+$/, ''))

export default defineConfig({
  plugins: [react()],
  server: {
    fs: {
      allow: [ROOT, REAL_SOURCE_DIR],
    },
  },
  resolve: {
    alias: {
      "@": resolve(ROOT, 'src')
    }
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: [resolve(ROOT, 'test/setup.ts')],
    unstubEnvs: true,
    include: ['test/**/*.test.{ts,tsx}', 'test/**/*.spec.{ts,tsx}'],
    exclude: ['test/pages/loginPage.test.tsx', 'test/pages/registrationPage.test.tsx'], // TODO: Temporary exclusion as it's coming from IDP now
    coverage: {
      provider: 'v8',
      reporter: ['text'],
      reportsDirectory: resolve(ROOT, 'coverage'),
      thresholds: COVERAGE_THRESHOLDS,
      exclude: ['./src/gen/**.ts', './src/**/*.css', './src/services/*.ts']
    },
    hookTimeout: 30000
  }
})
