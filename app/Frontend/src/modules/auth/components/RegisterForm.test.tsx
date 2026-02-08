import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { RegisterForm } from './RegisterForm';

// Mock the auth service
vi.mock('../services/authService.ts', () => ({
  registerWithEmail: vi.fn(),
}));

describe('RegisterForm', () => {
  it('renders register form with all fields', () => {
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    expect(screen.getByLabelText(/Nombre Completo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Correo Electrónico/i)).toBeInTheDocument();
    expect(screen.getAllByLabelText(/Contraseña/i).length).toBeGreaterThan(0);
    expect(screen.getByRole('button', { name: /Crear Cuenta/i })).toBeInTheDocument();
  });

  it('renders link to login page', () => {
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    expect(screen.getByRole('link', { name: /Inicia sesión aquí/i })).toBeInTheDocument();
  });
});
