import { useNavigate } from "react-router-dom";
import { cn } from "../../lib/utils";

interface TileProps {
  title: string;
  route: string;
  children?: React.ReactNode;
  className?: string;
}

export default function Tile({ title, route, children, className }: TileProps) {
    const navigate = useNavigate();

    return (
        <div
            role="button"
            onClick={() => navigate(route)}
            className={cn(
                // Base button styling
                "group inline-flex items-center justify-center gap-2",
                "whitespace-nowrap rounded-md text-sm font-medium",

                // Tile variant
                "rounded-2xl bg-white border border-zinc-200 shadow-sm",
                "transition-all duration-200 ease-out",
                "hover:scale-[1.03] hover:shadow-xl",
                "active:scale-[0.97] active:shadow-sm",

                // Tile size
                "w-64 h-40 flex flex-col items-center justify-center gap-3",

                // SVG overrides
                "[&_svg]:size-auto",

                // Interaction
                "cursor-pointer focus-visible:outline-none",
                "focus-visible:ring-2 focus-visible:ring-blue-500",

                className
            )}
        >
            {children}

            <span className="text-xl font-semibold">
                {title}
            </span>
        </div>
    );
}