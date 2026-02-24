import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { registerSchema, type RegisterFormData } from '../schemas/registerSchema';
import { useRegisterForm } from '../hooks/useRegisterForm';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

/**
 * RegisterForm Component
 * Handles user registration with email/password
 */
export const RegisterForm = () => {
    const { state, registerUser } = useRegisterForm();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
    });

    const onSubmit = async (data: RegisterFormData) => {
        await registerUser(data);
    };

    const isLoading = state.isLoading;

    return (
        <div className="w-full max-w-md space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
            <div className="space-y-2">
                <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
                    Crea tu cuenta
                </h2>
                <p className="text-slate-500 dark:text-slate-400 font-medium">
                    Únete para empezar a gestionar tu presupuesto.
                </p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                {/* Display Name Field */}
                <div className="space-y-2">
                    <Label htmlFor="displayName" className="text-sm font-semibold text-slate-700 dark:text-slate-300 ml-1">
                        Nombre Completo
                    </Label>
                    <Input
                        id="displayName"
                        type="text"
                        placeholder="Juan Pérez"
                        {...register('displayName')}
                        disabled={isLoading}
                        className={`w-full bg-transparent border-slate-200 dark:border-slate-800 rounded-xl p-6 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all placeholder:text-slate-400 dark:text-white ${errors.displayName ? 'border-red-500 ring-red-500/10' : ''
                            }`}
                    />
                    {errors.displayName && (
                        <p className="text-xs text-red-500 font-medium ml-1">{errors.displayName.message}</p>
                    )}
                </div>

                {/* Email Field */}
                <div className="space-y-2">
                    <Label htmlFor="email" className="text-sm font-semibold text-slate-700 dark:text-slate-300 ml-1">
                        Correo Electrónico
                    </Label>
                    <Input
                        id="email"
                        type="email"
                        placeholder="nombre@empresa.com"
                        {...register('email')}
                        disabled={isLoading}
                        className={`w-full bg-transparent border-slate-200 dark:border-slate-800 rounded-xl p-6 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all placeholder:text-slate-400 dark:text-white ${errors.email ? 'border-red-500 ring-red-500/10' : ''
                            }`}
                    />
                    {errors.email && (
                        <p className="text-xs text-red-500 font-medium ml-1">{errors.email.message}</p>
                    )}
                </div>

                {/* Password Field */}
                <div className="space-y-2">
                    <Label htmlFor="password" title="Contraseña" className="text-sm font-semibold text-slate-700 dark:text-slate-300 ml-1">
                        Contraseña
                    </Label>
                    <Input
                        id="password"
                        type="password"
                        placeholder="••••••••"
                        {...register('password')}
                        disabled={isLoading}
                        className={`w-full bg-transparent border-slate-200 dark:border-slate-800 rounded-xl p-6 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all placeholder:text-slate-400 dark:text-white ${errors.password ? 'border-red-500 ring-red-500/10' : ''
                            }`}
                    />
                    {errors.password && (
                        <p className="text-xs text-red-500 font-medium ml-1">{errors.password.message}</p>
                    )}
                </div>

                {/* Confirm Password Field */}
                <div className="space-y-2">
                    <Label htmlFor="confirmPassword" title="Confirmar Contraseña" className="text-sm font-semibold text-slate-700 dark:text-slate-300 ml-1">
                        Confirmar Contraseña
                    </Label>
                    <Input
                        id="confirmPassword"
                        type="password"
                        placeholder="••••••••"
                        {...register('confirmPassword')}
                        disabled={isLoading}
                        className={`w-full bg-transparent border-slate-200 dark:border-slate-800 rounded-xl p-6 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all placeholder:text-slate-400 dark:text-white ${errors.confirmPassword ? 'border-red-500 ring-red-500/10' : ''
                            }`}
                    />
                    {errors.confirmPassword && (
                        <p className="text-xs text-red-500 font-medium ml-1">{errors.confirmPassword.message}</p>
                    )}
                </div>

                {/* Register Button */}
                <Button
                    type="submit"
                    className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-4 h-auto rounded-xl transition-all duration-300 shadow-lg shadow-emerald-500/25 active:scale-[0.98] mt-2"
                    disabled={isLoading}
                >
                    {isLoading ? (
                        <div className="flex items-center justify-center gap-2">
                            <div className="h-5 w-5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                            <span>Creando cuenta...</span>
                        </div>
                    ) : (
                        'Comenzar Ahora'
                    )}
                </Button>
            </form>

            <div className="pt-4 text-center">
                <p className="text-sm text-slate-600 dark:text-slate-400">
                    ¿Ya tienes una cuenta?{' '}
                    <Link to="/login" className="text-emerald-600 dark:text-emerald-500 hover:text-emerald-400 font-bold transition-colors">
                        Inicia sesión aquí
                    </Link>
                </p>
            </div>
        </div>
    );
};