import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import CalendarFilter, {
  matchesDateFilter,
  dateFilterLabel,
  type DateFilter,
} from "../../../src/components/common/calendarFilter";

// ─── Pure logic ──────────────────────────────────────────────────────────────

describe("matchesDateFilter", () => {
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);

  const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate());

  it("returns true when filter is null", () => {
    expect(matchesDateFilter(today, null)).toBe(true);
  });

  describe("today", () => {
    const filter: DateFilter = { mode: "today" };

    it("matches a date from today", () => {
      expect(matchesDateFilter(today, filter)).toBe(true);
    });

    it("does not match yesterday", () => {
      expect(matchesDateFilter(yesterday, filter)).toBe(false);
    });

    it("matches today regardless of time-of-day", () => {
      const noon = new Date(today);
      noon.setHours(12, 0, 0, 0);
      expect(matchesDateFilter(noon, filter)).toBe(true);
    });
  });

  describe("yesterday", () => {
    const filter: DateFilter = { mode: "yesterday" };

    it("matches a date from yesterday", () => {
      expect(matchesDateFilter(yesterday, filter)).toBe(true);
    });

    it("does not match today", () => {
      expect(matchesDateFilter(today, filter)).toBe(false);
    });
  });

  describe("range", () => {
    const from = startOfDay(new Date(2025, 0, 10));  // Jan 10
    const to   = new Date(2025, 0, 15, 23, 59, 59);  // Jan 15 end-of-day
    const filter: DateFilter = { mode: "range", from, to };

    it("matches a date within the range", () => {
      expect(matchesDateFilter(new Date(2025, 0, 12), filter)).toBe(true);
    });

    it("matches the start boundary", () => {
      expect(matchesDateFilter(from, filter)).toBe(true);
    });

    it("matches the end boundary", () => {
      expect(matchesDateFilter(to, filter)).toBe(true);
    });

    it("does not match a date before the range", () => {
      expect(matchesDateFilter(new Date(2025, 0, 9), filter)).toBe(false);
    });

    it("does not match a date after the range", () => {
      expect(matchesDateFilter(new Date(2025, 0, 16), filter)).toBe(false);
    });
  });

  describe("specific", () => {
    const target = new Date(2025, 5, 15); // Jun 15 2025
    const filter: DateFilter = { mode: "specific", date: target };

    it("matches the exact date", () => {
      expect(matchesDateFilter(new Date(2025, 5, 15), filter)).toBe(true);
    });

    it("matches regardless of time on the target day", () => {
      expect(matchesDateFilter(new Date(2025, 5, 15, 23, 59), filter)).toBe(true);
    });

    it("does not match a different date", () => {
      expect(matchesDateFilter(new Date(2025, 5, 16), filter)).toBe(false);
    });
  });
});

// ─── dateFilterLabel ─────────────────────────────────────────────────────────

describe("dateFilterLabel", () => {
  it("returns default label when filter is null", () => {
    expect(dateFilterLabel(null)).toBe("Filter by Date");
  });

  it("returns 'Today' for today filter", () => {
    expect(dateFilterLabel({ mode: "today" })).toBe("Today");
  });

  it("returns 'Yesterday' for yesterday filter", () => {
    expect(dateFilterLabel({ mode: "yesterday" })).toBe("Yesterday");
  });

  it("returns formatted range for range filter", () => {
    const from = new Date(2025, 0, 1);
    const to   = new Date(2025, 0, 31);
    const label = dateFilterLabel({ mode: "range", from, to });
    expect(label).toContain(from.toLocaleDateString());
    expect(label).toContain(to.toLocaleDateString());
    expect(label).toContain("–");
  });

  it("returns formatted date for specific filter", () => {
    const date = new Date(2025, 5, 15);
    expect(dateFilterLabel({ mode: "specific", date })).toBe(date.toLocaleDateString());
  });
});

// ─── CalendarFilter component ─────────────────────────────────────────────────

