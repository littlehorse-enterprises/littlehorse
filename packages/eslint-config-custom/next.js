const { resolve } = require('node:path')

const project = resolve(process.cwd(), 'tsconfig.json')

/*
 * This is a custom ESLint configuration for use with
 * Next.js apps.
 *
 * This config extends the Vercel Engineering Style Guide.
 * For more information, see https://github.com/vercel/style-guide
 *
 */

module.exports = {
  extends: [
    '@vercel/style-guide/eslint/node',
    '@vercel/style-guide/eslint/typescript',
    '@vercel/style-guide/eslint/browser',
    '@vercel/style-guide/eslint/react',
    '@vercel/style-guide/eslint/next',
    '@vercel/style-guide/eslint/jest',
    '@vercel/style-guide/eslint/jest-react'
  ].map(require.resolve),
  parserOptions: {
    project,
  },
  globals: {
    React: true,
    JSX: true,
  },
  settings: {
    'import/resolver': {
      typescript: {
        project,
      },
    },
  },
  ignorePatterns: [ 'node_modules/', 'dist/' ],
  // add rules configurations here
  rules: {
    'import/no-default-export': 'off',
    '@next/next/no-html-link-for-pages': 'off',
    '@next/next/no-head-element': 'off',
    'unicorn/filename-case': 'off',
    '@next/next/no-img-element': 'off', // aparently using Next JS Imge requires extra storage on the server, disabling until a decision is taken
    'no-console': 'off',
    'camelcase': 'off',
    'no-return-await': 'off',
    'eqeqeq': 'off',
    'jsx-a11y/click-events-have-key-events': 'off',
    'import/no-named-as-default-member': 'off',
    'no-undef': 'off',
    'no-prototype-builtins': 'off',
    'func-names': 'off',
    'no-await-in-loop': 'off',
    'no-unsafe-optional-chaining': 'off',
    'jsx-a11y/alt-text': 'off',
    'jsx-a11y/no-static-element-interactions': 'off',
    'react/hook-use-state': 'off',
    '@typescript-eslint/ban-ts-comment': 'off',
    'react/button-has-type': 'off',
    'import/named': 'off',
    'jsx-a11y/no-noninteractive-element-interactions': 'off',
    'react/no-array-index-key': 'off',
    'react-hooks/exhaustive-deps': 'off', // do we really need it?
    '@typescript-eslint/naming-convention': 'off', // seems to be the easier to fix
    '@typescript-eslint/no-empty-interface': 'off',
    'import/no-named-as-default': 'off',
    'no-param-reassign': 'off',
    'no-unused-vars': 'off',
    'prefer-promise-reject-errors': 'off',
    '@typescript-eslint/no-extraneous-class': 'off',
    'typescript-eslint/require-await': 'off',
    '@typescript-eslint/require-await': 'off',
    '@typescript-eslint/no-non-null-assertion': 'off',
    '@typescript-eslint/restrict-template-expressions': 'off',
    '@typescript-eslint/no-base-to-string': 'off',
    'no-nested-ternary': 'off',
    '@typescript-eslint/no-shadow': 'off',
    '@typescript-eslint/no-empty-function': 'off',
    '@typescript-eslint/no-unused-vars': 'off',
    '@typescript-eslint/no-misused-promises': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/no-unsafe-assignment': 'off',
    '@typescript-eslint/no-unsafe-call': 'off',
    '@typescript-eslint/no-unsafe-member-access': 'off',
    '@typescript-eslint/no-unsafe-argument': 'off',
    '@typescript-eslint/no-unnecessary-condition': 'off',
    '@typescript-eslint/no-unsafe-return': 'off',
    '@typescript-eslint/no-floating-promises': 'off', // 1 occurrence that needs to be fixed
    '@typescript-eslint/dot-notation': 'off',
    'semi': [ 'error', 'never' ],
    'no-unreachable': 'error',
    'no-unexpected-multiline': 'error',
    'quotes': [ 'error', 'single', { 'allowTemplateLiterals': true, 'avoidEscape': true } ],
    'prefer-named-capture-group': 'off', // not needed
    'indent': [ 'error', 2 ],
    'object-curly-spacing': [ 'error', 'always' ],
    'array-bracket-spacing': [ 'error', 'always' ],
    'curly': 'error',
    'template-curly-spacing': 'error',
    'brace-style': [ 'error', '1tbs', { 'allowSingleLine': true } ]
  }
}
