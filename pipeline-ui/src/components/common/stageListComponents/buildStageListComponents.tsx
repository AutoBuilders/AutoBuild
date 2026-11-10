import { CheckCircle, XCircle, Loader } from "lucide-react";
import type { StageBuild, StageBuildCurrentStateEnum as StageBuildStateType } from "../../../gen/api";
import { StageBuildCurrentStateEnum, PipelineBuildCurrentStateEnum, type PipelineBuildCurrentStateEnum as PipelineBuildStateType } from "../../../gen/api";



// ── Build-specific components ─────────────────────────────────────────────────

// State icon for a build stage — only used in build context.
export function BuildStateIcon({ state }: { state: StageBuildStateType | undefined }) {
  switch (state) {
    case StageBuildCurrentStateEnum.Success:
      return <CheckCircle className="text-green-600 w-4 h-4" aria-label="success-circle-stage" />;
    case StageBuildCurrentStateEnum.Failed:
      return <XCircle className="text-red-600 w-4 h-4" />;
    case StageBuildCurrentStateEnum.Running:
      return <Loader className="text-yellow-500 animate-spin w-4 h-4" />;
    case StageBuildCurrentStateEnum.Waiting:
      return <Loader className="text-blue-500 animate-spin w-4 h-4" />;
    case StageBuildCurrentStateEnum.Stopped:
      return <XCircle className="text-zinc-400 w-4 h-4" />;
    default:
      return <span className="w-4 h-4 inline-block" />;
  }
}

// Border colour for a selected build stage card based on its state.
function buildCardBorderClass(state: StageBuildStateType | undefined, expanded: boolean): string {
  if (!expanded) return "border-zinc-200 hover:border-zinc-400";
  switch (state) {
    case StageBuildCurrentStateEnum.Success: return "border-green-500 shadow-sm";
    case StageBuildCurrentStateEnum.Failed:  return "border-red-500 shadow-sm";
    case StageBuildCurrentStateEnum.Running: return "border-yellow-400 shadow-sm";
    case StageBuildCurrentStateEnum.Waiting: return "border-blue-400 shadow-sm";
    default: return "border-zinc-400 shadow-sm";
  }
}

// Card for a single build stage — mirrors StageCard structure but uses StageBuild data
// and shows status icon + inline logs instead of commands.
export function BuildStageCard({ stage, order, expanded, onToggle, logs }: {
  stage: StageBuild;
  order: number;
  expanded: boolean;
  onToggle: () => void;
  logs: string;
}) {
  return (
    <button
      onClick={onToggle}
      className={`shrink-0 text-left rounded-lg border bg-white transition-all duration-150 ${buildCardBorderClass(stage.currentState, expanded)} ${expanded ? "w-72" : "w-48"}`}
    >
      <div className="p-4">
        <div className="flex items-start justify-between mb-2">
          <span className="font-mono text-xs text-zinc-400">
            {String(order).padStart(2, "0")}
          </span>
          <BuildStateIcon state={stage.currentState} />
        </div>
        <p className="text-sm font-medium text-zinc-900 leading-snug truncate">{stage.stageName}</p>
        <span className="inline-block mt-2 font-mono text-xs text-zinc-500 bg-zinc-100 rounded px-1.5 py-0.5 uppercase">
          BASH
        </span>
      </div>

      {expanded && (
        <div className="border-t border-zinc-100 px-4 pb-4 pt-3 text-left">
          <p className="text-xs font-medium uppercase tracking-widest text-zinc-400 mb-2">Logs</p>
          <pre className="font-mono text-xs text-zinc-800 bg-zinc-50 rounded p-2.5 whitespace-pre-wrap break-words leading-relaxed max-h-64 overflow-y-auto">
            {logs || <span className="text-zinc-400">No logs yet.</span>}
          </pre>
        </div>
      )}
    </button>
  );
}

// Banner shown at the top of the builds running page indicating the overall build status.
// Returns null for SUCCESS and other final/neutral states — only shown for RUNNING and FAILED.
export function BuildStatusBanner({ state, currentStageName }: {
  state: PipelineBuildStateType | undefined;
  currentStageName?: string;
}) {
  if (state === PipelineBuildCurrentStateEnum.Running) {
    return (
      <div className="flex items-center gap-2 px-4 py-3 rounded-lg bg-yellow-50 border border-yellow-200 text-yellow-800 text-sm mb-6">
        <Loader className="animate-spin w-4 h-4 shrink-0" />
        <span>
          Build is running{currentStageName ? ` · ${currentStageName} in progress` : ""}
        </span>
      </div>
    );
  }
  if (state === PipelineBuildCurrentStateEnum.Failed) {
    return (
      <div className="flex items-center gap-2 px-4 py-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm mb-6">
        <XCircle className="w-4 h-4 shrink-0" />
        <span>
          Build failed{currentStageName ? ` at stage ${currentStageName}` : ""}
        </span>
      </div>
    );
  }
  return null;
}
