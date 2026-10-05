import js from '@eslint/js';
import reactHooks from 'eslint-plugin-react-hooks';
import globals from 'globals';
import tseslint from 'typescript-eslint';

const BROWSER_STORAGE_MESSAGE =
  'Tokens are kept in memory only (docs/12-project-architecture.md#admin-web-client). Do not use browser storage.';

export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: { globals: globals.browser },
    plugins: { 'react-hooks': reactHooks },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'no-restricted-globals': [
        'error',
        { name: 'localStorage', message: BROWSER_STORAGE_MESSAGE },
        { name: 'sessionStorage', message: BROWSER_STORAGE_MESSAGE },
      ],
      'no-restricted-properties': [
        'error',
        { object: 'window', property: 'localStorage', message: BROWSER_STORAGE_MESSAGE },
        { object: 'window', property: 'sessionStorage', message: BROWSER_STORAGE_MESSAGE },
      ],
    },
  },
);
