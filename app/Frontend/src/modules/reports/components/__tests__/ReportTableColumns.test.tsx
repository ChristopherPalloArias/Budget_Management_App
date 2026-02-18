import { reportTableColumns } from "../ReportTableColumns";

describe("ReportTableColumns", () => {
  describe("Definición de columnas", () => {
    it("debe tener el número correcto de columnas", () => {
      // Arrange & Act
      const columns = reportTableColumns;

      // Assert
      expect(columns).toHaveLength(6);
    });

    it("debe definir todos los encabezados de las columnas", () => {
      // Arrange
      const headers = reportTableColumns.map((col) => col.header);

      // Act & Assert
      expect(headers).toEqual([
        "Periodo",
        "Ingresos Totales",
        "Gastos Totales",
        "Balance Neto",
        "Fecha de Generación",
        "Ahorros",
      ]);
    });
  });

  describe("Formateo de Celdas", () => {
    it("debe llamar a las funciones de renderizado definidas", () => {
      // Arrange & Act
      const columnsWithCells = reportTableColumns.filter(
        (col) => col.cell !== undefined,
      );

      // Assert
      expect(columnsWithCells).toHaveLength(6);
    });

    it("debe definir celdas personalizadas para todas las columnas", () => {
      // Arrange & Act
      const allColumnsHaveCell = reportTableColumns.every(
        (col) => typeof col.cell === "function",
      );

      // Assert
      expect(allColumnsHaveCell).toBe(true);
    });
  });
});
