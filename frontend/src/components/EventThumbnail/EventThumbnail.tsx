import {Box, Paper, Typography} from "@mui/material";
import {useState} from "react";
import ImageNotSupportedIcon from '@mui/icons-material/ImageNotSupported';

interface EventThumbnailProps {
    src?: string;
    height?: number;
}

export function EventThumbnail({src, height = 600}: EventThumbnailProps) {
    const [hasError, setHasError] = useState(false);
    const borderRadius = 10;
    if (!src || hasError){
        return(
            <Paper
                elevation={0}
                sx={{
                    borderRadius: {borderRadius},
                    mt: 2,
                    height,
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    border: "1px solid",
                    borderColor: "divider",
                    gap: 1,
                    color: "text.secondary",
                    backgroundColor: "lightGray"
                }}
            >
                <ImageNotSupportedIcon sx={{ fontSize: 48 }} />
                <Typography variant="body2" color="text.secondary">No image available</Typography>
            </Paper>
        );
    }

    return(
        <Paper elevation={4} sx={{ borderRadius: {borderRadius}, overflow: "hidden", mt: 2 }}>
            <Box
                component="img"
                src={src}
                width="100%"
                height={height}
                onError={() => setHasError(true)}
                sx={{ objectFit: "cover", display: "block" }}
            />
        </Paper>
    )
}