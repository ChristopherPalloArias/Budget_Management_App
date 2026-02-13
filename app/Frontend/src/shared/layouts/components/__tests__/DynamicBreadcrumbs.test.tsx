import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { DynamicBreadcrumbs } from "../DynamicBreadcrumbs";

describe("DynamicBreadcrumbs", () => {
  it("renders home breadcrumb for root path", () => {
    render(
      <MemoryRouter initialEntries={["/"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Inicio")).toBeInTheDocument();
  });

  it("renders breadcrumbs for transactions page", () => {
    render(
      <MemoryRouter initialEntries={["/transactions"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Transacciones")).toBeInTheDocument();
  });

  it("renders breadcrumbs for budgets page", () => {
    render(
      <MemoryRouter initialEntries={["/budgets"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Presupuestos")).toBeInTheDocument();
  });

  it("renders breadcrumbs for reports page", () => {
    render(
      <MemoryRouter initialEntries={["/reports"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Reportes")).toBeInTheDocument();
  });

  it("renders breadcrumbs for settings page", () => {
    render(
      <MemoryRouter initialEntries={["/settings"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Configuración")).toBeInTheDocument();
  });

  it("renders breadcrumbs for nested routes with capitalized segments", () => {
    render(
      <MemoryRouter initialEntries={["/transactions/create"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Transacciones")).toBeInTheDocument();
    expect(screen.getByText("Create")).toBeInTheDocument();
  });

  it("handles hyphenated routes correctly", () => {
    render(
      <MemoryRouter initialEntries={["/some-page/sub-section"]}>
        <DynamicBreadcrumbs />
      </MemoryRouter>,
    );

    expect(screen.getByText("Some page")).toBeInTheDocument();
    expect(screen.getByText("Sub section")).toBeInTheDocument();
  });
});