describe("CalendarFilter", () => {
  const onFilter = vi.fn();

  beforeEach(() => vi.clearAllMocks());

  function renderFilter(activeFilter: DateFilter | null = null) {
    return render(<CalendarFilter onFilter={onFilter} activeFilter={activeFilter} />);
  }

  describe("trigger button", () => {
    it("renders with default label when no active filter", () => {
      renderFilter();
      expect(screen.getByRole("button", { name: /filter by date/i })).toBeInTheDocument();
    });

    it("reflects the active filter in the button label", () => {
      renderFilter({ mode: "today" });
      expect(screen.getByRole("button", { name: /today/i })).toBeInTheDocument();
    });

    it("popover is not visible initially", () => {
      renderFilter();
      expect(screen.queryByText("Apply")).not.toBeInTheDocument();
    });

    it("opens popover on click", () => {
      renderFilter();
      fireEvent.click(screen.getByRole("button", { name: /filter by date/i }));
      expect(screen.getByRole("button", { name: /apply/i })).toBeInTheDocument();
    });

    it("closes popover on second click", () => {
      renderFilter();
      const btn = screen.getByRole("button", { name: /filter by date/i });
      fireEvent.click(btn);
      fireEvent.click(btn);
      expect(screen.queryByRole("button", { name: /apply/i })).not.toBeInTheDocument();
    });
  });

  describe("mode tabs", () => {
    beforeEach(() => {
      renderFilter();
      fireEvent.click(screen.getByRole("button", { name: /filter by date/i }));
    });

    it("renders all four mode tabs", () => {
      expect(screen.getByText("Today")).toBeInTheDocument();
      expect(screen.getByText("Yesterday")).toBeInTheDocument();
      expect(screen.getByText("Date Range")).toBeInTheDocument();
      expect(screen.getByText("Specific Date")).toBeInTheDocument();
    });

    it("shows a description for the Today tab by default", () => {
      expect(screen.getByText(/filter builds from today/i)).toBeInTheDocument();
    });

    it("shows a description for the Yesterday tab", () => {
      fireEvent.click(screen.getByText("Yesterday"));
      expect(screen.getByText(/filter builds from yesterday/i)).toBeInTheDocument();
    });

    it("shows a calendar for the Specific Date tab", () => {
      fireEvent.click(screen.getByText("Specific Date"));
      expect(screen.getByRole("grid")).toBeInTheDocument();
    });

    it("shows a calendar for the Date Range tab", () => {
      fireEvent.click(screen.getByText("Date Range"));
      expect(screen.getAllByRole("grid").length).toBeGreaterThanOrEqual(1);
    });
  });

  describe("Apply button", () => {
    beforeEach(() => {
      renderFilter();
      fireEvent.click(screen.getByRole("button", { name: /filter by date/i }));
    });

    it("is enabled on Today tab", () => {
      expect(screen.getByRole("button", { name: /apply/i })).not.toBeDisabled();
    });

    it("is enabled on Yesterday tab", () => {
      fireEvent.click(screen.getByText("Yesterday"));
      expect(screen.getByRole("button", { name: /apply/i })).not.toBeDisabled();
    });

    it("is disabled on Specific Date tab when no date is selected", () => {
      fireEvent.click(screen.getByText("Specific Date"));
      expect(screen.getByRole("button", { name: /apply/i })).toBeDisabled();
    });

    it("is disabled on Date Range tab when no range is selected", () => {
      fireEvent.click(screen.getByText("Date Range"));
      expect(screen.getByRole("button", { name: /apply/i })).toBeDisabled();
    });

    it("calls onFilter with { mode: 'today' } and closes popover", () => {
      fireEvent.click(screen.getByRole("button", { name: /apply/i }));
      expect(onFilter).toHaveBeenCalledWith({ mode: "today" });
      expect(screen.queryByRole("button", { name: /apply/i })).not.toBeInTheDocument();
    });

    it("calls onFilter with { mode: 'yesterday' }", () => {
      fireEvent.click(screen.getByText("Yesterday"));
      fireEvent.click(screen.getByRole("button", { name: /apply/i }));
      expect(onFilter).toHaveBeenCalledWith({ mode: "yesterday" });
    });
  });

  describe("Clear button", () => {
    it("calls onFilter(null) and closes the popover", () => {
      renderFilter({ mode: "today" });
      fireEvent.click(screen.getByRole("button", { name: /today/i }));
      fireEvent.click(screen.getByRole("button", { name: /clear/i }));
      expect(onFilter).toHaveBeenCalledWith(null);
      expect(screen.queryByRole("button", { name: /apply/i })).not.toBeInTheDocument();
    });
  });

  describe("outside click", () => {
    it("closes the popover when clicking outside", () => {
      const { container } = renderFilter();
      fireEvent.click(screen.getByRole("button", { name: /filter by date/i }));
      expect(screen.getByRole("button", { name: /apply/i })).toBeInTheDocument();

      // Click on the document root, outside the component's container
      fireEvent.mouseDown(container.ownerDocument.documentElement);
      expect(screen.queryByRole("button", { name: /apply/i })).not.toBeInTheDocument();
    });
  });
});
