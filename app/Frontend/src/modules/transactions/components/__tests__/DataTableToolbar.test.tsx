import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DataTableToolbar } from "../DataTableToolbar";

const mockOpenCreateModal = jest.fn();

jest.mock('../TransactionModalsProvider', () => ({
  useTransactionModals: () => ({
    openCreateModal: mockOpenCreateModal,
  }),
}));

describe("DataTableToolbar", () => {
  const mockOnSearchChange = jest.fn();
  const mockOnTypeFilterChange = jest.fn();
  const mockOnCategoryFilterChange = jest.fn();
  const mockOnResetFilters = jest.fn();

  const defaultProps = {
    searchQuery: "",
    onSearchChange: mockOnSearchChange,
    selectedType: new Set<string>(),
    selectedCategory: new Set<string>(),
    onTypeFilterChange: mockOnTypeFilterChange,
    onCategoryFilterChange: mockOnCategoryFilterChange,
    categories: ["Alimentación", "Transporte", "Vivienda", "Salario"],
    onResetFilters: mockOnResetFilters,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Renderizado inicial", () => {
    it("debe renderizar la barra de herramientas con todos los elementos", () => {
      // Arrange & Act
      render(<DataTableToolbar {...defaultProps} />);

      // Assert
      expect(
        screen.getByPlaceholderText("Filtrar por concepto..."),
      ).toBeInTheDocument();
      expect(screen.getByText("Tipo")).toBeInTheDocument();
      expect(screen.getByText("Categoría")).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: /nueva transacción/i }),
      ).toBeInTheDocument();
    });

    it("debe renderizar el campo de búsqueda con el valor inicial", () => {
      // Arrange
      const props = { ...defaultProps, searchQuery: "Test Query" };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      const searchInput = screen.getByPlaceholderText(
        "Filtrar por concepto...",
      );
      expect(searchInput).toHaveValue("Test Query");
    });

    it("no debe mostrar el botón 'Limpiar' cuando no hay filtros activos", () => {
      // Arrange & Act
      render(<DataTableToolbar {...defaultProps} />);

      // Assert
      expect(screen.queryByText("Limpiar")).not.toBeInTheDocument();
    });
  });

  describe("Funcionalidad de búsqueda", () => {
    it("debe llamar a onSearchChange cuando el usuario escribe en el campo de búsqueda", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableToolbar {...defaultProps} />);

      // Act
      const searchInput = screen.getByPlaceholderText(
        "Filtrar por concepto...",
      );
      await user.type(searchInput, "Supermercado");

      // Assert
      expect(mockOnSearchChange).toHaveBeenCalled();
      // Should be called for each character typed
      expect(mockOnSearchChange).toHaveBeenCalledWith(expect.any(String));
    });

    it("debe actualizar el valor del input cuando cambia searchQuery", () => {
      // Arrange
      const { rerender } = render(<DataTableToolbar {...defaultProps} />);

      // Act
      rerender(
        <DataTableToolbar {...defaultProps} searchQuery="Nueva búsqueda" />,
      );

      // Assert
      const searchInput = screen.getByPlaceholderText(
        "Filtrar por concepto...",
      );
      expect(searchInput).toHaveValue("Nueva búsqueda");
    });
  });

  describe("Filtros de tipo", () => {
    it("debe mostrar el filtro de tipo con las opciones correctas", async () => {
      // Arrange & Act
      const user = userEvent.setup();
      render(<DataTableToolbar {...defaultProps} />);

      const typeButton = screen.getByText("Tipo");
      await user.click(typeButton);

      // Assert
      expect(screen.getByText("Ingreso")).toBeInTheDocument();
      expect(screen.getByText("Egreso")).toBeInTheDocument();
    });

    it("debe mostrar el contador cuando hay filtros de tipo seleccionados", () => {
      // Arrange
      const props = {
        ...defaultProps,
        selectedType: new Set(["INCOME"]),
      };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      expect(screen.getByText("1")).toBeInTheDocument();
    });
  });

  describe("Filtros de categoría", () => {
    it("debe mostrar el filtro de categoría", () => {
      // Arrange & Act
      render(<DataTableToolbar {...defaultProps} />);

      // Assert
      expect(screen.getByText("Categoría")).toBeInTheDocument();
    });

    it("debe mostrar el contador cuando hay categorías seleccionadas", () => {
      // Arrange
      const props = {
        ...defaultProps,
        selectedCategory: new Set(["Alimentación", "Transporte"]),
      };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      expect(screen.getByText("2")).toBeInTheDocument();
    });
  });

  describe("Botón de limpiar filtros", () => {
    it("debe mostrar el botón 'Limpiar' cuando hay filtros de tipo activos", () => {
      // Arrange
      const props = {
        ...defaultProps,
        selectedType: new Set(["INCOME"]),
      };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      expect(screen.getByText("Limpiar")).toBeInTheDocument();
    });

    it("debe mostrar el botón 'Limpiar' cuando hay filtros de categoría activos", () => {
      // Arrange
      const props = {
        ...defaultProps,
        selectedCategory: new Set(["Alimentación"]),
      };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      expect(screen.getByText("Limpiar")).toBeInTheDocument();
    });

    it("debe mostrar el botón 'Limpiar' cuando hay múltiples filtros activos", () => {
      // Arrange
      const props = {
        ...defaultProps,
        selectedType: new Set(["INCOME"]),
        selectedCategory: new Set(["Salario"]),
      };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      expect(screen.getByText("Limpiar")).toBeInTheDocument();
    });

    it("debe llamar a onResetFilters cuando se hace clic en el botón 'Limpiar'", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        ...defaultProps,
        selectedType: new Set(["INCOME"]),
      };
      render(<DataTableToolbar {...props} />);

      // Act
      const clearButton = screen.getByText("Limpiar");
      await user.click(clearButton);

      // Assert
      expect(mockOnResetFilters).toHaveBeenCalledTimes(1);
    });
  });

  describe("Botón de nueva transacción", () => {
    it("debe renderizar el botón de nueva transacción", () => {
      // Arrange & Act
      render(<DataTableToolbar {...defaultProps} />);

      // Assert
      const createButton = screen.getByRole("button", {
        name: /nueva transacción/i,
      });
      expect(createButton).toBeInTheDocument();
    });

    it("debe llamar a onCreateTransaction cuando se hace clic", async () => {
      // Arrange
      const user = userEvent.setup();
      render(<DataTableToolbar {...defaultProps} />);

      // Act
      const createButton = screen.getByRole("button", {
        name: /nueva transacción/i,
      });
      await user.click(createButton);

      // Assert
      expect(mockOpenCreateModal).toHaveBeenCalledTimes(1);
    });

    it("debe mostrar el icono Plus en el botón", () => {
      // Arrange & Act
      const { container } = render(<DataTableToolbar {...defaultProps} />);

      // Assert
      const plusIcon = container.querySelector("svg");
      expect(plusIcon).toBeInTheDocument();
    });
  });

  describe("Estados de filtros combinados", () => {
    it("debe mantener la búsqueda y los filtros independientes", () => {
      // Arrange
      const props = {
        ...defaultProps,
        searchQuery: "Test",
        selectedType: new Set(["INCOME"]),
        selectedCategory: new Set(["Salario"]),
      };

      // Act
      render(<DataTableToolbar {...props} />);

      // Assert
      const searchInput = screen.getByPlaceholderText(
        "Filtrar por concepto...",
      );
      expect(searchInput).toHaveValue("Test");
      expect(screen.getByText("Limpiar")).toBeInTheDocument();
      const counters = screen.getAllByText("1");
      expect(counters).toHaveLength(2); // Type and Category counters
    });
  });

  describe("Responsive behavior", () => {
    it("debe aplicar las clases CSS responsivas correctas al input de búsqueda", () => {
      // Arrange & Act
      render(<DataTableToolbar {...defaultProps} />);

      // Assert
      const searchInput = screen.getByPlaceholderText(
        "Filtrar por concepto...",
      );
      expect(searchInput.classList.contains("w-[150px]")).toBe(true);
      expect(searchInput.classList.contains("lg:w-[250px]")).toBe(true);
    });
  });
});
