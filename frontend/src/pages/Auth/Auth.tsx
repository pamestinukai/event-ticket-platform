import { useState, type ReactNode } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { Alert, Box, Button, Container, Paper, TextField, Typography } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { login, register } from "../../api/auth";
import { useAuth } from "../../context/AuthContext";

type Mode = "login" | "register";

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <Box>
      <Typography variant="body2" sx={{ fontWeight: 700, mb: 0.75, color: "text.primary" }}>
        {label}
      </Typography>
      {children}
    </Box>
  );
}

export function Auth() {
  const { token, setAuth } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<Mode>("login");
  const [orgName, setOrgName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  if (token) return <Navigate to="/dashboard" replace />;

  function switchMode(next: Mode) {
    setMode(next);
    setErrors({});
  }

  function clearFieldError(field: string) {
    setErrors((prev) => { const next = { ...prev }; delete next[field]; return next; });
  }

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault();
    if (mode === "register" && password.length < 8) {
      setErrors({ password: "Password must be at least 8 characters" });
      return;
    }
    setErrors({});
    setLoading(true);
    try {
      const data = mode === "login"
        ? await login(email, password)
        : await register(email, password, orgName || undefined, phone || undefined);
      setAuth(data.token, data.email);
      navigate("/dashboard");
    } catch (err) {
      setErrors({ form: err instanceof Error ? err.message : "Something went wrong" });
    } finally {
      setLoading(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", bgcolor: "background.default" }}>
      <Container maxWidth="xs">
        <Button
          onClick={() => navigate("/")}
          startIcon={<ArrowBackIcon />}
          sx={{ mb: 2, textTransform: "none", fontWeight: 600, color: "text.secondary" }}
        >
          Back to home
        </Button>
        <Paper elevation={0} sx={{ borderRadius: 1, p: "40px 44px" }}>
          <Typography variant="h4" sx={{ fontWeight: 800, textAlign: "center", color: "text.primary" }}>
            Pamestinukai
          </Typography>
          <Typography variant="body2" sx={{ textAlign: "center", color: "primary.light", mt: 0.5, mb: 4 }}>
            Modern ticket sales platform
          </Typography>

          <Box sx={{ display: "flex", gap: 1, mb: 3 }}>
            {(["login", "register"] as Mode[]).map((m) => (
              <Button
                key={m}
                fullWidth
                disableElevation
                onClick={() => switchMode(m)}
                sx={{
                  borderRadius: 1,
                  py: 1,
                  fontWeight: 600,
                  textTransform: "none",
                  fontSize: "0.95rem",
                  bgcolor: mode === m ? "primary.main" : "#f0f2f5",
                  color: mode === m ? "primary.contrastText" : "text.secondary",
                  "&:hover": {
                    bgcolor: mode === m ? "primary.dark" : "#e5e7eb",
                  },
                }}
              >
                {m === "login" ? "Login" : "Sign Up"}
              </Button>
            ))}
          </Box>

          {errors.form && <Alert severity="error" sx={{ mb: 2 }}>{errors.form}</Alert>}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
            <Field label="Email">
              <TextField type="email" value={email} onChange={(e) => setEmail(e.target.value)} required fullWidth />
            </Field>

            {mode === "register" && (
              <>
                <Field label="Organization Name">
                  <TextField value={orgName} onChange={(e) => setOrgName(e.target.value)} fullWidth placeholder="Optional" />
                </Field>
                <Field label="Phone Number">
                  <TextField value={phone} onChange={(e) => setPhone(e.target.value)} fullWidth placeholder="Optional" />
                </Field>
              </>
            )}

            <Field label="Password">
              <TextField
                type="password"
                value={password}
                onChange={(e) => { setPassword(e.target.value); clearFieldError("password"); }}
                required
                fullWidth
                error={!!errors.password}
                helperText={errors.password ?? (mode === "register" ? "At least 8 characters" : undefined)}
              />
            </Field>

            <Button
              type="submit"
              variant="contained"
              fullWidth
              disabled={loading}
              disableElevation
              sx={{ mt: 1, py: 1.5, borderRadius: 1, fontWeight: 700, fontSize: "1rem", textTransform: "none" }}
            >
              {loading
                ? mode === "login" ? "Signing in…" : "Creating account…"
                : mode === "login" ? "Login" : "Create Account"}
            </Button>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
