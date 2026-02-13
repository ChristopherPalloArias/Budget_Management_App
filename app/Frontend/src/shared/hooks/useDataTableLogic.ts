import { useState, useMemo, useCallback } from "react";

export interface DataTableState {
  pageIndex: number;
  searchQuery: string;
  selectedType: Set<string>;
  selectedCategory: Set<string>;
}

export interface UseDataTableLogicOptions<T> {
  data: T[];
  pageSize?: number;
  searchFields?: (keyof T)[];
  filterFn?: (item: T, state: DataTableState) => boolean;
}

export interface UseDataTableLogicReturn<T> {
  // State
  state: DataTableState;
  
  // Processed data
  filteredData: T[];
  paginatedData: T[];
  totalPages: number;
  totalFiltered: number;
  
  // Categories (for filters)
  categories: string[];
  
  // Actions
  setPage: (page: number) => void;
  setSearchQuery: (query: string) => void;
  setTypeFilter: (types: Set<string>) => void;
  setCategoryFilter: (categories: Set<string>) => void;
  resetFilters: () => void;
  nextPage: () => void;
  prevPage: () => void;
}

export function useDataTableLogic<T>(
  options: UseDataTableLogicOptions<T>
): UseDataTableLogicReturn<T> {
  const { data, pageSize = 10, searchFields = [], filterFn } = options;

  const [pageIndex, setPageIndex] = useState(0);
  const [searchQuery, setSearchQueryState] = useState("");
  const [selectedType, setSelectedType] = useState<Set<string>>(new Set());
  const [selectedCategory, setSelectedCategory] = useState<Set<string>>(new Set());

  const categories = useMemo(() => {
    if (!Array.isArray(data)) return [];
    const uniqueCategories = Array.from(new Set(data.map((item: any) => item.category)));
    return uniqueCategories.sort();
  }, [data]);

  const state: DataTableState = useMemo(() => ({
    pageIndex,
    searchQuery,
    selectedType,
    selectedCategory,
  }), [pageIndex, searchQuery, selectedType, selectedCategory]);

  const filteredData = useMemo(() => {
    if (!Array.isArray(data)) return [];
    
    return data.filter((item) => {
      const matchesSearch = searchFields.length > 0 && searchQuery
        ? searchFields.some((field) => {
            const value = item[field];
            return typeof value === "string" && value.toLowerCase().includes(searchQuery.toLowerCase());
          })
        : true;

      const matchesType = selectedType.size === 0 || selectedType.has((item as any).type);
      const matchesCategory = selectedCategory.size === 0 || selectedCategory.has((item as any).category);

      if (filterFn) {
        return filterFn(item, state) && matchesType && matchesCategory && matchesSearch;
      }

      return matchesSearch && matchesType && matchesCategory;
    });
  }, [data, searchQuery, selectedType, selectedCategory, searchFields, filterFn, state]);

  const totalFiltered = filteredData.length;
  const totalPages = Math.ceil(totalFiltered / pageSize);

  const paginatedData = useMemo(() => {
    const startIndex = pageIndex * pageSize;
    return filteredData.slice(startIndex, startIndex + pageSize);
  }, [filteredData, pageIndex, pageSize]);

  const setPage = useCallback((page: number) => {
    setPageIndex((prev) => {
      if (page >= 0 && page < totalPages) {
        return page;
      }
      return prev;
    });
  }, [totalPages]);

  const setSearchQuery = useCallback((query: string) => {
    setSearchQueryState(query);
    setPageIndex(0);
  }, []);

  const setTypeFilter = useCallback((types: Set<string>) => {
    setSelectedType(types);
    setPageIndex(0);
  }, []);

  const setCategoryFilter = useCallback((categories: Set<string>) => {
    setSelectedCategory(categories);
    setPageIndex(0);
  }, []);

  const resetFilters = useCallback(() => {
    setSearchQueryState("");
    setSelectedType(new Set());
    setSelectedCategory(new Set());
    setPageIndex(0);
  }, []);

  const nextPage = useCallback(() => {
    setPageIndex((prev) => Math.min(prev + 1, totalPages - 1));
  }, [totalPages]);

  const prevPage = useCallback(() => {
    setPageIndex((prev) => Math.max(prev - 1, 0));
  }, []);

  return {
    state,
    filteredData,
    paginatedData,
    totalPages,
    totalFiltered,
    categories,
    setPage,
    setSearchQuery,
    setTypeFilter,
    setCategoryFilter,
    resetFilters,
    nextPage,
    prevPage,
  };
}
