import "@testing-library/jest-dom";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import * as ReactRouterDom from "react-router-dom";
import { RegisterForm } from "../RegisterForm";
import { authRepository } from "@/core/config/dependencies";
import type { IAuthUser } from "@/core/auth/interfaces/IAuthRepository";

/**
 * Integration Tests for RegisterForm Component
 * These tests validate the complete registration flow including:
 * - Navigation after successful registration
 * - Registration with user-provided data
 * - Execution of expected registration methods
 */
describe("RegisterForm - Integration Tests", () => {
  const mockUser: IAuthUser = {
    id: "new-user-uid-456",
    email: "newuser@example.com",
    displayName: "New User",
    photoURL: null,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Test 1: Navigation Validation
   * Verifies that the user is redirected to the dashboard after successful registration
   */
  describe("Validación de navegación", () => {
    it("debe redirigir al dashboard después de un registro exitoso", async () => {
      // Arrange: Configurar el mock del repositorio y los datos de prueba
      (authRepository.register as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      // Mock del navigate para verificar la navegación
      const mockNavigate = jest.fn();
      jest.spyOn(ReactRouterDom, "useNavigate").mockReturnValue(mockNavigate);

      // Renderizar el componente
      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Simular la interacción del usuario con datos válidos
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
      await user.type(emailInput, "juan.perez@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "Password123");
      await user.click(submitButton);

      // Assert: Verificar que se llamó al método de registro con los datos correctos
      await waitFor(() => {
        expect(authRepository.register).toHaveBeenCalledWith({
          displayName: "Juan Pérez",
          email: "juan.perez@example.com",
          password: "Password123",
        });
      });

      // Assert: Verificar que se llamó a navigate con la ruta del dashboard
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
      });
    });

    it("debe mantener al usuario en la página de registro cuando hay un error", async () => {
      // Arrange: Simular un error en el registro
      (authRepository.register as jest.Mock).mockRejectedValue(
        new Error("El email ya está registrado"),
      );
      const user = userEvent.setup();

      // Mock del navigate para verificar que NO se llama
      const mockNavigate = jest.fn();
      jest.spyOn(ReactRouterDom, "useNavigate").mockReturnValue(mockNavigate);

      // Renderizar el componente
      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Intentar registrarse con datos que causarán error
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "María López");
      await user.type(emailInput, "maria@example.com");
      await user.type(passwordInput, "Password456");
      await user.type(confirmPasswordInput, "Password456");
      await user.click(submitButton);

      // Assert: Verificar que el formulario aún está presente (no hubo navegación)
      await waitFor(() => {
        expect(screen.getByLabelText("Nombre Completo")).toBeInTheDocument();
        expect(screen.getByLabelText("Correo Electrónico")).toBeInTheDocument();
      });

      // Assert: Verificar que NO se llamó a navigate (no hubo redirección)
      expect(mockNavigate).not.toHaveBeenCalled();

      // Verificar que seguimos en la página de registro
      expect(
        screen.getByText("Completa el formulario para crear tu cuenta"),
      ).toBeInTheDocument();
    });
  });

  /**
   * Test 2: Authentication Validation with Input Data
   * Verifies complete registration flow with user-entered data
   */
  describe("Validación de autenticación mediante datos ingresados", () => {
    it("debe registrar correctamente al usuario con datos válidos completos", async () => {
      // Arrange: Preparar datos de prueba y mocks
      const testData = {
        displayName: "Carlos González",
        email: "carlos.gonzalez@example.com",
        password: "SecurePass123",
      };

      (authRepository.register as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Completar el formulario con datos válidos
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, testData.displayName);
      await user.type(emailInput, testData.email);
      await user.type(passwordInput, testData.password);
      await user.type(confirmPasswordInput, testData.password);
      await user.click(submitButton);

      // Assert: Verificar que el flujo de registro se completó correctamente
      await waitFor(() => {
        expect(authRepository.register).toHaveBeenCalledTimes(1);
        expect(authRepository.register).toHaveBeenCalledWith({
          displayName: testData.displayName,
          email: testData.email,
          password: testData.password,
        });
      });

      // Verificar que no se muestra ningún error
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("debe validar que las contraseñas coincidan antes de enviar el formulario", async () => {
      // Arrange: Preparar datos con contraseñas que no coinciden
      (authRepository.register as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

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

      await user.type(nameInput, "Ana Martínez");
      await user.type(emailInput, "ana@example.com");
      await user.type(passwordInput, "Password123");
      await user.type(confirmPasswordInput, "DifferentPass456");
      await user.click(submitButton);

      // Assert: Verificar que se muestra el error de validación
      await waitFor(() => {
        expect(
          screen.getByText("Las contraseñas no coinciden"),
        ).toBeInTheDocument();
      });

      // Verificar que NO se llamó al servicio de registro
      expect(authRepository.register).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando los datos de registro son rechazados por el servidor", async () => {
      // Arrange: Simular error del servidor
      const serverError = new Error("El correo electrónico ya está en uso");
      (authRepository.register as jest.Mock).mockRejectedValue(serverError);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Intentar registrarse con un email existente
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Usuario Duplicado");
      await user.type(emailInput, "existente@example.com");
      await user.type(passwordInput, "Password789");
      await user.type(confirmPasswordInput, "Password789");
      await user.click(submitButton);

      // Assert: Verificar que se muestra el error
      await waitFor(() => {
        expect(authRepository.register).toHaveBeenCalled();
      });

      // El botón debe volver a estar disponible después del error
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      });
    });
  });

  /**
   * Test 3: Expected Method Execution
   * Verifies that registration service methods are called correctly
   */
  describe("Ejecución de métodos esperados", () => {
    it("debe ejecutar el método register del authRepository con los parámetros correctos", async () => {
      // Arrange: Configurar el estado inicial
      const registrationData = {
        displayName: "Pedro Ramírez",
        email: "pedro.ramirez@example.com",
        password: "SecurePass456",
      };

      (authRepository.register as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Ejecutar el flujo de registro
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, registrationData.displayName);
      await user.type(emailInput, registrationData.email);
      await user.type(passwordInput, registrationData.password);
      await user.type(confirmPasswordInput, registrationData.password);
      await user.click(submitButton);

      // Assert: Verificar la ejecución correcta del método
      await waitFor(() => {
        // Verificar que se llamó exactamente una vez
        expect(authRepository.register).toHaveBeenCalledTimes(1);

        // Verificar los parámetros exactos
        expect(authRepository.register).toHaveBeenCalledWith({
          displayName: registrationData.displayName,
          email: registrationData.email,
          password: registrationData.password,
        });

        // Verificar que otros métodos no fueron llamados
        expect(authRepository.signIn).not.toHaveBeenCalled();
        expect(authRepository.signInWithProvider).not.toHaveBeenCalled();
      });
    });

    it("debe ejecutar el método register solo después de validar todos los campos", async () => {
      // Arrange: Configurar mock
      (authRepository.register as jest.Mock).mockResolvedValue(mockUser);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Intentar enviar el formulario vacío primero
      let submitButton = screen.getByRole("button", { name: /crear cuenta/i });
      await user.click(submitButton);

      // Assert: Verificar que NO se llamó al método (validación falló)
      await waitFor(() => {
        expect(authRepository.register).not.toHaveBeenCalled();
      });

      // Act: Ahora completar el formulario correctamente
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );

      await user.type(nameInput, "Laura Díaz");
      await user.type(emailInput, "laura.diaz@example.com");
      await user.type(passwordInput, "ValidPass123");
      await user.type(confirmPasswordInput, "ValidPass123");

      submitButton = screen.getByRole("button", { name: /crear cuenta/i });
      await user.click(submitButton);

      // Assert: Ahora sí debe llamarse el método
      await waitFor(() => {
        expect(authRepository.register).toHaveBeenCalledTimes(1);
      });
    });

    it("debe manejar el estado de carga correctamente durante el registro", async () => {
      // Arrange: Simular un registro que toma tiempo
      let resolveRegister: (value: IAuthUser) => void;
      const registerPromise = new Promise<IAuthUser>((resolve) => {
        resolveRegister = resolve;
      });

      (authRepository.register as jest.Mock).mockReturnValue(registerPromise);
      const user = userEvent.setup();

      render(
        <MemoryRouter initialEntries={["/register"]}>
          <RegisterForm />
        </MemoryRouter>,
      );

      // Act: Iniciar el registro
      const nameInput = screen.getByLabelText("Nombre Completo");
      const emailInput = screen.getByLabelText("Correo Electrónico");
      const passwordInput = screen.getByLabelText("Contraseña");
      const confirmPasswordInput = screen.getByLabelText(
        "Confirmar Contraseña",
      );
      const submitButton = screen.getByRole("button", {
        name: /crear cuenta/i,
      });

      await user.type(nameInput, "Roberto Sánchez");
      await user.type(emailInput, "roberto@example.com");
      await user.type(passwordInput, "TestPass123");
      await user.type(confirmPasswordInput, "TestPass123");
      await user.click(submitButton);

      // Assert: Verificar que el botón está deshabilitado durante la carga
      await waitFor(() => {
        expect(authRepository.register).toHaveBeenCalled();
      });

      const loadingButton = screen.getByRole("button", {
        name: /creando cuenta/i,
      });
      expect(loadingButton).toBeDisabled();

      // Completar el registro
      resolveRegister!(mockUser);

      // Verificar que el estado cambió después de completar
      await waitFor(() => {
        expect(
          screen.queryByRole("button", { name: /creando cuenta/i }),
        ).not.toBeInTheDocument();
      });
    });
  });
});
