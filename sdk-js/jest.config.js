module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  coveragePathIgnorePatterns: ['<rootDir>/node_modules/'],
  // Integration tests need a real server and their own globalSetup; they run
  // via `npm run test:integration`. Excluded here so a bare `npx jest` cannot
  // pick them up and fail confusingly.
  testPathIgnorePatterns: ['/node_modules/', '<rootDir>/src/integration/'],
}
