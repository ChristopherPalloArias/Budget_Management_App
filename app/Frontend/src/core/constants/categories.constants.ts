export const TRANSACTION_CATEGORIES = [
  "ALIMENTACION",
  "TRANSPORTE",
  "VIVIENDA",
  "SALUD",
  "EDUCACION",
  "ENTRETENIMIENTO",
  "SALARIO",
  "NEGOCIO",
  "INVERSIONES",
  "OTROS",
] as const;

export type TransactionCategory = (typeof TRANSACTION_CATEGORIES)[number];

export const CATEGORY_COLORS: Record<TransactionCategory, string> = {
  ALIMENTACION: "bg-blue-100 text-blue-800",
  TRANSPORTE: "bg-green-100 text-green-800",
  VIVIENDA: "bg-purple-100 text-purple-800",
  SALUD: "bg-red-100 text-red-800",
  EDUCACION: "bg-yellow-100 text-yellow-800",
  ENTRETENIMIENTO: "bg-pink-100 text-pink-800",
  SALARIO: "bg-emerald-100 text-emerald-800",
  NEGOCIO: "bg-orange-100 text-orange-800",
  INVERSIONES: "bg-indigo-100 text-indigo-800",
  OTROS: "bg-gray-100 text-gray-800",
};

export const CATEGORY_LABELS: Record<TransactionCategory, string> = {
  ALIMENTACION: "Alimentación",
  TRANSPORTE: "Transporte",
  VIVIENDA: "Vivienda",
  SALUD: "Salud",
  EDUCACION: "Educación",
  ENTRETENIMIENTO: "Entretenimiento",
  SALARIO: "Salario",
  NEGOCIO: "Negocio",
  INVERSIONES: "Inversiones",
  OTROS: "Otros",
};

export const getCategoryColor = (category: string): string => {
  const normalizedCategory = category.toUpperCase() as TransactionCategory;
  return CATEGORY_COLORS[normalizedCategory] || CATEGORY_COLORS.OTROS;
};

export const getCategoryLabel = (category: string): string => {
  const normalizedCategory = category.toUpperCase() as TransactionCategory;
  return CATEGORY_LABELS[normalizedCategory] || category;
};
