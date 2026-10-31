import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import NavBar from "../../../src/components/common/navBar";
import type { UserInfo } from "../../../src/types/user.types";
import { idp } from "../../../src/config/authConfig";

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock auth API
vi.mock("../../../src/config/authConfig");

describe("NavBar", () => {
  const mockUser: UserInfo = {
    username: "testuser",
    email: "test@example.com",
  };

  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  // Helper function to render NavBar
  const renderNavBar = () => {
    return render(
      <MemoryRouter>
        <NavBar />
      </MemoryRouter>,
    );
  };

  describe("Logo and Branding", () => {
    beforeEach(() => {
      sessionStorage.setItem("token", "test-token");
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);
      renderNavBar();
    });

    it("renders the AutoBuild logo", async () => {
      await waitFor(() => {
        expect(screen.getByText("AutoBuild")).toBeInTheDocument();
      });
    });

    it("navigates to home when logo is clicked", async () => {
      await waitFor(() => {
        expect(screen.getByText("AutoBuild")).toBeInTheDocument();
      });

      const logo = screen.getByText("AutoBuild");
      fireEvent.click(logo);

      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  describe("Navigation Links", () => {
    beforeEach(() => {
      sessionStorage.setItem("token", "test-token");
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);
      renderNavBar();
    });

    it("renders all navigation links", async () => {
      await waitFor(() => {
        expect(screen.getByText("Home")).toBeInTheDocument();
      });

      expect(screen.getByText("Pipelines")).toBeInTheDocument();
      expect(screen.getByText("Builds")).toBeInTheDocument();
      expect(screen.getByText("About")).toBeInTheDocument();
    });

    it("navigates to home when Home is clicked", async () => {
      await waitFor(() => {
        expect(screen.getByText("Home")).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("Home"));
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });

    it("navigates to pipelines when Pipelines is clicked", async () => {
      await waitFor(() => {
        expect(screen.getByText("Pipelines")).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("Pipelines"));
      expect(mockNavigate).toHaveBeenCalledWith("/pipelines");
    });

    it("navigates to builds when Builds is clicked", async () => {
      await waitFor(() => {
        expect(screen.getByText("Builds")).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("Builds"));
      expect(mockNavigate).toHaveBeenCalledWith("/builds");
    });

    it("navigates to about when About is clicked", async () => {
      await waitFor(() => {
        expect(screen.getByText("About")).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("About"));
      expect(mockNavigate).toHaveBeenCalledWith("/about");
    });

    // it("applies hover styles to navigation buttons", async () => {
    //   await waitFor(() => {
    //     expect(screen.getByText("Home")).toBeInTheDocument();
    //   });

    //   const homeButton = screen.getByText("Home");
    //   expect(homeButton).toHaveClass("hover:bg-white/10");
    // });
  });

  describe("User Profile - Fetching User Data", () => {
    it("fetches user data when token exists", async () => {
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);

      renderNavBar();

      await waitFor(() => {
        expect(idp.getUserInfo).toHaveBeenCalled();
      });
    });

    it("displays fetched username in profile button", async () => {
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);

      renderNavBar();

      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });
    });

    it("handles API error gracefully", async () => {
      const consoleErrorSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});
      vi.mocked(idp.getUserInfo).mockRejectedValue(new Error("API Error"));

      renderNavBar();

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          "Failed to fetch user:",
          expect.any(Error),
        );
      });

      consoleErrorSpy.mockRestore();
    });
  });

  describe("Profile Dropdown Menu", () => {
    beforeEach(() => {
      sessionStorage.setItem("token", "test-token");
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);
      renderNavBar();
    });

    it("opens dropdown when profile button is clicked", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");
      fireEvent.click(profileButton);

      await waitFor(() => {
        expect(screen.getByText("test@example.com")).toBeInTheDocument();
      });
    });

    it("closes dropdown when profile button is clicked again", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");

      // Open dropdown
      fireEvent.click(profileButton);
      await waitFor(() => {
        expect(screen.getByText("test@example.com")).toBeInTheDocument();
      });

      // Close dropdown
      fireEvent.click(profileButton);
      await waitFor(() => {
        expect(screen.queryByText("test@example.com")).not.toBeInTheDocument();
      });
    });

    it("displays user email in dropdown", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");
      fireEvent.click(profileButton);

      await waitFor(() => {
        expect(screen.getByText("test@example.com")).toBeInTheDocument();
      });
    });

    it("displays logout button in dropdown", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");
      fireEvent.click(profileButton);

      await waitFor(() => {
        expect(screen.getByText("Logout")).toBeInTheDocument();
      });
    });
  });

  describe("Dropdown Click Outside", () => {
    beforeEach(() => {
      sessionStorage.setItem("token", "test-token");
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);
      renderNavBar();
    });

    it("closes dropdown when clicking outside", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");

      // Open dropdown
      fireEvent.click(profileButton);
      await waitFor(() => {
        expect(screen.getByText("test@example.com")).toBeInTheDocument();
      });

      // Click outside
      fireEvent.mouseDown(document.body);

      await waitFor(() => {
        expect(screen.queryByText("test@example.com")).not.toBeInTheDocument();
      });
    });

    it("does not close dropdown when clicking inside menu", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");

      // Open dropdown
      fireEvent.click(profileButton);
      await waitFor(() => {
        expect(screen.getByText("test@example.com")).toBeInTheDocument();
      });

      // Click inside dropdown
      const email = screen.getByText("test@example.com");
      fireEvent.mouseDown(email);

      await waitFor(() => {
        expect(screen.getByText("test@example.com")).toBeInTheDocument();
      });
    });
  });

  describe("Logout Functionality", () => {
    beforeEach(() => {
      vi.mocked(idp.getUserInfo).mockResolvedValue(mockUser);
      renderNavBar();
    });

    it("displays logout icon", async () => {
      await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
      });

      const profileButton = screen.getByText("testuser");
      fireEvent.click(profileButton);

      await waitFor(() => {
        const logoutButton = screen.getByText("Logout").closest("button");
        const icon = logoutButton?.querySelector("svg");
        expect(icon).toBeInTheDocument();
      });
    });
  });
});
