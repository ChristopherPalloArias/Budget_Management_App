import "@testing-library/jest-dom";
import { TextEncoder, TextDecoder } from "util";

// Polyfill for TextEncoder/TextDecoder
global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder as any;

// Polyfill for fetch
const mockFetch = () =>
  Promise.resolve({
    ok: true,
    json: async () => ({}),
    text: async () => "",
    blob: async () => new Blob(),
    headers: new Headers(),
    redirected: false,
    status: 200,
    statusText: "OK",
    type: "basic" as ResponseType,
    url: "",
    clone() {
      return this;
    },
    body: null,
    bodyUsed: false,
    arrayBuffer: async () => new ArrayBuffer(0),
    formData: async () => new FormData(),
  } as Response);

global.fetch = mockFetch as any;

// Mock window.matchMedia
Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => { },
    removeListener: () => { },
    addEventListener: () => { },
    removeEventListener: () => { },
    dispatchEvent: () => false,
  }),
});

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor() { }
  disconnect() { }
  observe() { }
  takeRecords() {
    return [];
  }
  unobserve() { }
} as any;

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() { }
  disconnect() { }
  observe() { }
  unobserve() { }
} as any;

// Mock Element.scrollIntoView
Element.prototype.scrollIntoView = function () { };

// Mock HTMLElement.scrollTo
if (typeof HTMLElement !== "undefined") {
  HTMLElement.prototype.scrollTo = function () { };
}

// Mock pointer capture methods
if (typeof Element !== "undefined") {
  Element.prototype.hasPointerCapture = function () {
    return false;
  };
  Element.prototype.setPointerCapture = function () { };
  Element.prototype.releasePointerCapture = function () { };
}

// Mock HttpClient globally to prevent import.meta syntax errors in Jest
jest.mock("@/core/api/HttpClient", () => ({
  __esModule: true,
  default: {
    getInstance: jest.fn().mockReturnValue({
      get: jest.fn().mockResolvedValue({ data: {} }),
      post: jest.fn().mockResolvedValue({ data: {} }),
      put: jest.fn().mockResolvedValue({ data: {} }),
      delete: jest.fn().mockResolvedValue({ data: {} }),
      interceptors: {
        request: { use: jest.fn(), eject: jest.fn() },
        response: { use: jest.fn(), eject: jest.fn() },
      },
    }),
    clearInstances: jest.fn(),
  },
}));

// Polyfill for import.meta.env to avoid parse errors in HttpClient.ts
(globalThis as any).importMetaEnv = {
  VITE_API_TRANSACTIONS_URL: 'http://localhost:3000/v1/transactions',
  VITE_API_REPORTS_URL: 'http://localhost:3000/v1/reports',
  VITE_API_AUTH_URL: 'http://localhost:3000/v1/auth',
  MODE: 'test',
  DEV: true,
  PROD: false,
  SSR: false,
};

// Mock lucide-react to avoid "Element type is invalid" when icons are used but not mocked
jest.mock('lucide-react', () => {
  const React = require('react');
  const original = jest.requireActual('lucide-react');
  
  // Helper to create a mock icon component
  const createMockIcon = (name: string) => {
    const MockIcon = (props: any) => React.createElement('div', { ...props, 'data-testid': `icon-$ {name.toLowerCase()}` });
    MockIcon.displayName = name;
    return MockIcon;
  };

  // We can use a Proxy to catch all icon requests if needed, but let's stick to common ones for now
  // or just return the original if it exists, otherwise a mock.
  const icons: any = { ...original };
  const commonIcons = [
    'Home', 'TrendingUp', 'FileText', 'Moon', 'Sun', 'Laptop', 
    'LogOut', 'ChevronsUpDown', 'Bell', 'BadgeCheck', 'Sparkles', 
    'PanelLeft', 'Search', 'User', 'Settings', 'CreditCard', 'MoreHorizontal'
  ];

  commonIcons.forEach(name => {
    if (!icons[name]) {
      icons[name] = createMockIcon(name);
    }
  });

  return icons;
});
