import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { MemoryRouter } from "react-router-dom";

import LandingPage from "../../src/pages/LandingPage/LandingPage";

// Define the mock before using it in vi.mock to avoid hoist issues
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async (importActual) => {
  const actual: any = await importActual();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("LandingPage", () => {
  it("renders landing page content", () => {
    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>,
    );
    expect(screen.getByText("Welcome!")).toBeInTheDocument();
    expect(screen.getByText("Hello")).toBeInTheDocument();
  });

  it("navigates to Pipelines", async () => {
    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>,
    );

    const pipelineLinks = screen.getAllByText("Pipelines");
    const pipelineTile = pipelineLinks[pipelineLinks.length - 1];
    await pipelineTile.click();

    // Assert navigate was called with the expected path
    expect(mockNavigate).toHaveBeenCalledWith("/pipelines");
  });

  it("navigates to Builds", async () => {
    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>,
    );

    const buildsLinks = screen.getAllByText("Builds");
    const buildsTile = buildsLinks[buildsLinks.length - 1];
    await buildsTile.click();

    expect(mockNavigate).toHaveBeenCalledWith("/builds");
  });
});
