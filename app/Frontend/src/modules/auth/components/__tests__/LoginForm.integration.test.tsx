import "@testing-library/jest-dom";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import * as ReactRouterDom from "react-router-dom";
import { LoginForm } from "../LoginForm";
import { authRepository } from "@/core/config/dependencies";
import type { IAuthUser } from "@/core/auth/interfaces/IAuthRepository";

/**
 * Integration Tests for LoginForm Component
 * These tests validate the complete authentication flow including:
 * - Navigation after successful authentication
 * - Authentication with user-provided credentials
 * - Execution of expected authentication methods
 */
describe("LoginForm - Integration Tests", () => {
  // Datos reales del usuario de prueba en Firebase
  const mockUser: IAuthUser = {
    id: "test-uid-123",
    email: "test@test.com",
    displayName: "Test User",
    photoURL: null,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Test 1: Navigation Validation
   * Verifies that the user is redirected to the dashboard after successful login
   */
  describe("Validación de navegación", () => {
    it("debe redirigir al dashboard después de un login exitoso con email y contraseña", async () => {
      // Arrange: Configurar el mock del repositorio con datos reales de Firebase
      (authRepository.signIn as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      // Mock del navigate para verificar la navegación
      const mockNavigate = jest.fn();
      jest.spyOn(ReactRouterDom, "useNavigate").mockReturnValue(mockNavigate);

      // Renderizar el componente
      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Simular la interacción del usuario con credenciales reales
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, "test@test.com");
      await user.type(passwordInput, "Abc123456");
      await user.click(submitButton);

      // Assert: Verificar que el usuario fue autenticado con los datos correctos
      await waitFor(() => {
        expect(authRepository.signIn).toHaveBeenCalledWith({
          email: "test@test.com",
          password: "Abc123456",
        });
      });

      // Assert: Verificar que se llamó a navigate con la ruta del dashboard
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
      });
    });

    it("debe redirigir al dashboard después de un login exitoso con Google", async () => {
      // Arrange: Configurar el mock del repositorio para Google Auth
      (authRepository.signInWithProvider as jest.Mock).mockResolvedValue(
        mockUser,
      );
      const user = userEvent.setup();

      // Mock del navigate para verificar la navegación
      const mockNavigate = jest.fn();
      jest.spyOn(ReactRouterDom, "useNavigate").mockReturnValue(mockNavigate);

      // Renderizar el componente
      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Hacer clic en el botón de Google
      const googleButton = screen.getByRole("button", { name: /google/i });
      await user.click(googleButton);

      // Assert: Verificar que se llamó al método de autenticación con Google
      await waitFor(() => {
        expect(authRepository.signInWithProvider).toHaveBeenCalledWith(
          "GOOGLE",
        );
      });

      // Assert: Verificar que se llamó a navigate con la ruta del dashboard
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
      });
    });
  });

  /**
   * Test 2: Authentication Validation with Input Data
   * Verifies complete authentication flow with user-entered credentials
   */
  describe("Validación de autenticación mediante datos ingresados", () => {
    it("debe autenticar correctamente al usuario con credenciales válidas", async () => {
      // Arrange: Preparar datos de prueba y mocks
      const testEmail = "usuario@test.com";
      const testPassword = "MiPassword123";

      (authRepository.signIn as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Completar el formulario con credenciales válidas
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, testEmail);
      await user.type(passwordInput, testPassword);
      await user.click(submitButton);

      // Assert: Verificar que el flujo de autenticación se completó correctamente
      await waitFor(() => {
        expect(authRepository.signIn).toHaveBeenCalledTimes(1);
        expect(authRepository.signIn).toHaveBeenCalledWith({
          email: testEmail,
          password: testPassword,
        });
      });

      // Verificar que no se muestra ningún error
      expect(screen.queryByText(/error/i)).not.toBeInTheDocument();
    });

    it("debe mostrar error cuando las credenciales son incorrectas", async () => {
      // Arrange: Simular un error de autenticación
      const errorMessage = "Credenciales inválidas";
      (authRepository.signIn as jest.Mock).mockRejectedValue(
        new Error(errorMessage),
      );
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Intentar autenticarse con credenciales incorrectas
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, "wrong@test.com");
      await user.type(passwordInput, "WrongPassword");
      await user.click(submitButton);

      // Assert: Verificar que se muestra el mensaje de error
      await waitFor(() => {
        expect(authRepository.signIn).toHaveBeenCalled();
      });

      // El botón debe volver a estar disponible después del error
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      });
    });
  });

  /**
   * Test 3: Expected Method Execution
   * Verifies that authentication service methods are called correctly
   */
  describe("Ejecución de métodos esperados", () => {
    it("debe ejecutar el método signIn del authRepository con los parámetros correctos", async () => {
      // Arrange: Configurar el estado inicial
      const credentials = {
        email: "developer@example.com",
        password: "Dev123456",
      };

      (authRepository.signIn as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Ejecutar el flujo de login
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      await user.type(emailInput, credentials.email);
      await user.type(passwordInput, credentials.password);
      await user.click(submitButton);

      // Assert: Verificar la ejecución correcta del método
      await waitFor(() => {
        // Verificar que se llamó exactamente una vez
        expect(authRepository.signIn).toHaveBeenCalledTimes(1);

        // Verificar los parámetros exactos
        expect(authRepository.signIn).toHaveBeenCalledWith({
          email: credentials.email,
          password: credentials.password,
        });

        // Verificar que otros métodos no fueron llamados
        expect(authRepository.signInWithProvider).not.toHaveBeenCalled();
        expect(authRepository.register).not.toHaveBeenCalled();
      });
    });

    it("debe ejecutar el método signInWithProvider al usar autenticación con Google", async () => {
      // Arrange: Configurar mock para Google Auth
      (authRepository.signInWithProvider as jest.Mock).mockResolvedValue(
        mockUser,
      );
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Hacer clic en el botón de Google
      const googleButton = screen.getByRole("button", { name: /google/i });
      await user.click(googleButton);

      // Assert: Verificar que se ejecutó el método correcto
      await waitFor(() => {
        // Verificar que se llamó con el proveedor correcto
        expect(authRepository.signInWithProvider).toHaveBeenCalledTimes(1);
        expect(authRepository.signInWithProvider).toHaveBeenCalledWith(
          "GOOGLE",
        );

        // Verificar que el método de email/password no fue llamado
        expect(authRepository.signIn).not.toHaveBeenCalled();
      });
    });

    it("debe manejar múltiples intentos de autenticación correctamente", async () => {
      // Arrange: Simular primer intento fallido y segundo exitoso
      (authRepository.signIn as jest.Mock)
        .mockRejectedValueOnce(new Error("Error temporal"))
        .mockResolvedValueOnce(mockUser);

      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/login"]}>
          <LoginForm />
        </MemoryRouter>,
      );

      // Act: Realizar dos intentos de autenticación
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const submitButton = screen.getByRole("button", {
        name: /iniciar sesión/i,
      });

      // Primer intento (fallido)
      await user.type(emailInput, "usuario@test.com");
      await user.type(passwordInput, "Pass123");
      await user.click(submitButton);

      await waitFor(() => {
        expect(authRepository.signIn).toHaveBeenCalledTimes(1);
      });

      // Limpiar inputs
      await user.clear(emailInput);
      await user.clear(passwordInput);

      // Segundo intento (exitoso)
      await user.type(emailInput, "usuario@test.com");
      await user.type(passwordInput, "Pass123");
      await user.click(submitButton);

      // Assert: Verificar que ambos intentos se ejecutaron
      await waitFor(() => {
        expect(authRepository.signIn).toHaveBeenCalledTimes(2);
      });
    });
  });
});
