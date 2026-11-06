import { createBrowserRouter } from "react-router-dom";
import LandingPage from "../pages/LandingPage/LandingPage";
import PipelinePage from "../pages/PipelinePage/PipelinePage";
import PipelineDetailsPage from "../pages/PipelineDetailsPage/PipelineDetailsPage";
import BuildsRunningPage from "../pages/BuildsPage/BuildsRunningPage";
import BuildsListPage from "../pages/BuildsPage/BuildsListPage";
import RegisterPage from "../pages/RegistrationPage";
import ProtectedRoute from "../pages/ProtectedRoute";

export const router = createBrowserRouter([
  //protected routes
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <LandingPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/pipelines",
    element: (
      <ProtectedRoute>
        <PipelinePage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/pipelines/:id",
    element: (
      <ProtectedRoute>
        <PipelineDetailsPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/builds/:id",
    element: (
      <ProtectedRoute>
        <BuildsRunningPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/builds",
    element: (
      <ProtectedRoute>
        <BuildsListPage />
      </ProtectedRoute>
    ),
  },
  // Auth Routes
  // { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
]);
