import type { Stage } from "../../gen/api";

// ── Icons ─────────────────────────────────────────────────────────────────────

export function IconGrip() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="9" cy="5" r="1" /><circle cx="15" cy="5" r="1" />
      <circle cx="9" cy="12" r="1" /><circle cx="15" cy="12" r="1" />
      <circle cx="9" cy="19" r="1" /><circle cx="15" cy="19" r="1" />
    </svg>
  );
}

export function IconChevronRight() {
  return (
    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="9 18 15 12 9 6" />
    </svg>
  );
}

export function IconPlus() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="12" y1="5" x2="12" y2="19" />
      <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
  );
}

export function IconTrash() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
      <path d="M10 11v6M14 11v6" />
      <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
    </svg>
  );
}

export function IconGitBranch() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="6" y1="3" x2="6" y2="15" />
      <circle cx="18" cy="6" r="3" />
      <circle cx="6" cy="18" r="3" />
      <path d="M18 9a9 9 0 0 1-9 9" />
    </svg>
  );
}

// ── Connector ─────────────────────────────────────────────────────────────────

export function Connector({ faded }: { faded?: boolean }) {
  return (
    <div className={`flex items-center shrink-0 pt-[38px] transition-opacity ${faded ? "opacity-20" : ""}`}>
      <div className="w-5 h-px bg-[#D4D4D8]" />
      <span className="text-[#C4C4C4]"><IconChevronRight /></span>
    </div>
  );
}

// ── Drop zone ─────────────────────────────────────────────────────────────────

// This is the drop zone that appears when dragging a new stage to insert it between existing stages.
export function DropZone({ active, onDragOver, onDrop }: {
  active: boolean;
  onDragOver: (e: React.DragEvent) => void;
  onDrop: () => void;
}) {
  return (
    <div
      onDragOver={onDragOver}
      onDrop={onDrop}
      style={{ minHeight: 88 }}
      className={`shrink-0 rounded-lg transition-all duration-150 ${
        active ? "w-16 bg-blue-50 border-2 border-dashed border-blue-400 mx-2" : "w-3"
      }`}
    />
  );
}

// ── View-mode card ────────────────────────────────────────────────────────────

// This is the card that displays a stage in view mode (not editable).
export function StageCard({ stage, order, expanded, onToggle }: {
  stage: Stage;
  order: number;
  expanded: boolean;
  onToggle: () => void;
}) {
  return (
    <button
      onClick={onToggle}
      className={`w-[192px] shrink-0 text-left rounded-lg border bg-white transition-all duration-150 ${
        expanded
          ? "border-[#18181B] shadow-sm"
          : "border-[#E4E4E7] hover:border-[#A1A1AA] hover:shadow-[0_1px_4px_rgba(0,0,0,0.06)]"
      }`}
    >
      <div className="p-4">
        <div className="flex items-start justify-between mb-2">
          <span className="font-mono text-[11px] text-[#A1A1AA]">
            {String(order).padStart(2, "0")}
          </span>
          <span className={`text-[#A1A1AA] transition-transform duration-200 ${expanded ? "rotate-90" : ""}`}>
            <IconChevronRight />
          </span>
        </div>
        <p className="text-[14px] font-medium text-[#18181B] leading-snug truncate">{stage.name}</p>
        <span className="inline-block mt-2 font-mono text-[11px] text-[#71717A] bg-[#F4F4F5] rounded px-1.5 py-0.5 uppercase">
          {stage.scriptType ?? "shell"}
        </span>
      </div>

      {/* If expanded, show the comands included in the stage */}
      {expanded && (
        <div className="border-t border-[#F4F4F5] px-4 pb-4 pt-3 text-left">
          <p className="text-[11px] font-medium uppercase tracking-widest text-[#A1A1AA] mb-2">Commands</p>
          <pre className="font-mono text-[11px] text-[#18181B] bg-[#F8F8F8] rounded p-2.5 whitespace-pre-wrap break-words leading-relaxed">
            {stage.command || <span className="text-[#A1A1AA]">No commands defined.</span>}
          </pre>
        </div>
      )}
    </button>
  );
}

// ── Edit-mode card ────────────────────────────────────────────────────────────

/**
 * Function to render an editable card for a stage in the pipeline. This card allows users to edit the stage's name, script type, and commands. It also supports drag-and-drop for reordering stages.
 * @param stage The stage object containing its properties (name, scriptType, command).
 * @param order The order of the stage in the pipeline.
 * @param expanded Whether the card is expanded to show editable fields.
 * @param dragOver Whether the card is currently being dragged over (for ordering).
 * @param onToggle Callback to toggle the expanded state of the card.
 * @param onChange Callback to update the stage's properties when edited.
 * @param onDelete Callback to delete the stage.
 * @param onDragStart Callback when dragging starts.
 * @param onDragOver Callback when dragging over this card.
 * @param onDragEnd Callback when dragging ends.
 * @returns Editable card for a stage, with drag-and-drop support and fields for editing the stage's properties.
 */
