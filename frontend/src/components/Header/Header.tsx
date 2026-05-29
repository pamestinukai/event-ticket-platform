import { AppBar, Box, Toolbar, Typography, Button } from "@mui/material";
import {useNavigate} from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export function Header() {
    const navigate = useNavigate();
    const { token } = useAuth();
    return (
        <AppBar position="sticky" elevation={0} sx={{
            backgroundColor: "background.paper",
            borderBottom: "1px solid",
            borderColor: "divider",
        }}>
            <Toolbar sx={{ justifyContent: "space-between" }}>

                <Typography
                    variant="h6"
                    sx={{ fontWeight: 700, color: "text.primary", cursor: "pointer" }}
                    onClick={() => navigate("/")}
                >
                    Pamestinukai
                </Typography>

                <Box sx={{ display: "flex", gap: 2 }}>
                    <Button sx={{ color: "text.primary" }} onClick={() => navigate("/")}>Events</Button>
                    <Button sx={{ color: "text.primary" }} onClick={() => navigate("/about")}>About</Button>
                    <Button variant="contained" onClick={() => navigate(token ? "/dashboard" : "/auth")}>
                        {token ? "Dashboard" : "Sign In"}
                    </Button>
                </Box>

            </Toolbar>
        </AppBar>
    );
}