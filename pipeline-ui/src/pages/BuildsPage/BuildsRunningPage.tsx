import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import { getLiveBuildUpdates, pipelineBuildApiInstance, stageBuildApiInstance } from "../../services/pipelines.api";
import NavBar from "../../components/common/navBar";
import PipelineHeader from "../../components/pipelines/pipelineHeader";
import StageList from "../../components/common/stageList";
import { BuildStatusBanner } from "../../components/common/stageListComponents/buildStageListComponents";
import { Trash } from "lucide-react";
import { booleanFlags } from "../../flags/booleanFlags";
import { PipelineBuildCurrentStateEnum, StageBuildCurrentStateEnum, type PipelineBuild } from "../../gen";
import Breadcrumb from "../../components/common/breadcrumb";

export default function BuildsRunningPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [build, setBuild] = useState<PipelineBuild | null>(null);
  const [selectedStageId, setSelectedStageId] = useState<string>("");
  const [isDeleteVisible, setIsDeleteVisible] = useState(false);
  const [buildLogs, setBuildLogs] = useState<string>("");

  // Fetch initial build and set the first stage as selected
  // This use effect runs only once when the component mounts or when the id changes.
  useEffect(() => {
    const buildId = id as string;

    async function load() {
      const { status, data } = await pipelineBuildApiInstance.getPipelineBuild(buildId);

      if (status !== 200) {
        console.error("Error getting build");
      }

      setBuild(data);

      if (data.stageBuilds !== undefined && data.stageBuilds.length > 0) {
        setSelectedStageId(data.stageBuilds[0].id as string);
      }
    }

    load();
  }, [id]);


  // Fetch logs for selected stage, polling if not final
  useEffect(() => {
    if (!selectedStageId) return;

    // Find the selected stage from the build's stageBuilds
    const selectedStage = (build?.stageBuilds ?? []).find((s) => s.id === selectedStageId);

    let intervalId: ReturnType<typeof setInterval> | null = null;

    async function fetchLogs() {
      try {
        const { status, data } = await stageBuildApiInstance.getStageBuildLogs(selectedStageId);
        if (status !== 200) {
          console.error("Error getting stage build log");
        }
        setBuildLogs(data.log);
      } catch (err) {
        console.error("Failed to load logs", err);
      }
    }

    fetchLogs();

    const isFinal = ([
      StageBuildCurrentStateEnum.Failed,
      StageBuildCurrentStateEnum.Stopped,
      StageBuildCurrentStateEnum.Success,
    ] as string[]).includes(selectedStage?.currentState ?? "");

    if (!isFinal) {
      intervalId = setInterval(fetchLogs, 3000);
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [selectedStageId, build?.stageBuilds]);

  // Polling for overall build state every 3 seconds if not final (success or failed)
  useEffect(() => {
    const buildId = id as string;

    // If the build is already in a final state, we don't need to poll for updates.
    if (([PipelineBuildCurrentStateEnum.Success, PipelineBuildCurrentStateEnum.Failed] as PipelineBuildCurrentStateEnum[]).includes(build?.currentState as PipelineBuildCurrentStateEnum)) {
      return;
    }

    // Polling for build state every 3 seconds
    const intervalId = setInterval(async () => {
      const { status, data } = await pipelineBuildApiInstance.getPipelineBuild(buildId);

      if (status !== 200) {
        console.error("Error getting build");
      }

      setBuild(data);
    }, 3000);

    return () => clearInterval(intervalId);
  }, [build?.currentState, id]);

  // SSE for live build updates
  useEffect(() => {
    if (booleanFlags.ENABLE_SSE === false) return;
    if (build?.currentState !== PipelineBuildCurrentStateEnum.Running) return;

    const buildId = id as string;
    const url = getLiveBuildUpdates(buildId);
    const es = new EventSource(url);

    es.onmessage = (event) => {
      const parsed = JSON.parse(event.data);

      setBuild((prev) => ({
        ...prev!,
        currentState: parsed.currentState,
        stageBuilds: parsed.stageBuilds,
      }));

      if (parsed.currentState !== PipelineBuildCurrentStateEnum.Running) {
        es.close();
        setIsDeleteVisible(true);
      }
    };

    es.onerror = () => {
      es.close();
    };

    return () => es.close();
  }, [build?.currentState, id]);

  if (!build) return <div>Loading...</div>;

  if (isDeleteVisible === false && build.currentState !== PipelineBuildCurrentStateEnum.Running) {
    setIsDeleteVisible(true);
  }

  // The currently running/failed stage name for the banner
  const activeStageName =
    build.currentState === PipelineBuildCurrentStateEnum.Running
      ? (build.stageBuilds ?? []).find((s) => s.currentState === StageBuildCurrentStateEnum.Running)?.stageName
      : build.currentState === PipelineBuildCurrentStateEnum.Failed
        ? (build.stageBuilds ?? []).find((s) => s.currentState === StageBuildCurrentStateEnum.Failed)?.stageName
        : undefined;

  async function handleDelete() {
    const { status } = await pipelineBuildApiInstance.deletePipelineBuild(build?.id as string);

    if (status !== 204) {
      console.error("Error deleting build");
    }

    navigate("/builds");
  }

  return (
    <>
      <NavBar />

      <div className="min-h-screen bg-[#F5F5F5] px-16 py-10 pb-24">
        {/* Back link */}
        <Breadcrumb crumbs={[{ label: "Home", route: "/" }, { label: "Builds", route: "/builds" }, { label: build.pipelineName ?? "Build" }]} />

        {/* Header row */}
        <div className="flex items-start justify-between mb-2">
          <PipelineHeader name={build.pipelineName} />

          {/* Delete button */}
          <div className="relative group inline-block">
            <button
              onClick={isDeleteVisible ? handleDelete : undefined}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl transition-all shadow-sm
                ${isDeleteVisible
                  ? "bg-red-400 text-white hover:bg-red-500 cursor-pointer"
                  : "bg-zinc-200 text-zinc-400 cursor-not-allowed"
                }`}
            >
              <Trash size={18} />
              Delete Build
            </button>

            {!isDeleteVisible && (
              <div className="absolute left-1/2 -translate-x-1/2 mt-2 w-max px-3 py-1 rounded-md bg-black text-white text-xs opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none shadow-lg">
                This Build is running, can't delete now.
              </div>
            )}
          </div>
        </div>

        {/* Status banner */}
        <div className="mt-6">
          <BuildStatusBanner state={build.currentState} currentStageName={activeStageName} />
        </div>

        {/* Stage label */}
        <p className="text-xs font-medium uppercase tracking-widest text-zinc-400 mb-4">
          Stages · {(build.stageBuilds ?? []).length}
        </p>

        {/* Divider */}
        <div className="w-full h-px bg-[#E4E4E7] mb-6" />

        {/* Stage flow */}
        <StageList
          buildStages={build.stageBuilds ?? []}
          selectedBuildStageId={selectedStageId}
          onBuildStageSelect={(stageId) => {
            setSelectedStageId(stageId);
          }}
          buildLogs={buildLogs}
        />
      </div>
    </>
  );
}