export function EditableStageCard({ stage, order, expanded, dragOver, onToggle, onChange, onDelete, onDragStart, onDragOver, onDragEnd }: {
  stage: Stage;
  order: number;
  expanded: boolean;
  dragOver: boolean;
  onToggle: () => void;
  onChange: (s: Stage) => void;
  onDelete: () => void;
  onDragStart: () => void;
  onDragOver: (e: React.DragEvent) => void;
  onDragEnd: () => void;
}) {
  return (
    <div
      draggable
      onDragStart={onDragStart}
      onDragOver={onDragOver}
      onDragEnd={onDragEnd}
      className={`shrink-0 rounded-lg border bg-white transition-all duration-150 select-none ${
        dragOver
          ? "border-blue-400 scale-[1.02] shadow-lg"
          : expanded
            ? "border-blue-400 shadow-[0_0_0_3px_rgba(59,130,246,0.1)]"
            : "border-[#E4E4E7] hover:border-[#A1A1AA] hover:shadow-[0_1px_4px_rgba(0,0,0,0.06)]"
      } ${expanded ? "w-[280px]" : "w-[192px]"}`}
    >
      {/* Card header — click to expand, drag handle visible */}
      <div
        className="p-4 cursor-grab active:cursor-grabbing"
        onClick={onToggle}
      >
        <div className="flex items-start justify-between mb-2">
          <div className="flex items-center gap-1.5">
            <span className="text-[#C4C4C4]"><IconGrip /></span>
            <span className="font-mono text-[11px] text-[#A1A1AA]">
              {String(order).padStart(2, "0")}
            </span>
          </div>
          <span className={`text-[#A1A1AA] transition-transform duration-200 ${expanded ? "rotate-90" : ""}`}>
            <IconChevronRight />
          </span>
        </div>
        <p className="text-[14px] font-medium text-[#18181B] leading-snug truncate">
          {stage.name || <span className="text-[#A1A1AA] font-normal">Untitled stage</span>}
        </p>
        <span className="inline-block mt-2 font-mono text-[11px] text-[#71717A] bg-[#F4F4F5] rounded px-1.5 py-0.5 uppercase">
          {stage.scriptType ?? "shell"}
        </span>
      </div>

      {/* Expanded edit fields */}
      {expanded && (
        <div
          className="border-t border-[#F4F4F5] px-4 pb-4 pt-3 space-y-3 cursor-default"
          onClick={(e) => e.stopPropagation()}
          onDragStart={(e) => e.stopPropagation()}
        >
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-widest text-[#A1A1AA] mb-1.5">
              Name
            </label>
            <input
              className="w-full h-8 px-2.5 text-[13px] bg-white border border-[#E4E4E7] rounded text-[#18181B]
                placeholder-[#A1A1AA] focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition"
              value={stage.name}
              onChange={(e) => onChange({ ...stage, name: e.target.value })}
              placeholder="e.g. Build"
            />
          </div>

          <div>
            <label className="block text-[11px] font-medium uppercase tracking-widest text-[#A1A1AA] mb-1.5">
              Script type
            </label>
            <select
              className="w-full h-8 px-2.5 text-[13px] bg-white border border-[#E4E4E7] rounded text-[#18181B]
                focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition cursor-pointer"
              value={stage.scriptType ?? "SHELL"}
              onChange={(e) => onChange({ ...stage, scriptType: e.target.value })}
            >
              <option value="SHELL">SHELL</option>
              <option value="PYTHON">PYTHON</option>
              <option value="POWERSHELL">POWERSHELL</option>
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-medium uppercase tracking-widest text-[#A1A1AA] mb-1.5">
              Commands
            </label>
            <textarea
              data-testid="StageCommandsTextarea"
              className="w-full px-2.5 py-2 text-[12px] font-mono bg-[#F8F8F8] border border-[#E4E4E7] rounded
                text-[#18181B] placeholder-[#A1A1AA] focus:outline-none focus:border-blue-400 focus:ring-2
                focus:ring-blue-100 transition resize-none leading-relaxed"
              rows={5}
              value={stage.command}
              onChange={(e) => onChange({ ...stage, command: e.target.value })}
              placeholder={"npm install\nnpm run build"}
            />
          </div>

          <button
            onClick={onDelete}
            data-testid="StageDelete"
            className="flex items-center gap-1.5 text-[12px] text-[#DC2626] hover:text-[#B91C1C] transition-colors pt-0.5"
          >
            <IconTrash />
            Remove stage
          </button>
        </div>
      )}
    </div>
  );
}

// ── Floating new-stage source ─────────────────────────────────────────────────

// This is the floating box that can be clicked to add a new stage at the end, or dragged to insert a new stage at a specific position.
export function FloatingAddBox({ onDragStart, onDragEnd, onClick }: {
  onDragStart: () => void;
  onDragEnd: () => void;
  onClick: () => void;
}) {
  return (
    <div
      draggable
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
      onClick={onClick}
      data-testid="Plus"
      className="fixed bottom-8 right-8 z-40 flex flex-col items-center justify-center w-[112px] h-[68px]
        bg-white border-2 border-dashed border-[#A1A1AA] rounded-xl cursor-pointer
        shadow-md hover:border-[#18181B] hover:shadow-lg transition-all gap-1 select-none group"
      title="Click to add a stage, or drag to insert at a specific position"
    >
      <span className="text-[#A1A1AA] group-hover:text-[#18181B] transition-colors"><IconPlus /></span>
      <span className="text-[12px] font-medium text-[#71717A] group-hover:text-[#18181B] transition-colors">New Stage</span>
      <span className="text-[10px] text-[#A1A1AA]">click or drag</span>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────

// This is the empty state that appears when there are no stages configured in the pipeline.
export function EmptyStages({ canEdit, dragTarget }: { canEdit: boolean; dragTarget: boolean }) {
  return (
    <div className={`flex flex-col items-center justify-center py-14 gap-2.5 rounded-lg border border-dashed transition-all
      ${dragTarget ? "border-blue-400 bg-blue-50" : "border-[#E4E4E7]"}`}
    >
      <span className="text-[#A1A1AA]"><IconGitBranch /></span>
      <p className="text-[14px] font-medium text-[#18181B]">No stages configured</p>
      <p className="text-[13px] text-[#71717A]">
        {canEdit ? 'Drag the "New Stage" box into this area.' : "Edit this pipeline to add stages."}
      </p>
    </div>
  );
}
