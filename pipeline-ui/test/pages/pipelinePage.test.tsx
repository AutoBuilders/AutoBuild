import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { MemoryRouter } from "react-router-dom";

import PipelinePage from "../../src/pages/PipelineListPage/PipelinesListPage";
import { pipelineApiInstance } from "../../src/services/pipelines.api";

//mocks for use Effect
vi.mock("../../src/services/pipelines.api");

//mocks for useNavigate
const mockNavigate = vi.fn();

vi.mock("react-router-dom", async (importActual) => {
  const actual: any = await importActual();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("PipelinePage", () => {
  it("renders pipeline page content", () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);
    
    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );
    expect(screen.getByText("My Pipelines")).toBeInTheDocument();
    expect(screen.getByText("Create")).toBeInTheDocument();
  });

  it("fetches pipelines on load", async () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);

    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );

    expect(pipelineApiInstance.getAllPipelines).toHaveBeenCalled();
    // Wait for the pipelines to be rendered
    const pipeline1 = await screen.findByText("Pipeline 1");
    const pipeline2 = await screen.findByText("Pipeline 2");
    expect(pipeline1).toBeInTheDocument();
    expect(pipeline2).toBeInTheDocument();
  });

  it("filters pipelines based on search input", async () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
        { id: "3", name: "Test Pipeline"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);

    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );

    // Wait for pipelines to load
    await screen.findByText("Pipeline 1");
    await screen.findByText("Pipeline 2");
    await screen.findByText("Test Pipeline");

    const searchInput = screen.getByPlaceholderText("search pipelines...");

    // Simulate typing "Pipeline 1" in the search input
    fireEvent.change(searchInput, { target: { value: "Pipeline 1" } });

    // After filtering, only "Pipeline 1" should be visible
    await waitFor(() => {
      expect(screen.getByText("Pipeline 1")).toBeInTheDocument();
      expect(screen.queryByText("Pipeline 2")).not.toBeInTheDocument();
      expect(screen.queryByText("Test Pipeline")).not.toBeInTheDocument();
    });
  });

  it("filters pipelines with partial match (case insensitive)", async () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
        { id: "3", name: "Test Pipeline"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);

    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );

    // Wait for pipelines to load
    await screen.findByText("Pipeline 1");

    const searchInput = screen.getByPlaceholderText("search pipelines...");

    // Search for "test" (lowercase) to match "Test Pipeline"
    fireEvent.change(searchInput, { target: { value: "test" } });

    // Only "Test Pipeline" should match
    await waitFor(() => {
      expect(screen.getByText("Test Pipeline")).toBeInTheDocument();
      expect(screen.queryByText("Pipeline 1")).not.toBeInTheDocument();
      expect(screen.queryByText("Pipeline 2")).not.toBeInTheDocument();
    });
  });

  it("shows no pipelines when search has no matches", async () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);

    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );

    // Wait for pipelines to load
    await screen.findByText("Pipeline 1");

    const searchInput = screen.getByPlaceholderText("search pipelines...");

    // Search for something that doesn't match
    fireEvent.change(searchInput, { target: { value: "NonExistent" } });

    // No pipelines should be visible
    await waitFor(() => {
      expect(screen.queryByText("Pipeline 1")).not.toBeInTheDocument();
      expect(screen.queryByText("Pipeline 2")).not.toBeInTheDocument();
    });
  });

  it("shows all pipelines when search is cleared", async () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);

    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );

    // Wait for pipelines to load
    await screen.findByText("Pipeline 1");

    const searchInput = screen.getByPlaceholderText("search pipelines...");

    // First filter to show only Pipeline 1
    fireEvent.change(searchInput, { target: { value: "Pipeline 1" } });

    await waitFor(() => {
      expect(screen.queryByText("Pipeline 2")).not.toBeInTheDocument();
    });

    // Clear the search
    fireEvent.change(searchInput, { target: { value: "" } });

    // All pipelines should be visible again
    await waitFor(() => {
      expect(screen.getByText("Pipeline 1")).toBeInTheDocument();
      expect(screen.getByText("Pipeline 2")).toBeInTheDocument();
    });
  });

  it("navigates to pipeline creation page on Create button click", async () => {
    const mockResponse =  {
      status: 200,
      data: [
        { id: "1", name: "Pipeline 1"},
        { id: "2", name: "Pipeline 2"},
      ]
    };
    vi.mocked(pipelineApiInstance.getAllPipelines).mockResolvedValueOnce(mockResponse as any);

    render(
      <MemoryRouter>
        <PipelinePage />
      </MemoryRouter>,
    );

    const createButton = screen.getByText("Create");
    fireEvent.click(createButton);

    expect(mockNavigate).toHaveBeenCalledWith("/pipelines/0");
  });
});
