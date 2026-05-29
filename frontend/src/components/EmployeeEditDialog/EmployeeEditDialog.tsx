import { useEffect, useState, type ReactNode } from "react";
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  InputAdornment,
  Switch,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import AutorenewIcon from "@mui/icons-material/Autorenew";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";
import { getEmployees, updateEmployee } from "../../api/employees";
import type { EmployeeResponse } from "../../types/EmployeeResponse";
import { ConflictDialog } from "../ConflictDialog/ConflictDialog";
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

interface Props {
  open: boolean;
  employee: EmployeeResponse | null;
  token: string;
  onClose: () => void;
  onSaved: (updated: EmployeeResponse) => void;
}

export function EmployeeEditDialog({ open, employee, token, onClose, onSaved }: Props) {
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [active, setActive] = useState(true);
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [version, setVersion] = useState<number | undefined>(undefined);
  const [conflict, setConflict] = useState(false);

  useEffect(() => {
    if (employee) {
      setEmail(employee.email);
      setPhone(employee.phone ?? "");
      setActive(employee.active);
      setVersion(employee.version);
      setPassword("");
      setShowPassword(false);
      setError(null);
      setConflict(false);
    }
  }, [employee]);

  async function doSave(versionToUse: number | undefined) {
    if (!employee) return;
    if (password && password.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }
    setError(null);
    setSaving(true);
    try {
      const updated = await updateEmployee(
        employee.id,
        { version: versionToUse, email, phone: phone || undefined, active, password: password || undefined },
        token,
      );
      onSaved(updated);
      onClose();
    } catch (err) {
      if (err instanceof Error && err.message === "CONFLICT") {
        setConflict(true);
      } else {
        setError(err instanceof Error ? err.message : "Failed to update employee");
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleOverwrite() {
    if (!employee) return;
    setConflict(false);
    try {
      const latest = (await getEmployees(token)).find((e) => e.id === employee.id);
      setVersion(latest?.version);
      await doSave(latest?.version);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update employee");
    }
  }

  return (
    <>
    <Dialog open={open} onClose={saving ? undefined : onClose} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ fontWeight: 700 }}>Edit employee</DialogTitle>
      <DialogContent>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 1 }}>
          <Field label="Email">
            <TextField type="email" value={email} onChange={(e) => setEmail(e.target.value)} required fullWidth />
          </Field>
          <Field label="Phone Number">
            <TextField value={phone} onChange={(e) => setPhone(e.target.value)} fullWidth placeholder="Optional" />
          </Field>
          <Field label="New Password">
            <TextField
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => { setPassword(e.target.value); setError(null); }}
              fullWidth
              placeholder="Leave blank to keep current"
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
                        onClick={() => { setPassword(generatePassword()); setShowPassword(true); setError(null); }}
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
          <FormControlLabel
            control={<Switch checked={active} onChange={(e) => setActive(e.target.checked)} disabled={employee?.owner} />}
            label="Active"
          />
          {employee?.owner && (
            <Typography variant="caption" color="text.secondary">
              The organization owner cannot be deactivated.
            </Typography>
          )}
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={saving} sx={{ textTransform: "none" }}>Cancel</Button>
        <Button
          variant="contained"
          disableElevation
          onClick={() => doSave(version)}
          disabled={saving}
          sx={{ textTransform: "none", borderRadius: 1 }}
        >
          {saving ? "Saving…" : "Save changes"}
        </Button>
      </DialogActions>
    </Dialog>
    <ConflictDialog open={conflict} onClose={() => setConflict(false)} onRetry={handleOverwrite} />
    </>
  );
}
