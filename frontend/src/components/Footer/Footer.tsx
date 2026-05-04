import { Box, Typography } from "@mui/material";
import "./Footer.css";

export function Footer() {
  return (
    <Box component="footer" className="footerRoot">
      <Typography variant="body2">Pamestinukai </Typography>
      <Typography variant="caption">© {new Date().getFullYear()} Pagausim jei pavogsi</Typography>
    </Box>
  );
}
