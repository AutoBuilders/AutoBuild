import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import StageList from "../../components/pipelines/stageList";
import { booleanFlags } from "../../flags/booleanFlags";

import type { Pipeline, Stage } from "../../gen/api";
import { pipelineApiInstance, pipelineBuildApiInstance } from "../../services/pipelines.api";

import { Play, Pencil } from "lucide-react";
import NavBar from "../../components/common/navBar";

/**
 *  This page includes the Pipeline Details page.
 *  It displays the details of a pipeline and allows the user to edit the pipeline.
 *  It has two modes: view mode and edit mode. In view mode, the user can see the details of the pipeline and start a build. In edit mode, 
 *  the user can edit the name and stages of the pipeline.
 * */ 



export default function PipelineDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [pipeline, setPipeline] = useState<Pipeline | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isCreateMode, setIsCreateMode] = useState(false);
  const [editablePipeline, setEditablePipeline] = useState<Pipeline | null>(null);
  const [isDeleteVisible, setIsDeleteVisible] = useState(false);
  const [error, setError] = useState<string[] | null>(null);

  const saveOrCreate = isCreateMode ? "Create" : "Save";

  async function checkDelete(pipelineId: string) {
    const { status, data } = await pipelineBuildApiInstance.getAllBuilds();
    if (status !== 200) {
      console.error("Error getting builds");
      return false;
    }
    return !data.some((build) => build.pipelineId === pipelineId);
  }

  // Fetch pipeline data on component mount or when the id changes
  useEffect(() => {
    async function fetchData() {
      setError(null);

      // If the id is "0", it means we are creating a new pipeline, so we set up a new pipeline object and enter edit mode.
      if (id === "0") {
        const newPipeline: Pipeline = { id: crypto.randomUUID(), name: "New Pipeline", stages: [] };
        setEditablePipeline(newPipeline);
        setPipeline(newPipeline);
        setIsEditing(true);
        setIsCreateMode(true);
        return;
      }

      // Otherwise, we fetch the pipeline data from the API.
      const response = await pipelineApiInstance.getPipelineById(id as string);
      if (!response || response.status !== 200) {
        setError(["Pipeline not found"]);
        navigate("/pipelines");
        return;
      }

      setPipeline(response.data);
      setIsDeleteVisible((await checkDelete(id as string)) as boolean);
    }

    fetchData();
  }, [id, navigate]);

  if (!pipeline) {
    return (
      <div>
        <NavBar />
        <div className="px-16 py-12 min-h-screen bg-[#F5F5F5]">
          <p className="text-sm text-[#71717A]">Loading...</p>
        </div>
      </div>
    );
  }

  // Handler for the "Edit" button click
  function handleEdit() {
    setEditablePipeline(JSON.parse(JSON.stringify(pipeline)));
    setIsEditing(true);
  }

  // Handler for the "Delete" button click
  async function handleDelete() {
    const { status } = await pipelineApiInstance.deletePipeline(id as string);
    if (status === 204) navigate("/pipelines");
  }

  // helper function to remove the "order" property from stages before saving to the backend
  function removeOrderFromStages(stages: Stage[]): Stage[] {
    return stages.map(({ name, scriptType, command }) => ({ name, scriptType, command }));
  }

  // Handler for the "Save" button click
  async function handleSave() {
    // If we are in create mode, we need to create a new pipeline. Otherwise, we update the existing pipeline.
    // currently we don't have an update endpoint, so we only handle the create case.
    if (!isCreateMode) return;
    setIsCreateMode(false);

    const pipelineToSave = {
      name: editablePipeline!.name,
      stages: removeOrderFromStages(editablePipeline!.stages),
    } as unknown as Pipeline;

    const { status, data } = await pipelineApiInstance.createPipeline(pipelineToSave);
    if (status !== 201 || typeof data.id === "undefined") {
      // If the creation fails, we display the error message and stay in edit mode.
      // TODO: Handle error properly (e.g., show a notification to the user)
      setError(data as unknown as string[]);
      setIsCreateMode(true);
      setTimeout(() => setError(null), 5000);
      return;
    }

    setIsEditing(false);
    navigate(`/pipelines/${data.id}`);
  }

  // Handler for the "Cancel" button click
  function handleCancel() {
    // If we are in create mode, we navigate back to the pipelines list. Otherwise, we discard the changes and exit edit mode.
    if (isCreateMode) {
      navigate("/pipelines");
      return;
    }
    setEditablePipeline(null);
    setIsEditing(false);
  }

  // Handler for the "Run Now" button click
  async function handleStartBuild() {
    const { status, data } = await pipelineApiInstance.executePipeline({ pipelineId: id as string });
    if (status !== 202) {
      console.error("Error starting build");
      return;
    }
    navigate(`/builds/${data.id}`);
  }

  const activePipeline = isEditing ? editablePipeline! : pipeline;

  return (
    <>
      <NavBar />
      <div className="min-h-screen bg-[#F5F5F5] px-16 py-10 pb-24">

        {/* Error banner */}
        {error && (
          <div className="mb-6 px-4 py-3 bg-red-50 text-red-700 border border-red-200 rounded-lg text-sm">
            {`Error: ${error}`}
          </div>
        )}

        {/* Back link / editing badge */}
        {isEditing ? (
          <div className="flex items-center gap-2 mb-5">
            <span className="text-[12px] font-medium text-blue-600 bg-blue-50 border border-blue-200 rounded px-2 py-0.5">
              Editing
            </span>
            <span className="text-[13px] text-[#71717A]">Changes are not saved yet</span>
          </div>
        ) : (
          <button
            onClick={() => navigate("/pipelines")}
            className="flex items-center gap-1 text-[13px] text-[#71717A] hover:text-[#18181B] transition-colors mb-5"
          >
            ← My Pipelines
          </button>
        )}

        {/* Header row */}
        <div className="flex items-start justify-between mb-2">
          <div className="flex-1 mr-8">
            {isEditing ? (
              <input
                className="w-full max-w-xl bg-transparent text-[#18181B] text-[28px]
                  border-0 border-b-2 border-blue-500 outline-none px-0 py-1 placeholder:text-[#A1A1AA]"
                style={{ fontFamily: "'Libre Baskerville', Georgia, serif" }}
                value={editablePipeline?.name ?? ""}
                onChange={(e) =>
                  setEditablePipeline((prev) => prev ? { ...prev, name: e.target.value } : prev)
                }
              />
            ) : (
              <h1
                className="text-[28px] text-[#18181B] leading-tight tracking-tight"
                style={{ fontFamily: "'Libre Baskerville', Georgia, serif" }}
              >
                {pipeline.name}
              </h1>
            )}
          </div>

          {/* Action buttons */}
          <div className="flex items-center gap-2 mt-1 shrink-0">
            {isEditing ? (
              <>
                <button
                  onClick={handleCancel}
                  className="h-9 px-4 text-[13px] font-medium text-[#52525B] bg-white border border-[#E4E4E7]
                    rounded-md hover:bg-[#F4F4F5] transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSave}
                  className="h-9 px-4 text-[13px] font-medium text-white bg-[#18181B]
                    rounded-md hover:bg-[#27272A] transition-colors"
                >
                  {saveOrCreate}
                </button>
              </>
            ) : (
              <>
                {isDeleteVisible && (
                  <button
                    onClick={handleDelete}
                    className="h-9 px-4 text-[13px] font-medium text-red-600 border border-red-200
                      rounded-md hover:bg-red-50 transition-colors"
                  >
                    Delete
                  </button>
                )}
                <button
                  onClick={handleStartBuild}
                  className="flex items-center gap-1.5 h-9 px-4 text-[13px] font-medium text-[#18181B]
                    bg-white border border-[#E4E4E7] rounded-md hover:bg-[#F4F4F5] transition-colors"
                >
                  <Play size={13} />
                  Run Now
                </button>
                {booleanFlags.ENABLE_EDIT_PIPELINE && (
                  <button
                    onClick={handleEdit}
                    className="flex items-center gap-1.5 h-9 px-4 text-[13px] font-medium text-white
                      bg-[#18181B] rounded-md hover:bg-[#27272A] transition-colors"
                  >
                    <Pencil size={13} />
                    Edit Pipeline
                  </button>
                )}
              </>
            )}
          </div>
        </div>

        {/* Metadata row */}
        {!isEditing && (
          <div className="flex items-center gap-2 mb-6 text-[13px] text-[#52525B]">
            <span>{activePipeline.stages.length} stages</span>
          </div>
        )}

        {/* Divider */}
        <div className="w-full h-px bg-[#E4E4E7] mb-6" />

        {/* Stages label */}
        <p className="text-[11px] font-medium uppercase tracking-widest text-[#71717A] mb-4">
          Stages · {activePipeline.stages.length}
        </p>

        {/* Stage flow */}
        <StageList
          stages={activePipeline.stages}
          canEdit={isEditing}
          onStagesChange={(stages) =>
            setEditablePipeline((prev) => prev ? { ...prev, stages } : prev)
          }
        />
      </div>
    </>
  );
}
