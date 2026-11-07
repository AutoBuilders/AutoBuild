import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import PipelineHeader from "../../components/pipelines/pipelineHeader";
import StageList from "../../components/pipelines/stageList";
import StageDetails from "../../components/pipelines/stageDetails";
import { booleanFlags } from "../../flags/booleanFlags";

import type {
  Pipeline,
  Stage
} from "../../gen/api";

import {pipelineApiInstance, pipelineBuildApiInstance} from "../../services/pipelines.api";

import { Pencil, Play, Trash } from "lucide-react";
import NavBar from "../../components/common/navBar";
import { useNavigate } from "react-router-dom";

export default function PipelineDetailPage() {
  const { id } = useParams();

  // The "real" pipeline (server data)
  const [pipeline, setPipeline] = useState<Pipeline | null>(null);

  // The stage currently displayed on the right side
  const [selectedStageId, setSelectedStageId] = useState<string>("");

  // Whether the user is editing or just viewing
  const [isEditing, setIsEditing] = useState(false);

  //Create flow
  const [isCreateMode, setIsCreateMode] = useState(false);
  const saveOrCreate = isCreateMode ? "Create" : "Save";

  // Editable clone (we do not modify the real pipeline until Save)
  const [editablePipeline, setEditablePipeline] = useState<Pipeline | null>(
    null,
  );
  const [isDeleteVisible, setIsDeleteVisible] = useState(false);
  const [error, setError] = useState<string[] | null>(null);

  const navigate = useNavigate();

  // TODO: request change from backend as getBuildsList here is unnecessary dependency
  async function checkDelete(id: string) {
    const {status, data} = await pipelineBuildApiInstance.getAllBuilds();

    if (status !== 200) {
      console.error("Error getting builds");
      return;
    }

    let flag = true;
    //checking if pipeline id exists in the builds list
    data.forEach((build) => {
      if (build.pipelineId === id) {
        flag = false;
      }
    });

    return flag;
  }

  // Load pipeline on mount / when ID changes
  useEffect(() => {
    async function fetchData() {
      setError(null);
      if (id === "0") {
        // New pipeline template
        const newPipeline: Pipeline = {
          id: crypto.randomUUID(),
          name: "New Pipeline",
          stages: [],
        };
        setEditablePipeline(newPipeline);
        setIsEditing(true);
        setPipeline(newPipeline);
        setIsCreateMode(true);
        return;
      }
      
      const response = await pipelineApiInstance.getPipelineById(id as string);

      if (!response || response.status !== 200) {
        setError(["Pipeline not found"]);
        navigate("/pipelines");
        return;
      }

      const { data } = response;

      setPipeline(data);

      setIsDeleteVisible(await checkDelete(id as string) as boolean);

      // Auto-select first stage on load
      if (data.stages.length > 0) {
        setSelectedStageId(data.stages[0].id as string);
      }
    }

    fetchData();
  }, [id, navigate]);

  // While loading
  if (!pipeline)
    return (
      <div>
        <NavBar></NavBar>
        <div className="p-20 min-h-screen">Loading...</div>
      </div>
    );

  //   // Current displayed stage (from real pipeline)
  //   const selectedStage = pipeline.stages.find((s) => s.id === selectedStageId);

  // Begin editing -> clone pipeline
  function handleEdit() {
    // Create a deep-ish copy so user can safely edit
    setEditablePipeline(JSON.parse(JSON.stringify(pipeline)));
    setIsEditing(true);
    console.log("Editing pipeline:", pipeline);
  }

  async function handleDelete() {
    const {status} = await pipelineApiInstance.deletePipeline(id as string);
    if (status === 204) {
      console.log("Pipeline deleted");
      navigate("/pipelines");
    }
  }

  // Save changes and exit edit mode
  async function handleSave() {
    if (isCreateMode) {
      setIsCreateMode(false);
      const pipelineToSave = {
        name: editablePipeline!.name,
        stages: editablePipeline!.stages.map((stage, i) => ({
            name: stage.name,
            scriptType: stage.scriptType,
            command: stage.command,
            stageOrder: i,
          })),
      } as unknown as Pipeline;
      
      const {status, data} = await pipelineApiInstance.createPipeline(pipelineToSave);
      if (status !== 201 || typeof data.id === "undefined") {
        setError(data as unknown as string[]);
        setIsCreateMode(true);
        setTimeout(() => {
          setError(null);
        }, 5000);
        return;
      }
      setIsEditing(false);
      navigate(`/pipelines/${data.id}`);
      return;
    }
  }

  // Cancel editing -> discard clone
  function handleCancel() {
    if (isCreateMode) {
      navigate("/pipelines");
    }
    setEditablePipeline(null);
    setIsEditing(false);
  }

  async function handleStartBuild() {
    const {status, data} = await pipelineApiInstance.executePipeline({
      pipelineId: id as string
    });

    if (status !== 202) {
      console.error("Error starting build");
      return;
    }

    navigate(`/builds/${data.id}`);
  }

  // Helper: update a stage inside editablePipeline
  function updateStage(updatedStage: Stage) {
    setEditablePipeline((prev) => {
      if (!prev) return prev;

      return {
        ...prev,
        stages: prev.stages.map((s) =>
          s.id === updatedStage.id ? updatedStage : s,
        ),
      };
    });
  }

  function handleAddStage() {
    if (!editablePipeline) return;

    const nextId = crypto.randomUUID();

    const newStage: Stage = {
      id: nextId,
      name: "New Stage",
      scriptType: "bash",
      command: "",
    };

    setEditablePipeline((prev) => ({
      ...prev!,
      stages: [...prev!.stages, newStage],
    }));

    setSelectedStageId(nextId);
  }

  function handleDeleteStage(stageId: string) {
    if (!editablePipeline) return;

    const stages = editablePipeline.stages
      .filter((s) => s.id !== stageId)
      .map((s, i) => ({ ...s, order: i + 1 }));

    setEditablePipeline((prev) => ({ ...prev!, stages }));

    // ensure valid selection
    if (stages.length === 0) {
      setSelectedStageId("");
    } else if (!stages.some((s) => s.id === selectedStageId)) {
      setSelectedStageId(stages[0].id as string); //TODO: what to do incase of undefined id?
    }
  }

  function moveStage(stageId: string, direction: "up" | "down") {
    if (!editablePipeline) return;

    const stages = [...editablePipeline.stages]

    const index = stages.findIndex((s) => s.id === stageId);

    if (direction === "up" && index > 0) {
      [stages[index - 1], stages[index]] = [stages[index], stages[index - 1]];
    }

    if (direction === "down" && index < stages.length - 1) {
      [stages[index], stages[index + 1]] = [stages[index + 1], stages[index]];
    }

    const reordered = stages.map((s, i) => ({ ...s, order: i + 1 }));

    setEditablePipeline((prev) =>
      prev ? { ...prev, stages: reordered } : prev,
    );
  }

  // The pipeline used for rendering (depends on edit mode)
  const activePipeline = isEditing ? editablePipeline! : pipeline;

  return (
    <>
      <NavBar></NavBar>
      <div className="p-20 min-h-screen">
        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 border border-red-400 rounded">
            {`Error: ${error}`}
          </div>
        )}
        {/* --- HEADER AREA (Name + Action Buttons) --- */}
        <div className="w-full flex items-center justify-between">
          {/* Pipeline Name — text or input */}
          {isEditing ? (
            <input
              className="text-3xl font-bold border px-3 py-2 rounded-lg w-1/2"
              value={editablePipeline?.name ?? ""}
              onChange={(e) =>
                setEditablePipeline((prev) =>
                  prev ? { ...prev, name: e.target.value } : prev,
                )
              }
            />
          ) : (
            <PipelineHeader name={pipeline.name} />
          )}

          {/* ACTION BUTTONS */}
          <div className="flex items-center gap-4">
            {!isEditing ? (
              <>
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
                      This stage cannot be deleted because it is used in a
                      build.
                    </div>
                  )}
                </div>

                {/* Edit Button */}
                {booleanFlags.ENABLE_EDIT_PIPELINE && (
                  <button
                    onClick={handleEdit}
                    className="flex items-center gap-2 px-4 py-2 bg-gray-200 text-gray-700 
                  rounded-xl hover:bg-gray-300 transition-all shadow-sm"
                  >
                    <Pencil size={18} />
                    Edit
                  </button>
                )}

                {/* Start Build */}
                <button
                  onClick={handleStartBuild}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white 
                  rounded-xl hover:bg-blue-700 transition-all shadow shadow-blue-300"
                >
                  <Play size={18} />
                  Start Build
                </button>
              </>
            ) : (
              <>
                {/* Save Or Create*/}
                <button
                  onClick={handleSave}
                  className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white 
                  rounded-xl hover:bg-green-700 transition-all shadow shadow-green-300"
                >
                  {saveOrCreate}
                </button>

                {/* Cancel */}
                <button
                  onClick={handleCancel}
                  className="flex items-center gap-2 px-4 py-2 bg-gray-300 text-gray-700 
                  rounded-xl hover:bg-gray-400 transition-all"
                >
                  Cancel
                </button>
              </>
            )}
          </div>
        </div>
        {/* --- DESCRIPTION ---
      {isEditing ? (
        <textarea
          className="mt-4 w-full border px-3 py-2 rounded-lg"
          rows={3}
          value={editablePipeline?.description ?? ""}
          onChange={(e) =>
            setEditablePipeline((prev) =>
              prev ? { ...prev, description: e.target.value } : prev
            )
          }
        />
      ) : (
        <PipelineDescription text={pipeline.description} />
      )} */}
        {/* --- STAGES + RIGHT PANEL --- */}
        <div className="flex flex-row mt-10">
          <StageList
            stages={isEditing ? editablePipeline!.stages : pipeline.stages}
            selectedId={selectedStageId}
            onSelect={setSelectedStageId}
            canEdit={isEditing}
            onAddStage={handleAddStage}
            onDeleteStage={handleDeleteStage}
            onMoveStageUp={(id: string) => moveStage(id, "up")}
            onMoveStageDown={(id: string) => moveStage(id, "down")}
          />

          <StageDetails
            stage={activePipeline.stages.find((s) => s.id === selectedStageId)!}
            isEditing={isEditing}
            onChangeStage={updateStage}
          />
        </div>
      </div>
    </>
  );
}
