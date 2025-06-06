# Props Interface Naming Convention ESLint Rule

This project enforces a consistent naming convention for React component prop interfaces using a custom ESLint rule.

## Convention

All React component prop interfaces must be named using the pattern: `{ComponentName}Props`

## Examples

### ✅ Correct Usage

```tsx
interface WfSpecTabProps {
  wfSpec: WfSpec;
  wfRun?: WfRun;
}

export default function WfSpecTab({ wfSpec, wfRun }: WfSpecTabProps) {
  // component implementation
}
```

```tsx
interface UserCardProps {
  user: User;
  showAvatar?: boolean;
}

function UserCard({ user, showAvatar = true }: UserCardProps) {
  // component implementation
}
```

### ❌ Incorrect Usage

```tsx
// ❌ Interface name doesn't match component name
interface BadNaming {
  title: string;
}

function MyComponent({ title }: BadNaming) {
  return <h1>{title}</h1>;
}
```

```tsx
// ❌ Inline type instead of proper interface
function MyComponent({ title }: { title: string }) {
  return <h1>{title}</h1>;
}
```

```tsx
// ❌ Missing type annotation
function MyComponent({ title }) {
  return <h1>{title}</h1>;
}
```

## ESLint Rule Configuration

The rule is configured in `eslint.config.mjs`:

```javascript
rules: {
  "custom/enforce-props-interface-naming": "error",
}
```

## Error Messages

The rule provides helpful error messages:

1. **Incorrect naming**: "Props interface should be named 'ComponentNameProps' to match the component name 'ComponentName'"
2. **Missing interface**: "React component 'ComponentName' should have a props interface named 'ComponentNameProps'"

## Benefits

- **Consistency**: All prop interfaces follow the same naming pattern
- **Readability**: Easy to identify which interface belongs to which component
- **Maintainability**: Reduces confusion when working with multiple components
- **Team alignment**: Enforces coding standards across the entire team

## Disabling the Rule

If you need to disable this rule for a specific file or line, you can use ESLint disable comments:

```tsx
/* eslint-disable custom/enforce-props-interface-naming */
// Your code here
/* eslint-enable custom/enforce-props-interface-naming */
```

Or for a single line:

```tsx
// eslint-disable-next-line custom/enforce-props-interface-naming
interface SomeSpecialCase {
  // ...
}
```

However, it's recommended to follow the convention unless there's a compelling reason not to.
