import { Box, Button, Container, Paper, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import "./HomePage.css";

export function HomePage() {
  const navigate = useNavigate();

  return (
    <Box className="homePageRoot">
      <Container maxWidth="sm">
        <Paper elevation={0} className="homePageContainer">
          <Typography variant="h3" component="h1" sx={{ fontWeight: 700 }} gutterBottom>
            Pamestinukai
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
            Event ticket platform
          </Typography>
          <Button variant="contained" size="large" onClick={() => navigate("/auth")}>
            Authenticate
          </Button>
        </Paper>
      </Container>
    </Box>
  );
}
