import { renderHook, act } from '@testing-library/react';
import { useDataTableLogic } from '../useDataTableLogic';

describe('useDataTableLogic', () => {
  const mockData = [
    { id: 1, description: 'Lunch', amount: 10, type: 'EXPENSE', category: 'Food' },
    { id: 2, description: 'Salary', amount: 1000, type: 'INCOME', category: 'Pay' },
    { id: 3, description: 'Dinner', amount: 20, type: 'EXPENSE', category: 'Food' },
    { id: 4, description: 'Bonus', amount: 500, type: 'INCOME', category: 'Pay' },
  ];

  it('should initialize with default state', () => {
    const { result } = renderHook(() => useDataTableLogic({ data: mockData }));
    
    expect(result.current.state.pageIndex).toBe(0);
    expect(result.current.state.searchQuery).toBe('');
    expect(result.current.filteredData).toHaveLength(4);
    expect(result.current.categories).toEqual(['Food', 'Pay']);
  });

  it('should filter data by search query', () => {
    const { result } = renderHook(() => useDataTableLogic({ 
      data: mockData, 
      searchFields: ['description'] 
    }));
    
    act(() => {
      result.current.setSearchQuery('Lunch');
    });
    
    expect(result.current.filteredData).toHaveLength(1);
    expect(result.current.filteredData[0].description).toBe('Lunch');
  });

  it('should filter data by type', () => {
    const { result } = renderHook(() => useDataTableLogic({ data: mockData }));
    
    act(() => {
      result.current.setTypeFilter(new Set(['INCOME']));
    });
    
    expect(result.current.filteredData).toHaveLength(2);
    expect(result.current.filteredData.every(i => i.type === 'INCOME')).toBe(true);
  });

  it('should filter data by category', () => {
    const { result } = renderHook(() => useDataTableLogic({ data: mockData }));
    
    act(() => {
      result.current.setCategoryFilter(new Set(['Food']));
    });
    
    expect(result.current.filteredData).toHaveLength(2);
    expect(result.current.filteredData.every(i => i.category === 'Food')).toBe(true);
  });

  it('should paginate data', () => {
    const { result } = renderHook(() => useDataTableLogic({ 
      data: mockData, 
      pageSize: 2 
    }));
    
    expect(result.current.paginatedData).toHaveLength(2);
    expect(result.current.paginatedData[0].id).toBe(1);
    
    act(() => {
      result.current.nextPage();
    });
    
    expect(result.current.state.pageIndex).toBe(1);
    expect(result.current.paginatedData[0].id).toBe(3);
  });

  it('should reset filters', () => {
    const { result } = renderHook(() => useDataTableLogic({ data: mockData }));
    
    act(() => {
      result.current.setSearchQuery('Test');
      result.current.setTypeFilter(new Set(['INCOME']));
      result.current.resetFilters();
    });
    
    expect(result.current.state.searchQuery).toBe('');
    expect(result.current.state.selectedType.size).toBe(0);
    expect(result.current.filteredData).toHaveLength(4);
  });
});
