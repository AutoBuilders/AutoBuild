import { useEffect, useRef, useState } from "react";
import { ChevronDown, LogOut, Settings, User } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { idp } from "../../config/authConfig";
import type { UserInfo } from "../../types/user.types";

/*
 * This component is a navigation bar which is displayed constantly at the top of t=every page
 * This would include two main options for navigations: Projects and About Us for now (additional options can be added later)
 */


export default function NavBar() {
  const navigate = useNavigate();
  const [user, setUser] = useState<UserInfo>();
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

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

  // if (loading) {
  //   return (
  //     <div className="fixed inset-0 flex items-center justify-center bg-[#0F0F11] text-white z-50">
  //       <Settings className="animate-spin" size={48} />
  //       <p className="ml-4 text-xl tracking-wide">Loading...</p>
  //     </div>
  //   );
  // }

  return (
    <nav
      className="
    w-full sticky top-0 z-40
    h-12
    bg-[#fff]
    text-gray
    border-b border-[#E4E4E7]
    px-8
    grid grid-cols-3 items-center
    shadow-sm
  "
    >
      {/* LEFT — Logo */}
      <div
        className="flex items-center gap-3 group cursor-pointer select-none"
        onClick={() => navigate("/")}
      >
        <Settings
          size={14}
          className="transition-transform duration-300 group-hover:rotate-180"
        />
        <span className="text-m font-semibold tracking-wide">AutoBuild</span>
      </div>

      {/* CENTER — Navigation */}
      <div className="flex items-center justify-center gap-8 text-lg">
        <NavButton label="Home" route="/" />
        <NavButton label="Pipelines" route="/pipelines" />
        <NavButton label="Builds" route="/builds" />
        <NavButton label="Projects" route="/" />
        <NavButton label="About" route="/about" />
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
      className="group relative px-1 py-2 tracking-wide"
    >
      {label}
      <span className="absolute left-0 bottom-0 w-0 h-0.5 bg-black transition-all group-hover:w-full"></span>
    </button>
  );
}
