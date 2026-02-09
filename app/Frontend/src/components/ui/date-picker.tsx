import * as React from "react"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Field, FieldLabel } from "@/components/ui/field"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { addDays, format } from "@/lib/date-utils"
import { CalendarIcon } from "lucide-react"
import { type DateRange as CustomDateRange } from "@/lib/date-utils"
import { type DateRange } from "react-day-picker"

export function DatePickerWithRange({
  className,
  value,
  onChange,
}: {
  className?: string
  value?: CustomDateRange
  onChange?: (range: CustomDateRange | undefined) => void
}) {
  const [date, setDate] = React.useState<DateRange | undefined>((value as DateRange) || {
    from: new Date(new Date().getFullYear(), 0, 20),
    to: addDays(new Date(new Date().getFullYear(), 0, 20), 20),
  })

  React.useEffect(() => {
    if (value !== undefined) {
      setDate(value as DateRange)
    }
  }, [value])

  React.useEffect(() => {
    onChange?.(date as CustomDateRange)
  }, [date, onChange])

  return (
    <Field className={className}>
      <FieldLabel htmlFor="date-picker-range">Rango de fechas </FieldLabel>
      <Popover>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            id="date-picker-range"
            className="justify-start px-2.5 font-normal"
          >
            <CalendarIcon />
            {date?.from ? (
              date.to ? (
                <>
                  {format(date.from, "LLL dd, y")} -{" "}
                  {format(date.to, "LLL dd, y")}
                </>
              ) : (
                format(date.from, "LLL dd, y")
              )
            ) : (
              <span>Pick a date</span>
            )}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Calendar
            mode="range"
            defaultMonth={date?.from}
            selected={date}
            onSelect={setDate}
            numberOfMonths={2}
          />
        </PopoverContent>
      </Popover>
    </Field>
  )
}