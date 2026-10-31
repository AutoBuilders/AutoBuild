import { useRef, useState } from "react";
import type { Stage } from "../../gen/api";
import { Connector, DropZone, StageCard, EditableStageCard, FloatingAddBox, EmptyStages } from "./stageListComponents";

interface Props {
  stages: Stage[];
  canEdit: boolean;
  onStagesChange: (stages: Stage[]) => void;
}

export default function StageList({ stages, canEdit, onStagesChange }: Props) {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  // drag-to-reorder
  const dragIdx = useRef<number | null>(null);
  const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);

  // drag-to-add new
  const [isDraggingNew, setIsDraggingNew] = useState(false);
  const [newDropIdx, setNewDropIdx] = useState<number | null>(null);

  function toggleExpand(id: string) {
    setExpandedId((prev) => (prev === id ? null : id));
  }

  function updateStage(updated: Stage) {
    onStagesChange(stages.map((s) => (s.id === updated.id ? updated : s)));
  }

  function deleteStage(id: string) {
    onStagesChange(stages.filter((s) => s.id !== id));
    if (expandedId === id) setExpandedId(null);
  }

  function addStageAtEnd() {
    const newStage: Stage = {
      id: crypto.randomUUID(),
      name: "",
      scriptType: "SHELL",
      command: "",
    };
    onStagesChange([...stages, newStage].map((s, i) => ({ ...s, order: i + 1 })));
    setExpandedId((newStage as any).id);
  }

  // Reorder handlers
  function handleStageDragStart(idx: number) {
    dragIdx.current = idx;
  }

  // When dragging over a stage, we set the dragOverIdx to the index of that stage.
  function handleStageDragOver(e: React.DragEvent, idx: number) {
    if (isDraggingNew) return;
    e.preventDefault();
    setDragOverIdx(idx);
  }

  // When the drag ends, we check if the dragged stage was moved to a new position. If so, we reorder the stages and call onStagesChange with the new order.
  function handleStageDragEnd() {
    if (dragIdx.current !== null && dragOverIdx !== null && dragIdx.current !== dragOverIdx) {
      const next = [...stages];
      const [moved] = next.splice(dragIdx.current, 1);
      next.splice(dragOverIdx, 0, moved);
      onStagesChange(next.map((s, i) => ({ ...s, order: i + 1 })));
    }
    dragIdx.current = null;
    setDragOverIdx(null);
  }

  // New stage drag handlers
  function handleNewDragStart() {
    setIsDraggingNew(true);
  }

  // When the drag ends, if the newDropIdx is not null, we create a new stage and insert it at the newDropIdx position. 
  // We then call onStagesChange with the updated stages and reset the dragging state.
  function handleNewDragEnd() {
    if (newDropIdx !== null) {
      const newStage: Stage = {
        id: crypto.randomUUID(),
        name: "",
        scriptType: "SHELL",
        command: "",
      };
      const next = [...stages];
      next.splice(newDropIdx, 0, newStage);
      onStagesChange(next.map((s, i) => ({ ...s, order: i + 1 })));
      setExpandedId((newStage as any).id);
    }
    setIsDraggingNew(false);
    setNewDropIdx(null);
  }

  // When dragging over a drop zone, we set the newDropIdx to the index of that drop zone.
  function handleDropZoneDragOver(e: React.DragEvent, insertIdx: number) {
    e.preventDefault();
    if (isDraggingNew) setNewDropIdx(insertIdx);
  }

  //
  function handleDropZoneDrop(insertIdx: number) {
    if (!isDraggingNew) return;
    const newStage: Stage = {
      id: crypto.randomUUID(),
      name: "",
      scriptType: "SHELL",
      command: "",
    };
    const next = [...stages];
    next.splice(insertIdx, 0, newStage);
    onStagesChange(next.map((s, i) => ({ ...s, order: i + 1 })));
    setExpandedId((newStage as any).id);
    setIsDraggingNew(false);
    setNewDropIdx(null);
  }

  // Empty state
  if (stages.length === 0) {
    return (
      <>
        <div
          onDragOver={(e) => { e.preventDefault(); if (isDraggingNew) setNewDropIdx(0); }}
          onDrop={() => handleDropZoneDrop(0)}
        >
          <EmptyStages canEdit={canEdit} dragTarget={isDraggingNew && newDropIdx === 0} />
        </div>
        {canEdit && (
          <FloatingAddBox onDragStart={handleNewDragStart} onDragEnd={handleNewDragEnd} onClick={addStageAtEnd} />
        )}
      </>
    );
  }

  return (
    <>
      <div className="flex items-start overflow-x-auto pb-4">
        {canEdit ? (
          <>
            <DropZone
              active={isDraggingNew && newDropIdx === 0}
              onDragOver={(e) => handleDropZoneDragOver(e, 0)}
              onDrop={() => handleDropZoneDrop(0)}
            />
            {stages.map((stage, idx) => (
              <div key={stage.id} className="flex items-start shrink-0">
                <EditableStageCard
                  stage={stage}
                  order={idx + 1}
                  expanded={expandedId === stage.id}
                  dragOver={dragOverIdx === idx && dragIdx.current !== null && dragIdx.current !== idx}
                  onToggle={() => toggleExpand(stage.id as string)}
                  onChange={updateStage}
                  onDelete={() => deleteStage(stage.id as string)}
                  onDragStart={() => handleStageDragStart(idx)}
                  onDragOver={(e) => handleStageDragOver(e, idx)}
                  onDragEnd={handleStageDragEnd}
                />
                <Connector faded={isDraggingNew} />
                <DropZone
                  active={isDraggingNew && newDropIdx === idx + 1}
                  onDragOver={(e) => handleDropZoneDragOver(e, idx + 1)}
                  onDrop={() => handleDropZoneDrop(idx + 1)}
                />
              </div>
            ))}
          </>
        ) : (
          stages.map((stage, idx) => (
            <div key={stage.id} className="flex items-start shrink-0">
              <StageCard
                stage={stage}
                order={idx + 1}
                expanded={expandedId === stage.id}
                onToggle={() => toggleExpand(stage.id as string)}
              />
              {idx < stages.length - 1 && <Connector />}
            </div>
          ))
        )}
      </div>

      {canEdit && (
        <FloatingAddBox onDragStart={handleNewDragStart} onDragEnd={handleNewDragEnd} onClick={addStageAtEnd} />
      )}
    </>
  );
}
