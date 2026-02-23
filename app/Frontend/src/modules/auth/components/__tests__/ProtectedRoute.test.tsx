import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { ProtectedRoute } from "../ProtectedRoute";
import { useAuthStatus } from "../../hooks/useAuthStatus";

// Mock dependencies
jest.mock("../../hooks/useAuthStatus");
jest.mock("react-router-dom", () => ({
  Navigate: ({ to, replace }: { to: string; replace?: boolean }) => (
    <div data-testid="navigate" data-to={to} data-replace={replace}>
      Redirecting to {to}
    </div>
  ),
  Outlet: () => <div data-testid="outlet">Outlet Content</div>,
}));

describe("ProtectedRoute Component", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Estado de autenticación verificando", () => {
    it("debe mostrar spinner de carga cuando está verificando autenticación", () => {
      // Arrange: Configurar estado de verificación
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      // Act: Renderizar el componente
      render(<ProtectedRoute />);

      // Assert: Verificar que se muestra el spinner
      const spinner = document.querySelector(".animate-spin");
      expect(spinner).toBeInTheDocument();
      expect(spinner).toHaveClass("rounded-full", "h-12", "w-12");
    });

    it("debe mostrar el loader centrado en la pantalla", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      // Act
      render(<ProtectedRoute />);

      // Assert
      const loaderContainer = document.querySelector(".min-h-screen");
      expect(loaderContainer).toBeInTheDocument();
      expect(loaderContainer).toHaveClass(
        "flex",
        "items-center",
        "justify-center",
      );
    });

    it("no debe renderizar children ni Outlet durante la verificación", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      // Act
      render(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert
      expect(screen.queryByTestId("protected-content")).not.toBeInTheDocument();
      expect(screen.queryByTestId("outlet")).not.toBeInTheDocument();
    });
  });

  describe("Usuario no autenticado", () => {
    it("debe redirigir a /login cuando el usuario no está autenticado", () => {
      // Arrange: Usuario no autenticado
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: false,
      });

      // Act: Renderizar componente
      render(<ProtectedRoute />);

      // Assert: Verificar redirección
      const navigate = screen.getByTestId("navigate");
      expect(navigate).toBeInTheDocument();
      expect(navigate).toHaveAttribute("data-to", "/login");
      expect(navigate).toHaveAttribute("data-replace", "true");
    });

    it("debe usar replace en la redirección para evitar history stack", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: false,
      });

      // Act
      render(<ProtectedRoute />);

      // Assert
      const navigate = screen.getByTestId("navigate");
      expect(navigate).toHaveAttribute("data-replace", "true");
    });

    it("no debe renderizar children cuando no está autenticado", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: false,
      });

      // Act
      render(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert
      expect(screen.queryByTestId("protected-content")).not.toBeInTheDocument();
    });
  });

  describe("Usuario autenticado", () => {
    it("debe renderizar children cuando el usuario está autenticado y se pasan children", () => {
      // Arrange: Usuario autenticado
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act: Renderizar con children
      render(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert: Verificar que se renderizan los children
      expect(screen.getByTestId("protected-content")).toBeInTheDocument();
      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });

    it("debe renderizar Outlet cuando el usuario está autenticado y no hay children", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act: Renderizar sin children
      render(<ProtectedRoute />);

      // Assert: Verificar que se renderiza el Outlet
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
      expect(screen.getByText("Outlet Content")).toBeInTheDocument();
    });

    it("debe renderizar múltiples children cuando está autenticado", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act
      render(
        <ProtectedRoute>
          <div data-testid="child-1">Child 1</div>
          <div data-testid="child-2">Child 2</div>
          <div data-testid="child-3">Child 3</div>
        </ProtectedRoute>,
      );

      // Assert
      expect(screen.getByTestId("child-1")).toBeInTheDocument();
      expect(screen.getByTestId("child-2")).toBeInTheDocument();
      expect(screen.getByTestId("child-3")).toBeInTheDocument();
    });

    it("no debe mostrar el loader cuando está autenticado", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act
      render(
        <ProtectedRoute>
          <div>Content</div>
        </ProtectedRoute>,
      );

      // Assert
      const spinner = document.querySelector(".animate-spin");
      expect(spinner).not.toBeInTheDocument();
    });

    it("no debe redirigir cuando está autenticado", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act
      render(
        <ProtectedRoute>
          <div data-testid="content">Content</div>
        </ProtectedRoute>,
      );

      // Assert
      expect(screen.queryByTestId("navigate")).not.toBeInTheDocument();
      expect(screen.getByTestId("content")).toBeInTheDocument();
    });
  });

  describe("Transiciones de estado", () => {
    it("debe cambiar de loader a contenido cuando la autenticación se completa", () => {
      // Arrange: Inicial - verificando
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      const { rerender } = render(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert: Verificar loader
      expect(document.querySelector(".animate-spin")).toBeInTheDocument();
      expect(screen.queryByTestId("protected-content")).not.toBeInTheDocument();

      // Act: Cambiar a autenticado
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      rerender(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert: Verificar contenido
      expect(document.querySelector(".animate-spin")).not.toBeInTheDocument();
      expect(screen.getByTestId("protected-content")).toBeInTheDocument();
    });

    it("debe redirigir cuando la verificación termina sin autenticación", () => {
      // Arrange: Inicial - verificando
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      const { rerender } = render(<ProtectedRoute />);

      // Assert: Verificar loader
      expect(document.querySelector(".animate-spin")).toBeInTheDocument();

      // Act: Cambiar a no autenticado
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: false,
      });

      rerender(<ProtectedRoute />);

      // Assert: Verificar redirección
      expect(screen.getByTestId("navigate")).toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toHaveAttribute(
        "data-to",
        "/login",
      );
    });

    it("debe manejar cambio de autenticado a no autenticado", () => {
      // Arrange: Inicial - autenticado
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      const { rerender } = render(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert: Verificar contenido
      expect(screen.getByTestId("protected-content")).toBeInTheDocument();

      // Act: Cambiar a no autenticado
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: false,
      });

      rerender(
        <ProtectedRoute>
          <div data-testid="protected-content">Protected Content</div>
        </ProtectedRoute>,
      );

      // Assert: Verificar redirección
      expect(screen.queryByTestId("protected-content")).not.toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toHaveAttribute(
        "data-to",
        "/login",
      );
    });
  });

  describe("Casos edge", () => {
    it("debe manejar correctamente cuando children es null", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act
      render(<ProtectedRoute>{null}</ProtectedRoute>);

      // Assert: Debe renderizar sin errores
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
    });

    it("debe manejar correctamente cuando children es undefined", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act
      render(<ProtectedRoute>{undefined}</ProtectedRoute>);

      // Assert: Debe renderizar Outlet
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
    });

    it("debe priorizar children sobre Outlet cuando ambos están disponibles", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act
      render(
        <ProtectedRoute>
          <div data-testid="children-content">Children Content</div>
        </ProtectedRoute>,
      );

      // Assert: Children debe renderizarse, no Outlet
      expect(screen.getByTestId("children-content")).toBeInTheDocument();
      expect(screen.queryByTestId("outlet")).not.toBeInTheDocument();
    });
  });

  describe("Integración con Router", () => {
    it("debe funcionar como componente de ruta anidada", () => {
      // Arrange: Simular uso como ruta anidada
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act: Renderizar sin children (usando Outlet)
      render(<ProtectedRoute />);

      // Assert: Outlet debe renderizarse para rutas anidadas
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
    });

    it("debe permitir wrapper de páginas específicas con children", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: true,
        isChecking: false,
      });

      // Act: Simular wrapper de página específica
      render(
        <ProtectedRoute>
          <div data-testid="dashboard-page">
            <h1>Dashboard</h1>
            <p>Welcome User</p>
          </div>
        </ProtectedRoute>,
      );

      // Assert
      expect(screen.getByTestId("dashboard-page")).toBeInTheDocument();
      expect(screen.getByText("Dashboard")).toBeInTheDocument();
    });
  });

  describe("Estilos de loader", () => {
    it("debe aplicar las clases de estilo correctas al spinner", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      // Act
      render(<ProtectedRoute />);

      // Assert: Verificar clases de Tailwind
      const spinner = document.querySelector(".animate-spin");
      expect(spinner).toHaveClass(
        "rounded-full",
        "h-12",
        "w-12",
        "border-b-2",
        "border-indigo-600",
      );
    });

    it("debe tener un contenedor de altura completa", () => {
      // Arrange
      (useAuthStatus as jest.Mock).mockReturnValue({
        isAuthenticated: false,
        isChecking: true,
      });

      // Act
      render(<ProtectedRoute />);

      // Assert
      const container = document.querySelector(".min-h-screen");
      expect(container).toBeInTheDocument();
    });
  });
});
