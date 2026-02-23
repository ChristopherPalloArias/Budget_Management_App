import { render, screen } from "@testing-library/react";
import { ReportSummaryCards } from "../ReportSummaryCards";

describe("ReportSummaryCards", () => {
  describe("Cargar estado", () => {
    it("debe renderizar elementos skeleton cuando isLoading es true", () => {
      // Arrange
      const props = {
        balance: 0,
        totalIncome: 0,
        totalExpenses: 0,
        isLoading: true,
      };

      // Act
      render(<ReportSummaryCards {...props} />);

      // Assert
      const skeletons = document.querySelectorAll('[class*="animate-pulse"]');
      expect(skeletons.length).toBeGreaterThan(0);
    });
  });

  describe("Mostrar datos", () => {
    it("debe renderizar todas las cards con títulos correctos", () => {
      // Arrange
      const props = {
        balance: 100000,
        totalIncome: 500000,
        totalExpenses: 400000,
        isLoading: false,
      };

      // Act
      render(<ReportSummaryCards {...props} />);

      // Assert
      expect(screen.getByText("Balance Total")).toBeInTheDocument();
      expect(screen.getByText("Total Ingresos")).toBeInTheDocument();
      expect(screen.getByText("Total Gastos")).toBeInTheDocument();
    });

    it("debe mostrar el balance positivo con color verde", () => {
      // Arrange
      const props = {
        balance: 100000,
        totalIncome: 500000,
        totalExpenses: 400000,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportSummaryCards {...props} />);

      // Assert
      const balanceElement = container.querySelector(".text-green-600");
      expect(balanceElement).toBeInTheDocument();
      expect(balanceElement?.textContent).toContain("100.000");
    });

    it("debe mostrar el balance negativo con color rojo", () => {
      // Arrange
      const props = {
        balance: -100000,
        totalIncome: 400000,
        totalExpenses: 500000,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportSummaryCards {...props} />);

      // Assert
      const balanceElements = container.querySelectorAll(".text-red-600");
      expect(balanceElements.length).toBeGreaterThan(0);
    });

    it("debe formatear los valores de monedas correctamente", () => {
      // Arrange
      const props = {
        balance: 1500000,
        totalIncome: 2500000,
        totalExpenses: 1000000,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportSummaryCards {...props} />);

      // Assert
      expect(container.textContent).toContain("1.500.000");
      expect(container.textContent).toContain("2.500.000");
      expect(container.textContent).toContain("1.000.000");
    });

    it("debe renderizar texto descriptivo en cada card", () => {
      // Arrange
      const props = {
        balance: 100000,
        totalIncome: 500000,
        totalExpenses: 400000,
        isLoading: false,
      };

      // Act
      render(<ReportSummaryCards {...props} />);

      // Assert
      expect(screen.getByText("Balance neto del período")).toBeInTheDocument();
      expect(screen.getByText("Ingresos del período")).toBeInTheDocument();
      expect(screen.getByText("Gastos del período")).toBeInTheDocument();
    });
  });

  describe("Casos extremos", () => {
    it("debe controlar valores cero (0) correctamente", () => {
      // Arrange
      const props = {
        balance: 0,
        totalIncome: 0,
        totalExpenses: 0,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportSummaryCards {...props} />);

      // Assert
      expect(container.textContent).toContain("0");
    });

    it("debe administrar valores muy grandes correctamente", () => {
      // Arrange
      const props = {
        balance: 999999999,
        totalIncome: 999999999,
        totalExpenses: 0,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportSummaryCards {...props} />);

      // Assert
      expect(container.textContent).toContain("999.999.999");
    });
  });
});
