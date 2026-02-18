import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ReportTable } from "../ReportTable";
import type { ReportModel } from "../../types/report.types";

const mockReports: ReportModel[] = [
  {
    id: 1,
    userId: "user1",
    period: "2024-01",
    balance: 100000,
    totalIncome: 500000,
    totalExpenses: 400000,
    savings: 50000,
    createdAt: new Date("2024-02-01"),
    updatedAt: new Date("2024-02-01"),
  },
  {
    id: 2,
    userId: "user1",
    period: "2024-02",
    balance: 200000,
    totalIncome: 600000,
    totalExpenses: 400000,
    savings: 100000,
    createdAt: new Date("2024-03-01"),
    updatedAt: new Date("2024-03-01"),
  },
  {
    id: 3,
    userId: "user1",
    period: "2024-03",
    balance: -50000,
    totalIncome: 300000,
    totalExpenses: 350000,
    savings: 0,
    createdAt: new Date("2024-04-01"),
    updatedAt: new Date("2024-04-01"),
  },
];

describe("ReportTable", () => {
  describe("Estado de carga", () => {
    it("debe renderizar elementos skeleton cuando isLoading es true", () => {
      // Arrange
      const props = {
        data: [],
        isLoading: true,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      const skeletons = document.querySelectorAll('[class*="animate-pulse"]');
      expect(skeletons.length).toBeGreaterThan(0);
    });

    it("debe renderizar las cabeceras de la tabla mientras se consulta", () => {
      // Arrange
      const props = {
        data: [],
        isLoading: true,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      expect(screen.getByText("Periodo")).toBeInTheDocument();
      expect(screen.getByText("Ingresos Totales")).toBeInTheDocument();
      expect(screen.getByText("Gastos Totales")).toBeInTheDocument();
      expect(screen.getByText("Balance Neto")).toBeInTheDocument();
      expect(screen.getByText("Fecha de Generación")).toBeInTheDocument();
      expect(screen.getByText("Ahorros")).toBeInTheDocument();
    });
  });

  describe("Mostrar Datos", () => {
    it("debe renderizar la tabla con datos correctamente", () => {
      // Arrange
      const props = {
        data: mockReports,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportTable {...props} />);

      // Assert
      const rows = container.querySelectorAll("tbody tr");
      expect(rows.length).toBe(3);
      expect(container.textContent).toContain("2024");
    });

    it("debe formatear los valores de la moneda correctamente", () => {
      // Arrange
      const props = {
        data: mockReports,
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportTable {...props} />);

      // Assert
      expect(container.textContent).toContain("500.000");
      expect(container.textContent).toContain("400.000");
    });

    it("debe aplicar el color verde cuando el balance es positivo", () => {
      // Arrange
      const props = {
        data: [mockReports[0]],
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportTable {...props} />);

      // Assert
      const positiveBalance = container.querySelector(".text-green-600");
      expect(positiveBalance).toBeInTheDocument();
    });

    it("debe aplicar el color rojo cuando el balance es positivo", () => {
      // Arrange
      const props = {
        data: [mockReports[2]],
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportTable {...props} />);

      // Assert
      const negativeBalance = container.querySelector(".text-red-600");
      expect(negativeBalance).toBeInTheDocument();
    });

    it("debe mostrar la cantidad de reportes", () => {
      // Arrange
      const props = {
        data: mockReports,
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      expect(
        screen.getByText("3 reporte(s) encontrado(s)."),
      ).toBeInTheDocument();
    });
  });

  describe("Estado vacío", () => {
    it("debe mostrar un mensaje de estatus vacío cuando no exista datos", () => {
      // Arrange
      const props = {
        data: [],
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      expect(
        screen.getByText(
          /No se encontraron reportes en el período seleccionado/i,
        ),
      ).toBeInTheDocument();
    });

    it("debe mostrar texto de ayuda cuando no se encuentran datos", () => {
      // Arrange
      const props = {
        data: [],
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      expect(
        screen.getByText(/Intenta ajustando los filtros de fecha/i),
      ).toBeInTheDocument();
    });
  });

  describe("Paginación", () => {
    const createManyReports = (count: number): ReportModel[] => {
      return Array.from({ length: count }, (_, i) => ({
        id: i + 1,
        userId: "user1",
        period: `2024-${String(i + 1).padStart(2, "0")}`,
        balance: 100000,
        totalIncome: 500000,
        totalExpenses: 400000,
        savings: 50000,
        createdAt: new Date("2024-02-01"),
        updatedAt: new Date("2024-02-01"),
      }));
    };

    it("no debe mostrar paginación cuando los datos entran en una sola página", () => {
      // Arrange
      const props = {
        data: mockReports,
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      expect(screen.queryByText(/Página/i)).not.toBeInTheDocument();
    });

    it("debe mostrar paginación cuando los datos exceden el tamaño de la página", () => {
      // Arrange
      const props = {
        data: createManyReports(15),
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      expect(screen.getByText(/Página 1 de 2/i)).toBeInTheDocument();
    });

    it("debe navegar a la siguiente página cuando el botón Siguiente se selecciona", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        data: createManyReports(15),
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);
      const nextButton = screen
        .getAllByRole("button")
        .find(
          (btn) =>
            !(btn as HTMLButtonElement).disabled &&
            btn.querySelector('svg path[d*="6.1584"]'),
        );

      if (nextButton) {
        await user.click(nextButton);
      }

      // Assert
      expect(screen.getByText(/Página 2 de 2/i)).toBeInTheDocument();
    });

    it("debe navegar a la anterior página cuando el botón Anterior se selecciona", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        data: createManyReports(15),
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Go to page 2 first
      const nextButton = screen
        .getAllByRole("button")
        .find(
          (btn) =>
            !(btn as HTMLButtonElement).disabled &&
            btn.querySelector('svg path[d*="6.1584"]'),
        );
      if (nextButton) {
        await user.click(nextButton);
      }

      // Then go back to page 1
      const prevButton = screen
        .getAllByRole("button")
        .find(
          (btn) =>
            !(btn as HTMLButtonElement).disabled &&
            btn.querySelector('svg path[d*="8.84182"]'),
        );
      if (prevButton) {
        await user.click(prevButton);
      }

      // Assert
      expect(screen.getByText(/Página 1 de 2/i)).toBeInTheDocument();
    });

    it("debe deshabilitar el botón Anterior en la primera página", () => {
      // Arrange
      const props = {
        data: createManyReports(15),
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Assert
      const prevButton = screen
        .getAllByRole("button")
        .find((btn) => btn.querySelector('svg path[d*="8.84182"]'));
      expect(prevButton).toBeDisabled();
    });

    it("debe deshabilitar el botón Siguiente en la última página", async () => {
      // Arrange
      const user = userEvent.setup();
      const props = {
        data: createManyReports(15),
        isLoading: false,
      };

      // Act
      render(<ReportTable {...props} />);

      // Navigate to last page
      const nextButton = screen
        .getAllByRole("button")
        .find(
          (btn) =>
            !(btn as HTMLButtonElement).disabled &&
            btn.querySelector('svg path[d*="6.1584"]'),
        );
      if (nextButton) {
        await user.click(nextButton);
      }

      // Assert
      const nextButtonAfter = screen
        .getAllByRole("button")
        .find((btn) => btn.querySelector('svg path[d*="6.1584"]'));
      expect(nextButtonAfter).toBeDisabled();
    });
  });

  describe("Formateo de Fecha", () => {
    it("debe renderizar la información del periodo", () => {
      // Arrange
      const props = {
        data: [mockReports[0]],
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportTable {...props} />);

      // Assert
      const periodCell = container.querySelector("tbody tr td:first-child");
      expect(periodCell).toBeInTheDocument();
      expect(periodCell?.textContent).toBeTruthy();
    });

    it("debe renderizar la fecha de creación", () => {
      // Arrange
      const props = {
        data: [mockReports[0]],
        isLoading: false,
      };

      // Act
      const { container } = render(<ReportTable {...props} />);

      // Assert
      const dateCell = container.querySelector(".text-muted-foreground");
      expect(dateCell).toBeInTheDocument();
      expect(dateCell?.textContent).toBeTruthy();
    });
  });
});
