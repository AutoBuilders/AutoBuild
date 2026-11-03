import { Button } from "../../components/ui/button";
import { Workflow, Settings } from "lucide-react";
import NavBar from "../../components/common/navBar";
import { useNavigate } from "react-router-dom";

function LandingPage() {
  const navigate = useNavigate();
  return (
    <>
      <NavBar></NavBar>
      <div className="flex flex-col p-10 max-w-screen min-h-screen bg-gradient-to-b from-gray-50 to-white">
        <h1 className="text-3xl font-semibold mb-2">Hello</h1>
        <p className="text-xl text-gray-600 mb-10">Welcome!</p>

        <div className="flex flex-col gap-6">
          {/* PIPELINES TILE */}
          <Button
            variant="tile"
            size="lg"
            className="w-64 h-40 flex flex-col items-center justify-center gap-3"
            onClick={() => navigate("/pipelines")}
          >
            <Workflow className="w-10 h-10 text-blue-500" />
            <span className="text-xl font-semibold">Pipelines</span>
          </Button>

          {/* BUILDS TILE */}
          <Button
            variant="tile"
            size="lg"
            className="w-64 h-40 flex flex-col items-center justify-center gap-3"
            onClick={() => navigate("/builds")}
          >
            <Settings className="w-10 h-10 text-green-500" />
            <span className="text-xl font-semibold">Builds</span>
          </Button>
        </div>
      </div>
    </>
  );
}

export default LandingPage;
