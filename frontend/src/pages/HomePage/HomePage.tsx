import { useEffect, useState } from "react";
import { Box, Button, Container, Paper, Typography } from "@mui/material";
import { getCount, incrementCount } from "../../api";
import "./HomePage.css";

export function HomePage() {
  const [count, setCount] = useState<number | null>(null);

  useEffect(() => {
    getCount().then(setCount).catch(() => setCount(null));
  }, []);

  const handleIncrement = () => {
    incrementCount().then(setCount).catch(() => setCount(null));
  };

  return (
    <Box className="homePageRoot">
      <Container maxWidth="sm">
        <Paper elevation={0} className="homePageContainer">
          <Typography variant="h3" component="h1" fontWeight={700} gutterBottom>
            Pamestinukai
          </Typography>
          <Typography variant="h5" sx={{ mb: 3 }}>
            Count: {count ?? "..."}
          </Typography>
          <Button variant="contained" size="large" onClick={handleIncrement}>
            Increment
          </Button>
        </Paper>
      </Container>
    </Box>
  );
}
