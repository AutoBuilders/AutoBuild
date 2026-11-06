import { useNavigate } from "react-router-dom";
import { ChevronRight } from "lucide-react";

interface Crumb {
  label: string;
  route?: string; // omit for the current (last) crumb
}

export default function Breadcrumb({ crumbs }: { crumbs: Crumb[] }) {
  const navigate = useNavigate();

  return (
    <nav className="flex items-center gap-1 text-sm text-zinc-400 mb-5">
      {crumbs.map((crumb, idx) => {
        const isLast = idx === crumbs.length - 1;
        return (
          <span key={idx} className="flex items-center gap-1">
            {idx > 0 && <ChevronRight size={13} className="text-zinc-300" />}
            {isLast || !crumb.route ? (
              <span className="text-zinc-500 font-medium">{crumb.label}</span>
            ) : (
              <button
                onClick={() => navigate(crumb.route!)}
                className="hover:text-zinc-900 transition-colors"
              >
                {crumb.label}
              </button>
            )}
          </span>
        );
      })}
    </nav>
  );
}
