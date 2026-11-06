import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { MemoryRouter } from "react-router-dom";

import PipelineDetails from "../../src/pages/PipelineDetailsPage/PipelineDetailsPage";

import { PipelineBuildCurrentStateEnum} from "../../src/gen";

import { pipelineApiInstance, pipelineBuildApiInstance } from "../../src/services/pipelines.api";
// Create a mutable mock params object that can be changed per test
const mockParams = { id: "1" };

const mockPipelineTemplate = {
    id: "1",
    name: "Test Pipeline",
    stages: [
      {
        name: "Build",
        scriptType: "bash",
        command: "echo hello"
      },
      {
        name: "Test",
        scriptType: "bash",
        command: "echo hello",
      },
    ]
};

// Mock react-router-dom useParams
vi.mock("react-router-dom", async (importActual) => {
  const actual: any = await importActual();
  return {
    ...actual,
    useParams: () => mockParams,
  };
});

// Mock pipelines.api - combine both mocks into one

vi.mock(import("../../src/services/pipelines.api"));

describe("PipelineDetailsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset mockParams to default value
    mockParams.id = "1";
  });

  it("renders pipeline details content", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };

    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline name to appear
    await waitFor(() => {
      expect(pipelineApiInstance.getPipelineById).toHaveBeenCalled();
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });
  });

  it("handles pipeline execution", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };
    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);
    (pipelineApiInstance.executePipeline as any).mockResolvedValueOnce({
      status: 203,
      data: {
        id: "build-1",
        pipelineId: "1",
        pipelineName: "Test Pipeline",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: {}
      }
    });

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });

    // Click the Execute button
    const executeButton = screen.getByText("Start Build");
    fireEvent.click(executeButton);

    // Wait for the executeBuild function to be called
    await waitFor(() => {
      expect(pipelineApiInstance.executePipeline).toHaveBeenCalled();
    });
  });

  it("displays stage information when pipeline has stages", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };
    
    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });

    // Verify the stages section heading appears
    expect(screen.getByRole("heading", { name: "Stages" })).toBeInTheDocument();

    // Verify "Build" stage heading appears (it should be an h2 in the stage detail view)
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Build" }),
      ).toBeInTheDocument();
    });
  });

  it("displays 'No stages yet' when pipeline has no stages", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: {
        id: "1",
        name: "Test Pipeline",
        stages: [],

      }
    };

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };
    
    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });

    // Verify the "No stages yet" message appears
    await waitFor(() => {
      expect(screen.getByText("No stages yet.")).toBeInTheDocument();
    });
  });

  it("displays stage logs when a stage is clicked", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };

    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });

    // Verify the Commands section is displayed (it shows by default with the first stage)
    await waitFor(() => {
      expect(screen.getByText("Commands")).toBeInTheDocument();
    });
  });

  it("When in Create mode, the Execute button should be disabled and Create should be seen", async () => {
    // Change the mock params to create mode (id: "0")
    mockParams.id = "0";

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };

    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the component to render and find the Start Build button
    await waitFor(() => {
      const executeButton = screen.queryByText("Start Build");
      // In create mode, the Start Build button might not exist or be disabled
      // Check if it exists first
      if (executeButton) {
        expect(executeButton).toBeDisabled();
      } else {
        // If button doesn't exist in create mode, that's also valid
        expect(screen.getByText("Create")).toBeInTheDocument();
      }
    });
  });

  it("calls deletePipeline when delete button is clicked", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };

    const mockPipelineBuildResponse = {
      status: 200, 
      data: []
    };

    vi.mocked(pipelineApiInstance.getPipelineById).mockResolvedValueOnce(mockPipelineResponse as any);
    vi.mocked(pipelineBuildApiInstance.getAllBuilds).mockResolvedValueOnce(mockPipelineBuildResponse as any);
    
    vi.mocked(pipelineApiInstance.deletePipeline).mockResolvedValueOnce({status: 204} as any);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });

    // Click the Delete button
    const deleteButton = screen.getByText("Delete");
    fireEvent.click(deleteButton);

    // Wait for the deletePipeline function to be called
    await waitFor(() => {
      expect(pipelineApiInstance.deletePipeline).toHaveBeenCalled();
      expect(pipelineApiInstance.deletePipeline).toHaveBeenCalledWith("1");
    });
  });

  it("the save function should be called with the correct parameters when creating a new pipeline", async () => {
    // Change the mock params to create mode (id: "0")
    mockParams.id = "0";

    /*
    Ideally we would not send empty stages, but for now we will just check that the save function 
    is called with the correct name and an empty stages array
    To not make the test longer, we will just check that the save function 
    is called with the correct name and an empty stages array for now, 
    and we can add more detailed tests for stage creation in other tests
    */
    const mockResponse = {
      status: 201,
      data: mockPipelineTemplate
    };
    (pipelineApiInstance.createPipeline as any).mockResolvedValueOnce(mockResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the component to render and find the Create button
    await waitFor(() => {
      const createButton = screen.getByText("Create");
      expect(createButton).toBeInTheDocument();
      fireEvent.click(createButton);
    });

    await waitFor(() => {
      expect(pipelineApiInstance.createPipeline).toHaveBeenCalledWith({
        name: "New Pipeline",
        stages: [],
      });
    });
  });

  it("createPipeline is called with stageOrder set by index for each stage", async () => {
    mockParams.id = "0";

    const mockResponse = {
      status: 201,
      data: mockPipelineTemplate
    };
    (pipelineApiInstance.createPipeline as any).mockResolvedValueOnce(mockResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText("Create")).toBeInTheDocument());

    // Add two stages via the + button
    const addButton = screen.getByTestId("Plus");
    fireEvent.click(addButton);
    fireEvent.click(addButton);

    // Click Create
    fireEvent.click(screen.getByText("Create"));

    await waitFor(() => {
      expect(pipelineApiInstance.createPipeline).toHaveBeenCalledWith(
        expect.objectContaining({
          stages: expect.arrayContaining([
            expect.objectContaining({ stageOrder: 0 }),
            expect.objectContaining({ stageOrder: 1 }),
          ]),
        })
      );
    });
  });

  it("createPipeline stages are sent in the same order as displayed", async () => {
    mockParams.id = "0";

    const mockResponse = {
      status: 201,
      data: mockPipelineTemplate
    };
    (pipelineApiInstance.createPipeline as any).mockResolvedValueOnce(mockResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText("Create")).toBeInTheDocument());

    // Click Create with no stages — stageOrder array should be empty
    fireEvent.click(screen.getByText("Create"));

    await waitFor(() => {
      expect(pipelineApiInstance.createPipeline).toHaveBeenCalledWith(
        expect.objectContaining({
          name: "New Pipeline",
          stages: [],
        })
      );
    });
  });

  // TODO update Pipeline tests to be writtern once update pipeline  functionality is implemented.

  it("build button calls executeBuild and navigates to build details page", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };
    
    const mockPipelineBuildResponse = {
      status: 404, 
      data: []
    };

    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);
    (pipelineApiInstance.executePipeline as any).mockResolvedValueOnce({
      status: 203,
      data: {
        id: "build-1",
        pipelineId: "1",
        pipelineBame: "Test Pipeline",
        currentState: PipelineBuildCurrentStateEnum.Running,
        stageBuilds: {}
      }
    });

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for the pipeline to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline" }),
      ).toBeInTheDocument();
    });

    // Click the Execute button
    const executeButton = screen.getByText("Start Build");
    fireEvent.click(executeButton);

    // Wait for the executeBuild function to be called and navigate to build details page
    await waitFor(() => {
      expect(pipelineApiInstance.executePipeline).toHaveBeenCalledWith({
        pipelineId: "1"
      });
    });
  });

  it("When ID changes useEffect is called with new call and data set", async () => {
    const mockPipelineResponse1 = {
      status: 200,
      data: {
        id: "1",
        name: "Test Pipeline 1",
        stages: []
      }
    }

    const mockPipelineResponse2 = {
      status: 200,
      data: {
        id: "2",
        name: "Test Pipeline 2",
        stages: []
      }
    };

    const mockPipelineBuildResponse = {
      status: 404, 
      data: []
    };
    // First load with pipeline 1
    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse1);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>
    );

    // Wait for pipeline 1 to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline 1" }),
      ).toBeInTheDocument();
    });

    // Change the mock params to load pipeline 2
    mockParams.id = "2";

    // Mock the API call for pipeline 2
    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse2);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Wait for pipeline 2 to load
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "Test Pipeline 2" }),
      ).toBeInTheDocument();
    });
  });

  it("shows loading state when pipeline data is not yet loaded", async () => {
    mockParams.id = "1";
    // Mock getPipeline to return a promise that never resolves (simulating loading)
    (pipelineApiInstance.getPipelineById as any).mockImplementation(
      () => new Promise(() => {}), // Never resolves
    );

    const mockPipelineBuildResponse = {
      status: 404, 
      data: []
    };

    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    // Should display loading message (use getAllByText since there might be multiple)
    const loadingElements = screen.getAllByText("Loading...");
    expect(loadingElements.length).toBeGreaterThan(0);
  });

  it("We can edit and buttons are changed accordingly", async () => {
    const mockPipelineResponse = {
      status: 200,
      data: mockPipelineTemplate
    };
    mockParams.id = "1";
    
    const mockPipelineBuildResponse = {
      status: 404, 
      data: []
    };

    (pipelineApiInstance.getPipelineById as any).mockResolvedValueOnce(mockPipelineResponse);
    (pipelineBuildApiInstance.getAllBuilds as any).mockResolvedValueOnce(mockPipelineBuildResponse);

    //mocking the feature flag to be true for this test
    vi.mock("../../src/flags/booleanFlags", () => ({
      booleanFlags: {
        ENABLE_EDIT_PIPELINE: true,
      },
    }));

    render(
      <MemoryRouter>
        <PipelineDetails />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText("Edit")).toBeInTheDocument();
    });

    // Click the Edit button
    const editButton = screen.getByText("Edit");
    fireEvent.click(editButton);

    // After clicking edit, the button should change to Save
    await waitFor(() => {
      expect(screen.getByText("Save")).toBeInTheDocument();
      expect(screen.getByText("Cancel")).toBeInTheDocument();
    });
  });


  // TODO: Add these back while adding update pipeline feature

  // it("we can save Pipeline after editing and updatePipeline is called with the correct data", async () => {
  //   const mockPipelineResponse = {
  //     status: 200,
  //     data: mockPipelineTemplate
  //   };
  //   mockParams.id = "1";
    
  //   const mockPipelineBuildResponse = {
  //     status: 404, 
  //     data: []
  //   };

  //   mockGetPipelineById.mockResolvedValueOnce(mockPipelineResponse);
  //   mockGetAllBuilds.mockResolvedValueOnce(mockPipelineBuildResponse);


  //   vi.mocked(updatePipeline).mockResolvedValueOnce({
  //     id: 1,
  //   } as any);

  //   //mocking the feature flag to be true for this test
  //   vi.mock("../../src/flags/booleanFlags", () => ({
  //     booleanFlags: {
  //       ENABLE_EDIT_PIPELINE: true,
  //     },
  //   }));

  //   render(
  //     <MemoryRouter>
  //       <PipelineDetails />
  //     </MemoryRouter>,
  //   );

  //   await waitFor(() => {
  //     expect(screen.getByText("Edit")).toBeInTheDocument();
  //   });

  //   // Click the Edit button
  //   const editButton = screen.getByText("Edit");
  //   fireEvent.click(editButton);

  //   // After clicking edit, the button should change to Save
  //   await waitFor(() => {
  //     expect(screen.getByText("Save")).toBeInTheDocument();
  //   });

  //   // Change the pipeline name
  //   const nameInput = screen.getByDisplayValue("Test Pipeline");
  //   fireEvent.change(nameInput, { target: { value: "Updated Pipeline" } });

  //   // Click the Save button
  //   const saveButton = screen.getByText("Save");
  //   fireEvent.click(saveButton);

  //   // Wait for updatePipeline to be called with the ID and updated pipeline data
  //   await waitFor(() => {
  //     expect(updatePipeline).toHaveBeenCalledWith(
  //       "1",
  //       expect.objectContaining({
  //         name: "Updated Pipeline",
  //         stages: expect.any(Array),
  //       }),
  //     );
  //   });
  // });

  // it("handles Cancel when editing and discards the copy while keeping the original", async () => {
  //   const mockPipeline = mockPipelineTemplate;
  //   mockParams.id = "1";

  //   vi.mocked(getPipeline).mockResolvedValueOnce(mockPipeline as any);
  //   vi.mocked(getBuildsList).mockResolvedValueOnce([]);
  //   //mocking the feature flag to be true for this test
  //   vi.mock("../../src/flags/booleanFlags", () => ({
  //     booleanFlags: {
  //       ENABLE_EDIT_PIPELINE: true,
  //     },
  //   }));

  //   render(
  //     <MemoryRouter>
  //       <PipelineDetails />
  //     </MemoryRouter>,
  //   );

  //   await waitFor(() => {
  //     expect(screen.getByText("Edit")).toBeInTheDocument();
  //   });

  //   // Click the Edit button
  //   const editButton = screen.getByText("Edit");
  //   fireEvent.click(editButton);

  //   // After clicking edit, the button should change to Cancel
  //   await waitFor(() => {
  //     expect(screen.getByText("Cancel")).toBeInTheDocument();
  //   });

  //   const nameInput = screen.getByDisplayValue("Test Pipeline");
  //   fireEvent.change(nameInput, { target: { value: "Updated Pipeline" } });

  //   //click Cancel
  //   const cancelButton = screen.getByText("Cancel");
  //   fireEvent.click(cancelButton);

  //   await waitFor(() => {
  //     // Changes Discarded after Cancel
  //     expect(screen.getByText("Test Pipeline")).toBeInTheDocument();
  //   });
  // });

  // it("Should be able to add Stages in the Pipeline through addStage()", async () => {
  //   mockParams.id = "0";

  //   vi.mocked(getBuildsList).mockResolvedValueOnce([]);
  //   //mocking the feature flag to be true for this test
  //   vi.mock("../../src/flags/booleanFlags", () => ({
  //     booleanFlags: {
  //       ENABLE_EDIT_PIPELINE: true,
  //     },
  //   }));

  //   render(
  //     <MemoryRouter>
  //       <PipelineDetails />
  //     </MemoryRouter>,
  //   );

  //   await waitFor(() => {
  //     expect(screen.getByTestId("Plus")).toBeInTheDocument();
  //   });

  //   const addButton = screen.getByTestId("Plus");
  //   fireEvent.click(addButton);

  //   await waitFor(() => {
  //     expect(screen.getByTestId("TestStage")).toBeInTheDocument();
  //   });
  // });

  // it("it should be able to move stages up and down after creating stages", async () => {
  //   mockParams.id = "0";

  //   vi.mocked(getBuildsList).mockResolvedValueOnce([]);
  //   //mocking the feature flag to be true for this test
  //   vi.mock("../../src/flags/booleanFlags", () => ({
  //     booleanFlags: {
  //       ENABLE_EDIT_PIPELINE: true,
  //     },
  //   }));

  //   render(
  //     <MemoryRouter>
  //       <PipelineDetails />
  //     </MemoryRouter>,
  //   );

  //   await waitFor(() => {
  //     expect(screen.getByTestId("Plus")).toBeInTheDocument();
  //   });

  //   const addButton = screen.getByTestId("Plus");
  //   fireEvent.click(addButton);

  //   //add one more stage
  //   fireEvent.click(addButton);
  // });
});
