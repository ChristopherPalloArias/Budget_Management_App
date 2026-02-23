import "@testing-library/jest-dom";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { RegisterForm } from "../RegisterForm";
import { registerWithEmail } from "../../services/authService";
import { useNavigate } from "react-router-dom";

// Mock dependencies
jest.mock("../../services/authService");
jest.mock("react-router-dom", () => ({
  useNavigate: jest.fn(),
}));

describe("RegisterForm Component", () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  describe("Renderizado inicial", () => {
    it("debe renderizar el formulario de registro correctamente", () => {
      // Arrange & Act
      render(<RegisterForm />);

      // Assert: Verificar que todos los elementos se renderizan
      const titles = screen.getAllByText("Crear Cuenta");
      expect(titles.length).toBeGreaterThan(0);
      expect(
        screen.getByText("Completa el formulario para crear tu cuenta"),
      ).toBeInTheDocument();
      expect(screen.getByLabelText("Nombre Completo")).toBeInTheDocument();
      expect(screen.getByLabelText("Correo Electrónico")).toBeInTheDocument();
      expect(screen.getByLabelText("Contraseña")).toBeInTheDocument();
      expect(screen.getByLabelText("Confirmar Contraseña")).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: /crear cuenta/i }),
      ).toBeInTheDocument();
    });

    it("debe renderizar el enlace de inicio de sesión", () => {
      // Arrange
      render(<RegisterForm />);

      // Act & Assert
      const loginLink = screen.getByText("Inicia sesión aquí");
      expect(loginLink).toBeInTheDocument();
      expect(loginLink).toHaveAttribute("href", "/login");
    });

    it("debe renderizar los campos con placeholders correctos", () => {
      // Arrange
      render(<RegisterForm />);

      // Act & Assert
      expect(screen.getByPlaceholderText("Juan Pérez")).toBeInTheDocument();
      expect(
        screen.getByPlaceholderText("ejemplo@correo.com"),
      ).toBeInTheDocument();
      expect(screen.getAllByPlaceholderText("••••••••")).toHaveLength(2);
    });

    it("debe renderizar todos los campos de tipo correcto", () => {
      // Arrange
      render(<RegisterForm />);

      // Act & Assert
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );

      expect(nameInput).toHaveAttribute("type", "text");
      expect(emailInput).toHaveAttribute("type", "email");
      expect(passwordInput).toHaveAttribute("type", "password");
      expect(confirmPasswordInput).toHaveAttribute("type", "password");
    });
  });

  describe("Validación de formulario", () => {
    it("debe mostrar error cuando el nombre es muy corto", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act: Ingresar nombre corto y enviar
      const nameInput = screen.getByLabelText("Nombre Completo");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Jo");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("El nombre debe tener al menos 3 caracteres"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando el nombre contiene caracteres inválidos", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act: Ingresar nombre con números
      const nameInput = screen.getByLabelText("Nombre Completo");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Juan123");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("El nombre solo puede contener letras y espacios"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando el email es inválido", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);
      const initialCallCount = (registerWithEmail as jest.Mock).mock.calls
        .length;

      // Act
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(emailInput, "invalid-email");
      await user.click(submitButton);

      // Assert - La validación debe prevenir que se llame al servicio
      await new Promise((resolve) => setTimeout(resolve, 500));
      expect((registerWithEmail as jest.Mock).mock.calls.length).toBe(
        initialCallCount,
      );
    });

    it("debe mostrar error cuando la contraseña es muy corta", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(passwordInput, "Short1");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La contraseña debe tener al menos 8 caracteres"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando la contraseña no tiene mayúscula", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(passwordInput, "password123");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La contraseña debe tener al menos una mayúscula"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando la contraseña no tiene minúscula", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(passwordInput, "PASSWORD123");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La contraseña debe tener al menos una minúscula"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando la contraseña no tiene número", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(passwordInput, "Password");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La contraseña debe tener al menos un número"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando las contraseñas no coinciden", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act: Ingresar contraseñas diferentes
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Juan Pérez");
      await user.type(emailInput, "juan@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "DifferentPassword123");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("Las contraseñas no coinciden"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando el confirmPassword está vacío", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La confirmación de contraseña es requerida"),
        ).toBeInTheDocument();
      });
      expect(registerWithEmail).not.toHaveBeenCalled();
    });
  });

  describe("Registro exitoso", () => {
    it("debe registrar usuario correctamente con datos válidos", async () => {
      // Arrange: Preparar datos de prueba y mock
      const user = userEvent.setup();
      const mockUser = {
        uid: "test-uid",
        email: "test@example.com",
        displayName: "Test User",
        photoURL: null,
      };
      (registerWithEmail as jest.Mock).mockResolvedValue(mockUser);

      render(<RegisterForm />);

      // Act: Completar formulario con datos válidos
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");
      await user.click(submitButton);

      // Assert: Verificar que se llamó al servicio y se navegó
      await waitFor(() => {
        expect(registerWithEmail).toHaveBeenCalledWith(
          "Test User",
          "test@example.com",
          "Password123",
        );
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
      });
    });

    it("debe navegar a dashboard después de registro exitoso", async () => {
      // Arrange
      const user = userEvent.setup();
      (registerWithEmail as jest.Mock).mockResolvedValue({
        uid: "test-uid",
        email: "test@example.com",
        displayName: "Test User",
        photoURL: null,
      });

      render(<RegisterForm />);

      // Act
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "SecurePass123");
      await user.type(confirmPasswordInput, "SecurePass123");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
      });
    });
  });

  describe("Estado de carga", () => {
    it("debe mostrar estado de carga durante el registro", async () => {
      // Arrange: Simular registro lento
      const user = userEvent.setup();
      (registerWithEmail as jest.Mock).mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100)),
      );

      render(<RegisterForm />);

      // Act: Enviar formulario
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");

      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });
      await user.click(submitButton);

      // Assert: Verificar estado de carga
      expect(screen.getByText("Creando cuenta...")).toBeInTheDocument();
      expect(submitButton).toBeDisabled();
    });

    it("debe deshabilitar todos los inputs durante el registro", async () => {
      // Arrange
      const user = userEvent.setup();
      (registerWithEmail as jest.Mock).mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100)),
      );

      render(<RegisterForm />);

      // Act
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");

      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });
      await user.click(submitButton);

      // Assert: Verificar que los inputs están deshabilitados
      expect(nameInput).toBeDisabled();
      expect(emailInput).toBeDisabled();
      expect(passwordInput).toBeDisabled();
      expect(confirmPasswordInput).toBeDisabled();
    });
  });

  describe("Manejo de errores", () => {
    it("debe mostrar mensaje de error cuando el registro falla", async () => {
      // Arrange: Simular error de registro
      const user = userEvent.setup();
      const errorMessage = "Email already in use";
      (registerWithEmail as jest.Mock).mockRejectedValueOnce(
        new Error(errorMessage),
      );

      render(<RegisterForm />);

      // Act: Intentar registrar
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "existing@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");

      // Limpiar el mock de navegación antes de la acción
      mockNavigate.mockClear();

      await user.click(submitButton);

      // Assert: Verificar que se muestra el error
      await waitFor(
        () => {
          expect(screen.getByText(errorMessage)).toBeInTheDocument();
        },
        { timeout: 3000 },
      );
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it("debe mostrar el estilo de error correcto", async () => {
      // Arrange
      const user = userEvent.setup();
      (registerWithEmail as jest.Mock).mockRejectedValue(
        new Error("Registration failed"),
      );

      render(<RegisterForm />);

      // Act
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        const errorDiv = screen.getByText("Registration failed").closest("div");
        expect(errorDiv).toHaveClass(
          "bg-red-50",
          "border-red-200",
          "text-red-800",
        );
      });
    });

    it("debe limpiar el error anterior al enviar nuevamente", async () => {
      // Arrange: Primer intento con error
      const user = userEvent.setup();
      (registerWithEmail as jest.Mock).mockRejectedValueOnce(
        new Error("First error"),
      );

      render(<RegisterForm />);

      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Test User");
      await user.type(emailInput, "test@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText("First error")).toBeInTheDocument();
      });

      // Act: Segundo intento exitoso
      (registerWithEmail as jest.Mock).mockResolvedValueOnce({
        uid: "test-uid",
        email: "test@example.com",
        displayName: "Test User",
        photoURL: null,
      });

      await user.clear(emailInput);
      await user.type(emailInput, "newemail@example.com");
      await user.click(submitButton);

      // Assert: El error anterior debe desaparecer
      await waitFor(() => {
        expect(screen.queryByText("First error")).not.toBeInTheDocument();
      });
    });
  });

  describe("Estilos de validación", () => {
    it("debe aplicar estilos de error a campos con errores de validación", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<RegisterForm />);

      // Act: Enviar formulario vacío
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });
      await user.click(submitButton);

      // Assert: Verificar estilos de error
      await waitFor(() => {
        const nameInput = screen.getByLabelText("Nombre Completo");
        const emailInput = screen.getByLabelText("Correo Electrónico");
        expect(nameInput).toHaveClass("border-red-500");
        expect(emailInput).toHaveClass("border-red-500");
      });
    });
  });

  describe("Accesibilidad", () => {
    it("debe tener labels correctamente asociados a los inputs", () => {
      // Arrange
      render(<RegisterForm />);

      // Act & Assert
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );

      expect(nameInput).toHaveAttribute("id", "displayName");
      expect(emailInput).toHaveAttribute("id", "email");
      expect(passwordInput).toHaveAttribute("id", "password");
      expect(confirmPasswordInput).toHaveAttribute("id", "confirmPassword");
    });

    it("debe tener nombres descriptivos accesibles en acentos españoles", () => {
      // Arrange
      render(<RegisterForm />);

      // Act & Assert: Verificar texto en español
      expect(screen.getByText("Nombre Completo")).toBeInTheDocument();
      expect(screen.getByText("Correo Electrónico")).toBeInTheDocument();
      expect(screen.getByText("Contraseña")).toBeInTheDocument();
      expect(screen.getByText("Confirmar Contraseña")).toBeInTheDocument();
    });
  });
});
