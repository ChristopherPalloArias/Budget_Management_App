import * as React from "react";

export const useNavigate = jest.fn(() => jest.fn());

export const Navigate = jest.fn(({ to }: { to: string; replace?: boolean }) => {
  return (
    <div data-testid="redirect" data-to={to}>
      Redirecting to {to}
    </div>
  );
});

export const Link = jest.fn(
  ({
    to,
    children,
    className,
  }: {
    to: string;
    children: React.ReactNode;
    className?: string;
  }) => {
    return (
      <a href={to} className={className}>
        {children}
      </a>
    );
  },
);

export const Outlet = jest.fn(() => <div data-testid="outlet">Outlet</div>);

export const BrowserRouter = ({ children }: { children: React.ReactNode }) => (
  <>{children}</>
);

export const MemoryRouter = ({ children }: { children: React.ReactNode }) => (
  <>{children}</>
);
