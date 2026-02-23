import type { Config } from "jest";

const config: Config = {
  preset: "ts-jest",
  testEnvironment: "jsdom",
  rootDir: ".",

  // Setup files
  setupFilesAfterEnv: ["<rootDir>/jest.setup.ts"],

  // Module paths
  moduleNameMapper: {
    "^@/core/config/firebase\\.config$":
      "<rootDir>/src/core/config/__mocks__/firebase.config.ts",
    "^@/core/config/dependencies$":
      "<rootDir>/src/core/config/__mocks__/dependencies.ts",
    "^firebase/auth$": "<rootDir>/src/test/__mocks__/firebase/auth.ts",
    "^firebase/app$": "<rootDir>/src/test/__mocks__/firebase/app.ts",
    "^@/(.*)$": "<rootDir>/src/$1",
    "\\.(css|less|scss|sass)$": "identity-obj-proxy",
    "\\.(jpg|jpeg|png|gif|svg|webp)$":
      "<rootDir>/src/test/__mocks__/fileMock.ts",
  },

  // Test patterns
  testMatch: [
    "<rootDir>/src/**/__tests__/**/*.{ts,tsx}",
    "<rootDir>/src/**/*.{spec,test}.{ts,tsx}",
  ],

  // Ignore TypeScript .ts extensions in imports
  testPathIgnorePatterns: ["/node_modules/", "/dist/"],
  modulePathIgnorePatterns: [],

  // Transform files
  transform: {
    "^.+\\.tsx?$": [
      "ts-jest",
      {
        tsconfig: {
          jsx: "react-jsx",
          esModuleInterop: true,
          allowSyntheticDefaultImports: true,
          module: "esnext",
          moduleResolution: "bundler",
          skipLibCheck: true,
          isolatedModules: true,
        },
        diagnostics: {
          ignoreCodes: [5097, 1343, 2339],
        },
      },
    ],
  },

  // Coverage configuration
  collectCoverageFrom: [
    "src/**/*.{ts,tsx}",
    "!src/**/*.d.ts",
    "!src/main.tsx",
    "!src/vite-env.d.ts",
    "!src/**/*.stories.{ts,tsx}",
    "!src/test/**",
  ],

  coverageDirectory: "coverage",
  coverageReporters: ["text", "lcov", "html", "json-summary"],

  // Performance
  maxWorkers: "50%",

  // Module file extensions
  moduleFileExtensions: ["ts", "tsx", "js", "jsx", "json"],

  // Ignore patterns
  // testPathIgnorePatterns: ["/node_modules/", "/dist/"],
};

export default config;
