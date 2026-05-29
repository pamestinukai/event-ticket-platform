import { useEffect, useState, type ReactNode } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Container,
  Divider,
  IconButton,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import EditIcon from "@mui/icons-material/Edit";
import AutorenewIcon from "@mui/icons-material/Autorenew";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { getMyOrganization } from "../../api/organizations";
import { createEmployee, getEmployees } from "../../api/employees";
import type { EmployeeResponse } from "../../types/EmployeeResponse";
import { EmployeeEditDialog } from "../../components/EmployeeEditDialog/EmployeeEditDialog";
import { generatePassword } from "../../utils/password";

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

export function Employees() {
  const { token, email } = useAuth();
  const navigate = useNavigate();

  const [isOwner, setIsOwner] = useState<boolean | null>(null);
  const [employees, setEmployees] = useState<EmployeeResponse[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [newEmail, setNewEmail] = useState("");
  const [password, setPassword] = useState("");
  const [phone, setPhone] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const [editing, setEditing] = useState<EmployeeResponse | null>(null);

  useEffect(() => {
    if (!token) return;
    getMyOrganization(token)
      .then((org) => {
        const owner = org.ownerEmail === email;
        setIsOwner(owner);
        if (owner) {
          return getEmployees(token).then(setEmployees);
        }
      })
      .catch((err) => {
        setIsOwner(false);
        setLoadError(err instanceof Error ? err.message : "Failed to load");
      });
  }, [token, email]);

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault();
    if (!token) return;
    if (password.length < 8) {
      setFormError("Password must be at least 8 characters");
      return;
    }
    setFormError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      const created = await createEmployee(
        { email: newEmail, password, phone: phone || undefined },
        token,
      );
      setEmployees((prev) => (prev ? [...prev, created] : [created]));
      setSuccess(`Employee ${created.email} created`);
      setNewEmail("");
      setPassword("");
      setPhone("");
    } catch (err) {
      setFormError(err instanceof Error ? err.message : "Failed to create employee");
    } finally {
      setSubmitting(false);
    }
  }

  if (isOwner === null) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 10 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isOwner === false) {
    return (
      <Box sx={{ minHeight: "100vh", py: 6 }}>
        <Container maxWidth="sm">
          <Alert severity="warning" sx={{ mb: 2 }}>
            {loadError ?? "Only the organization owner can manage employees."}
          </Alert>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate("/dashboard")} sx={{ textTransform: "none" }}>
            Back to dashboard
          </Button>
        </Container>
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: "100vh", py: 6 }}>
      <Container maxWidth="md">
        <Stack direction="row" spacing={1} sx={{ alignItems: "center", mb: 3 }}>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate("/dashboard")} sx={{ textTransform: "none" }}>
            Back
          </Button>
        </Stack>

        <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>Employees</Typography>

        <Paper elevation={0} sx={{ p: 3, mb: 4, border: "1px solid", borderColor: "divider", borderRadius: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>Add employee</Typography>

          {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
            <Field label="Email">
              <TextField
                type="email"
                value={newEmail}
                onChange={(e) => setNewEmail(e.target.value)}
                required
                fullWidth
              />
            </Field>
            <Field label="Password">
              <TextField
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => { setPassword(e.target.value); setFormError(null); }}
                required
                fullWidth
                helperText="At least 8 characters"
                slotProps={{ input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <Tooltip title={showPassword ? "Hide" : "Show"}>
                        <IconButton onClick={() => setShowPassword((v) => !v)} edge="end">
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Generate password">
                        <IconButton
                          onClick={() => { setPassword(generatePassword()); setShowPassword(true); setFormError(null); }}
                          edge="end"
                        >
                          <AutorenewIcon />
                        </IconButton>
                      </Tooltip>
                    </InputAdornment>
                  ),
                } }}
              />
            </Field>
            <Field label="Phone Number">
              <TextField
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                fullWidth
                placeholder="Optional"
              />
            </Field>
            <Box>
              <Button
                type="submit"
                variant="contained"
                disableElevation
                startIcon={<PersonAddIcon />}
                disabled={submitting}
                sx={{ textTransform: "none", borderRadius: 1, py: 1.25, px: 3 }}
              >
                {submitting ? "Creating…" : "Create employee"}
              </Button>
            </Box>
          </Box>
        </Paper>

        <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>Team members</Typography>

        {loadError && <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert>}

        {employees === null && !loadError && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {employees !== null && employees.length === 0 && (
          <Paper elevation={0} sx={{ p: 4, textAlign: "center", border: "1px dashed", borderColor: "divider", borderRadius: 1 }}>
            <Typography variant="body1" color="text.secondary">No employees yet.</Typography>
          </Paper>
        )}

        {employees !== null && employees.length > 0 && (
          <Paper elevation={0} sx={{ border: "1px solid", borderColor: "divider", borderRadius: 1 }}>
            <Stack divider={<Divider />}>
              {employees.map((emp) => (
                <Stack
                  key={emp.id}
                  direction="row"
                  sx={{ justifyContent: "space-between", alignItems: "center", p: 2 }}
                >
                  <Box>
                    <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                      <Typography sx={{ fontWeight: 600 }}>{emp.email}</Typography>
                      {emp.owner && <Chip label="Owner" size="small" color="primary" />}
                      {!emp.active && <Chip label="Inactive" size="small" />}
                    </Stack>
                    {emp.phone && (
                      <Typography variant="body2" color="text.secondary">{emp.phone}</Typography>
                    )}
                  </Box>
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<EditIcon />}
                    onClick={() => setEditing(emp)}
                    sx={{ textTransform: "none" }}
                  >
                    Edit
                  </Button>
                </Stack>
              ))}
            </Stack>
          </Paper>
        )}
      </Container>

      {token && (
        <EmployeeEditDialog
          open={editing !== null}
          employee={editing}
          token={token}
          onClose={() => setEditing(null)}
          onSaved={(updated) =>
            setEmployees((prev) =>
              prev ? prev.map((e) => (e.id === updated.id ? updated : e)) : prev,
            )
          }
        />
      )}
    </Box>
  );
}
