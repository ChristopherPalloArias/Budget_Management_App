import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TransactionForm } from "../TransactionForm";

describe("TransactionForm", () => {
  const mockOnSubmit = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Renderizado inicial", () => {
    it("debe renderizar el formulario con todos los campos requeridos", () => {
      // Arrange & Act
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Assert
      expect(screen.getByLabelText("Tipo")).toBeInTheDocument();
      expect(screen.getByLabelText("Descripción")).toBeInTheDocument();
      expect(screen.getByLabelText("Monto")).toBeInTheDocument();
      expect(screen.getByLabelText("Categoría")).toBeInTheDocument();
      expect(screen.getByLabelText("Fecha")).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: /crear transacción/i }),
      ).toBeInTheDocument();
    });

    it("debe establecer valores por defecto correctamente", () => {
      // Arrange & Act
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Assert
      const descriptionInput = screen.getByPlaceholderText("Ej: Supermercado");
      const amountInput = screen.getByPlaceholderText("0.00");

      expect(descriptionInput).toHaveValue("");
      expect(amountInput).toHaveValue(0);
    });

    it("debe cargar valores por defecto cuando se proporcionan", () => {
      // Arrange
      const defaultValues = {
        description: "Test Transaction",
        amount: 100,
        category: "Alimentación",
        type: "EXPENSE" as const,
        date: "2024-01-15",
      };

      // Act
      render(
        <TransactionForm
          onSubmit={mockOnSubmit}
          defaultValues={defaultValues}
        />,
      );

      // Assert
      const descriptionInput = screen.getByPlaceholderText("Ej: Supermercado");
      const amountInput = screen.getByPlaceholderText("0.00");

      expect(descriptionInput).toHaveValue("Test Transaction");
      expect(amountInput).toHaveValue(100);
    });
  });

  describe("Validación de formulario", () => {
    it("debe mostrar error cuando la descripción está vacía", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const submitButton = screen.getByRole("button", {
        name: /crear transacción/i,
      });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La descripción es requerida"),
        ).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando el monto es 0 o negativo", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const descriptionInput = screen.getByPlaceholderText("Ej: Supermercado");
      const submitButton = screen.getByRole("button", {
        name: /crear transacción/i,
      });

      await user.type(descriptionInput, "Test");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("El monto debe ser mayor a 0"),
        ).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando la categoría no está seleccionada", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const descriptionInput = screen.getByPlaceholderText("Ej: Supermercado");
      const amountInput = screen.getByPlaceholderText("0.00");
      const submitButton = screen.getByRole("button", {
        name: /crear transacción/i,
      });

      await user.type(descriptionInput, "Test");
      await user.clear(amountInput);
      await user.type(amountInput, "100");
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText("La categoría es requerida"),
        ).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it("debe mostrar error cuando la fecha no está seleccionada", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const descriptionInput = screen.getByPlaceholderText("Ej: Supermercado");
      const amountInput = screen.getByPlaceholderText("0.00");
      const dateInput = screen.getByLabelText("Fecha");
      const submitButton = screen.getByRole("button", {
        name: /crear transacción/i,
      });

      await user.type(descriptionInput, "Test");
      await user.clear(amountInput);
      await user.type(amountInput, "100");
      await user.clear(dateInput);
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText("La fecha es requerida")).toBeInTheDocument();
      });
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe("Envío del formulario", () => {
    it("debe llamar a onSubmit con los datos correctos cuando el formulario es válido", async () => {
      // Arrange
      const user = userEvent.setup();
      const defaultValues = {
        description: "",
        amount: 0,
        category: "Alimentación",
        type: "EXPENSE" as const,
        date: new Date().toISOString().split("T")[0],
      };
      render(
        <TransactionForm
          onSubmit={mockOnSubmit}
          defaultValues={defaultValues}
        />,
      );

      // Act
      const descriptionInput = screen.getByPlaceholderText("Ej: Supermercado");
      const amountInput = screen.getByPlaceholderText("0.00");

      await user.type(descriptionInput, "Supermercado compras");
      await user.clear(amountInput);
      await user.type(amountInput, "150.50");

      const submitButton = screen.getByRole("button", {
        name: /crear transacción/i,
      });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledTimes(1);
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            description: "Supermercado compras",
            amount: 150.5,
            category: "Alimentación",
            type: "EXPENSE",
            date: expect.any(String),
          }),
          expect.anything(),
        );
      });
    });

    it("debe deshabilitar el botón de envío cuando isLoading es true", () => {
      // Arrange & Act
      render(<TransactionForm onSubmit={mockOnSubmit} isLoading={true} />);

      // Assert
      const submitButton = screen.getByRole("button", { name: /creando/i });
      expect(submitButton).toBeDisabled();
    });

    it("debe mostrar texto 'Creando...' cuando isLoading es true", () => {
      // Arrange & Act
      render(<TransactionForm onSubmit={mockOnSubmit} isLoading={true} />);

      // Assert
      expect(screen.getByText("Creando...")).toBeInTheDocument();
    });
  });

  describe("Cambio de tipo de transacción", () => {
    it("debe actualizar las categorías cuando se cambia el tipo a INCOME", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const typeSelect = screen.getByRole("combobox", { name: /tipo/i });
      await user.click(typeSelect);

      const incomeOptions = await screen.findAllByText("Ingreso");
      // Click the last one (in the popover)
      await user.click(incomeOptions[incomeOptions.length - 1]);

      // Open category select to check options
      const categorySelect = screen.getByRole("combobox", {
        name: /categoría/i,
      });
      await user.click(categorySelect);

      // Assert
      await waitFor(() => {
        const salarioElements = screen.getAllByText("Salario");
        const negocioElements = screen.getAllByText("Negocio");
        const inversionesElements = screen.getAllByText("Inversiones");
        expect(salarioElements.length).toBeGreaterThan(0);
        expect(negocioElements.length).toBeGreaterThan(0);
        expect(inversionesElements.length).toBeGreaterThan(0);
      });
    });

    it("debe mantener las categorías de EXPENSE por defecto", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const categorySelect = screen.getByRole("combobox", {
        name: /categoría/i,
      });
      await user.click(categorySelect);

      // Assert
      await waitFor(() => {
        const alimentacionElements = screen.getAllByText("Alimentación");
        const transporteElements = screen.getAllByText("Transporte");
        const viviendaElements = screen.getAllByText("Vivienda");
        expect(alimentacionElements.length).toBeGreaterThan(0);
        expect(transporteElements.length).toBeGreaterThan(0);
        expect(viviendaElements.length).toBeGreaterThan(0);
      });
    });
  });

  describe("Interacción de campos", () => {
    it("debe permitir ingresar valores decimales en el campo monto", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const amountInput = screen.getByPlaceholderText("0.00");
      await user.clear(amountInput);
      await user.type(amountInput, "99.99");

      // Assert
      expect(amountInput).toHaveValue(99.99);
    });

    it("debe limpiar y establecer un valor cuando se escribe en el campo monto", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<TransactionForm onSubmit={mockOnSubmit} />);

      // Act
      const amountInput = screen.getByPlaceholderText("0.00");
      await user.clear(amountInput);
      await user.type(amountInput, "250");

      // Assert
      expect(amountInput).toHaveValue(250);
    });
  });
});
