import type {ReactNode} from "react";
import {Box, Paper, Typography} from "@mui/material";

interface InfoCardProps {
    title: string;
    children: ReactNode;
}

export function InfoCard({ title, children }: InfoCardProps){
    return(
        <Paper elevation={0} sx={{ border: "1px solid", borderColor: "divider", borderRadius: "12px", padding: 3 }}>
            <Typography variant="h5" color="text.secondary" sx={{fontWeight: 500}}>{title} </Typography>
            <Box sx={{ mt: 1.5 }}>{children}</Box>
        </Paper>
    );
}