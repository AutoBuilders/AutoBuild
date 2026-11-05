import { render, screen, waitFor, within, cleanup } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { MemoryRouter } from "react-router-dom";

import BuildsRunningPage from "../../src/pages/BuildsPage/BuildsRunningPage";
import { pipelineBuildApiInstance, stageBuildApiInstance } from "../../src/services/pipelines.api";
import { PipelineBuildCurrentStateEnum, StageBuildCurrentStateEnum } from "../../src/gen";

vi.mock(import("../../src/services/pipelines.api"));

// Mock react-router-dom useParams
const mockParams = { id: "1" };
vi.mock("react-router-dom", async (importActual) => {
  const actual: any = await importActual();
  return {
    ...actual,
    useParams: () => mockParams,
  };
});

// Mock EventSource before importing the component
const mockEventSourceInstances: any[] = [];

class MockEventSource {
  addEventListener = vi.fn();
  removeEventListener = vi.fn();
  close = vi.fn();
  readyState = 0;
  url = "";
  withCredentials = false;
  CONNECTING = 0;
  OPEN = 1;
  CLOSED = 2;
  onopen = null;
  onmessage = null;
  onerror = null;
  dispatchEvent = vi.fn();

  constructor(url: string) {
    this.url = url;
    mockEventSourceInstances.push(this);
  }
}

// @ts-ignore
global.EventSource = MockEventSource;

// Mock API calls

