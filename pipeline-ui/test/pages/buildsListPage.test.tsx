import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { MemoryRouter } from "react-router-dom";

import BuildsListPage from "../../src/pages/BuildsPage/BuildsListPage";
import { pipelineBuildApiInstance } from "../../src/services/pipelines.api";
import { PipelineBuildCurrentStateEnum } from "../../src/gen";

// Mock the API calls
vi.mock(import("../../src/services/pipelines.api"));

describe("BuildsListPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders builds list page content", () => {
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce([]);

    render(
      <MemoryRouter>
        <BuildsListPage />
      </MemoryRouter>,
    );
    expect(screen.getByText("MY BUILDS")).toBeInTheDocument();
  });

  it("fetches builds on load and displays them", async () => {
    const mockBuildsResponse = {
      status: 200,
      statusText: "OK",
      data: [
        {
          id: "build-1",
          pipelineId: "1",
          pipelineName: "Pipeline 1",
          currentState: PipelineBuildCurrentStateEnum.Running,
          stageBuilds: []
        },
        {
          id: "build-2",
          pipelineId: "2",
          pipelineName: "Pipeline 2",
          currentState: PipelineBuildCurrentStateEnum.Running,
          stageBuilds: []
        }
      ]
    };

    vi.mocked(pipelineBuildApiInstance.getAllBuilds).mockResolvedValueOnce(mockBuildsResponse as any);

    render(
      <MemoryRouter>
        <BuildsListPage />
      </MemoryRouter>,
    );

    expect(pipelineBuildApiInstance.getAllBuilds).toHaveBeenCalled();

    // Wait for the builds to be rendered (the names come from getPipeline)
    const build1 = await screen.findByText("build-1");
    const build2 = await screen.findByText("build-2");
    expect(build1).toBeInTheDocument();
    expect(build2).toBeInTheDocument();
  });

  it("Filters builds based on search input", async () => {
    const mockBuildsResponse = {
      status: 200,
      statusText: "OK",
      data: [
        {
          id: "build-1",
          pipelineId: "1",
          pipelineName: "Pipeline 1",
          currentState: PipelineBuildCurrentStateEnum.Running,
          stageBuilds: []
        },
        {
          id: "build-2",
          pipelineId: "2",
          pipelineName: "Pipeline 2",
          currentState: PipelineBuildCurrentStateEnum.Running,
          stageBuilds: []
        }
      ]
    };
    
    vi.mocked(pipelineBuildApiInstance.getAllBuilds).mockResolvedValueOnce(mockBuildsResponse as any);

    render(
      <MemoryRouter>
        <BuildsListPage />
      </MemoryRouter>,
    );

    // Wait for the builds to be rendered
    const build1 = await screen.findByText("build-1");
    const build2 = await screen.findByText("build-2");
    expect(build1).toBeInTheDocument();
    expect(build2).toBeInTheDocument();

    // Type in the search input
    const searchInput = screen.getByPlaceholderText("search builds...");
    fireEvent.change(searchInput, { target: { value: "Pipeline 1" } });

    // Wait for the filtering to take effect
    await waitFor(() => {
      expect(screen.getByText("Pipeline 1")).toBeInTheDocument();
      expect(screen.queryByText("Pipeline 2")).not.toBeInTheDocument();
    });
  });
});
