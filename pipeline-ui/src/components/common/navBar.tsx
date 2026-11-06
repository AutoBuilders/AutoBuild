import { useEffect, useRef, useState } from "react";
import { ChevronDown, LogOut, Settings, User } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { idp } from "../../config/authConfig";
import type { UserInfo } from "../../types/user.types";

export default function NavBar() {
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const [user, setUser] = useState<UserInfo>();
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const t = setTimeout(() => setLoading(false), 600);
    async function fetchUser() {
      try{
        const data = await idp.getUserInfo();
        setUser(data);
      } catch (err) {
        console.error("Failed to fetch user:", err);
      }
    }
    fetchUser();
    return () => clearTimeout(t);
  }, []);

  // Close popover when clicking outside
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  if (loading) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-[#0F0F11] text-white z-50">
        <Settings className="animate-spin" size={48} />
        <p className="ml-4 text-xl tracking-wide">Loading...</p>
      </div>
    );
  }

  return (
    <nav
      className="
    w-full sticky top-0 z-40
    h-12
    bg-[#444]
    text-white
    shadow-lg
    px-8
    grid grid-cols-3 items-center
  "
    >
      {/* LEFT — Logo */}
      <div
        className="flex items-center gap-3 group cursor-pointer select-none"
        onClick={() => navigate("/")}
      >
        <Settings
          size={28}
          className="transition-transform duration-300 group-hover:rotate-180"
        />
        <span className="text-xl font-semibold tracking-wide">AutoBuild</span>
      </div>

      {/* CENTER — Navigation */}
      <div className="flex items-center justify-center gap-8 text-lg">
        <NavButton label="Home" route="/" />
        <NavButton label="Pipelines" route="/pipelines" />
        <NavButton label="Builds" route="/builds" />
      </div>

      {/* RIGHT — Profile Menu */}
      <div className="flex justify-end">
        <div className="relative" ref={menuRef}>
          <button
            onClick={() => setOpen(!open)}
            className="flex items-center gap-2 bg-white/10 px-3 py-2 rounded-full hover:bg-white/20 transition"
          >
            <User size={18} />
            <span className="text-sm font-medium">
              {user?.username || "User"}
            </span>
            <ChevronDown
              size={16}
              className={`transition-transform ${
                open ? "rotate-180" : "rotate-0"
              }`}
            />
          </button>

          {open && (
            <div
              className="
            absolute right-0 mt-2 w-56 
            bg-[#333] text-white rounded-xl shadow-xl 
            border border-white/10 p-4 animate-fadeIn
          "
            >
              <p className="font-semibold text-lg">{user?.username}</p>
              <p className="text-sm text-gray-300 mb-3">{user?.email}</p>

              <hr className="border-white/10 my-3" />

              <button
                onClick={async () => {
                  await idp.logout();
                }}
                className="
              w-full flex items-center gap-2 px-3 py-2 
              hover:bg-white/10 rounded-lg transition
            "
              >
                <LogOut size={18} />
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}

function NavButton({ label, route }: { label: string; route: string }) {
  const navigate = useNavigate();

  return (
    <button
      onClick={() => navigate(route)}
      className="
        relative px-1 py-2 rounded-lg
        hover:bg-white/10 
        transition-all tracking-wide
      "
    >
      {label}
      <span
        className="
          absolute left-1/2 bottom-1 -translate-x-1/2 
          w-0 h-0.5 bg-white transition-all duration-300 
          hover:w-full
        "
      ></span>
    </button>
  );
}
