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

// Mock import.meta.env for Vite
(global as any).import = {
  meta: {
    env: {
      VITE_API_AUTH_URL: 'http://localhost:8081/api/v1/auth',
      VITE_API_TRANSACTIONS_URL: 'http://localhost:8082/api/v1/transactions',
      VITE_API_REPORTS_URL: 'http://localhost:8083/api/v1/reports',
      MODE: 'test',
      DEV: true,
      PROD: false,
      SSR: false,
    },
  },
};
