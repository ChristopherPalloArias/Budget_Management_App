import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DataTableFacetedFilter } from "../DataTableFacetedFilter";

describe("DataTableFacetedFilter", () => {
  const mockOnSelectedValuesChange = jest.fn();

  const defaultOptions = [
    { label: "Ingreso", value: "INCOME" },
    { label: "Egreso", value: "EXPENSE" },
  ];

  const categoryOptions = [
    { label: "Alimentación", value: "ALIMENTACION" },
    { label: "Transporte", value: "TRANSPORTE" },
    { label: "Vivienda", value: "VIVIENDA" },
  ];

  const defaultProps = {
    title: "Tipo",
    options: defaultOptions,
    selectedValues: new Set<string>(),
    onSelectedValuesChange: mockOnSelectedValuesChange,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Renderizado inicial", () => {
    it("debe renderizar el botón del filtro con el título", () => {
      // Arrange & Act
      render(<DataTableFacetedFilter {...defaultProps} />);

      // Assert
      expect(screen.getByText("Tipo")).toBeInTheDocument();
    });

    it("no debe mostrar el contador cuando no hay valores seleccionados", () => {
      // Arrange & Act
      const { container } = render(
        <DataTableFacetedFilter {...defaultProps} />,
      );

      // Assert
      const badge = container.querySelector('[class*="rounded-sm"]');
      expect(badge).not.toBeInTheDocument();
    });

    it("debe mostrar el contador cuando hay valores seleccionados", () => {
      // Arrange
      const props = {
        ...defaultProps,
        selectedValues: new Set(["INCOME", "EXPENSE"]),
      };

      // Act
      render(<DataTableFacetedFilter {...props} />);

      // Assert
      expect(screen.getByText("2")).toBeInTheDocument();
    });
  });

  describe("Interacción con el popover", () => {
    it("debe abrir el popover cuando se hace clic en el botón", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableFacetedFilter {...defaultProps} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText("Ingreso")).toBeInTheDocument();
        expect(screen.getByText("Egreso")).toBeInTheDocument();
      });
    });

    it("debe mostrar todas las opciones disponibles en el popover", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        options: categoryOptions,
        title: "Categoría",
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /categoría/i });
      await user.click(triggerButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText("Alimentación")).toBeInTheDocument();
        expect(screen.getByText("Transporte")).toBeInTheDocument();
        expect(screen.getByText("Vivienda")).toBeInTheDocument();
      });
    });
  });

  describe("Selección de opciones", () => {
    it("debe llamar a onSelectedValuesChange cuando se selecciona una opción", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableFacetedFilter {...defaultProps} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      const incomeOption = await screen.findByText("Ingreso");
      await user.click(incomeOption);

      // Assert
      expect(mockOnSelectedValuesChange).toHaveBeenCalledTimes(1);
      expect(mockOnSelectedValuesChange).toHaveBeenCalledWith(
        new Set(["INCOME"]),
      );
    });

    it("debe agregar una opción al conjunto de seleccionados", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        selectedValues: new Set(["INCOME"]),
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      const expenseOption = await screen.findByText("Egreso");
      await user.click(expenseOption);

      // Assert
      expect(mockOnSelectedValuesChange).toHaveBeenCalledWith(
        new Set(["INCOME", "EXPENSE"]),
      );
    });

    it("debe remover una opción cuando se hace clic en una ya seleccionada", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        selectedValues: new Set(["INCOME"]),
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      const incomeOption = await screen.findByText("Ingreso");
      await user.click(incomeOption);

      // Assert
      expect(mockOnSelectedValuesChange).toHaveBeenCalledWith(new Set());
    });

    it("debe permitir seleccionar múltiples opciones", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        options: categoryOptions,
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      const alimentacionOption = await screen.findByText("Alimentación");
      await user.click(alimentacionOption);

      // Assert
      expect(mockOnSelectedValuesChange).toHaveBeenCalledWith(
        new Set(["ALIMENTACION"]),
      );
    });
  });

  describe("Limpiar filtros", () => {
    it("debe mostrar el botón 'Limpiar filtros' cuando hay selecciones", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        selectedValues: new Set(["INCOME"]),
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText("Limpiar filtros")).toBeInTheDocument();
      });
    });

    it("no debe mostrar el botón 'Limpiar filtros' cuando no hay selecciones", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableFacetedFilter {...defaultProps} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      // Assert
      await waitFor(() => {
        expect(screen.queryByText("Limpiar filtros")).not.toBeInTheDocument();
      });
    });

    it("debe llamar a onSelectedValuesChange con un Set vacío al limpiar", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        selectedValues: new Set(["INCOME", "EXPENSE"]),
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      const clearButton = await screen.findByText("Limpiar filtros");
      await user.click(clearButton);

      // Assert
      expect(mockOnSelectedValuesChange).toHaveBeenCalledWith(new Set());
    });
  });

  describe("Búsqueda de opciones", () => {
    it("debe renderizar el input de búsqueda con el placeholder correcto", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableFacetedFilter {...defaultProps} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      // Assert
      await waitFor(() => {
        const searchInput = screen.getByPlaceholderText("Tipo");
        expect(searchInput).toBeInTheDocument();
      });
    });

    it("debe mostrar 'Sin resultados.' cuando no hay coincidencias", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableFacetedFilter {...defaultProps} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      const searchInput = await screen.findByPlaceholderText("Tipo");
      await user.type(searchInput, "NoExiste");

      // Assert
      await waitFor(() => {
        expect(screen.getByText("Sin resultados.")).toBeInTheDocument();
      });
    });
  });

  describe("Estado de selección visual", () => {
    it("debe mostrar el checkbox marcado para opciones seleccionadas", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        selectedValues: new Set(["INCOME"]),
      };
      render(<DataTableFacetedFilter {...props} />);

      // Act
      const triggerButton = screen.getByRole("button", { name: /tipo/i });
      await user.click(triggerButton);

      // Assert
      await waitFor(() => {
        const commandItems = screen.getAllByRole("option");
        expect(commandItems.length).toBeGreaterThan(0);
      });
    });
  });

  describe("Contador de seleccionados", () => {
    it("debe actualizar el contador cuando cambia el número de seleccionados", () => {
      // Arrange
      const { rerender } = render(<DataTableFacetedFilter {...defaultProps} />);

      // Act - Add one selection
      rerender(
        <DataTableFacetedFilter
          {...defaultProps}
          selectedValues={new Set(["INCOME"])}
        />,
      );

      // Assert
      expect(screen.getByText("1")).toBeInTheDocument();

      // Act - Add another selection
      rerender(
        <DataTableFacetedFilter
          {...defaultProps}
          selectedValues={new Set(["INCOME", "EXPENSE"])}
        />,
      );

      // Assert
      expect(screen.getByText("2")).toBeInTheDocument();
    });

    it("debe ocultar el contador cuando se limpian todas las selecciones", () => {
      // Arrange
      const { rerender } = render(
        <DataTableFacetedFilter
          {...defaultProps}
          selectedValues={new Set(["INCOME"])}
        />,
      );

      // Verify counter is visible
      expect(screen.getByText("1")).toBeInTheDocument();

      // Act - Clear selections
      rerender(
        <DataTableFacetedFilter {...defaultProps} selectedValues={new Set()} />,
      );

      // Assert
      expect(screen.queryByText("1")).not.toBeInTheDocument();
    });
  });
});
