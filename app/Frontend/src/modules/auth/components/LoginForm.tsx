import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { loginSchema, type LoginFormData } from '../schemas/loginSchema';
import { useLoginForm } from '../hooks/useLoginForm';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export const LoginForm = () => {
  const { state, login } = useLoginForm();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginFormData) => {
    login(data);
  };

  const isLoading = state.isLoading;

  return (
    <div className="w-full max-w-md space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="space-y-2">
        <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Bienvenido de nuevo
        </h2>
        <p className="text-slate-500 dark:text-slate-400 font-medium">
          Ingresa tus credenciales para acceder a tu panel.
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
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

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <Label htmlFor="password" title="Contraseña" className="text-sm font-semibold text-slate-700 dark:text-slate-300 ml-1">
              Contraseña
            </Label>
          </div>
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

        <Button
          type="submit"
          className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-4 h-auto rounded-xl transition-all duration-300 shadow-lg shadow-emerald-500/25 active:scale-[0.98] disabled:opacity-70"
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center justify-center gap-2">
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-white border-t-transparent" />
              <span>Iniciando sesión...</span>
            </div>
          ) : (
            'Entrar a la plataforma'
          )}
        </Button>
      </form>

      <div className="pt-4 text-center">
        <p className="text-sm text-slate-600 dark:text-slate-400">
          ¿Aún no tienes una cuenta?{' '}
          <Link to="/register" className="text-emerald-600 dark:text-emerald-500 hover:text-emerald-400 font-bold transition-colors">
            Crea una cuenta gratuita
          </Link>
        </p>
      </div>
    </div>
  );
};

