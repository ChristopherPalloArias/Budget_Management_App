import { ApiAuthRepository } from '@/infrastructure/auth/ApiAuthRepository';
import type { IAuthRepository } from '@/core/auth/interfaces/IAuthRepository';

export const authRepository: IAuthRepository = new ApiAuthRepository();