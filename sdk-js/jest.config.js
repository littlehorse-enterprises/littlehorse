module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  coveragePathIgnorePatterns: ['<rootDir>/node_modules/'],
  // 'default' keeps Jest's own PASS/FAIL and summary; the second adds a
  // per-area feature-matrix breakdown (see jest.reporter.js).
  reporters: ['default', '<rootDir>/jest.reporter.js'],
}
