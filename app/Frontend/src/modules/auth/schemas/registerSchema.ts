import { z } from 'zod';

/**
 * Register Form Validation Schema
 */
export const registerSchema = z
    .object({
        displayName: z
            .string()
            .min(3, 'El nombre debe tener al menos 3 caracteres')
            .max(50, 'El nombre es demasiado largo')
            .regex(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/, 'El nombre solo puede contener letras y espacios'),

        email: z
            .string()
            .min(1, 'El correo es requerido')
            .email('Formato de correo inválido'),

        password: z
            .string()
            .min(8, 'La contraseña debe tener al menos 8 caracteres')
            .max(100, 'La contraseña es demasiado larga')
            .regex(/[A-Z]/, 'La contraseña debe tener al menos una mayúscula')
            .regex(/[a-z]/, 'La contraseña debe tener al menos una minúscula')
            .regex(/[0-9]/, 'La contraseña debe tener al menos un número'),

        confirmPassword: z
            .string()
            .min(1, 'La confirmación de contraseña es requerida'),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: 'Las contraseñas no coinciden',
        path: ['confirmPassword'],
    });

export type RegisterFormData = z.infer<typeof registerSchema>;