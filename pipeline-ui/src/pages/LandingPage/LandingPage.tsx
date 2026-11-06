import { Button } from "../../components/ui/button";
import { Workflow, Settings } from "lucide-react";
import NavBar from "../../components/common/navBar";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { idp } from "../../config/authConfig";
import type { UserInfo } from "../../types/user.types";
import Breadcrumb from "../../components/common/breadcrumb";

function LandingPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<UserInfo>();

  useEffect(() => {
    async function fetchUser() {
      try{
        const data = await idp.getUserInfo();
        setUser(data);
      } catch (err) {
        // TODO: Handle error properly (e.g., show a notification to the user)
        console.error("Failed to fetch user:", err);
      }
    }
    fetchUser();
  }, []);

  return (
    <>
      <NavBar></NavBar>
      <div className="min-h-screen flex justify-center">
        <div className="w-full max-w-[80%] p-20">
          <Breadcrumb crumbs={[{ label: "" }]} />
          <h1 className="text-3xl font-semibold text-left">Hello {user?.username}!</h1>
          <p className="text-xl font-light text-gray-600 mb-10 text-left">What are we working on today?</p>
          
          <p className="text-xl text-gray-300 text-left">CI/CD</p>
          {/* Divider */}
          <div className="w-full h-px bg-[#E4E4E7] mb-6" />

        <div className="flex flex-row gap-6">
          {/* PIPELINES TILE */}
          <Button
            variant="tile"
            size="tile"
            className="hover:shadow-blue-200"
            onClick={() => navigate("/pipelines")}
          >
            <Workflow className="w-10 h-10 text-blue-500" />
            <span className="text-xl font-semibold">Pipelines</span>
          </Button>

            {/* BUILDS TILE */}
            <Button
              variant="tile"
              size="tile"
              className="hover:shadow-green-200"
              onClick={() => navigate("/builds")}
            >
              <Settings className="w-10 h-10 text-green-500 transition-transform duration-200 group-hover:scale-110" />
              <span className="text-xl font-semibold text-zinc-800">Builds</span>
            </Button>
          </div>
        </div>
      </div>
    </>
  );
}

export default LandingPage;
