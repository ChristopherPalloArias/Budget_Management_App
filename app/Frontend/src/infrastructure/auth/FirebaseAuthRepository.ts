import {
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  createUserWithEmailAndPassword,
  updateProfile,
  onAuthStateChanged,
  GoogleAuthProvider,
  EmailAuthProvider,
  type User as FirebaseUser,
} from 'firebase/auth';

import { auth } from '@/core/config/firebase.config';
import type {
  IAuthRepository,
  IAuthUser,
  IAuthCredentials,
  IRegisterCredentials,
  AuthProvider,
  Unsubscribe,
} from '@/core/auth/interfaces';

export class FirebaseAuthRepository implements IAuthRepository {
  private mapFirebaseUser(firebaseUser: FirebaseUser): IAuthUser {
    return {
      id: firebaseUser.uid,
      email: firebaseUser.email,
      displayName: firebaseUser.displayName,
      photoURL: firebaseUser.photoURL,
    };
  }

  private handleFirebaseError(error: unknown): never {
    if (error instanceof Error && 'code' in error) {
      const firebaseError = error as { code: string; message: string };
      
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

      const message = errorMessages[firebaseError.code] || 'Error de autenticación. Intenta de nuevo.';
      throw new Error(message);
    }

    throw new Error('Error de autenticación desconocido');
  }

  private getAuthProvider(provider: AuthProvider) {
    switch (provider) {
      case 'GOOGLE':
        return new GoogleAuthProvider();
      case 'EMAIL':
        return new EmailAuthProvider();
      default:
        throw new Error(`Proveedor de autenticación no soportado: ${provider}`);
    }
  }

  async signIn(credentials: IAuthCredentials): Promise<IAuthUser> {
    try {
      const userCredential = await signInWithEmailAndPassword(
        auth,
        credentials.email,
        credentials.password
      );
      return this.mapFirebaseUser(userCredential.user);
    } catch (error) {
      this.handleFirebaseError(error);
    }
  }

  async signInWithProvider(provider: AuthProvider): Promise<IAuthUser> {
    try {
      const authProvider = this.getAuthProvider(provider);
      const userCredential = await signInWithPopup(auth, authProvider);
      return this.mapFirebaseUser(userCredential.user);
    } catch (error) {
      this.handleFirebaseError(error);
    }
  }

  async signOut(): Promise<void> {
    try {
      await signOut(auth);
    } catch (error) {
      this.handleFirebaseError(error);
    }
  }

  async register(credentials: IRegisterCredentials): Promise<IAuthUser> {
    try {
      const userCredential = await createUserWithEmailAndPassword(
        auth,
        credentials.email,
        credentials.password
      );

      await updateProfile(userCredential.user, {
        displayName: credentials.displayName,
      });

      return this.mapFirebaseUser(userCredential.user);
    } catch (error) {
      this.handleFirebaseError(error);
    }
  }

  onAuthStateChanged(callback: (user: IAuthUser | null) => void): Unsubscribe {
    return onAuthStateChanged(auth, (firebaseUser) => {
      if (firebaseUser) {
        callback(this.mapFirebaseUser(firebaseUser));
      } else {
        callback(null);
      }
    });
  }
}