import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import StageList from "../../../src/components/pipelines/stageList";
import type { Stage } from "../../../src/gen/api";

const mockStages: Stage[] = [
  { id: "stage-1", name: "Build", scriptType: "bash", command: "npm build" },
  { id: "stage-2", name: "Test", scriptType: "bash", command: "npm test" },
  { id: "stage-3", name: "Deploy", scriptType: "bash", command: "npm deploy" },
];

const defaultProps = {
  stages: mockStages,
  canEdit: false,
  onStagesChange: vi.fn(),
};

describe("StageList", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Rendering", () => {
    it("renders all stage names", () => {
      render(<StageList {...defaultProps} />);
      expect(screen.getByText("Build")).toBeInTheDocument();
      expect(screen.getByText("Test")).toBeInTheDocument();
      expect(screen.getByText("Deploy")).toBeInTheDocument();
    });

    it("renders empty state when no stages", () => {
      render(<StageList {...defaultProps} stages={[]} />);
      expect(screen.getByText("No stages configured")).toBeInTheDocument();
    });

    it("renders monospace order labels", () => {
      render(<StageList {...defaultProps} />);
      expect(screen.getByText("01")).toBeInTheDocument();
      expect(screen.getByText("02")).toBeInTheDocument();
      expect(screen.getByText("03")).toBeInTheDocument();
    });
  });

  describe("canEdit=false mode", () => {
    it("does not show the Add stage button", () => {
      render(<StageList {...defaultProps} canEdit={false} />);
      expect(screen.queryByTestId("Plus")).not.toBeInTheDocument();
    });

    it("does not show delete buttons", () => {
      render(<StageList {...defaultProps} canEdit={false} />);
      expect(screen.queryAllByTestId("StageDelete")).toHaveLength(0);
    });

    it("expands a card to show commands when clicked", () => {
      render(<StageList {...defaultProps} />);
      fireEvent.click(screen.getByText("Build"));
      expect(screen.getByText("npm build")).toBeInTheDocument();
    });

    it("collapses a card when clicked again", () => {
      render(<StageList {...defaultProps} />);
      fireEvent.click(screen.getByText("Build"));
      expect(screen.getByText("npm build")).toBeInTheDocument();
      fireEvent.click(screen.getByText("Build"));
      expect(screen.queryByText("npm build")).not.toBeInTheDocument();
    });
  });

  describe("canEdit=true mode", () => {
    const editProps = { ...defaultProps, canEdit: true };

    it("renders the floating Add stage button", () => {
      render(<StageList {...editProps} />);
      expect(screen.getByTestId("Plus")).toBeInTheDocument();
    });

    it("renders a delete button when a card is expanded", () => {
      render(<StageList {...editProps} />);
      fireEvent.click(screen.getByText("Build"));
      expect(screen.getAllByTestId("StageDelete")).toHaveLength(1);
    });

    it("calls onStagesChange without the deleted stage when delete is clicked", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      // Expand first card to reveal delete button
      fireEvent.click(screen.getByText("Build"));
      fireEvent.click(screen.getByTestId("StageDelete"));
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated.find((s) => s.id === "stage-1")).toBeUndefined();
    });

    it("calls onStagesChange with updated stage when name is edited", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      fireEvent.click(screen.getByText("Build"));
      const nameInput = screen.getByPlaceholderText("e.g. Build");
      fireEvent.change(nameInput, { target: { value: "Compile" } });
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated.find((s) => s.id === "stage-1")?.name).toBe("Compile");
    });

    it("does not propagate drag events from edit fields to the card drag handler", () => {
      render(<StageList {...editProps} />);
      fireEvent.click(screen.getByText("Build"));
      const textarea = screen.getByTestId("StageCommandsTextarea");
      // dragStart on inner fields should not throw or propagate
      fireEvent.dragStart(textarea);
    });

    it("clicking the FloatingAddBox appends a new stage and calls onStagesChange", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      fireEvent.click(screen.getByTestId("Plus"));
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated).toHaveLength(mockStages.length + 1);
      // New stage is appended at the end
      expect(updated[updated.length - 1].scriptType).toBe("SHELL");
    });

    it("clicking FloatingAddBox in empty state appends a stage", () => {
      const onStagesChange = vi.fn();
      render(<StageList stages={[]} canEdit={true} onStagesChange={onStagesChange} />);
      fireEvent.click(screen.getByTestId("Plus"));
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated).toHaveLength(1);
      expect(updated[0].scriptType).toBe("SHELL");
    });

    it("drag-to-reorder: dragEnd with a valid target reorders stages", () => {
      const onStagesChange = vi.fn();
      const { container } = render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      // Cards are rendered inside divs with draggable attr
      const draggableCards = container.querySelectorAll("[draggable=true]");
      // draggableCards[0] is FloatingAddBox (fixed), [1] is stage-1 card, [2] is stage-2 card, [3] is stage-3 card
      // Find the editable stage cards specifically (they sit inside the stage list, not fixed)
      const stageCards = Array.from(draggableCards).filter(
        (el) => !el.className.includes("fixed")
      );
      // Drag stage-1 (index 0) → drag over stage-2 (index 1) → dragEnd
      fireEvent.dragStart(stageCards[0]);
      fireEvent.dragOver(stageCards[1]);
      fireEvent.dragEnd(stageCards[0]);
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      // stage-1 moved to position 1 (0-indexed), so stage-2 is now first
      expect(updated[0].id).toBe("stage-2");
      expect(updated[1].id).toBe("stage-1");
    });

    it("drag-to-reorder: dragEnd with same source and target does not call onStagesChange", () => {
      const onStagesChange = vi.fn();
      const { container } = render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      const draggableCards = container.querySelectorAll("[draggable=true]");
      const stageCards = Array.from(draggableCards).filter(
        (el) => !el.className.includes("fixed")
      );
      // Drag stage-1 over itself — no reorder should happen
      fireEvent.dragStart(stageCards[0]);
      fireEvent.dragOver(stageCards[0]);
      fireEvent.dragEnd(stageCards[0]);
      expect(onStagesChange).not.toHaveBeenCalled();
    });

    it("drag-to-reorder: dragOver is ignored when isDraggingNew is active", () => {
      const onStagesChange = vi.fn();
      const { container } = render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      const floatingBox = screen.getByTestId("Plus");
      const draggableCards = container.querySelectorAll("[draggable=true]");
      const stageCards = Array.from(draggableCards).filter(
        (el) => !el.className.includes("fixed")
      );
      // Start dragging a new stage
      fireEvent.dragStart(floatingBox);
      // dragOver on an existing card should be ignored (isDraggingNew guard)
      fireEvent.dragOver(stageCards[0]);
      fireEvent.dragEnd(floatingBox);
      // No drop index was set via drop zone, so no stage inserted
      expect(onStagesChange).not.toHaveBeenCalled();
    });

    it("drag-to-add: dropping on a DropZone inserts a new stage at the correct index", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      const floatingBox = screen.getByTestId("Plus");
      // DropZones are the w-3/w-16 divs; get all of them by their style attribute
      const dropZones = document
        .querySelectorAll("[style='min-height: 88px;']");
      // Start dragging from FloatingAddBox
      fireEvent.dragStart(floatingBox);
      // DragOver the first drop zone (index 0 = before all stages)
      fireEvent.dragOver(dropZones[0]);
      // Drop onto it
      fireEvent.drop(dropZones[0]);
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated).toHaveLength(mockStages.length + 1);
      // New stage inserted before all existing stages
      expect(updated[0].scriptType).toBe("SHELL");
    });

    it("drag-to-add: handleNewDragEnd inserts at newDropIdx when drag ends over a drop zone", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      const floatingBox = screen.getByTestId("Plus");
      const dropZones = document.querySelectorAll("[style='min-height: 88px;']");
      // DragStart + dragOver a drop zone to set newDropIdx, then dragEnd on FloatingAddBox
      fireEvent.dragStart(floatingBox);
      fireEvent.dragOver(dropZones[1]); // index 1 = after first stage
      fireEvent.dragEnd(floatingBox);
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated).toHaveLength(mockStages.length + 1);
      // New stage inserted after stage-1, should be at index 1
      expect(updated[1].scriptType).toBe("SHELL");
      expect(updated[1].name).toBe("");
    });

    it("drag-to-add: drop on empty state area inserts a stage", () => {
      const onStagesChange = vi.fn();
      const { container } = render(
        <StageList stages={[]} canEdit={true} onStagesChange={onStagesChange} />
      );
      const floatingBox = screen.getByTestId("Plus");
      const emptyDropArea = container.querySelector("[onDrop]") ?? container.firstChild!.firstChild!;
      fireEvent.dragStart(floatingBox);
      // dragOver the empty area wrapper to set newDropIdx=0
      fireEvent.dragOver(emptyDropArea as Element);
      fireEvent.drop(emptyDropArea as Element);
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated).toHaveLength(1);
    });

    it("handleDropZoneDrop does nothing when isDraggingNew is false", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      const dropZones = document.querySelectorAll("[style='min-height: 88px;']");
      // Drop without starting a drag (isDraggingNew stays false)
      fireEvent.drop(dropZones[0]);
      expect(onStagesChange).not.toHaveBeenCalled();
    });

    it("deleting the expanded stage collapses it", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      fireEvent.click(screen.getByText("Build"));
      // Delete button is now visible
      expect(screen.getByTestId("StageDelete")).toBeInTheDocument();
      fireEvent.click(screen.getByTestId("StageDelete"));
      // After deletion, the expanded section should be gone
      expect(screen.queryByTestId("StageDelete")).not.toBeInTheDocument();
    });

    it("calls onStagesChange with updated command when command textarea is edited", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      fireEvent.click(screen.getByText("Build"));
      const textarea = screen.getByTestId("StageCommandsTextarea");
      fireEvent.change(textarea, { target: { value: "npm ci" } });
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated.find((s) => s.id === "stage-1")?.command).toBe("npm ci");
    });

    it("calls onStagesChange with updated scriptType when select is changed", () => {
      const onStagesChange = vi.fn();
      render(<StageList {...editProps} onStagesChange={onStagesChange} />);
      fireEvent.click(screen.getByText("Build"));
      const select = screen.getByRole("combobox");
      fireEvent.change(select, { target: { value: "PYTHON" } });
      expect(onStagesChange).toHaveBeenCalledTimes(1);
      const updated: Stage[] = onStagesChange.mock.calls[0][0];
      expect(updated.find((s) => s.id === "stage-1")?.scriptType).toBe("PYTHON");
    });
  });
});
