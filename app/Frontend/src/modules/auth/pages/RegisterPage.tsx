import { RegisterForm } from '../components/RegisterForm.tsx';

export const RegisterPage = () => {
    return (
        <div className="min-h-[calc(100vh-200px)] flex items-center justify-center px-4">
            <div className="w-full max-w-md">
                <RegisterForm />
            </div>
        </div>
    );
};