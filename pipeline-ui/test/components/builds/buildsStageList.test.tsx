import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import BuildStageList from "../../../src/components/builds/buildsStageList";
import type { StageBuild } from "../../../src/gen/api";

describe("BuildStageList", () => {
  const mockOnSelect = vi.fn();

  const mockStages: StageBuild[] = [
    {
      id: "1",
      stageName: "Build Stage",
      stageId: "101",
      currentState: "SUCCESS",
    },
    {
      id: "2",
      stageName: "Test Stage",
      stageId: "102",
      currentState: "FAILED",
    },
    {
      id: "3",
      stageName: "Deploy Stage",
      stageId: "103",
      currentState: "RUNNING",
    },
    {
      id: "4",
      stageName: "Verify Stage",
      stageId: "104",
      currentState: "STOPPED",
    },
    {
      id: "5",
      stageName: "Cleanup Stage",
      stageId: "105",
      currentState: "WAITING",
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Rendering", () => {
    it("renders the component with title", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      expect(screen.getByText("Stages")).toBeInTheDocument();
    });

    it("renders all stages with correct names", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      expect(screen.getByText("Build Stage")).toBeInTheDocument();
      expect(screen.getByText("Test Stage")).toBeInTheDocument();
      expect(screen.getByText("Deploy Stage")).toBeInTheDocument();
      expect(screen.getByText("Verify Stage")).toBeInTheDocument();
      expect(screen.getByText("Cleanup Stage")).toBeInTheDocument();
    });

    it("renders empty list when no stages provided", () => {
      render(
        <BuildStageList stages={[]} selectedId="1" onSelect={mockOnSelect} />,
      );

      expect(screen.getByText("Stages")).toBeInTheDocument();
      expect(screen.queryByRole("button")).not.toBeInTheDocument();
    });

    it("renders correct number of stage buttons", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      const buttons = screen.getAllByRole("button");
      expect(buttons).toHaveLength(5);
    });
  });

  describe("Stage Selection", () => {
    it("highlights the selected stage by id", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="2"
          onSelect={mockOnSelect}
        />,
      );

      const testStageButton = screen.getByText("Test Stage").closest("button");
      expect(testStageButton).toHaveClass("bg-blue-100", "border-blue-400");
    });

    it("does not highlight non-selected stages", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="2"
          onSelect={mockOnSelect}
        />,
      );

      const buildStageButton = screen
        .getByText("Build Stage")
        .closest("button");
      expect(buildStageButton).toHaveClass("bg-gray-100", "border-gray-300");
      expect(buildStageButton).not.toHaveClass("bg-blue-100");
    });

    it("calls onSelect with correct stage id when stage is clicked", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      const testStageButton = screen.getByText("Test Stage");
      fireEvent.click(testStageButton);

      expect(mockOnSelect).toHaveBeenCalledTimes(1);
      expect(mockOnSelect).toHaveBeenCalledWith("2");
    });

    it("calls onSelect with different stage ids on multiple clicks", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      fireEvent.click(screen.getByText("Build Stage"));
      fireEvent.click(screen.getByText("Deploy Stage"));
      fireEvent.click(screen.getByText("Cleanup Stage"));

      expect(mockOnSelect).toHaveBeenCalledTimes(3);
      expect(mockOnSelect).toHaveBeenNthCalledWith(1, "1");
      expect(mockOnSelect).toHaveBeenNthCalledWith(2, "3");
      expect(mockOnSelect).toHaveBeenNthCalledWith(3, "5");
    });
  });

  describe("Stage Status Icons", () => {
    it("displays green check icon for SUCCESS state", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      const buildStageButton = screen
        .getByText("Build Stage")
        .closest("button");
      const icon = buildStageButton?.querySelector("svg");

      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass("text-green-600");
    });

    it("displays red X icon for FAILED state", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="2"
          onSelect={mockOnSelect}
        />,
      );

      const testStageButton = screen.getByText("Test Stage").closest("button");
      const icon = testStageButton?.querySelector("svg");

      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass("text-red-600");
    });

    it("displays yellow spinning loader for RUNNING state", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="3"
          onSelect={mockOnSelect}
        />,
      );

      const deployStageButton = screen
        .getByText("Deploy Stage")
        .closest("button");
      const icon = deployStageButton?.querySelector("svg");

      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass("text-yellow-600", "animate-spin");
    });

    it("displays gray X icon for STOPPED state", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="4"
          onSelect={mockOnSelect}
        />,
      );

      const verifyStageButton = screen
        .getByText("Verify Stage")
        .closest("button");
      const icon = verifyStageButton?.querySelector("svg");

      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass("text-gray-600");
    });

    it("displays blue spinning loader for WAITING state", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="5"
          onSelect={mockOnSelect}
        />,
      );

      const cleanupStageButton = screen
        .getByText("Cleanup Stage")
        .closest("button");
      const icon = cleanupStageButton?.querySelector("svg");

      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass("text-blue-600", "animate-spin");
    });

    it("renders correct icon based on currentState", () => {
      const stagesWithDifferentStates: StageBuild[] = [
        { id: "1", stageName: "Stage 1", stageId: "1", currentState: "SUCCESS" },
        { id: "2", stageName: "Stage 2", stageId: "2", currentState: "FAILED" },
        { id: "3", stageName: "Stage 3", stageId: "3", currentState: "STOPPED" },
      ];

      render(
        <BuildStageList
          stages={stagesWithDifferentStates}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      const icons = screen
        .getAllByRole("button")
        .map((btn) => btn.querySelector("svg"));

      expect(icons[0]).toHaveClass("text-green-600");
      expect(icons[1]).toHaveClass("text-red-600");
      expect(icons[2]).toHaveClass("text-gray-600");
    });
  });

  describe("Edge Cases", () => {
    it("handles single stage", () => {
      const singleStage: StageBuild[] = [
        {
          id: "1",
          stageName: "Only Stage",
          stageId: "101",
          currentState: "SUCCESS",
        },
      ];

      render(
        <BuildStageList
          stages={singleStage}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      expect(screen.getByText("Only Stage")).toBeInTheDocument();
      expect(screen.getAllByRole("button")).toHaveLength(1);
    });

    it("handles very long stage names", () => {
      const longNameStage: StageBuild[] = [
        {
          id: "1",
          stageName:
            "This is a very long stage name that might overflow the container and cause layout issues",
          stageId: "101",
          currentState: "SUCCESS",
        },
      ];

      render(
        <BuildStageList
          stages={longNameStage}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      expect(
        screen.getByText(
          "This is a very long stage name that might overflow the container and cause layout issues",
        ),
      ).toBeInTheDocument();
    });

    it("handles unknown selectedId (no selection highlighted)", () => {
      render(
        <BuildStageList
          stages={mockStages}
          selectedId="999"
          onSelect={mockOnSelect}
        />,
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        expect(button).not.toHaveClass("bg-blue-100");
        expect(button).toHaveClass("bg-gray-100");
      });
    });

    it("handles stages with special characters in names", () => {
      const specialCharStages: StageBuild[] = [
        {
          id: "1",
          stageName: "Build & Test",
          stageId: "101",
          currentState: "SUCCESS",
        },
        {
          id: "2",
          stageName: "Deploy (Production)",
          stageId: "102",
          currentState: "RUNNING",
        },
      ];

      render(
        <BuildStageList
          stages={specialCharStages}
          selectedId="1"
          onSelect={mockOnSelect}
        />,
      );

      expect(screen.getByText("Build & Test")).toBeInTheDocument();
      expect(screen.getByText("Deploy (Production)")).toBeInTheDocument();
    });
  });
});
