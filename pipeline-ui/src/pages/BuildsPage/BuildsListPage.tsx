import { useEffect, useState } from "react";
import { SearchBar } from "../../components/common/searchBar";
import { pipelineBuildApiInstance } from "../../services/pipelines.api";
import BuildsTable from "../../components/builds/buildsTable";
import { useParams } from "react-router-dom";
import NavBar from "../../components/common/navBar";
import type { PipelineBuild } from "../../gen";

export default function BuildsListPage() {
  const { id } = useParams();
  const [search, setSearch] = useState("");

  const [data, setData] = useState<PipelineBuild[]>([]);

  const filtered = data.filter((p) =>
    (p.pipelineName ?? "").toLowerCase().includes(search.toLowerCase()),
  );

  useEffect(() => {
    async function fetchData() {
      const {status, data} = await pipelineBuildApiInstance.getAllBuilds();

      if (status !== 200) {
        console.error("Error getting builds");
      }

      setData(data);
    }
    fetchData();
  }, [id]);

  return (
    <>
      <NavBar></NavBar>
      <div className="main-container min-h-screen justify-center display-flex">
        <div className="container p-20 text-center">
          <h1 className="text-2xl font-bold text-center">MY BUILDS</h1>

          <div className="flex justify-between items-center mt-10">
            <SearchBar page="builds" value={search} onChange={setSearch} />
          </div>

          <BuildsTable data={filtered} />
        </div>
      </div>
    </>
  );
}
