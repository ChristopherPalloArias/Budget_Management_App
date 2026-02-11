export const TRANSACTION_CATEGORIES = {
  INCOME: ["Salario", "Negocio", "Inversiones", "Otros"],
  EXPENSE: [
    "Alimentación",
    "Transporte",
    "Vivienda",
    "Salud",
    "Educación",
    "Entretenimiento",
    "Otros",
  ],
} as const;

export const ALL_CATEGORIES = [
  ...TRANSACTION_CATEGORIES.INCOME,
  ...TRANSACTION_CATEGORIES.EXPENSE,
];
