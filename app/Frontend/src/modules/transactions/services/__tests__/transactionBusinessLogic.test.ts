import { transactionBusinessLogic, ValidationError } from '../transactionBusinessLogic';
import { createTransaction, updateTransaction } from '../transactionService';

// Mock transactionService
jest.mock('../transactionService', () => ({
  createTransaction: jest.fn(),
  updateTransaction: jest.fn()
}));

describe('TransactionBusinessLogic', () => {
  const mockFormData = {
    description: 'Test',
    amount: 100,
    category: 'FOOD',
    type: 'EXPENSE',
    date: new Date()
  } as any;

  describe('createTransaction', () => {
    it('should validate and process data before calling service', async () => {
      (createTransaction as jest.Mock).mockResolvedValue({ id: 1, ...mockFormData });
      
      const result = await transactionBusinessLogic.createTransaction(mockFormData, 'user1');
      
      expect(createTransaction).toHaveBeenCalledWith(expect.objectContaining({
        description: 'Test',
        amount: 100
      }));
      expect(result.id).toBe(1);
    });

    it('should throw ValidationError if description is empty', async () => {
      const invalidData = { ...mockFormData, description: '' };
      await expect(transactionBusinessLogic.createTransaction(invalidData, 'user1'))
        .rejects.toThrow(ValidationError);
    });

    it('should throw ValidationError if amount is negative', async () => {
        const invalidData = { ...mockFormData, amount: -10 };
        await expect(transactionBusinessLogic.createTransaction(invalidData, 'user1'))
          .rejects.toThrow(ValidationError);
      });
  });

  describe('calculateMonthlyTotal', () => {
    it('should calculate total correctly for a given month', () => {
      const transactions = [
        { amount: 100, type: 'INCOME', date: '2023-01-15' },
        { amount: 50, type: 'EXPENSE', date: '2023-01-20' },
        { amount: 200, type: 'INCOME', date: '2023-02-15' }
      ] as any;
      
      const total = transactionBusinessLogic.calculateMonthlyTotal(transactions, 0, 2023); // January is 0
      expect(total).toBe(50); // 100 - 50
    });
  });

  describe('getCategoryTotals', () => {
    it('should group by category', () => {
      const transactions = [
        { amount: 100, category: 'FOOD' },
        { amount: 50, category: 'FOOD' },
        { amount: 200, category: 'TRANSPORT' }
      ] as any;
      
      const totals = transactionBusinessLogic.getCategoryTotals(transactions);
      expect(totals).toEqual({
        FOOD: 150,
        TRANSPORT: 200
      });
    });
  });
});
