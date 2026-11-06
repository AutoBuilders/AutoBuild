import { useNavigate } from "react-router-dom";
import { ChevronRight } from "lucide-react";

<<<<<<< HEAD
=======
/**
 * Breadcrumb component for navigation.
 * @param crumbs - An array of breadcrumb objects, each containing a label and an optional route.
 * The last crumb in the array is considered the current page and does not have a route.
 * TODO: The current UI design is a little weird and shows breadcrumbs all over the place, but this is a temporary solution until we have a better design in the future.
 */

>>>>>>> 61d44b71 (Adding breadcrumbs to the UI)
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
