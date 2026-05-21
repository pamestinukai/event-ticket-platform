import { AppBar, Box, Toolbar, Typography, Button } from "@mui/material";
import {useNavigate} from "react-router-dom";

export function Header() {
    const navigate = useNavigate();
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
                    <Button sx={{ color: "text.primary" }}>About</Button>
                    <Button variant="contained" onClick={() => navigate("/auth")}>Sign In</Button>
                </Box>

            </Toolbar>
        </AppBar>
    );
}