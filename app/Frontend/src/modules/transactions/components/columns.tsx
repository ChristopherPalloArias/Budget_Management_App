// Temporarily define ColumnDef to avoid dependency issues
interface ColumnDef {
    accessorKey?: string;
    header?: string | ((context: any) => React.ReactNode);
    cell?: (context: any) => React.ReactNode;
    id?: string;
}
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { MoreHorizontal, Pencil, Trash2 } from 'lucide-react';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import type { TransactionModel } from '../types/transaction.types';

const formatCurrency = (amount: number) => {
    const formatted = new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP'
    }).format(Math.abs(amount));
    
    return formatted;
};

const getCategoryColor = (category: string) => {
    const colors: Record<string, string> = {
        'Alimentación': 'bg-blue-100 text-blue-800',
        'Transporte': 'bg-green-100 text-green-800',
        'Vivienda': 'bg-purple-100 text-purple-800',
        'Salud': 'bg-red-100 text-red-800',
        'Educación': 'bg-yellow-100 text-yellow-800',
        'Entretenimiento': 'bg-pink-100 text-pink-800',
        'Salario': 'bg-emerald-100 text-emerald-800',
        'Negocio': 'bg-orange-100 text-orange-800',
        'Inversiones': 'bg-indigo-100 text-indigo-800',
        'Otros': 'bg-gray-100 text-gray-800',
    };
    
    return colors[category] || colors['Otros'];
};

interface TransactionColumnsProps {
    onEdit: (transaction: TransactionModel) => void;
    onDelete: (transaction: TransactionModel) => void;
}

export const createTransactionColumns = ({ onEdit, onDelete }: TransactionColumnsProps): ColumnDef[] => {
    return [
        {
            accessorKey: 'date',
            header: 'Fecha',
            cell: ({ row }) => {
                const date = new Date(row.getValue('date'));
                return (
                    <div className="font-medium">
                        {date.toLocaleDateString('es-CO', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric'
                        })}
                    </div>
                );
            },
        },
        {
            accessorKey: 'description',
            header: 'Concepto',
            cell: ({ row }) => (
                <div className="max-w-[200px] truncate">
                    {row.getValue('description')}
                </div>
            ),
        },
        {
            accessorKey: 'category',
            header: 'Categoría',
            cell: ({ row }) => (
                <Badge className={getCategoryColor(row.getValue('category'))}>
                    {row.getValue('category')}
                </Badge>
            ),
        },
        {
            accessorKey: 'amount',
            header: 'Monto',
            cell: ({ row }) => {
                const amount = row.getValue('amount') as number;
                const type = row.getValue('type') as 'income' | 'expense';
                const colorClass = type === 'income' ? 'text-green-600' : 'text-red-600';
                const prefix = type === 'income' ? '+' : '-';
                
                return (
                    <div className={`font-semibold ${colorClass}`}>
                        {prefix} {formatCurrency(amount)}
                    </div>
                );
            },
        },
        {
            accessorKey: 'type',
            header: 'Tipo',
            cell: ({ row }) => {
                const type = row.getValue('type') as 'income' | 'expense';
                return (
                    <Badge variant={type === 'income' ? 'default' : 'destructive'}>
                        {type === 'income' ? 'Ingreso' : 'Egreso'}
                    </Badge>
                );
            },
        },
        {
            id: 'actions',
            header: 'Acciones',
            cell: ({ row }) => {
                const transaction = row.original;
                
                return (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                                <span className="sr-only">Abrir menú</span>
                                <MoreHorizontal className="h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => onEdit(transaction)}>
                                <Pencil className="mr-2 h-4 w-4" />
                                Editar
                            </DropdownMenuItem>
                            <DropdownMenuItem 
                                onClick={() => onDelete(transaction)}
                                className="text-red-600"
                            >
                                <Trash2 className="mr-2 h-4 w-4" />
                                Eliminar
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                );
            },
        },
    ];
};