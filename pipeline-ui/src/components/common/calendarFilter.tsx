import { useEffect, useRef, useState } from "react"
import type { DateRange } from "react-day-picker"
import { Calendar } from "../ui/calendar"
import { Button } from "../ui/button"
import { CalendarDays } from "lucide-react"
import { cn } from "../../lib/utils"

/**
 * A filter for builds based on their start date.
 * - "today" matches builds that started today.
 * - "yesterday" matches builds that started yesterday.
 * - "range" matches builds that started between two dates (inclusive).
 * - "specific" matches builds that started on a specific date.
 */

// TODO: Refactor this to use a more generic date filter type that can be used in other places (e.g. for filtering pipelines by creation date).
// It's very extensive now, let's change later

export type DateFilter =
    | { mode: "today" }
    | { mode: "yesterday" }
    | { mode: "range"; from: Date; to: Date }
    | { mode: "specific"; date: Date }

/** Returns true when `date` satisfies the given filter. */
export function matchesDateFilter(date: Date, filter: DateFilter | null): boolean {
    if (!filter) return true

    const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate())
    const target = startOfDay(date)
    const today = startOfDay(new Date())

    const matchers: Record<DateFilter["mode"], () => boolean> = {
        today: () => target.getTime() === today.getTime(),
        yesterday: () => target.getTime() === today.getTime() - 864e5,
        range: () => filter.mode === "range" && date >= filter.from && date <= filter.to,
        specific: () => filter.mode === "specific" && target.getTime() === startOfDay(filter.date).getTime(),
    }

    return matchers[filter.mode]()
}

export function dateFilterLabel(filter: DateFilter | null): string {
    if (!filter) return "Filter by Date"
    const fmt = (d: Date) => d.toLocaleDateString()
    const labels: Record<DateFilter["mode"], () => string> = {
        today: () => "Today",
        yesterday: () => "Yesterday",
        range: () => filter.mode === "range" ? `${fmt(filter.from)} – ${fmt(filter.to)}` : "",
        specific: () => filter.mode === "specific" ? fmt(filter.date) : "",
    }
    return labels[filter.mode]()
}


type FilterMode = DateFilter["mode"]

const TABS: { key: FilterMode; label: string }[] = [
    { key: "today", label: "Today" },
    { key: "yesterday", label: "Yesterday" },
    { key: "range", label: "Date Range" },
    { key: "specific", label: "Specific Date" },
]

interface CalendarFilterProps {
    onFilter: (filter: DateFilter | null) => void
    activeFilter?: DateFilter | null
}

export default function CalendarFilter({ onFilter, activeFilter = null }: CalendarFilterProps) {
    const [open, setOpen] = useState(false)
    const [mode, setMode] = useState<FilterMode>("today")
    const [range, setRange] = useState<DateRange | undefined>(undefined)
    const [specific, setSpecific] = useState<Date | undefined>(undefined)

    const buttonRef = useRef<HTMLButtonElement>(null)
    const popoverRef = useRef<HTMLDivElement>(null)

    // Close the popover when clicking outside of it
    useEffect(() => {
        if (!open) return
        const handler = (e: MouseEvent) => {
            const outside = (ref: React.RefObject<HTMLElement | null>) =>
                ref.current && !ref.current.contains(e.target as Node)
            if (outside(popoverRef) && outside(buttonRef)) setOpen(false)
        }
        document.addEventListener("mousedown", handler)
        return () => document.removeEventListener("mousedown", handler)
    }, [open])

    // Build a DateFilter object based on the current state of the component
    function buildFilter(): DateFilter | null {
        if (mode === "today") return { mode: "today" }
        if (mode === "yesterday") return { mode: "yesterday" }
        if (mode === "range" && range?.from && range?.to) return { mode: "range", from: range.from, to: range.to }
        if (mode === "specific" && specific) return { mode: "specific", date: specific }
        return null
    }

    const canApply =
        mode === "today" ||
        mode === "yesterday" ||
        (mode === "range" && !!(range?.from && range?.to)) ||
        (mode === "specific" && !!specific)

    // Apply the current filter and close the popover
    function apply() {
        const filter = buildFilter()
        if (filter) { onFilter(filter); setOpen(false) }
    }

    function clear() {
        setRange(undefined)
        setSpecific(undefined)
        onFilter(null)
        setOpen(false)
    }

    return (
        <div className="relative">
            <Button
                ref={buttonRef}
                variant={activeFilter ? "default" : "outline"}
                size="sm"
                className="gap-2"
                onClick={() => setOpen((o) => !o)}
            >
                <CalendarDays className="size-4" />
                {dateFilterLabel(activeFilter)}
            </Button>

            {open && (
                <div
                    ref={popoverRef}
                    className="absolute left-0 top-full z-50 mt-2 rounded-xl border border-gray-200 bg-white shadow-xl"
                >
                    {/* Mode tabs */}
                    <div className="flex border-b border-gray-100">
                        {TABS.map(({ key, label }) => (
                            <button
                                key={key}
                                onClick={() => setMode(key)}
                                className={cn(
                                    "flex-1 whitespace-nowrap px-4 py-2 text-xs font-medium transition-colors",
                                    mode === key
                                        ? "border-b-2 border-indigo-500 text-indigo-600"
                                        : "text-gray-500 hover:text-gray-700"
                                )}
                            >
                                {label}
                            </button>
                        ))}
                    </div>

                    {/* Calendar (only for modes that need it) */}
                    <div className="flex justify-center p-3">
                        {mode === "range" && (
                            <Calendar mode="range" selected={range} onSelect={setRange} numberOfMonths={2} />
                        )}
                        {mode === "specific" && (
                            <Calendar mode="single" selected={specific} onSelect={setSpecific} />
                        )}
                        {(mode === "today" || mode === "yesterday") && (
                            <p className="px-2 py-6 text-center text-sm text-gray-500">
                                Click <strong>Apply</strong> to filter builds from {mode === "today" ? "today" : "yesterday"}.
                            </p>
                        )}
                    </div>

                    {/* Actions */}
                    <div className="flex justify-end gap-2 border-t border-gray-100 px-3 py-2">
                        <Button variant="ghost" size="sm" onClick={clear}>Clear</Button>
                        <Button size="sm" disabled={!canApply} onClick={apply}>Apply</Button>
                    </div>
                </div>
            )}
        </div>
    )
}
