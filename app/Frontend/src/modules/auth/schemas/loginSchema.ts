import { z } from 'zod';

/**
 * Login Form Validation Schema
 */
export const loginSchema = z.object({
    email: z
        .string()
        .min(1, 'El correo es requerido')
        .email('Formato de correo inválido'),

    password: z
        .string()
        .min(6, 'La contraseña debe tener al menos 6 caracteres')
        .max(100, 'La contraseña es demasiado larga'),
});

export type LoginFormData = z.infer<typeof loginSchema>;
