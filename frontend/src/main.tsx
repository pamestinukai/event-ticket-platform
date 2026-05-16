import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import "./index.css";
import App from "./App.tsx";

const theme = createTheme({
  palette: {
    primary: {
      main: "#2563eb",
      dark: "#1d4ed8",
      light: "#6b8ec7",
    },
    background: {
      default: "#dfe4f0",
      paper: "#ffffff",
    },
    divider: "#e5e7eb",
  },
  shape: {
    borderRadius: 16,
  },
  components: {
    MuiOutlinedInput: {
      styleOverrides: {
        notchedOutline: {
          borderColor: "#e5e7eb",
        },
        input: {
          paddingTop: "10px",
          paddingBottom: "10px",
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          boxShadow: "0 10px 30px rgba(25, 118, 210, 0.12)",
        },
      },
    },
  },
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <App />
    </ThemeProvider>
  </StrictMode>,
);
