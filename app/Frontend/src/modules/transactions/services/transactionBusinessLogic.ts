import type {
  TransactionModel,
  TransactionFormData,
} from "../types/transaction.types";
import { createTransaction, updateTransaction } from "./transactionService";
import { ALL_CATEGORIES } from "../constants/transaction.constants";

export class ValidationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "ValidationError";
  }
}

export class TransactionBusinessLogic {
  async createTransaction(
    formData: TransactionFormData,
    userId: string,
  ): Promise<TransactionModel> {
    // Validation logic
    this.validateTransactionData(formData);

    // Business rules
    const processedData = this.applyBusinessRules(formData);

    // Call API
    return await createTransaction(processedData, userId);
  }

  async updateTransaction(
    id: string,
    formData: TransactionFormData,
  ): Promise<TransactionModel> {
    this.validateTransactionData(formData);
    const processedData = this.applyBusinessRules(formData);
    return await updateTransaction(id, processedData);
  }

  private validateTransactionData(data: TransactionFormData): void {
    if (!data.description || data.description.trim().length === 0) {
      throw new ValidationError("Description is required");
    }

    if (data.amount <= 0) {
      throw new ValidationError("Amount must be greater than 0");
    }

    if (!ALL_CATEGORIES.includes(data.category as any)) {
      throw new ValidationError("Invalid category");
    }
  }

  private applyBusinessRules(data: TransactionFormData): TransactionFormData {
    // Apply business transformations
    return {
      ...data,
      description: data.description.trim(),
      amount: Math.round(data.amount * 100) / 100, // Round to 2 decimals
    };
  }

  calculateMonthlyTotal(
    transactions: TransactionModel[],
    month: number,
    year: number,
  ): number {
    return transactions
      .filter((t) => {
        const date = new Date(t.date);
        return date.getMonth() === month && date.getFullYear() === year;
      })
      .reduce((sum, t) => {
        return t.type === "INCOME" ? sum + t.amount : sum - t.amount;
      }, 0);
  }

  getCategoryTotals(transactions: TransactionModel[]): Record<string, number> {
    return transactions.reduce(
      (acc, transaction) => {
        const category = transaction.category;
        acc[category] = (acc[category] || 0) + transaction.amount;
        return acc;
      },
      {} as Record<string, number>,
    );
  }
}

export const transactionBusinessLogic = new TransactionBusinessLogic();
