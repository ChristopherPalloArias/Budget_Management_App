import { LoginForm } from '../components/LoginForm.tsx';

export const LoginPage = () => {
    return (
        <div className="min-h-[calc(100vh-200px)] flex items-center justify-center px-4">
            <div className="w-full max-w-md">
                <LoginForm />
            </div>
        </div>
    );
};
