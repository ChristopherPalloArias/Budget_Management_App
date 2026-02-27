import { RegisterForm } from '../components/RegisterForm';
import { Wallet } from 'lucide-react';

export const RegisterPage = () => {
    return (
        <div className="w-full min-h-screen flex flex-col lg:flex-row overflow-hidden bg-white dark:bg-[#0B0F19]">
            {/* Panel Izquierdo: Branding (Enterprise SaaS style) */}
            <div className="hidden lg:flex w-1/2 flex-col justify-center items-center relative overflow-hidden bg-gradient-to-br from-emerald-900 via-[#0B0F19] to-[#0B0F19]">
                {/* Micro-animaciones de fondo */}
                <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-emerald-500/10 rounded-full blur-[120px] animate-pulse" />
                <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-emerald-500/5 rounded-full blur-[100px]" />

                <div className="relative z-10 flex flex-col items-center text-center space-y-6 px-12">
                    <div className="p-5 bg-emerald-500/10 rounded-3xl border border-emerald-500/20 backdrop-blur-sm">
                        <Wallet className="h-20 w-20 text-emerald-500" />
                    </div>
                    <div className="space-y-4">
                        <h1 className="text-6xl font-black tracking-tighter text-white">
                            Budget<br />
                            <span className="bg-gradient-to-r from-emerald-400 to-teal-300 bg-clip-text text-transparent">
                                Management
                            </span>
                        </h1>
                        <p className="text-xl font-medium text-slate-400">
                            Estructuras financieras inteligentes.
                        </p>
                    </div>
                </div>

                {/* Footer del branding panel */}
                <div className="absolute bottom-12 left-12 text-slate-500 text-sm font-medium">
                    © 2026 Budget Management App. Christopher Pallo | Jean Pierre Villacis | Hans Ortiz | Elian Condor | Leonel Pachacama
                </div>
            </div>

            {/* Panel Derecho: Formulario Minimalista */}
            <div className="w-full lg:w-1/2 flex flex-col justify-center items-center bg-white dark:bg-[#0B0F19] px-8 sm:px-12 lg:px-20 py-12">
                <div className="w-full max-w-md">
                    <RegisterForm />
                </div>
            </div>
        </div>
    );
};