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

// Mock Sonner toast to avoid DOM dependencies in tests
jest.mock("sonner", () => ({
  __esModule: true,
  toast: {
    success: jest.fn(),
    error: jest.fn(),
    message: jest.fn(),
    warning: jest.fn(),
  },
  Toaster: () => null,
}));