import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle, XCircle, Loader2 } from "lucide-react";
import NavBar from "../../components/common/navBar";
import Breadcrumb from "../../components/common/breadcrumb";
import { SearchBar } from "../../components/common/searchBar";
import CalendarFilter, { matchesDateFilter, type DateFilter } from "../../components/common/calendarFilter";
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "../../components/ui/table";
import { pipelineBuildApiInstance } from "../../services/pipelines.api";
import type { PipelineBuild } from "../../gen";
import { PipelineBuildCurrentStateEnum } from "../../gen";

/**
 * This page includes the Builds List page.
 * It displays a list of builds and allows the user to search for builds by name.
 */


export default function BuildsListPage() {
  const navigate = useNavigate();
  const [builds, setBuilds] = useState<PipelineBuild[]>([]);
  const [search, setSearch] = useState("");
  const [dateFilter, setDateFilter] = useState<DateFilter | null>(null);

  useEffect(() => {
    pipelineBuildApiInstance.getAllBuilds().then(({ status, data }) => {
      if (status !== 200) console.error("Error fetching builds");
      else setBuilds(data);
    });
  }, []);

  const filtered = builds.filter(
    (b) =>
      (b.pipelineName ?? "").toLowerCase().includes(search.toLowerCase()) &&
      matchesDateFilter(new Date(b.startedAt ?? ""), dateFilter),
  );

  return (
    <>
      <NavBar />
      <div className="min-h-screen flex justify-center">
        <div className="w-full max-w-[80%] p-20">
          <Breadcrumb crumbs={[{ label: "Home", route: "/" }, { label: "Builds" }]} />
          <h1 className="text-2xl font-bold">MY BUILDS</h1>
          <p className="text-sm font-light text-gray-500">Find the finished/in-progress builds here</p>

          <div className="mt-10 flex items-center gap-4">
            <SearchBar page="builds" value={search} onChange={setSearch} />
            <CalendarFilter onFilter={setDateFilter} activeFilter={dateFilter} />
          </div>

          <div className="mt-6 shadow-lg rounded-xl overflow-hidden bg-white">
            <Table>
              <TableHeader>
                <TableRow className="bg-gray-200">
                  <TableHead className="w-[50%] font-semibold">Name</TableHead>
                  <TableHead className="w-[30%] font-semibold">First Built At</TableHead>
                  <TableHead className="w-[20%] font-semibold">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filtered.map((b) => (
                  <TableRow key={b.id} className="cursor-pointer" onClick={() => navigate(`/builds/${b.id}`)}>
                    <TableCell className="font-semibold text-left">{b.pipelineName}</TableCell>
                    <TableCell className="text-left">{new Date(b.startedAt ?? "").toLocaleString()}</TableCell>
                    <TableCell>
                      {b.currentState === PipelineBuildCurrentStateEnum.Success && <CheckCircle className="text-green-500" />}
                      {b.currentState === PipelineBuildCurrentStateEnum.Failed && <XCircle className="text-red-500" />}
                      {b.currentState === PipelineBuildCurrentStateEnum.Running && (
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
