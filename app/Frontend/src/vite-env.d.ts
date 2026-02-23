/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_API_TRANSACTIONS_URL: string
    readonly VITE_API_REPORTS_URL: string
    readonly VITE_API_AUTH_URL: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}

