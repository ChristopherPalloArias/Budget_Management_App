import { render, screen } from "@testing-library/react";

// Componente de ejemplo para testing
const ExampleComponent = ({ name }: { name: string }) => {
  return (
    <div>
      <h1>Hello, {name}!</h1>
      <button>Click me</button>
    </div>
  );
};

describe("ExampleComponent", () => {
  it("should render the component with the correct name", () => {
    render(<ExampleComponent name="World" />);

    const heading = screen.getByText("Hello, World!");
    expect(heading).toBeInTheDocument();
  });

  it("should render a button", () => {
    render(<ExampleComponent name="Jest" />);

    const button = screen.getByRole<HTMLButtonElement>("button", {
      name: /click me/i,
    });
    expect(button).toBeInTheDocument();
  });
});

// Ejemplo de prueba de función pura
describe("Math operations", () => {
  it("should add two numbers correctly", () => {
    const result = 2 + 2;
    expect(result).toBe(4);
  });

  it("should multiply numbers correctly", () => {
    const multiply = (a: number, b: number) => a * b;
    expect(multiply(3, 4)).toBe(12);
  });
});
