import {Box, Stack, Typography} from "@mui/material";
import type {ReactNode} from "react";

interface EventDetailProps {
    icon: ReactNode;
    text: string;
    href?: string;
}

export function EventDetail({ icon, text, href }: EventDetailProps) {
    return (
        <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
            <Box sx={{ fontSize: 18, color: "text.secondary", display: "flex" }}>
                {icon}
            </Box>
            {href ? (
                <Typography variant="body2" component="a" href={href} sx={{ color: "primary.main", textDecoration: "none" }}>
                    {text}
                </Typography>
            ) : (
                <Typography variant="body2" color="textSecondary" sx={{fontSize: 17}}>{text}</Typography>
            )}
        </Stack>
    );
}