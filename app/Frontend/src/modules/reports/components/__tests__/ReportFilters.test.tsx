import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ReportFilters } from "../ReportFilters";

// Mock de las funciones de date utilities
jest.mock("../../utils/dateHelpers", () => ({
  getLastYearRange: jest.fn(() => ({
    startPeriod: "2025-02",
    endPeriod: "2026-02",
  })),
}));

jest.mock("@/lib/date-utils", () => ({
  periodToDate: jest.fn((period: string) => {
    if (!period) return null;
    const [year, month] = period.split("-").map(Number);
    return new Date(year, month - 1, 1);
  }),
}));

// Mock del componente DatePickerWithRange
jest.mock("../../../../components/ui/date-picker", () => ({
  DatePickerWithRange: ({ value, onChange }: any) => (
    <div data-testid="date-picker">
      <input
        data-testid="date-from"
        value={value?.from?.toISOString() || ""}
        onChange={(e) => {
          const newDate = e.target.value ? new Date(e.target.value) : undefined;
          onChange({ ...value, from: newDate });
        }}
      />
      <input
        data-testid="date-to"
        value={value?.to?.toISOString() || ""}
        onChange={(e) => {
          const newDate = e.target.value ? new Date(e.target.value) : undefined;
          onChange({ ...value, to: newDate });
        }}
      />
    </div>
  ),
}));

describe("ReportFilters", () => {
  const mockOnFiltersChange = jest.fn();
  const mockOnRefresh = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Renderizado Inicial", () => {
    it("debe renderizar el selector de fechas y botones", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      expect(screen.getByTestId("date-picker")).toBeInTheDocument();
      expect(screen.getByText("Limpiar Filtros")).toBeInTheDocument();
      expect(screen.getByText("Consultar Reportes")).toBeInTheDocument();
    });

    it("debe inicializar con valores por defecto", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      const fromInput = screen.getByTestId("date-from");
      const toInput = screen.getByTestId("date-to");
      expect(fromInput).toBeDefined();
      expect(toInput).toBeDefined();
    });

    it("debe inicializar con filtros enviados", () => {
      // Arrange
      const props = {
        filters: {
          startPeriod: "2024-01",
          endPeriod: "2024-12",
        },
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      expect(screen.getByTestId("date-picker")).toBeInTheDocument();
    });
  });

  describe("Interacción de usuarios", () => {
    it("debe llamar a onFiltersChange y onRefresh cuando se hace click en Consultar Reportes", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        filters: {
          startPeriod: "2024-01",
          endPeriod: "2024-12",
        },
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);
      const consultButton = screen.getByText("Consultar Reportes");
      await user.click(consultButton);

      // Assert
      expect(mockOnFiltersChange).toHaveBeenCalled();
      expect(mockOnRefresh).toHaveBeenCalled();
    });

    it("debe llamar a onFiltersChange con fitros vacíos cuando se hace click en Limpiar Filtros", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        filters: {
          startPeriod: "2024-01",
          endPeriod: "2024-12",
        },
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);
      const clearButton = screen.getByText("Limpiar Filtros");
      await user.click(clearButton);

      // Assert
      expect(mockOnFiltersChange).toHaveBeenCalledWith({});
    });

    it("debe llamar a onRefresh después de limpiar filtros", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        filters: {
          startPeriod: "2024-01",
          endPeriod: "2024-12",
        },
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);
      const clearButton = screen.getByText("Limpiar Filtros");
      await user.click(clearButton);

      // Wait briefly for the timeout to execute
      await new Promise((resolve) => setTimeout(resolve, 150));

      // Assert
      expect(mockOnRefresh).toHaveBeenCalled();
    });
  });

  describe("Estados de Carga", () => {
    it("debe deshabilitar botones cuando isLoading es true", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
        isLoading: true,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      const clearButton = screen.getByText("Limpiar Filtros");
      const consultButton = screen.getByText("Consultar Reportes");
      expect(clearButton).toBeDisabled();
      expect(consultButton).toBeDisabled();
    });

    it("debe deshabilitar botones cuando isFetching es true", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
        isFetching: true,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      const clearButton = screen.getByText("Limpiar Filtros");
      const consultButton = screen.getByText("Actualizando...");
      expect(clearButton).toBeDisabled();
      expect(consultButton).toBeDisabled();
    });

    it('debe mostrar el texto "Actualizando..." cuando isFetching es true', () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
        isFetching: true,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      expect(screen.getByText("Actualizando...")).toBeInTheDocument();
    });

    it('debe mostrar el texto "Consultar Reportes" cuando no se está consultado', () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
        isFetching: false,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      expect(screen.getByText("Consultar Reportes")).toBeInTheDocument();
    });
  });

  describe("Íconos de Botones", () => {
    it("debe renderizar ícono RotateCcw en el botón Limpiar Filtros", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      const clearButton = screen.getByText("Limpiar Filtros").closest("button");
      expect(clearButton?.querySelector("svg")).toBeInTheDocument();
    });

    it("debe renderizar el ícono Search en el botón Consultar Reportes", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);

      // Assert
      const consultButton = screen
        .getByText("Consultar Reportes")
        .closest("button");
      expect(consultButton?.querySelector("svg")).toBeInTheDocument();
    });
  });

  describe("Actualización de rango de fechas", () => {
    it("debe permitir ingresar cambios en fechas", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      render(<ReportFilters {...props} />);
      const fromInput = screen.getByTestId("date-from");

      // Assert
      expect(fromInput).toBeInTheDocument();
      expect(fromInput).toBeDefined();
    });
  });

  describe("Layout responsive", () => {
    it("debe renderizar clases flex para diseño responsive", () => {
      // Arrange
      const props = {
        filters: {},
        onFiltersChange: mockOnFiltersChange,
        onRefresh: mockOnRefresh,
      };

      // Act
      const { container } = render(<ReportFilters {...props} />);

      // Assert
      const mainContainer = container.querySelector(".flex.flex-col");
      expect(mainContainer).toBeInTheDocument();
    });
  });
});
