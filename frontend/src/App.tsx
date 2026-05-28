import { Box } from "@mui/material";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ProtectedLayout } from "./components/ProtectedRoute/ProtectedRoute";
import { Footer } from "./components/Footer/Footer";
import { HomePage } from "./pages/HomePage/HomePage";
import { Auth } from "./pages/Auth/Auth";
import { Dashboard } from "./pages/Dashboard/Dashboard";
import { EventCreate } from "./pages/EventCreate/EventCreate";
import { EventEdit } from "./pages/EventEdit/EventEdit";
import { EventAnalyticsPage } from "./pages/EventAnalytics/EventAnalytics";
import { EventPage } from "./pages/Event/Event.tsx"

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
          <Routes>
            <Route path="/" element={<><HomePage /><Footer /></>} />
            <Route path="/auth" element={<Auth />} />
            <Route path="/event/:id" element={<EventPage />} />
            <Route element={<ProtectedLayout />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/events/new" element={<EventCreate />} />
              <Route path="/events/:id/edit" element={<EventEdit />} />
              <Route path="/events/:id/analytics" element={<EventAnalyticsPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Box>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
