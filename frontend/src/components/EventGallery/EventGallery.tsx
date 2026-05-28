import { Box, Dialog, IconButton, Paper, Stack, Typography } from "@mui/material";
import { useState } from "react";
import ImageNotSupportedIcon from "@mui/icons-material/ImageNotSupported";
import CloseIcon from "@mui/icons-material/Close";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";

interface EventGalleryProps {
    images: string[];
    height?: number;
}

export function EventGallery({ images, height = 600 }: EventGalleryProps) {
    const [selected, setSelected] = useState(0);
    const [brokenSrcs, setBrokenSrcs] = useState<Set<string>>(new Set());
    const [lightboxOpen, setLightboxOpen] = useState(false);
    const borderRadius = 10;

    const validImages = images.filter((src) => src && !brokenSrcs.has(src));

    function markBroken(src: string) {
        setBrokenSrcs((prev) => new Set(prev).add(src));
    }

    if (validImages.length === 0) {
        return (
            <Paper
                elevation={0}
                sx={{
                    borderRadius: { borderRadius },
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
                    backgroundColor: "lightGray",
                }}
            >
                <ImageNotSupportedIcon sx={{ fontSize: 48 }} />
                <Typography variant="body2" color="text.secondary">
                    No image available
                </Typography>
            </Paper>
        );
    }

    const activeIndex = Math.min(selected, validImages.length - 1);
    const mainSrc = validImages[activeIndex];

    function showRelative(delta: number) {
        setSelected((prev) => {
            const current = Math.min(prev, validImages.length - 1);
            return (current + delta + validImages.length) % validImages.length;
        });
    }

    return (
        <Box sx={{ mt: 2 }}>
            <Paper elevation={4} sx={{ borderRadius: { borderRadius }, overflow: "hidden" }}>
                <Box
                    component="img"
                    src={mainSrc}
                    width="100%"
                    height={height}
                    onError={() => markBroken(mainSrc)}
                    onClick={() => setLightboxOpen(true)}
                    sx={{ objectFit: "cover", display: "block", cursor: "zoom-in" }}
                />
            </Paper>

            {validImages.length > 1 && (
                <Stack
                    direction="row"
                    spacing={1}
                    sx={{ mt: 1.5, overflowX: "auto", pb: 1 }}
                >
                    {validImages.map((src, idx) => {
                        const isActive = idx === activeIndex;
                        return (
                            <Box
                                key={`${idx}-${src.slice(0, 32)}`}
                                component="img"
                                src={src}
                                onClick={() => setSelected(idx)}
                                onError={() => markBroken(src)}
                                sx={{
                                    width: 96,
                                    height: 64,
                                    flexShrink: 0,
                                    objectFit: "cover",
                                    borderRadius: 1,
                                    cursor: "pointer",
                                    border: "3px solid",
                                    borderColor: isActive ? "primary.main" : "transparent",
                                    opacity: isActive ? 1 : 0.7,
                                    transition: "opacity 0.15s, border-color 0.15s",
                                    "&:hover": { opacity: 1 },
                                }}
                            />
                        );
                    })}
                </Stack>
            )}

            <Dialog
                open={lightboxOpen}
                onClose={() => setLightboxOpen(false)}
                maxWidth="lg"
                fullWidth
                slotProps={{ paper: { sx: { backgroundColor: "transparent", boxShadow: "none" } } }}
            >
                <Box sx={{ position: "relative", display: "flex", justifyContent: "center" }}>
                    <IconButton
                        onClick={() => setLightboxOpen(false)}
                        sx={{ position: "absolute", top: 8, right: 8, color: "#fff", bgcolor: "rgba(0,0,0,0.5)", "&:hover": { bgcolor: "rgba(0,0,0,0.7)" } }}
                    >
                        <CloseIcon />
                    </IconButton>

                    {validImages.length > 1 && (
                        <IconButton
                            onClick={() => showRelative(-1)}
                            sx={{ position: "absolute", top: "50%", left: 8, transform: "translateY(-50%)", color: "#fff", bgcolor: "rgba(0,0,0,0.5)", "&:hover": { bgcolor: "rgba(0,0,0,0.7)" } }}
                        >
                            <ChevronLeftIcon />
                        </IconButton>
                    )}

                    <Box
                        component="img"
                        src={mainSrc}
                        onError={() => markBroken(mainSrc)}
                        sx={{ maxWidth: "100%", maxHeight: "85vh", objectFit: "contain", borderRadius: 1 }}
                    />

                    {validImages.length > 1 && (
                        <IconButton
                            onClick={() => showRelative(1)}
                            sx={{ position: "absolute", top: "50%", right: 8, transform: "translateY(-50%)", color: "#fff", bgcolor: "rgba(0,0,0,0.5)", "&:hover": { bgcolor: "rgba(0,0,0,0.7)" } }}
                        >
                            <ChevronRightIcon />
                        </IconButton>
                    )}
                </Box>
            </Dialog>
        </Box>
    );
}
