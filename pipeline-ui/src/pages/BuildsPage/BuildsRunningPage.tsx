import type React from "react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import {
  getLiveBuildUpdates,
} from "../../services/pipelines.api";

import NavBar from "../../components/common/navBar";
import PipelineHeader from "../../components/pipelines/pipelineHeader";
import BuildStageList from "../../components/builds/buildsStageList";
import BuildStageDetails from "../../components/builds/buildsStageDetails";
import { CheckCircle, Loader, Trash, XCircle } from "lucide-react";
import { booleanFlags } from "../../flags/booleanFlags";

import { PipelineBuildCurrentStateEnum, type PipelineBuild } from "../../gen";
import { pipelineBuildApiInstance } from "../../services/pipelines.api";

export default function BuildsRunningPage() {
  const { id } = useParams();
  const [build, setBuild] = useState<PipelineBuild | null>(null);
  const [selectedStageId, setSelectedStageId] = useState<string>("");
  const [isDeleteVisible, setIsDeleteVisible] = useState(false);

  useEffect(() => {
    const buildId = id as string;

    // --- 1. Fetch initial build ---
    async function load() {
      const {status, data} = await pipelineBuildApiInstance.getPipelineBuild(buildId);

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

  useEffect(() => {
    const buildId = id as string;

    // Stop polling for final states
    if (([PipelineBuildCurrentStateEnum.Success, PipelineBuildCurrentStateEnum.Failed] as PipelineBuildCurrentStateEnum[]).includes(build?.currentState as PipelineBuildCurrentStateEnum)) {
      return; // no interval created
    }

    // Skip polling while running (SSE should handle this)
    if (build?.currentState === PipelineBuildCurrentStateEnum.Running) return;

    // Start polling
    const intervalId = setInterval(async () => {
      const {status, data} = await pipelineBuildApiInstance.getPipelineBuild(buildId);

      if (status !== 200) {
        console.error("Error getting build");
      }

      setBuild(data);
    }, 10000);

    // Cleanup
    return () => clearInterval(intervalId);
  }, [build?.currentState, id]);

  useEffect(() => {
    if (booleanFlags.ENABLE_SSE === false) return;

    if (build?.currentState !== PipelineBuildCurrentStateEnum.Running) return;

    const buildId = id as string;
    const url = getLiveBuildUpdates(buildId);

    const es = new EventSource(url);

    // TODO: Better implementation with ID.
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
  const selectedStage = (build.stageBuilds ?? []).find((s) => s.id === selectedStageId);

  async function handleDelete() {
    const {status} = await pipelineBuildApiInstance.deletePipelineBuild(build?.id as string);

    if (status !== 204) {
      console.error("Error deleting build");
    }

    window.location.href = "/builds";
  }

  const stateMap: Record<string, { icon: React.ReactNode; color: string }> = {
    [PipelineBuildCurrentStateEnum.Success]: { icon: <CheckCircle className="text-green-600" size={18} aria-label="success-circle-pipeline"/>, color: "text-green-600" },
    [PipelineBuildCurrentStateEnum.Failed]: { icon: <XCircle className="text-red-600" size={18} />, color: "text-red-600" },
    [PipelineBuildCurrentStateEnum.Running]: { icon: <Loader className="text-yellow-600 animate-spin" size={25} />, color: "text-yellow-600" },
    [PipelineBuildCurrentStateEnum.Waiting]: { icon: <Loader className="text-blue-600 animate-spin" size={18} />, color: "text-blue-600" },
  };
  const { icon = null, color = "text-gray-500" } = stateMap[build.currentState ?? ""] ?? {};

  return (
    <>
      <NavBar />

      <div className="min-h-screen p-14 ml-10">
        <div className=" mb-10 mt-10 text-center flex-row gap-6 flex">
          <PipelineHeader name={build.pipelineName} />
          <span className={color}>{icon}</span>
          {/* Delete button */}
          <div className="relative group inline-block">
            <button
              onClick={isDeleteVisible ? handleDelete : undefined}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl transition-all shadow-sm
      ${
        isDeleteVisible
          ? "bg-red-400 text-white hover:bg-red-500 cursor-pointer"
          : "bg-gray-300 text-gray-500 cursor-not-allowed"
      }`}
            >
              <Trash size={18} />
              Delete
            </button>

            {/* Tooltip */}
            {!isDeleteVisible && (
              <div
                className="absolute left-1/2 -translate-x-1/2 mt-2 w-max px-3 py-1 
      rounded-md bg-black text-white text-xs opacity-0 group-hover:opacity-100 
      transition-opacity pointer-events-none shadow-lg"
              >
                This Build is running, can't delete now.
              </div>
            )}
          </div>
        </div>

        <div className="flex flex-row gap-10">
          <BuildStageList
            stages={build.stageBuilds!}
            selectedId={selectedStageId}
            onSelect={setSelectedStageId}
          />

          {selectedStage && <BuildStageDetails stage={selectedStage} />}
        </div>
      </div>
    </>
  );
}