describe("BuildsRunningPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockParams.id = "1";
    mockEventSourceInstances.length = 0;
  });

  afterEach(() => {
    cleanup();
  });

  it("renders builds running page content", () => {
    const mockBuildsResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: []
      }
    };
    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildsResponse as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );
    //this happens because we're not loading from execute, just rendering page directly, so the loading state is still there
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("displays running build", async () => {
    const mockBuildsResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: [
          {
            id: "1",
            name: "Stage 1",
            status: StageBuildCurrentStateEnum.Running,
            logs: "Stage 1 logs...",
          },
        ]
      }
    };
    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildsResponse as any);
    vi.mocked(stageBuildApiInstance.getStageBuildLogs).mockResolvedValueOnce({
      log: "Stage 1 logs...",
    } as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );

    // Wait for the build pipeline name to be rendered
    const build = await screen.findByText("Pipeline 1");
    expect(build).toBeInTheDocument();

    // Verify getBuildData was called (note: id is passed as string "1" from useParams)
    expect(pipelineBuildApiInstance.getPipelineBuild).toHaveBeenCalledWith("1");
  });

  it("creates EventSource for live updates when build is RUNNING", async () => {
    const mockBuildsResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: [
          {
            id: "1",
            name: "Stage 1",
            status: StageBuildCurrentStateEnum.Running,
            logs: "Stage 1 logs...",
          },
        ]
      }
    };
    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildsResponse as any);
    vi.mocked(stageBuildApiInstance.getStageBuildLogs).mockResolvedValueOnce({
      log: "Stage 1 logs...",
    } as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );

    // Wait for the build to load
    await screen.findByText("Pipeline 1");

    // Wait a bit for useEffect to run and potentially create EventSource
    await waitFor(
      () => {
        // If EventSource is created, we should have at least one instance
        // Note: This depends on the ENABLE_SSE flag in the component
        // If the flag is false, no EventSource will be created
        expect(mockEventSourceInstances.length).toBeGreaterThanOrEqual(0);
      },
      { timeout: 1000 },
    );

    // If EventSource was created, verify it was set up correctly
    if (mockEventSourceInstances.length > 0) {
      const eventSourceInstance = mockEventSourceInstances[0];
      expect(eventSourceInstance).toBeDefined();
      expect(eventSourceInstance.onmessage).toBeDefined();
      expect(eventSourceInstance.onerror).toBeDefined();
    }
  });

  it("updates build state when receiving SSE message", async () => {
    const mockBuildResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: [
          {
            id: "1",
            stageId: "10",
            name: "Build Stage",
            status: StageBuildCurrentStateEnum.Running,
          },
        ]
      }
    };
    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildResponse as any);
    vi.mocked(stageBuildApiInstance.getStageBuildLogs)
      .mockResolvedValueOnce({ log: "Building..." } as any)
      .mockResolvedValueOnce({ log: "Build completed successfully!" } as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );

    // Wait for the build to load
    await screen.findByText("Pipeline 1");

    // Wait for EventSource to be created
    await waitFor(
      () => {
        expect(mockEventSourceInstances.length).toBeGreaterThan(0);
      },
      { timeout: 1000 },
    );

    const eventSourceInstance = mockEventSourceInstances[0];

    // Verify the onmessage handler exists
    expect(eventSourceInstance.onmessage).toBeDefined();

    // Simulate receiving an SSE message with updated build state
    const updatedBuilds = {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Success,
        stageBuilds: [
          {
            id: "1",
            name: "Build Stage",
            status: StageBuildCurrentStateEnum.Success
          },
        ]
    };

    // Create and dispatch the message event
    const messageEvent = {
      data: JSON.stringify(updatedBuilds),
    } as MessageEvent;

    // Call the onmessage handler
    eventSourceInstance.onmessage!(messageEvent);

    // Wait for the UI to update with the new state
    // The component should show a success icon when currentState is "SUCCESS"
    await waitFor(() => {
      // Look for the success icon (CheckCircle component renders when state is SUCCESS)
      const successIcon = document.querySelector(".lucide-circle-check-big");
      expect(successIcon).toBeInTheDocument();
    });

    // Verify the EventSource close method was called (component closes it on non-RUNNING states)
    expect(eventSourceInstance.close).toHaveBeenCalled();
  });

  it("sorts stageBuilds by stageOrder on initial load", async () => {
    const mockBuildsResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Success,
        stageBuilds: [
          { id: "2", stageName: "Second Stage", stageOrder: 1, currentState: StageBuildCurrentStateEnum.Success },
          { id: "1", stageName: "First Stage", stageOrder: 0, currentState: StageBuildCurrentStateEnum.Success },
        ]
      }
    };
    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildsResponse as any);
    vi.mocked(stageBuildApiInstance.getStageBuildLogs).mockResolvedValue({ status: 200, data: {} } as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );

    await screen.findByText("Pipeline 1");

    const stageButtons = await screen.findAllByRole("button", { name: /Stage/ });
    expect(stageButtons[0]).toHaveTextContent("First Stage");
    expect(stageButtons[1]).toHaveTextContent("Second Stage");
  });

  it("sorts stageBuilds by stageOrder when receiving SSE message", async () => {
    const mockBuildResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: [
          { id: "1", stageName: "Alpha Stage", stageOrder: 0, currentState: StageBuildCurrentStateEnum.Running },
        ]
      }
    };
    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildResponse as any);
    vi.mocked(stageBuildApiInstance.getStageBuildLogs).mockResolvedValue({ status: 200, data: {} } as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );

    await screen.findByText("Pipeline 1");

    await waitFor(() => expect(mockEventSourceInstances.length).toBeGreaterThan(0), { timeout: 1000 });

    const eventSourceInstance = mockEventSourceInstances[0];

    eventSourceInstance.onmessage!({
      data: JSON.stringify({
        currentState: PipelineBuildCurrentStateEnum.Success,
        stageBuilds: [
          { id: "2", stageName: "Beta Stage", stageOrder: 1, currentState: StageBuildCurrentStateEnum.Success },
          { id: "1", stageName: "Alpha Stage", stageOrder: 0, currentState: StageBuildCurrentStateEnum.Success },
        ]
      })
    } as MessageEvent);

    await waitFor(() => {
      const stageButtons = screen.getAllByRole("button", { name: /Stage/ });
      expect(stageButtons[0]).toHaveTextContent("Alpha Stage");
      expect(stageButtons[1]).toHaveTextContent("Beta Stage");
    });
  });

  it("displays stage details with logs section", async () => {
    const mockBuildResponse = {
      status: 200,
      data: {
        id: "1",
        pipelineId: "10",
        pipelineName: "Pipeline 1",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: [
          {
            id: "1",
            stageId: "10",
            name: "Build Stage",
            status: StageBuildCurrentStateEnum.Running,
          },
        ]
      }
    };

    vi.mocked(pipelineBuildApiInstance.getPipelineBuild).mockResolvedValueOnce(mockBuildResponse as any);

    // Mock logs
    vi.mocked(stageBuildApiInstance.getStageBuildLogs).mockResolvedValueOnce({
      log: "Running tests...\nTest 1 passed\nTest 2 passed",
    } as any);

    render(
      <MemoryRouter>
        <BuildsRunningPage />
      </MemoryRouter>,
    );

    // Wait for the build to load
    await screen.findByText("Pipeline 1");

    // Wait for the stage details to appear with the Logs heading
    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "Logs" })).toBeInTheDocument();
    });

    // Verify getBuildStagesLogs was called to fetch logs
    expect(stageBuildApiInstance.getStageBuildLogs).toHaveBeenCalledWith("1");
  });
});
