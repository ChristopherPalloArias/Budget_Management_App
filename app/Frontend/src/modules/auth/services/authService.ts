import {
    signInWithEmailAndPassword,
    signInWithPopup,
    GoogleAuthProvider,
    signOut,
    createUserWithEmailAndPassword,
    updateProfile,
    type UserCredential,
} from 'firebase/auth';
import { auth } from '../../../core/config/firebase.config.js';

/**
 * Login with Email and Password
 */
export const loginWithEmail = async (
    email: string,
    password: string
): Promise<UserCredential> => {
    try {
        const userCredential = await signInWithEmailAndPassword(auth, email, password);
        console.log('[Auth Service] Login successful:', userCredential.user.email);
        return userCredential;
    } catch (error: any) {
        console.error('[Auth Service] Login error:', error.code, error.message);
        throw mapFirebaseError(error);
    }
};

/**
 * Login with Google
 */
export const loginWithGoogle = async (): Promise<UserCredential> => {
    try {
        const provider = new GoogleAuthProvider();
        const userCredential = await signInWithPopup(auth, provider);
        console.log('[Auth Service] Google login successful:', userCredential.user.email);
        return userCredential;
    } catch (error: any) {
        console.error('[Auth Service] Google login error:', error.code, error.message);
        throw mapFirebaseError(error);
    }
};

/**
 * Register with Email and Password
 */
export const registerWithEmail = async (
    displayName: string,
    email: string,
    password: string
): Promise<UserCredential> => {
    try {
        const userCredential = await createUserWithEmailAndPassword(auth, email, password);

        await updateProfile(userCredential.user, {
            displayName,
        });

        console.log('[Auth Service] Registration successful:', userCredential.user.email);
        return userCredential;
    } catch (error: any) {
        console.error('[Auth Service] Registration error:', error.code, error.message);
        throw mapFirebaseError(error);
    }
};

/**
 * Logout
 */
export const logout = async (): Promise<void> => {
    try {
        await signOut(auth);
        console.log('[Auth Service] Logout successful');
    } catch (error: any) {
        console.error('[Auth Service] Logout error:', error.code, error.message);
        throw new Error('Error al cerrar sesión');
    }
};

/**
 * Map Firebase errors to user-friendly messages
 */
const mapFirebaseError = (error: any): Error => {
    const errorCode = error.code;

    const errorMessages: Record<string, string> = {
        'auth/invalid-email': 'El correo electrónico no es válido',
        'auth/user-disabled': 'Esta cuenta ha sido deshabilitada',
        'auth/user-not-found': 'No existe una cuenta con este correo',
        'auth/wrong-password': 'Contraseña incorrecta',
        'auth/invalid-credential': 'Credenciales incorrectas',
        'auth/too-many-requests': 'Demasiados intentos. Intenta más tarde',
        'auth/network-request-failed': 'Error de conexión. Verifica tu internet',
        'auth/popup-closed-by-user': 'Inicio de sesión cancelado',
        'auth/cancelled-popup-request': 'Inicio de sesión cancelado',
        'auth/email-already-in-use': 'El correo electrónico ya está en uso',
        'auth/weak-password': 'La contraseña debe tener al menos 6 caracteres',
        'auth/operation-not-allowed': 'El registro con correo y contraseña está deshabilitado',
    };

    const message = errorMessages[errorCode] || 'Error de autenticación. Intenta de nuevo.';
    return new Error(message);
};
