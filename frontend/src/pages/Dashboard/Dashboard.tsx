import { Box, Button, Container, Typography } from "@mui/material";
import { useAuth } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";

export function Dashboard() {
  const { email, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/");
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
      <Container maxWidth="sm" sx={{ textAlign: "center" }}>
        <Typography variant="h4" sx={{ fontWeight: 700 }} gutterBottom>
          Dashboard
        </Typography>
        <Typography variant="body1" sx={{ mb: 4 }}>
          Logged in as <strong>{email}</strong>
        </Typography>
        <Button variant="outlined" onClick={handleLogout}>
          Log out
        </Button>
      </Container>
    </Box>
  );
}
