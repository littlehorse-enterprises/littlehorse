/**
 * Integration tests (PARITY_PLAN.md tier 2) run against a REAL LittleHorse
 * server and are kept out of the default `npm test` run, which stays
 * Docker-free. Run them with `npm run test:integration`.
 *
 * globalSetup starts a fresh, uniquely named lh-standalone container per run
 * (override with LH_IT_HOST/LH_IT_PORT to use a server you manage) and
 * globalTeardown removes it (keep it with LH_IT_KEEP=1).
 */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['<rootDir>/src/integration/**/*.test.ts'],
  // A real server is slower than the fake one used by the unit tests.
  testTimeout: 120000,
  // The suite shares one server, so parallel files would collide on metadata
  // names and on tenant creation.
  maxWorkers: 1,
  reporters: ['default', '<rootDir>/jest.reporter.js'],
  globalSetup: '<rootDir>/src/integration/globalSetup.ts',
  globalTeardown: '<rootDir>/src/integration/globalTeardown.ts',
}
