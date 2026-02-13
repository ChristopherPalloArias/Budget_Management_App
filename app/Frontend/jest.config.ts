import type { Config } from "jest";

const config: Config = {
  preset: "ts-jest",
  testEnvironment: "jsdom",
  rootDir: ".",

  // Setup files
  setupFilesAfterEnv: ["<rootDir>/jest.setup.ts"],

  // Module paths
  moduleNameMapper: {
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

  // Transform files
  transform: {
    "^.+\\.tsx?$": [
      "ts-jest",
      {
        tsconfig: {
          jsx: "react-jsx",
          esModuleInterop: true,
          allowSyntheticDefaultImports: true,
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
  testPathIgnorePatterns: ["/node_modules/", "/dist/"],

  // Globals
  globals: {
    "ts-jest": {
      isolatedModules: true,
    },
  },
};

export default config;
