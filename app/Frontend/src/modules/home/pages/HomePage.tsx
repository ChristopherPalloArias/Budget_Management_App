import { Link } from "react-router-dom";

export const HomePage = () => {
  return (
    <div className="text-center">
      <h1 className="text-4xl font-bold text-gray-800 mb-4">
        Bienvenido a Finanzas Personales
      </h1>
      <p className="text-gray-600">
        Sistema de gestión financiera personal
      </p>
      <div className="mt-8">
        <Link
          to="/login"
          className="inline-block bg-indigo-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-indigo-700 transition-colors"
        >
          Iniciar Sesión
        </Link>
      </div>
    </div>
  );
};
