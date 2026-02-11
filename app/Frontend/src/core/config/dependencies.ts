import { FirebaseAuthRepository } from '@/infrastructure/auth/FirebaseAuthRepository';
import type { IAuthRepository } from '@/core/auth/interfaces/IAuthRepository';

export const authRepository: IAuthRepository = new FirebaseAuthRepository();