/**
 * Currency formatter helpers para manejo seguro de valores numéricos
 */

/**
 * Convierte cualquier valor a un número seguro
 * @param value - Valor a convertir (number, string, null, undefined)
 * @returns Número seguro (0 si no es válido)
 */
export const toSafeNumber = (value: any): number => {
  if (value === null || value === undefined || value === '') {
    return 0;
  }
  
  const num = Number(value);
  
  // Manejar NaN, Infinity y otros valores no válidos
  if (isNaN(num) || !isFinite(num)) {
    return 0;
  }
  
  return num;
};

/**
 * Formatea un valor como moneda de forma segura
 * @param amount - Valor a formatear
 * @param locale - Locale para formateo (default: 'es-CO')
 * @param currency - Moneda (default: 'COP')
 * @returns String formateado como moneda
 */
export const formatCurrency = (
  amount: any,
  locale: string = 'es-CO',
  currency: string = 'COP'
): string => {
  const safeAmount = toSafeNumber(amount);
  
  try {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(safeAmount);
  } catch (error) {
    // Fallback en caso de error con Intl.NumberFormat
    return `$ ${safeAmount.toLocaleString(locale)}`;
  }
};

/**
 * Formatea un valor como moneda simple (símbolo + número)
 * @param amount - Valor a formatear
 * @returns String formateado como moneda simple
 */
export const formatCurrencySimple = (amount: any): string => {
  const safeAmount = toSafeNumber(amount);
  return `$ ${safeAmount.toLocaleString('es-CO')}`;
};

/**
 * Formatea un valor numérico con separadores de miles
 * @param amount - Valor a formatear
 * @returns String formateado con separadores
 */
export const formatNumber = (amount: any): string => {
  const safeAmount = toSafeNumber(amount);
  return safeAmount.toLocaleString('es-CO');
};

/**
 * Verifica si un valor es un número válido
 * @param value - Valor a verificar
 * @returns true si es un número válido
 */
export const isValidNumber = (value: any): boolean => {
  if (value === null || value === undefined || value === '') {
    return false;
  }
  
  const num = Number(value);
  return !isNaN(num) && isFinite(num);
};