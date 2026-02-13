import { Suspense, lazy } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { PublicLayout } from "../../shared/layouts/PublicLayout";
import { DashboardLayout } from "../../shared/layouts/DashboardLayout";
import { ProtectedRoute, PublicRoute } from "../../modules/auth";

const LoginPage = lazy(() => import("../../modules/auth/pages/LoginPage").then(m => ({ default: m.LoginPage })));
const RegisterPage = lazy(() => import("../../modules/auth/pages/RegisterPage").then(m => ({ default: m.RegisterPage })));
const HomePage = lazy(() => import("../../modules/home/pages/HomePage").then(m => ({ default: m.HomePage })));
const TransactionPage = lazy(() => import("../../modules/transactions/pages/TransactionPage").then(m => ({ default: m.TransactionPage })));
const ReportsPage = lazy(() => import("../../modules/reports/pages/ReportsPage").then(m => ({ default: m.ReportsPage })));

const PageLoader = () => (
  <div className="flex items-center justify-center min-h-[200px]">
    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
  </div>
);

export const AppRouter = () => {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          <Route element={<PublicRoute />}>
            <Route element={<PublicLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>
          </Route>

          <Route element={<ProtectedRoute />}>
            <Route element={<DashboardLayout />}>
              <Route path="/dashboard" element={<ReportsPage />} />
              <Route path="/transactions" element={<TransactionPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
};
