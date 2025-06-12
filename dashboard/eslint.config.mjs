import { FlatCompat } from '@eslint/eslintrc'
import { dirname } from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const compat = new FlatCompat({
  baseDirectory: __dirname,
})

/**
 * Custom ESLint rule to enforce React component prop interface naming convention.
 * Ensures that prop interfaces are named {ComponentName}Props.
 */
const enforcePropsInterfaceNamingRule = {
  'enforce-props-interface-naming': {
    meta: {
      type: 'problem',
      docs: {
        description: 'Enforce that React component prop interfaces follow the naming convention {ComponentName}Props',
        category: 'Best Practices',
      },
      messages: {
        incorrectNaming:
          "Props interface should be named '{{expectedName}}' to match the component name '{{componentName}}'",
        missingPropsInterface:
          "React component '{{componentName}}' should have a props interface named '{{expectedName}}'",
      },
    },
    create(context) {
      const sourceCode = context.getSourceCode()
      const interfaces = new Map()
      const components = new Map()

      return {
        // Collect all interface declarations
        TSInterfaceDeclaration(node) {
          interfaces.set(node.id.name, node)
        },

        // Collect all function declarations that look like React components
        FunctionDeclaration(node) {
          if (node.id && isReactComponent(node)) {
            const componentName = node.id.name
            const expectedPropsName = `${componentName}Props`
            components.set(componentName, { node, expectedPropsName })
          }
        },

        // Collect all variable declarations that are React components
        VariableDeclarator(node) {
          if (
            node.id.type === 'Identifier' &&
            node.init &&
            (node.init.type === 'ArrowFunctionExpression' || node.init.type === 'FunctionExpression') &&
            isReactComponent({ params: node.init.params, body: node.init.body })
          ) {
            const componentName = node.id.name
            const expectedPropsName = `${componentName}Props`
            components.set(componentName, { node, expectedPropsName })
          }
        },

        // Check export default function components
        ExportDefaultDeclaration(node) {
          if (
            node.declaration.type === 'FunctionDeclaration' &&
            node.declaration.id &&
            isReactComponent(node.declaration)
          ) {
            const componentName = node.declaration.id.name
            const expectedPropsName = `${componentName}Props`
            components.set(componentName, {
              node: node.declaration,
              expectedPropsName,
            })
          }
        },

        // At the end, validate all components
        'Program:exit'() {
          for (const [componentName, { node, expectedPropsName }] of components) {
            const params = node.params || (node.init && node.init.params)

            if (params && params.length > 0) {
              const firstParam = params[0]

              // Check if the first parameter has a type annotation
              if (firstParam.typeAnnotation) {
                const typeAnnotation = firstParam.typeAnnotation.typeAnnotation

                if (typeAnnotation.type === 'TSTypeReference' && typeAnnotation.typeName.type === 'Identifier') {
                  const actualPropsName = typeAnnotation.typeName.name

                  // Check if the interface exists and has the correct name
                  if (interfaces.has(actualPropsName)) {
                    if (actualPropsName !== expectedPropsName) {
                      context.report({
                        node: interfaces.get(actualPropsName),
                        messageId: 'incorrectNaming',
                        data: {
                          expectedName: expectedPropsName,
                          componentName: componentName,
                        },
                      })
                    }
                  }
                } else if (typeAnnotation.type === 'TSTypeLiteral') {
                  // Inline type - suggest creating a proper interface
                  context.report({
                    node: firstParam,
                    messageId: 'missingPropsInterface',
                    data: {
                      componentName: componentName,
                      expectedName: expectedPropsName,
                    },
                  })
                }
              } else if (firstParam.type === 'ObjectPattern') {
                // Destructured props without type annotation
                context.report({
                  node: firstParam,
                  messageId: 'missingPropsInterface',
                  data: {
                    componentName: componentName,
                    expectedName: expectedPropsName,
                  },
                })
              }
            }
          }
        },
      }

      function isReactComponent(node) {
        // Check if it's likely a React component based on:
        // 1. Function name starts with uppercase
        // 2. Has JSX in the body (simplified check)
        const name = node.id?.name || node.key?.name
        if (!name || !name[0] || name[0] !== name[0].toUpperCase()) {
          return false
        }

        // Simple heuristic: check if the function body contains JSX-like patterns
        const bodyText = sourceCode.getText(node.body || node)
        return bodyText.includes('<') && bodyText.includes('>')
      }
    },
  },
}

/**
 * Custom ESLint rule to prevent pages from being client components.
 * Prevents the use of "use client"; directive in page.tsx and page.ts files.
 */
const noClientPagesRule = {
  'no-client-pages': {
    meta: {
      type: 'problem',
      docs: {
        description: "Prevent the use of 'use client'; directive in page.tsx and page.ts files",
        category: 'Best Practices',
      },
      messages: {
        noClientDirective: "Pages cannot be client components. Remove 'use client'; directive from page files.",
      },
    },
    create(context) {
      return {
        ExpressionStatement(node) {
          const filename = context.getFilename()
          const isPageFile = filename.endsWith('page.tsx') || filename.endsWith('page.ts')

          if (isPageFile && node.expression.type === 'Literal' && node.expression.value === 'use client') {
            context.report({
              node,
              messageId: 'noClientDirective',
            })
          }
        },
      }
    },
  },
}

const eslintConfig = [
  ...compat.extends('next/core-web-vitals', 'next/typescript'),
  {
    plugins: {
      custom: {
        rules: {
          ...enforcePropsInterfaceNamingRule,
          ...noClientPagesRule,
        },
      },
    },
    rules: {
      'custom/enforce-props-interface-naming': 'error',
      'custom/no-client-pages': 'error',
    },
  },
]

export default eslintConfig
