import { useState } from "react";
import { login as loginApi } from "../services/auth.api";
import { useAuth } from "../auth/authContext";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    /* eslint-disable @typescript-eslint/no-explicit-any */
    try {
      const { token } = await loginApi(username, password);
      void token;
      login();
      navigate("/");
    } catch (err: any) {
      console.error(err);
      const message =
        err?.response?.data?.detail ||
        err?.data?.message ||
        "Invalid credentials";

      setError(message);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-blue-100 p-4">
      <form
        onSubmit={handleSubmit}
        className="bg-white/70 backdrop-blur-lg shadow-xl p-10 rounded-2xl w-full max-w-md space-y-6 border border-white/30"
      >
        <h1 className="text-3xl font-bold text-center text-gray-800">
          Welcome Back
        </h1>
        <p className="text-center text-gray-500 mb-4">Log in to continue</p>

        {error && (
          <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded-xl">
            {error}
          </div>
        )}
        <div className="space-y-1">
          <label className="text-gray-700 font-medium">Username</label>
          <input
            type="text"
            className="border px-4 py-3 w-full rounded-xl focus:ring-2 focus:ring-blue-400 focus:outline-none transition"
            placeholder="Enter your username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>

        <div className="space-y-1 relative">
          <label className="text-gray-700 font-medium">Password</label>
          <input
            type={showPassword ? "text" : "password"}
            className="border px-4 py-3 w-full rounded-xl focus:ring-2 focus:ring-blue-400 focus:outline-none transition pr-12"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button
            data-testid="togglePassword"
            type="button"
            className="absolute right-4 top-10 text-gray-500 hover:text-gray-700 transition"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
          </button>
        </div>

        <button className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl shadow-md transition active:scale-[0.98]">
          Login
        </button>

        <p className="text-center text-gray-600 text-sm mt-4">
          Don’t have an account?{" "}
          <Link
            to="/register"
            className="text-blue-600 font-medium hover:underline"
          >
            Register
          </Link>
        </p>
      </form>
    </div>
  );
}