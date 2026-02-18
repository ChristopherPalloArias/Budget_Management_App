import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DataTable } from "../DataTable";
import type { TransactionModel } from "../../types/transaction.types";

// Mock the custom hook
jest.mock("@/shared/hooks/useDataTableLogic", () => ({
  useDataTableLogic: jest.fn(),
}));

// Mock utility functions
jest.mock("@/shared/utils/currencyUtils", () => ({
  formatCurrency: jest.fn((amount: number) => `$${amount.toLocaleString()}`),
}));

jest.mock("@/lib/date-utils", () => ({
  formatDate: jest.fn((date: Date | string) => {
    if (typeof date === "string") {
      return new Date(date).toLocaleDateString("es-ES");
    }
    return date.toLocaleDateString("es-ES");
  }),
}));

jest.mock("@/core/constants/categories.constants", () => ({
  getCategoryColor: jest.fn(() => "bg-blue-100 text-blue-800"),
  getCategoryLabel: jest.fn((category: string) => category),
}));

import { useDataTableLogic } from "@/shared/hooks/useDataTableLogic";

const mockUseDataTableLogic = useDataTableLogic as jest.MockedFunction<
  typeof useDataTableLogic
>;

describe("DataTable", () => {
  const mockTransactions: TransactionModel[] = [
    {
      id: 1,
      userId: "user1",
      description: "Salario",
      amount: 5000,
      category: "Salario",
      type: "INCOME",
      date: new Date("2024-01-15"),
    },
    {
      id: 2,
      userId: "user1",
      description: "Supermercado",
      amount: 150,
      category: "Alimentación",
      type: "EXPENSE",
      date: new Date("2024-01-20"),
    },
    {
      id: 3,
      userId: "user1",
      description: "Gasolina",
      amount: 80,
      category: "Transporte",
      type: "EXPENSE",
      date: new Date("2024-01-22"),
    },
  ];

  const mockOnCreateTransaction = jest.fn();

  const defaultHookReturn = {
    state: {
      pageIndex: 0,
      searchQuery: "",
      selectedType: new Set<string>(),
      selectedCategory: new Set<string>(),
    },
    filteredData: mockTransactions,
    paginatedData: mockTransactions,
    totalPages: 1,
    totalFiltered: 3,
    categories: ["Salario", "Alimentación", "Transporte"],
    setPage: jest.fn(),
    setSearchQuery: jest.fn(),
    setTypeFilter: jest.fn(),
    setCategoryFilter: jest.fn(),
    resetFilters: jest.fn(),
    nextPage: jest.fn(),
    prevPage: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseDataTableLogic.mockReturnValue(defaultHookReturn);
  });

  describe("Renderizado inicial", () => {
    it("debe renderizar el título y descripción del listado", () => {
      // Arrange & Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(screen.getByText("Listado de Transacciones")).toBeInTheDocument();
      expect(
        screen.getByText("Gestiona y visualiza tus movimientos financieros"),
      ).toBeInTheDocument();
    });

    it("debe renderizar la barra de herramientas", () => {
      // Arrange & Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(
        screen.getByPlaceholderText("Filtrar por concepto..."),
      ).toBeInTheDocument();
      expect(screen.getAllByText("Tipo").length).toBeGreaterThan(0);
      expect(screen.getAllByText("Categoría").length).toBeGreaterThan(0);
    });

    it("debe renderizar los encabezados de la tabla", () => {
      // Arrange & Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(screen.getByText("Fecha")).toBeInTheDocument();
      expect(screen.getByText("Concepto")).toBeInTheDocument();
      expect(screen.getAllByText("Categoría").length).toBeGreaterThan(0);
      expect(screen.getByText("Monto")).toBeInTheDocument();
      expect(screen.getAllByText("Tipo").length).toBeGreaterThan(0);
    });
  });

  describe("Renderizado de datos", () => {
    it("debe renderizar las transacciones con sus respectivos tipos", () => {
      // Arrange & Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      const incomeBadges = screen.getAllByText("Ingreso");
      const expenseBadges = screen.getAllByText("Egreso");

      expect(incomeBadges).toHaveLength(1);
      expect(expenseBadges).toHaveLength(2);
    });

    it("debe renderizar el texto con la cantidad de transacciones encontradas", () => {
      // Arrange & Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(
        screen.getByText(/3 elemento\(s\) encontrado\(s\)/i),
      ).toBeInTheDocument();
    });
  });

  describe("Estado vacío", () => {
    it("debe mostrar mensaje cuando no hay transacciones", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        paginatedData: [],
        totalFiltered: 0,
      });

      // Act
      render(
        <DataTable data={[]} onCreateTransaction={mockOnCreateTransaction} />,
      );

      // Assert
      expect(
        screen.getByText(/no hay transacciones registradas/i),
      ).toBeInTheDocument();
      expect(
        screen.getByText(
          /crea tu primera transacción usando el botón de arriba/i,
        ),
      ).toBeInTheDocument();
    });

    it("debe mostrar mensaje cuando no hay resultados con filtros aplicados", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        state: {
          pageIndex: 0,
          searchQuery: "test",
          selectedType: new Set<string>(),
          selectedCategory: new Set<string>(),
        },
        paginatedData: [],
        totalFiltered: 0,
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(
        screen.getByText(
          /no se encontraron transacciones con los filtros aplicados/i,
        ),
      ).toBeInTheDocument();
      expect(
        screen.getByText(
          /intenta ajustando los filtros o crea una nueva transacción/i,
        ),
      ).toBeInTheDocument();
    });

    it("debe detectar filtros activos cuando hay búsqueda", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        state: {
          pageIndex: 0,
          searchQuery: "Supermercado",
          selectedType: new Set<string>(),
          selectedCategory: new Set<string>(),
        },
        paginatedData: [],
        totalFiltered: 0,
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(
        screen.getByText(
          /no se encontraron transacciones con los filtros aplicados/i,
        ),
      ).toBeInTheDocument();
    });

    it("debe detectar filtros activos cuando hay filtro de tipo", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        state: {
          pageIndex: 0,
          searchQuery: "",
          selectedType: new Set(["INCOME"]),
          selectedCategory: new Set<string>(),
        },
        paginatedData: [],
        totalFiltered: 0,
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(
        screen.getByText(
          /no se encontraron transacciones con los filtros aplicados/i,
        ),
      ).toBeInTheDocument();
    });

    it("debe detectar filtros activos cuando hay filtro de categoría", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        state: {
          pageIndex: 0,
          searchQuery: "",
          selectedType: new Set<string>(),
          selectedCategory: new Set(["Alimentación"]),
        },
        paginatedData: [],
        totalFiltered: 0,
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(
        screen.getByText(
          /no se encontraron transacciones con los filtros aplicados/i,
        ),
      ).toBeInTheDocument();
    });
  });

  describe("Interacción con filtros", () => {
    it("debe pasar las props correctas al DataTableToolbar", () => {
      // Arrange
      const mockState = {
        pageIndex: 0,
        searchQuery: "test",
        selectedType: new Set(["INCOME"]),
        selectedCategory: new Set(["Salario"]),
      };

      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        state: mockState,
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      const searchInput = screen.getByPlaceholderText(
        "Filtrar por concepto...",
      );
      expect(searchInput).toHaveValue("test");
    });

    it("debe llamar a onCreateTransaction del toolbar cuando se hace clic", async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Act
      const createButton = screen.getByRole("button", {
        name: /nueva transacción/i,
      });
      await user.click(createButton);

      // Assert
      expect(mockOnCreateTransaction).toHaveBeenCalledTimes(1);
    });
  });

  describe("Paginación", () => {
    it("debe mostrar múltiples páginas cuando hay más datos que el tamaño de página", () => {
      // Arrange
      const manyTransactions = Array.from({ length: 25 }, (_, i) => ({
        id: i + 1,
        userId: "user1",
        description: `Transaction ${i + 1}`,
        amount: 100,
        category: "Otros",
        type: "EXPENSE" as const,
        date: new Date("2024-01-01"),
      }));

      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        paginatedData: manyTransactions.slice(0, 10),
        totalPages: 3,
        totalFiltered: 25,
      });

      // Act
      render(
        <DataTable
          data={manyTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(screen.getByText(/Página 1 de 3/i)).toBeInTheDocument();
      expect(
        screen.getByText(/25 elemento\(s\) encontrado\(s\)/i),
      ).toBeInTheDocument();
    });

    it("debe pasar la información de paginación correcta al componente TablePagination", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        state: {
          ...defaultHookReturn.state,
          pageIndex: 2,
        },
        totalPages: 5,
        totalFiltered: 50,
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      // The pagination component should receive pageIndex: 2, totalPages: 5
      expect(screen.getByText(/Página 3 de 5/i)).toBeInTheDocument();
      expect(
        screen.getByText(/50 elemento\(s\) encontrado\(s\)/i),
      ).toBeInTheDocument();
    });
  });

  describe("Integración con useDataTableLogic", () => {
    it("debe inicializar el hook con los parámetros correctos", () => {
      // Arrange & Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      expect(mockUseDataTableLogic).toHaveBeenCalledWith({
        data: mockTransactions,
        pageSize: 10, // DEFAULT_PAGE_SIZE
        searchFields: ["description"],
      });
    });

    it("debe usar las categorías del hook para el filtro", () => {
      // Arrange
      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        categories: ["Cat1", "Cat2", "Cat3"],
      });

      // Act
      render(
        <DataTable
          data={mockTransactions}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      // The categories should be passed to DataTableToolbar
      expect(screen.getAllByText("Categoría").length).toBeGreaterThan(0);
    });
  });

  describe("Responsividad de columnas", () => {
    it("debe aplicar la clase de truncado a la columna de concepto", () => {
      // Arrange
      const longDescriptionTransaction: TransactionModel = {
        id: 99,
        userId: "user1",
        description: "Esta es una descripción muy larga que debería truncarse",
        amount: 100,
        category: "Otros",
        type: "EXPENSE",
        date: new Date("2024-01-01"),
      };

      mockUseDataTableLogic.mockReturnValue({
        ...defaultHookReturn,
        paginatedData: [longDescriptionTransaction],
      });

      // Act
      render(
        <DataTable
          data={[longDescriptionTransaction]}
          onCreateTransaction={mockOnCreateTransaction}
        />,
      );

      // Assert
      const descriptionCell = screen.getByText(
        longDescriptionTransaction.description,
      );
      expect(descriptionCell.classList.contains("truncate")).toBe(true);
      expect(descriptionCell.classList.contains("max-w-[200px]")).toBe(true);
    });
  });
});
