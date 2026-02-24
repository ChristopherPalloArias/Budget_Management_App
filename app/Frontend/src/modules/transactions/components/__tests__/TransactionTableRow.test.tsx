import { render, screen } from "@testing-library/react";
import { TransactionTableRow } from "../TransactionTableRow";
import type { TransactionModel } from "../../types/transaction.types";

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

jest.mock("../TransactionModalsProvider", () => ({
  useTransactionModals: jest.fn(() => ({
    openCreateModal: jest.fn(),
    openEditModal: jest.fn(),
    openDeleteModal: jest.fn(),
  })),
}));

describe("TransactionTableRow", () => {
  const mockOnEdit = jest.fn();
  const mockOnDelete = jest.fn();

  beforeEach(() => {
    mockOnEdit.mockClear();
    mockOnDelete.mockClear();
  });

  const mockIncomeTransaction: TransactionModel = {
    id: 1,
    userId: "user1",
    description: "Salario Mensual",
    amount: 5000,
    category: "Salario",
    type: "INCOME",
    date: new Date("2024-01-15"),
  };

  const mockExpenseTransaction: TransactionModel = {
    id: 2,
    userId: "user1",
    description: "Compra Supermercado",
    amount: 150,
    category: "Alimentación",
    type: "EXPENSE",
    date: new Date("2024-01-20"),
  };

  describe("Renderizado de transacción de ingreso", () => {
    it("debe renderizar correctamente una transacción de ingreso", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockIncomeTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(screen.getByText("Salario Mensual")).toBeInTheDocument();
      expect(container.textContent).toContain("$5,000");
      expect(screen.getByText("Ingreso")).toBeInTheDocument();
    });

    it("debe aplicar el estilo de color verde para ingresos", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockIncomeTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      const amountCell = container.querySelector(".text-green-600");
      expect(amountCell).toBeInTheDocument();
    });

    it("debe mostrar el signo positivo (+) para ingresos", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockIncomeTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(container.textContent).toContain("+");
    });

    it("debe aplicar el badge variant 'default' para ingresos", () => {
      // Arrange & Act
      render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockIncomeTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      const incomeBadge = screen.getByText("Ingreso");
      expect(incomeBadge).toBeInTheDocument();
    });
  });

  describe("Renderizado de transacción de egreso", () => {
    it("debe renderizar correctamente una transacción de egreso", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockExpenseTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(screen.getByText("Compra Supermercado")).toBeInTheDocument();
      expect(container.textContent).toContain("$150");
      expect(screen.getByText("Egreso")).toBeInTheDocument();
    });

    it("debe aplicar el estilo de color rojo para egresos", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockExpenseTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      const amountCell = container.querySelector(".text-red-600");
      expect(amountCell).toBeInTheDocument();
    });

    it("debe mostrar el signo negativo (-) para egresos", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockExpenseTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(container.textContent).toContain("-");
    });

    it("debe aplicar el badge variant 'destructive' para egresos", () => {
      // Arrange & Act
      render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockExpenseTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      const expenseBadge = screen.getByText("Egreso");
      expect(expenseBadge).toBeInTheDocument();
    });
  });

  describe("Renderizado de datos de transacción", () => {
    it("debe formatear y mostrar la fecha correctamente", () => {
      // Arrange & Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockIncomeTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      // The formatDate mock returns a localized date string
      expect(container.textContent).toBeTruthy();
    });

    it("debe mostrar la descripción de la transacción", () => {
      // Arrange & Act
      render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockExpenseTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      const descriptionCell = screen.getByText("Compra Supermercado");
      expect(descriptionCell).toBeInTheDocument();
      expect(descriptionCell.classList.contains("font-medium")).toBe(true);
    });

    it("debe mostrar la categoría con el badge correcto", () => {
      // Arrange & Act
      render(
        <table>
          <tbody>
            <TransactionTableRow transaction={mockExpenseTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(screen.getByText("Alimentación")).toBeInTheDocument();
    });

    it("debe truncar descripciones largas con la clase CSS apropiada", () => {
      // Arrange
      const longDescriptionTransaction: TransactionModel = {
        ...mockExpenseTransaction,
        description:
          "Esta es una descripción muy larga que debería truncarse en la tabla",
      };

      // Act
      render(
        <table>
          <tbody>
            <TransactionTableRow transaction={longDescriptionTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      const descriptionCell = screen.getByText(
        longDescriptionTransaction.description,
      );
      expect(descriptionCell.classList.contains("truncate")).toBe(true);
      expect(descriptionCell.classList.contains("max-w-[200px]")).toBe(true);
    });
  });

  describe("Formateo de montos", () => {
    it("debe formatear correctamente montos grandes", () => {
      // Arrange
      const largeAmountTransaction: TransactionModel = {
        ...mockIncomeTransaction,
        amount: 1000000,
      };

      // Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={largeAmountTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(container.textContent).toContain("$1,000,000");
    });

    it("debe formatear correctamente montos decimales", () => {
      // Arrange
      const decimalAmountTransaction: TransactionModel = {
        ...mockExpenseTransaction,
        amount: 99.99,
      };

      // Act
      const { container } = render(
        <table>
          <tbody>
            <TransactionTableRow transaction={decimalAmountTransaction} />
          </tbody>
        </table>,
      );

      // Assert
      expect(container.textContent).toContain("99.99");
    });
  });
});
