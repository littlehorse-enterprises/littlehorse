/**
 * Integration tests (PARITY_PLAN.md tier 2) run against a REAL LittleHorse
 * server and are kept out of the default `npm test` run, which must stay
 * Docker-free. Run them with `npm run test:integration`.
 */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['<rootDir>/src/integration/**/*.test.ts'],
  // Real servers are slower than the fake one; per-test timeouts still apply.
  testTimeout: 120000,
  // Integration tests share one server, so parallel suites would collide on
  // metadata names and worker registration.
  maxWorkers: 1,
  // Creates the per-run tenant and waits for the server to be reachable.
  globalSetup: '<rootDir>/src/integration/globalSetup.ts',
}
