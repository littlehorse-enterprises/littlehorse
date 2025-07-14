import js from '@eslint/js'
import autofix from 'eslint-plugin-autofix'
import reactPlugin from 'eslint-plugin-react'
import unusedImports from 'eslint-plugin-unused-imports'
import globals from 'globals'
import tseslint from 'typescript-eslint'

export default tseslint.config(
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    plugins: {
      react: reactPlugin,
      'unused-imports': unusedImports,
      autofix: autofix,
    },
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
      },
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      // Unused imports rules
      'unused-imports/no-unused-imports': 'error',
      // Disable the regular unused-vars rule in favor of autofix version
      'unused-imports/no-unused-vars': 'off',
      // Autofix rules for unused variables
      'autofix/no-unused-vars': [
        'error',
        {
          vars: 'all',
          args: 'all',
          ignoreRestSiblings: false,
          destructuredArrayIgnorePattern: '^_',
          caughtErrors: 'all',
          varsIgnorePattern: '^_',
          argsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
      // React rules
      'react/react-in-jsx-scope': 'off', // Not needed with Next.js
      'react/prop-types': 'off', // Using TypeScript
      // TypeScript rules
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': 'off', // Using autofix instead
    },
    settings: {
      react: {
        version: 'detect',
      },
    },
  },
  {
    files: ['**/*.{ts,tsx}'],
    rules: {
      // TypeScript specific rules
      '@typescript-eslint/no-unused-vars': 'off', // Using autofix instead
    },
  },
  {
    ignores: ['.next/**', 'node_modules/**', 'dist/**', 'build/**', '*.config.js', '*.config.ts'],
  }
)
