import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export function ProtectedLayout() {
  const { token } = useAuth();
  if (!token) return <Navigate to="/auth" replace />;
  return <Outlet />;
}
