import HttpClient from '../../../../core/api/HttpClient';
import { getTransactionsByUser, createTransaction, updateTransaction, deleteTransaction } from '../transactionService';
import { API_ENDPOINTS } from '@/core/constants/app.constants';

// Mock HttpClient
const mockTransactionsHttpClient = HttpClient.getInstance('transactions');

describe('transactionService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getTransactionsByUser', () => {
    it('should call GET /v1/transactions and return adapted data', async () => {
      const mockResponse = {
        data: {
          content: [
            {
              transactionId: 1,
              userId: 'u1',
              description: 'Income',
              amount: 1000,
              type: 'INCOME',
              category: 'SALARY',
              date: '2023-01-01'
            }
          ]
        }
      };
      
      (mockTransactionsHttpClient.get as jest.Mock).mockResolvedValue(mockResponse);
      
      const result = await getTransactionsByUser();
      
      expect(mockTransactionsHttpClient.get).toHaveBeenCalledWith(API_ENDPOINTS.TRANSACTIONS);
      expect(result).toHaveLength(1);
      expect(result[0].id).toBe(1);
    });

    it('should call GET /v1/transactions with period if provided', async () => {
        (mockTransactionsHttpClient.get as jest.Mock).mockResolvedValue({ data: { content: [] } });
        
        await getTransactionsByUser('2023-01');
        
        expect(mockTransactionsHttpClient.get).toHaveBeenCalledWith(
            `${API_ENDPOINTS.TRANSACTIONS}?period=2023-01`
        );
    });
  });

  describe('createTransaction', () => {
    it('should call POST /v1/transactions and return adapted data', async () => {
      const mockResponse = {
        data: {
          transactionId: 2,
          description: 'Expense',
          amount: 500,
          type: 'EXPENSE',
          category: 'FOOD',
          date: '2023-01-02'
        }
      };
      
      (mockTransactionsHttpClient.post as jest.Mock).mockResolvedValue(mockResponse);
      
      const formData = {
        description: 'Expense',
        amount: 500,
        type: 'EXPENSE' as any,
        category: 'FOOD', // Should be category, not categoryId based on the lint error
        date: new Date('2023-01-02')
      };
      
      const result = await createTransaction(formData);
      
      expect(mockTransactionsHttpClient.post).toHaveBeenCalledWith(
        API_ENDPOINTS.TRANSACTIONS,
        formData
      );
      expect(result.id).toBe(2);
    });
  });

  describe('updateTransaction', () => {
    it('should call PUT /v1/transactions/:id', async () => {
      const mockResponse = {
        data: {
          transactionId: 2,
          amount: 600
        }
      };
      (mockTransactionsHttpClient.put as jest.Mock).mockResolvedValue(mockResponse);
      
      const formData = { amount: 600 } as any;
      await updateTransaction('2;500;EXPENSE;FOOD;2023-01-02', formData);
      
      expect(mockTransactionsHttpClient.put).toHaveBeenCalledWith(
        `${API_ENDPOINTS.TRANSACTIONS}/2;500;EXPENSE;FOOD;2023-01-02`,
        formData
      );
    });
  });

  describe('deleteTransaction', () => {
    it('should call DELETE /v1/transactions/:id', async () => {
      (mockTransactionsHttpClient.delete as jest.Mock).mockResolvedValue({});
      
      await deleteTransaction('123');
      
      expect(mockTransactionsHttpClient.delete).toHaveBeenCalledWith(`${API_ENDPOINTS.TRANSACTIONS}/123`);
    });
  });
});
