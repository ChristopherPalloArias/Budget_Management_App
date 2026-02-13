import { Badge } from "@/components/ui/badge";
import { getCategoryColor, getCategoryLabel } from "@/core/constants/categories.constants";

interface CategoryBadgeProps {
  category: string;
  className?: string;
}

export const CategoryBadge = ({ category, className }: CategoryBadgeProps) => {
  return (
    <Badge className={`${getCategoryColor(category)} ${className || ""}`}>
      {getCategoryLabel(category)}
    </Badge>
  );
};
