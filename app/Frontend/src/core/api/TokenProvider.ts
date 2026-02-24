export interface TokenProvider {
    getToken(): string | null;
}

export class LocalStorageTokenProvider implements TokenProvider {
    getToken(): string | null {
        return localStorage.getItem('auth_token');
    }
}
