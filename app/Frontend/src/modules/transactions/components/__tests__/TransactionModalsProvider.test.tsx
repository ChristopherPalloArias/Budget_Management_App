import { render, screen, act } from '@testing-library/react';
import { TransactionModalsProvider, useTransactionModals } from '../TransactionModalsProvider';
import { useTransactionOperations } from '../../hooks/useTransactionOperations';
import { useDeleteTransaction } from '../../hooks/useDeleteTransaction';
import { useUserStore } from '@/modules/auth';

// Mock hooks
jest.mock('../../hooks/useTransactionOperations');
jest.mock('../../hooks/useDeleteTransaction');
jest.mock('@/modules/auth');

// Mock components to simplify
jest.mock('../TransactionForm', () => ({
  TransactionForm: ({ onSubmit }: any) => (
    <button onClick={() => onSubmit({ description: 'Test', amount: 100, category: 'FOOD', type: 'EXPENSE', date: '2023-01-01' })}>
      Submit Form
    </button>
  ),
}));

jest.mock('../DeleteTransactionDialog', () => ({
  DeleteTransactionDialog: ({ isOpen, onConfirm }: any) => (
    isOpen ? <button onClick={onConfirm}>Confirm Delete</button> : null
  ),
}));

// Mock shadcn Dialog
jest.mock('@/components/ui/dialog', () => ({
  Dialog: ({ children, open }: any) => open ? <div>{children}</div> : null,
  DialogContent: ({ children }: any) => <div>{children}</div>,
  DialogHeader: ({ children }: any) => <div>{children}</div>,
  DialogTitle: ({ children }: any) => <div>{children}</div>,
}));

const TestComponent = () => {
  const { openCreateModal, openEditModal, openDeleteModal } = useTransactionModals();
  return (
    <div>
      <button onClick={openCreateModal}>Open Create</button>
      <button onClick={() => openEditModal({ id: 1, description: 'Edit Me' } as any)}>Open Edit</button>
      <button onClick={() => openDeleteModal({ id: 1, description: 'Delete Me' } as any)}>Open Delete</button>
    </div>
  );
};

describe('TransactionModalsProvider', () => {
  const mockCreateTransaction = jest.fn();
  const mockEditTransaction = jest.fn();
  const mockDeleteMutate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useUserStore as unknown as jest.Mock).mockReturnValue({ user: { id: 'user1' } });
    (useTransactionOperations as jest.Mock).mockReturnValue({
      createTransaction: mockCreateTransaction,
      editTransaction: mockEditTransaction,
      isLoading: false,
    });
    (useDeleteTransaction as jest.Mock).mockReturnValue({
      mutate: mockDeleteMutate,
      isPending: false,
    });
  });

  it('should open and submit create modal', async () => {
    mockCreateTransaction.mockResolvedValue({ success: true });
    
    render(
      <TransactionModalsProvider>
        <TestComponent />
      </TransactionModalsProvider>
    );

    act(() => {
      screen.getByText('Open Create').click();
    });

    expect(screen.getByText('Nueva Transacción')).toBeDefined();

    await act(async () => {
      screen.getByText('Submit Form').click();
    });

    expect(mockCreateTransaction).toHaveBeenCalled();
  });

  it('should open and submit edit modal', async () => {
    mockEditTransaction.mockResolvedValue({ success: true });
    
    render(
      <TransactionModalsProvider>
        <TestComponent />
      </TransactionModalsProvider>
    );

    act(() => {
      screen.getByText('Open Edit').click();
    });

    expect(screen.getByText('Editar Transacción')).toBeDefined();

    await act(async () => {
      screen.getByText('Submit Form').click();
    });

    expect(mockEditTransaction).toHaveBeenCalledWith("1", expect.anything());
  });

  it('should open and confirm delete modal', () => {
    render(
      <TransactionModalsProvider>
        <TestComponent />
      </TransactionModalsProvider>
    );

    act(() => {
      screen.getByText('Open Delete').click();
    });

    act(() => {
      screen.getByText('Confirm Delete').click();
    });

    expect(mockDeleteMutate).toHaveBeenCalledWith(1, expect.anything());
  });
});
