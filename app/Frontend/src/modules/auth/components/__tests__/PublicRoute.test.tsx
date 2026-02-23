import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { PublicRoute } from "../PublicRoute";
import { useUserStore } from "../../store/useUserStore";

// Mock dependencies
jest.mock("../../store/useUserStore");
jest.mock("react-router-dom", () => ({
  Navigate: ({ to, replace }: { to: string; replace?: boolean }) => (
    <div data-testid="navigate" data-to={to} data-replace={replace}>
      Redirecting to {to}
    </div>
  ),
  Outlet: () => <div data-testid="outlet">Outlet Content</div>,
}));

describe("PublicRoute Component", () => {
  const mockUser = {
    uid: "test-user-id",
    email: "test@example.com",
    displayName: "Test User",
    photoURL: null,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Estado de carga", () => {
    it("debe mostrar spinner cuando está cargando", () => {
      // Arrange: Estado de carga activo
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      // Act: Renderizar el componente
      render(<PublicRoute />);

      // Assert: Verificar que se muestra el spinner
      const spinner = document.querySelector(".animate-spin");
      expect(spinner).toBeInTheDocument();
      expect(spinner).toHaveClass("rounded-full", "h-12", "w-12");
    });

    it("debe centrar el loader en la pantalla", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      // Act
      render(<PublicRoute />);

      // Assert
      const loaderContainer = document.querySelector(".min-h-screen");
      expect(loaderContainer).toBeInTheDocument();
      expect(loaderContainer).toHaveClass(
        "flex",
        "items-center",
        "justify-center",
      );
    });

    it("no debe renderizar children durante la carga", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="public-content">Public Content</div>
        </PublicRoute>,
      );

      // Assert
      expect(screen.queryByTestId("public-content")).not.toBeInTheDocument();
      expect(document.querySelector(".animate-spin")).toBeInTheDocument();
    });

    it("no debe renderizar Outlet durante la carga", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      // Act
      render(<PublicRoute />);

      // Assert
      expect(screen.queryByTestId("outlet")).not.toBeInTheDocument();
    });
  });

  describe("Usuario autenticado", () => {
    it("debe redirigir a dashboard cuando el usuario está autenticado", () => {
      // Arrange: Usuario autenticado
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      // Act: Renderizar el componente
      render(<PublicRoute />);

      // Assert: Verificar redirección
      const navigate = screen.getByTestId("navigate");
      expect(navigate).toBeInTheDocument();
      expect(navigate).toHaveAttribute("data-to", "/dashboard");
      expect(navigate).toHaveAttribute("data-replace", "true");
    });

    it("debe usar replace en la redirección", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      // Act
      render(<PublicRoute />);

      // Assert
      const navigate = screen.getByTestId("navigate");
      expect(navigate).toHaveAttribute("data-replace", "true");
    });

    it("no debe renderizar children cuando el usuario está autenticado", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="public-content">Public Content</div>
        </PublicRoute>,
      );

      // Assert: Solo debe mostrar Navigate
      expect(screen.queryByTestId("public-content")).not.toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toBeInTheDocument();
    });

    it("debe redirigir incluso con children presentes", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="login-form">Login Form</div>
        </PublicRoute>,
      );

      // Assert: Debe redirigir, no mostrar el formulario
      expect(screen.queryByTestId("login-form")).not.toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toHaveAttribute(
        "data-to",
        "/dashboard",
      );
    });
  });

  describe("Usuario no autenticado", () => {
    it("debe renderizar children cuando no hay usuario autenticado", () => {
      // Arrange: Sin usuario
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act: Renderizar con children
      render(
        <PublicRoute>
          <div data-testid="public-content">Public Content</div>
        </PublicRoute>,
      );

      // Assert: Verificar que children se renderizan
      expect(screen.getByTestId("public-content")).toBeInTheDocument();
      expect(screen.getByText("Public Content")).toBeInTheDocument();
    });

    it("debe renderizar Outlet cuando no hay usuario y no hay children", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act: Renderizar sin children
      render(<PublicRoute />);

      // Assert: Verificar que se renderiza el Outlet
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
      expect(screen.getByText("Outlet Content")).toBeInTheDocument();
    });

    it("debe renderizar múltiples children cuando no hay usuario", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="child-1">Login Form</div>
          <div data-testid="child-2">Social Login</div>
          <div data-testid="child-3">Footer</div>
        </PublicRoute>,
      );

      // Assert: Todos los children deben renderizarse
      expect(screen.getByTestId("child-1")).toBeInTheDocument();
      expect(screen.getByTestId("child-2")).toBeInTheDocument();
      expect(screen.getByTestId("child-3")).toBeInTheDocument();
    });

    it("no debe mostrar loader cuando no está autenticado y no está cargando", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div>Content</div>
        </PublicRoute>,
      );

      // Assert
      const spinner = document.querySelector(".animate-spin");
      expect(spinner).not.toBeInTheDocument();
    });

    it("no debe redirigir cuando no hay usuario autenticado", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="content">Content</div>
        </PublicRoute>,
      );

      // Assert
      expect(screen.queryByTestId("navigate")).not.toBeInTheDocument();
      expect(screen.getByTestId("content")).toBeInTheDocument();
    });
  });

  describe("Transiciones de estado", () => {
    it("debe cambiar de loader a contenido cuando termina de cargar sin usuario", () => {
      // Arrange: Inicial - cargando
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      const { rerender } = render(
        <PublicRoute>
          <div data-testid="login-form">Login Form</div>
        </PublicRoute>,
      );

      // Assert: Verificar loader
      expect(document.querySelector(".animate-spin")).toBeInTheDocument();
      expect(screen.queryByTestId("login-form")).not.toBeInTheDocument();

      // Act: Cambiar a no autenticado
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      rerender(
        <PublicRoute>
          <div data-testid="login-form">Login Form</div>
        </PublicRoute>,
      );

      // Assert: Verificar contenido
      expect(document.querySelector(".animate-spin")).not.toBeInTheDocument();
      expect(screen.getByTestId("login-form")).toBeInTheDocument();
    });

    it("debe cambiar de loader a redirección cuando carga con usuario", () => {
      // Arrange: Inicial - cargando
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      const { rerender } = render(<PublicRoute />);

      // Assert: Verificar loader
      expect(document.querySelector(".animate-spin")).toBeInTheDocument();

      // Act: Cambiar a autenticado
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      rerender(<PublicRoute />);

      // Assert: Verificar redirección
      expect(screen.getByTestId("navigate")).toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toHaveAttribute(
        "data-to",
        "/dashboard",
      );
    });

    it("debe renderizar children cuando el usuario se desautentica", () => {
      // Arrange: Inicial - autenticado
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      const { rerender } = render(
        <PublicRoute>
          <div data-testid="login-form">Login Form</div>
        </PublicRoute>,
      );

      // Assert: Verificar redirección
      expect(screen.getByTestId("navigate")).toBeInTheDocument();
      expect(screen.queryByTestId("login-form")).not.toBeInTheDocument();

      // Act: Cambiar a no autenticado
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      rerender(
        <PublicRoute>
          <div data-testid="login-form">Login Form</div>
        </PublicRoute>,
      );

      // Assert: Verificar contenido
      expect(screen.queryByTestId("navigate")).not.toBeInTheDocument();
      expect(screen.getByTestId("login-form")).toBeInTheDocument();
    });
  });

  describe("Casos edge", () => {
    it("debe manejar correctamente cuando children es null", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act
      render(<PublicRoute>{null}</PublicRoute>);

      // Assert: Debe renderizar Outlet
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
    });

    it("debe manejar correctamente cuando children es undefined", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act
      render(<PublicRoute>{undefined}</PublicRoute>);

      // Assert: Debe renderizar Outlet
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
    });

    it("debe priorizar children sobre Outlet cuando están disponibles", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="children-content">Children Content</div>
        </PublicRoute>,
      );

      // Assert: Children debe renderizarse, no Outlet
      expect(screen.getByTestId("children-content")).toBeInTheDocument();
      expect(screen.queryByTestId("outlet")).not.toBeInTheDocument();
    });
  });

  describe("Integración con Router", () => {
    it("debe funcionar como ruta pública con Outlet para rutas anidadas", () => {
      // Arrange: Simular uso con routes anidadas
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act: Renderizar sin children
      render(<PublicRoute />);

      // Assert: Outlet debe renderizarse
      expect(screen.getByTestId("outlet")).toBeInTheDocument();
    });

    it("debe permitir wrapper de páginas públicas específicas", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: false };
        return selector(state);
      });

      // Act: Simular página de login específica
      render(
        <PublicRoute>
          <div data-testid="login-page">
            <h1>Login</h1>
            <form data-testid="login-form">Login Form</form>
          </div>
        </PublicRoute>,
      );

      // Assert
      expect(screen.getByTestId("login-page")).toBeInTheDocument();
      expect(screen.getByText("Login")).toBeInTheDocument();
      expect(screen.getByTestId("login-form")).toBeInTheDocument();
    });
  });

  describe("Prevención de acceso autenticado a rutas públicas", () => {
    it("debe evitar que usuarios autenticados accedan a login", () => {
      // Arrange: Usuario ya autenticado intentando acceder a login
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="login-form">Login Form</div>
        </PublicRoute>,
      );

      // Assert: Debe redirigir, no mostrar login
      expect(screen.queryByTestId("login-form")).not.toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toHaveAttribute(
        "data-to",
        "/dashboard",
      );
    });

    it("debe evitar que usuarios autenticados accedan a register", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: mockUser, isLoading: false };
        return selector(state);
      });

      // Act
      render(
        <PublicRoute>
          <div data-testid="register-form">Register Form</div>
        </PublicRoute>,
      );

      // Assert
      expect(screen.queryByTestId("register-form")).not.toBeInTheDocument();
      expect(screen.getByTestId("navigate")).toBeInTheDocument();
    });
  });

  describe("Estilos del loader", () => {
    it("debe aplicar las clases correctas al spinner", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      // Act
      render(<PublicRoute />);

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

    it("debe tener un contenedor de altura completa para el loader", () => {
      // Arrange
      (useUserStore as unknown as jest.Mock).mockImplementation((selector) => {
        const state = { user: null, isLoading: true };
        return selector(state);
      });

      // Act
      render(<PublicRoute />);

      // Assert
      const container = document.querySelector(".min-h-screen");
      expect(container).toBeInTheDocument();
    });
  });
});
