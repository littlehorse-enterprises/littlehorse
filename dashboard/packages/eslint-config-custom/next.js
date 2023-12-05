const {
    resolve
} = require('node:path')

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
        '@vercel/style-guide/eslint/typescript',
        '@vercel/style-guide/eslint/react',
    ].map(require.resolve),
    plugins: [
        '@stylistic'
    ],
    parserOptions: {
        project,
        sourceType: 'module',
        ecmaVersion: 2015
    },
    globals: {
        React: true,
        JSX: true
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
        'import/no-default-export': 'off', // turned off intentionally, we like to have default exports
        '@typescript-eslint/restrict-template-expressions': 'off', // turning this rule off, as we don't need it
        'unicorn/filename-case': 'off', // turned off as we like the CamelCase naming convention for files
        '@typescript-eslint/no-extraneous-class': 'off', // check if we have static field on classes, for now we don't need this check as we are using typescript and we need static attrs.
        'jsx-a11y/click-events-have-key-events': 'off', // needs to be addressed in: https://littlehorse.atlassian.net/browse/LH-224
        'jsx-a11y/alt-text': 'off', // needs to be addressed in: https://littlehorse.atlassian.net/browse/LH-224
        'jsx-a11y/no-static-element-interactions': 'off', // needs to be addressed in: https://littlehorse.atlassian.net/browse/LH-224
        'jsx-a11y/no-noninteractive-element-interactions': 'off', // needs to be addressed in: https://littlehorse.atlassian.net/browse/LH-224
        '@typescript-eslint/no-unnecessary-condition': 'off', // ignored because it has issues with the key-signature we have for objects in our protos
        '@typescript-eslint/no-base-to-string': 'off', // turned off as we don't need it
        '@typescript-eslint/no-misused-promises': 'off', // turned off as the violations we have are mainly on jsx event handlers which is acceptable
        'react-hooks/exhaustive-deps': 'off', // a lot of warnings, ticket created to analyze case by case if needed -- https://littlehorse.atlassian.net/browse/LH-235
        'react/button-has-type': 'off', // not needed as we are using only submit buttons
        // '@next/next/no-img-element': 'off', // aparently using Next JS Imge requires extra storage on the server, disabling until a decision is taken
        'react/hook-use-state': 'error',
        'react/no-array-index-key': 'error',
        'react-hooks/rules-of-hooks': 'error',
        // '@next/next/no-html-link-for-pages': 'error',
        // '@next/next/no-head-element': 'error',
        '@typescript-eslint/no-explicit-any': 'off', // https://littlehorse.atlassian.net/browse/LH-233
        '@typescript-eslint/explicit-function-return-type': 'off', //https://littlehorse.atlassian.net/browse/LH-232
        '@typescript-eslint/no-unsafe-assignment': 'off', // https://littlehorse.atlassian.net/browse/LH-231
        '@typescript-eslint/no-unsafe-call': 'off', // https://littlehorse.atlassian.net/browse/LH-230
        '@typescript-eslint/no-unsafe-member-access': 'off', // https://littlehorse.atlassian.net/browse/LH-229
        '@typescript-eslint/no-unsafe-argument': 'off', // https://littlehorse.atlassian.net/browse/LH-228
        '@typescript-eslint/no-unsafe-return': 'off', // https://littlehorse.atlassian.net/browse/LH-225
        '@typescript-eslint/no-floating-promises': 'off', // https://littlehorse.atlassian.net/browse/LH-227
        // starting here, we have our formatting rules
        '@stylistic/keyword-spacing': 'error',
        '@stylistic/semi': [ 'error', 'never' ],
        '@stylistic/quotes': [ 'error', 'single', {
            'allowTemplateLiterals': true, 'avoidEscape': true
        } ],
        '@stylistic/prefer-named-capture-group': 'off', // not needed
        '@stylistic/indent': [ 'error', 4 ],
        '@stylistic/object-curly-spacing': [ 'error', 'always' ],
        '@stylistic/jsx-curly-spacing': 'error',
        '@stylistic/array-bracket-spacing': [ 'error', 'always' ],
        // '@stylistic/jsx-indent': [ 'error', 4 ],
        '@stylistic/template-curly-spacing': 'error',
        '@stylistic/brace-style': [ 'error', '1tbs', {
            'allowSingleLine': true
        } ],
        'curly': 'error',
        'no-unreachable': 'error',
        'no-unexpected-multiline': 'error'
    }
}
