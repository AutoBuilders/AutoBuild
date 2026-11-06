import { useEffect, useState } from "react";
import { SearchBar } from "../../components/common/searchBar";
import { pipelineBuildApiInstance } from "../../services/pipelines.api";
import { useNavigate } from "react-router-dom";
import NavBar from "../../components/common/navBar";
import type { PipelineBuild } from "../../gen";
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "../../components/ui/table";
import { CheckCircle, XCircle, Loader2 } from "lucide-react";
import Breadcrumb from "../../components/common/breadcrumb";

/**
 * This page includes the Builds List page.
 * It displays a list of builds and allows the user to search for builds by name.
 */


export default function BuildsListPage() {
  const [search, setSearch] = useState("");
  const navigate = useNavigate();

  const [data, setData] = useState<PipelineBuild[]>([]);

  const filtered = data.filter((p) =>
    (p.pipelineName ?? "").toLowerCase().includes(search.toLowerCase()),
  );

  

  useEffect(() => {
    async function fetchData() {
      const {status, data} = await pipelineBuildApiInstance.getAllBuilds();

      // TODO: handle error properly
      if (status !== 200) {
        console.error("Error getting builds");
      }

      setData(data);
    }
    fetchData();
  }, []);

  return (
    <>
      <NavBar></NavBar>
      <div className="min-h-screen flex justify-center">
        <div className="w-full max-w-[80%] p-20 text-center">
          <Breadcrumb crumbs={[{ label: "Home", route: "/" }, { label: "Builds" }]} />
          <h1 className="text-2xl font-bold text-left">MY BUILDS</h1>
          <h3 className="text-md font-light text-left">Find the finished/in-progress builds here</h3>

          <div className="flex justify-between items-center mt-10">
            <SearchBar page="builds" value={search} onChange={setSearch} />
          </div>

          {/* TABLE */}
          <div className="mt-6 shadow-lg rounded-xl overflow-hidden bg-white">
            <Table>
              <TableHeader>
                <TableRow className="bg-gray-200">
                  <TableHead className="w-[80%] font-semibold">Name</TableHead>
                  <TableHead className="w-[20%] font-semibold">Status</TableHead>
                </TableRow>
              </TableHeader>

              <TableBody>
                {filtered.map((p) => (
                  <TableRow
                    key={p.id}
                    onClick={() => {
                      navigate(`/builds/${p.id}`);
                    }}
                    className="hover:cursor-pointer"
                  >
                    <TableCell className="font-semibold text-left">{p.pipelineName}</TableCell>

                    <TableCell>
                      {p.currentState === "SUCCESS" && <CheckCircle className="text-green-500" />}
                      {p.currentState === "FAILED" && <XCircle className="text-red-500" />}
                      {p.currentState === "RUNNING" && (
                        <Loader2 className="animate-spin text-blue-500" />
                      )}
                    </TableCell>
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
