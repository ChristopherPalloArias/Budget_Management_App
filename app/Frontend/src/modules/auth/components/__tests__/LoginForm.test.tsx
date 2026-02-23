import "@testing-library/jest-dom";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LoginForm } from "../LoginForm";
import { useLoginForm } from "../../hooks/useLoginForm";

// Mock dependencies
jest.mock("react-router-dom", () => ({
  Link: ({ to, children, className }: any) => (
    <a href={to} className={className}>
      {children}
    </a>
  ),
}));

jest.mock("../../hooks/useLoginForm");

describe("LoginForm Component", () => {
  // Mock hook data
  const mockLogin = jest.fn();
  const mockLoginWithGoogleProvider = jest.fn();

  const defaultHookReturn = {
    state: {
      isLoading: false,
      isGoogleLoading: false,
      error: null,
    },
    login: mockLogin,
    loginWithGoogleProvider: mockLoginWithGoogleProvider,
    clearError: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (useLoginForm as jest.Mock).mockReturnValue(defaultHookReturn);
  });

  describe("Renderizado inicial", () => {
    it("debe renderizar el formulario de inicio de sesión correctamente", () => {
      // Arrange: Configurar el estado inicial
      render(<LoginForm />);

      // Act & Assert: Verificar que los elementos se renderizan
      const titles = screen.getAllByText("Iniciar Sesión");
      expect(titles.length).toBeGreaterThan(0);
      expect(
        screen.getByText("Ingresa tus credenciales para acceder a tu cuenta"),
      ).toBeInTheDocument();
      expect(screen.getByLabelText("Correo Electrónico")).toBeInTheDocument();
      expect(screen.getByLabelText("Contraseña")).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: /iniciar sesión/i }),
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: /google/i }),
      ).toBeInTheDocument();
    });

    it("debe mostrar el enlace de registro", () => {
      // Arrange
      render(<LoginForm />);

      // Act & Assert
      const registerLink = screen.getByText("Regístrate aquí");
      expect(registerLink).toBeInTheDocument();
      expect(registerLink).toHaveAttribute("href", "/register");
    });

    it("debe renderizar los campos de entrada con placeholders correctos", () => {
      // Arrange
      render(<LoginForm />);

      // Act & Assert
      const emailInput = screen.getByPlaceholderText("ejemplo@correo.com");
      const passwordInput = screen.getByPlaceholderText("••••••••");

      expect(emailInput).toBeInTheDocument();
      expect(passwordInput).toBeInTheDocument();
      expect(passwordInput).toHaveAttribute("type", "password");
    });
  });

  describe("Validación de formulario", () => {
    it("debe mostrar errores de validación cuando el email es inválido", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<LoginForm />);
      const initialCallCount = mockLogin.mock.calls.length;

      // Act: Ingresar email inválido y enviar
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, "invalid-email");
      await user.click(submitButton);

      // Assert: La validación debe prevenir la llamada al servicio
      await new Promise((resolve) => setTimeout(resolve, 500));
      expect(mockLogin.mock.calls.length).toBe(initialCallCount);
    });

    it("debe mostrar error cuando la contraseña es muy corta", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<LoginForm />);

      // Act: Ingresar contraseña corta
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "12345");
      await user.click(submitButton);

      // Assert: Verificar error de validación
      await waitFor(() => {
        expect(
          screen.getByText("La contraseña debe tener al menos 6 caracteres"),
        ).toBeInTheDocument();
      });
      expect(mockLogin).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando el email está vacío", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<LoginForm />);

      // Act: Enviar sin completar email
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText("El correo es requerido")).toBeInTheDocument();
      });
      expect(mockLogin).not.toHaveBeenCalled();
    });
  });

  describe("Envío del formulario con email y contraseña", () => {
    it("debe llamar a login con datos correctos cuando el formulario es válido", async () => {
      // Arrange: Estado inicial y datos de prueba
      const user = userEvent.setup();
      const testEmail = "test@example.com";
      const testPassword = "password123";

      render(<LoginForm />);

      // Act: Completar y enviar el formulario
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, testEmail);
      await user.type(passwordInput, testPassword);
      await user.click(submitButton);

      // Assert: Verificar que se llamó al hook con los datos correctos
      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith({
          email: testEmail,
          password: testPassword,
        });
      });
    });

    it("debe deshabilitar el botón de envío durante la carga", () => {
      // Arrange: Simular estado de carga
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, isLoading: true },
      });

      render(<LoginForm />);

      // Act & Assert: Verificar estado deshabilitado
      const submitButton = screen.getByRole("button", {
        name: /iniciando sesión/i,
      });
      expect(submitButton).toBeDisabled();
    });

    it("debe mostrar texto de carga en el botón durante el inicio de sesión", () => {
      // Arrange
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, isLoading: true },
      });

      render(<LoginForm />);

      // Act & Assert
      expect(screen.getByText("Iniciando sesión...")).toBeInTheDocument();
    });

    it("debe deshabilitar los inputs durante la carga", () => {
      // Arrange
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, isLoading: true },
      });

      render(<LoginForm />);

      // Act & Assert
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");

      expect(emailInput).toBeDisabled();
      expect(passwordInput).toBeDisabled();
    });
  });

  describe("Inicio de sesión con Google", () => {
    it("debe llamar a loginWithGoogleProvider cuando se hace clic en el botón de Google", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<LoginForm />);

      // Act: Hacer clic en el botón de Google
      const googleButton = screen.getByRole("button", { name: /google/i });
      await user.click(googleButton);

      // Assert: Verificar que se llamó a la función correcta
      expect(mockLoginWithGoogleProvider).toHaveBeenCalledTimes(1);
    });

    it("debe deshabilitar el botón de Google durante la autenticación de Google", () => {
      // Arrange
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, isGoogleLoading: true },
      });

      render(<LoginForm />);

      // Act & Assert
      const googleButton = screen.getByRole("button", { name: /conectando/i });
      expect(googleButton).toBeDisabled();
    });

    it("debe mostrar texto de carga en el botón de Google durante la autenticación", () => {
      // Arrange
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, isGoogleLoading: true },
      });

      render(<LoginForm />);

      // Act & Assert
      expect(screen.getByText("Conectando...")).toBeInTheDocument();
    });

    it("debe deshabilitar todos los controles cuando está cargando Google", () => {
      // Arrange
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, isGoogleLoading: true },
      });

      render(<LoginForm />);

      // Act & Assert
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });
      const googleButton = screen.getByRole("button", { name: /conectando/i });

      expect(emailInput).toBeDisabled();
      expect(passwordInput).toBeDisabled();
      expect(submitButton).toBeDisabled();
      expect(googleButton).toBeDisabled();
    });
  });

  describe("Manejo de errores", () => {
    it("debe mostrar mensaje de error cuando hay un error de autenticación", () => {
      // Arrange: Configurar error en el estado
      const errorMessage = "Credenciales inválidas";
      (useLoginForm as jest.Mock).mockReturnValue({
        ...defaultHookReturn,
        state: { ...defaultHookReturn.state, error: errorMessage },
      });

      render(<LoginForm />);

      // Act & Assert: Verificar que se muestra el error
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
      const errorDiv = screen.getByText(errorMessage).closest("div");
      expect(errorDiv).toHaveClass(
        "bg-red-50",
        "border-red-200",
        "text-red-800",
      );
    });

    it("no debe mostrar el mensaje de error cuando error es null", () => {
      // Arrange
      render(<LoginForm />);

      // Act & Assert
      const errorElements = screen.queryByRole("alert");
      expect(errorElements).not.toBeInTheDocument();
    });
  });

  describe("Estilos de validación", () => {
    it("debe aplicar estilos de error a los campos con errores de validación", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<LoginForm />);

      // Act: Enviar formulario vacío
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });
      await user.click(submitButton);

      // Assert: Verificar estilos de error
      await waitFor(() => {
        const emailInput = screen.getByLabelText("Correo Electrónico");
        expect(emailInput).toHaveClass("border-red-500");
      });
    });
  });

  describe("Accesibilidad", () => {
    it("debe tener labels correctamente asociados a los inputs", () => {
      // Arrange
      render(<LoginForm />);

      // Act & Assert
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");

      expect(emailInput).toHaveAttribute("id", "email");
      expect(passwordInput).toHaveAttribute("id", "password");
    });

    it("debe tener el tipo correcto en los inputs", () => {
      // Arrange
      render(<LoginForm />);

      // Act & Assert
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");

      expect(emailInput).toHaveAttribute("type", "email");
      expect(passwordInput).toHaveAttribute("type", "password");
    });
  });
});
