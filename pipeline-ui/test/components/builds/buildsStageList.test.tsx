import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { BuildStageCard } from "../../../src/components/common/stageListComponents/buildStageListComponents";
import type { StageBuild } from "../../../src/gen/api";

describe("BuildStageCard", () => {
  const mockOnToggle = vi.fn();

  const successStage: StageBuild = { id: "1", stageName: "Build Stage", stageId: "101", currentState: "SUCCESS" };
  const failedStage: StageBuild  = { id: "2", stageName: "Test Stage",  stageId: "102", currentState: "FAILED"  };
  const runningStage: StageBuild = { id: "3", stageName: "Deploy Stage",stageId: "103", currentState: "RUNNING" };
  const stoppedStage: StageBuild = { id: "4", stageName: "Verify Stage",stageId: "104", currentState: "STOPPED" };
  const waitingStage: StageBuild = { id: "5", stageName: "Cleanup Stage",stageId: "105",currentState: "WAITING" };

  beforeEach(() => vi.clearAllMocks());

  describe("Rendering", () => {
    it("renders stage name", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      expect(screen.getByText("Build Stage")).toBeInTheDocument();
    });

    it("renders order number padded to two digits", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      expect(screen.getByText("01")).toBeInTheDocument();
    });

    it("renders BASH badge", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      expect(screen.getByText("BASH")).toBeInTheDocument();
    });

    it("renders logs section when expanded", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={true} onToggle={mockOnToggle} logs="some logs" />);
      expect(screen.getByText("Logs")).toBeInTheDocument();
      expect(screen.getByText("some logs")).toBeInTheDocument();
    });

    it("does not render logs section when collapsed", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={false} onToggle={mockOnToggle} logs="some logs" />);
      expect(screen.queryByText("LOGS")).not.toBeInTheDocument();
    });

    it("shows placeholder text when expanded but no logs", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={true} onToggle={mockOnToggle} logs="" />);
      expect(screen.getByText("No logs yet.")).toBeInTheDocument();
    });

    it("handles long stage names", () => {
      const longName: StageBuild = { id: "1", stageName: "This is a very long stage name that might overflow", stageId: "1", currentState: "SUCCESS" };
      render(<BuildStageCard stage={longName} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      expect(screen.getByText("This is a very long stage name that might overflow")).toBeInTheDocument();
    });

    it("handles special characters in names", () => {
      const specialStage: StageBuild = { id: "1", stageName: "Build & Test (Prod)", stageId: "1", currentState: "SUCCESS" };
      render(<BuildStageCard stage={specialStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      expect(screen.getByText("Build & Test (Prod)")).toBeInTheDocument();
    });
  });

  describe("Selection / toggle", () => {
    it("calls onToggle when clicked", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      fireEvent.click(screen.getByText("Build Stage"));
      expect(mockOnToggle).toHaveBeenCalledTimes(1);
    });
  });

  describe("Stage status icons", () => {
    it("displays green check icon for SUCCESS state", () => {
      render(<BuildStageCard stage={successStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      const icon = screen.getByText("Build Stage").closest("button")?.querySelector("svg");
      expect(icon).toHaveClass("text-green-600");
    });

    it("displays red X icon for FAILED state", () => {
      render(<BuildStageCard stage={failedStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      const icon = screen.getByText("Test Stage").closest("button")?.querySelector("svg");
      expect(icon).toHaveClass("text-red-600");
    });

    it("displays yellow spinning loader for RUNNING state", () => {
      render(<BuildStageCard stage={runningStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      const icon = screen.getByText("Deploy Stage").closest("button")?.querySelector("svg");
      expect(icon).toHaveClass("text-yellow-500", "animate-spin");
    });

    it("displays gray X icon for STOPPED state", () => {
      render(<BuildStageCard stage={stoppedStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      const icon = screen.getByText("Verify Stage").closest("button")?.querySelector("svg");
      expect(icon).toHaveClass("text-zinc-400");
    });

    it("displays blue spinning loader for WAITING state", () => {
      render(<BuildStageCard stage={waitingStage} order={1} expanded={false} onToggle={mockOnToggle} logs="" />);
      const icon = screen.getByText("Cleanup Stage").closest("button")?.querySelector("svg");
      expect(icon).toHaveClass("text-blue-500", "animate-spin");
    });
  });
});
