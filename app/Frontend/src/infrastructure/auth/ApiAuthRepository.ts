import HttpClient from '@/core/api/HttpClient';
import { API_ENDPOINTS } from '@/core/constants/app.constants';
import type {
  IAuthRepository,
  IAuthUser,
  IAuthCredentials,
  IRegisterCredentials,
  AuthProvider,
  Unsubscribe,
} from '@/core/auth/interfaces';

const AUTH_TOKEN_KEY = 'auth_token';

/**
 * API response types matching the backend Auth microservice DTOs.
 */
interface AuthApiResponse {
  userId: string;
  email: string;
  displayName: string;
  token: string;
}

interface UserApiResponse {
  userId: string;
  email: string;
  displayName: string;
  photoURL: string | null;
}

interface ApiErrorResponse {
  dateTime: string;
  message: string;
  path: string;
}

/**
 * Implementación del repositorio de autenticación que consume la API REST
 * del microservicio auth (Spring Boot + JWT).
 *
 * Cumple con la interfaz IAuthRepository para que el swap sea transparente
 * para el resto de la aplicación.
 *
 * El JWT devuelto por el backend se persiste en localStorage bajo la clave
 * 'auth_token'. La verificación de sesión al recargar la página se hace
 * consultando GET /api/v1/auth/me con el token almacenado.
 */
export class ApiAuthRepository implements IAuthRepository {

  private get httpClient() {
    return HttpClient.getInstance('auth');
  }

  /**
   * Mapea la respuesta de la API auth a la interfaz IAuthUser usada
   * internamente por el frontend.
   */
  private mapApiResponse(apiResponse: AuthApiResponse): IAuthUser {
    return {
      id: apiResponse.userId,
      email: apiResponse.email,
      displayName: apiResponse.displayName,
      photoURL: null,
    };
  }

  /**
   * Mapea la respuesta de GET /me a IAuthUser.
   */
  private mapUserResponse(apiResponse: UserApiResponse): IAuthUser {
    return {
      id: apiResponse.userId,
      email: apiResponse.email,
      displayName: apiResponse.displayName,
      photoURL: apiResponse.photoURL,
    };
  }

  /**
   * Extrae un mensaje de error legible de la respuesta del backend.
   */
  private handleApiError(error: unknown): never {
    if (error && typeof error === 'object' && 'response' in error) {
      const axiosError = error as { response?: { data?: ApiErrorResponse; status?: number } };
      const data = axiosError.response?.data;

      if (data?.message) {
        throw new Error(data.message);
      }

      const status = axiosError.response?.status;
      const statusMessages: Record<number, string> = {
        401: 'Credenciales incorrectas',
        409: 'El correo electrónico ya está registrado',
        400: 'Datos inválidos. Verifica la información ingresada',
        500: 'Error en el servidor. Intenta más tarde',
        503: 'Servicio no disponible. Intenta más tarde',
      };

      if (status && statusMessages[status]) {
        throw new Error(statusMessages[status]);
      }
    }

    if (error && typeof error === 'object' && 'message' in error) {
      throw new Error((error as { message: string }).message);
    }

    throw new Error('Error de autenticación desconocido');
  }

  /**
   * Persiste el token JWT en localStorage.
   */
  private saveToken(token: string): void {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
  }

  /**
   * Elimina el token JWT de localStorage.
   */
  private removeToken(): void {
    localStorage.removeItem(AUTH_TOKEN_KEY);
  }

  /**
   * Obtiene el token JWT almacenado, o null si no existe.
   */
  static getToken(): string | null {
    return localStorage.getItem(AUTH_TOKEN_KEY);
  }

  /**
   * Login con email y contraseña.
   * Llama a POST /api/v1/auth/login, guarda el JWT retornado y devuelve el usuario.
   */
  async signIn(credentials: IAuthCredentials): Promise<IAuthUser> {
    try {
      const response = await this.httpClient.post<AuthApiResponse>(
        `${API_ENDPOINTS.AUTH}/login`,
        {
          email: credentials.email,
          password: credentials.password,
        }
      );

      this.saveToken(response.data.token);
      return this.mapApiResponse(response.data);
    } catch (error) {
      this.handleApiError(error);
    }
  }

  /**
   * Login con proveedor externo (Google, etc).
   * NO SOPORTADO en Phase 1 — lanza error informativo.
   */
  async signInWithProvider(_provider: AuthProvider): Promise<IAuthUser> {
    throw new Error(
      'Inicio de sesión con Google no disponible temporalmente. Use email y contraseña.'
    );
  }

  /**
   * Cierra la sesión del usuario.
   * Elimina el token de localStorage y limpia el estado.
   */
  async signOut(): Promise<void> {
    try {
      const token = ApiAuthRepository.getToken();
      if (token) {
        // Best-effort call to backend logout endpoint
        await this.httpClient.post(`${API_ENDPOINTS.AUTH}/logout`, null, {
          headers: { Authorization: `Bearer ${token}` },
        }).catch(() => { /* ignore errors on logout */ });
      }
    } finally {
      this.removeToken();
    }
  }

  /**
   * Registra un nuevo usuario.
   * Llama a POST /api/v1/auth/register, guarda el JWT retornado y devuelve el usuario.
   */
  async register(credentials: IRegisterCredentials): Promise<IAuthUser> {
    try {
      const response = await this.httpClient.post<AuthApiResponse>(
        `${API_ENDPOINTS.AUTH}/register`,
        {
          displayName: credentials.displayName,
          email: credentials.email,
          password: credentials.password,
        }
      );

      return this.mapApiResponse(response.data);
    } catch (error) {
      this.handleApiError(error);
    }
  }

  /**
   * Verifica el estado de autenticación al iniciar la aplicación.
   *
   * A diferencia de un listener reactivo vía WebSocket,
   * esta implementación:
   * 1. Lee el token JWT de localStorage.
   * 2. Si existe, llama a GET /api/v1/auth/me para validar que el token siga vigente.
   * 3. Invoca el callback con el usuario (si el token es válido) o null (si no hay token o expiró).
   * 4. Retorna una función noop como Unsubscribe (no hay listener activo que limpiar).
   */
  onAuthStateChanged(callback: (user: IAuthUser | null) => void): Unsubscribe {
    const token = ApiAuthRepository.getToken();

    if (!token) {
      // No hay token — el usuario no está autenticado
      callback(null);
      return () => { };
    }

    // Verificar el token con el backend
    this.httpClient
      .get<UserApiResponse>(`${API_ENDPOINTS.AUTH}/me`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        callback(this.mapUserResponse(response.data));
      })
      .catch(() => {
        // Token inválido o expirado — limpiar y reportar no autenticado
        this.removeToken();
        callback(null);
      });

    // No hay un listener real que desuscribir — retorna noop
    return () => { };
  }
}
