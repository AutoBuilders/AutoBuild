import { useEffect, useState } from "react";
import { SearchBar } from "../../components/common/searchBar";
import { Button } from "../../components/ui/button";
import { pipelineApiInstance } from "../../services/pipelines.api";
import type {Pipeline} from '../../gen/api';
import "./PipelinePage.css";
import NavBar from "../../components/common/navBar";
import { useNavigate } from "react-router-dom";
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "../../components/ui/table";

/**
 * This page includes the Pipelines List page.
 * It displays a list of pipelines and allows the user to search for pipelines by name.
 */

export default function PipelinePage() {
  const [search, setSearch] = useState("");

  const [data, setData] = useState<Pipeline[]>([]);

  const filtered = data.filter((p) =>
    p.name.toLowerCase().includes(search.toLowerCase()),
  );
  
  const navigate = useNavigate();

  // TODO: check call is happening twice
  useEffect(() => {
    async function fetchData() {
      const {status, data} = await pipelineApiInstance.getAllPipelines();

      if (status != 200) {
        console.log("obtained status: ", 200); //TODO: error popup
        return;
      }

      const pipelines = data;
      setData(pipelines);
    }
    fetchData();
  }, []);

  function onCreate(): void {
    navigate("/pipelines/0");
  }

  return (
    <>
      <NavBar></NavBar>
      <div className="main-container min-h-screen justify-center display-flex">
        <div className="container p-20 text-center">
          <h1 className="text-3xl font-semibold text-left">My Pipelines</h1>
          <h3 className="text-l font-light text-left">Manage your CI/CD Pipelines</h3>

          <div className="flex justify-between items-center mt-10">
            <SearchBar page = "pipelines" value={search} onChange={setSearch} />

            <Button
              variant="outline"
              className="rounded-half px-8 py-2 text-sm font-medium text-white-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              onClick={onCreate}
            >
              Create
            </Button>
          </div>

          {/* Using the common table template and passing the values */}
          <div className="mt-6 shadow-lg rounded-xl overflow-hidden bg-white">
            <Table>
              <TableHeader>
                <TableRow className="bg-gray-200">
                  <TableHead className="w-[80%] font-semibold">Name</TableHead>
                </TableRow>
              </TableHeader>

              <TableBody>
                {filtered.map((p) => (
                  <TableRow
                    onClick={() => {
                      window.location.href = `/pipelines/${p.id}`;
                    }}
                    className="hover:cursor-pointer"
                  >
                    <TableCell className="text-left">{p.name}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>
      </div>
    </>
  );
}
