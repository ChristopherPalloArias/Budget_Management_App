import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from './core/config/queryClient';
import { AuthProvider } from './modules/auth/components/AuthProvider';
import { AppRouter } from './core/router/AppRouter';
import HttpClient from './core/api/HttpClient';
import { LocalStorageTokenProvider } from './core/api/TokenProvider';
import './index.css';

import { Toaster } from './components/ui/sonner';

// Inject TokenProvider into HttpClient (DIP implementation)
HttpClient.setTokenProvider(new LocalStorageTokenProvider());

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AppRouter />
        <Toaster />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>
);

