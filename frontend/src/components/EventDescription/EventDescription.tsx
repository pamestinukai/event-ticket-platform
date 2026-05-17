import {Box, Button, Collapse, Typography} from "@mui/material";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import {useState} from "react";

export interface EventDescriptionProps{
    description :string;
}

export function EventDescription ({description} : EventDescriptionProps){
    const [expanded, setExpanded] = useState(false);
    const SHORT_LENGTH = 420;
    const isLong = (description.length ?? 0) > SHORT_LENGTH;
    const fontSize = 16;

    if ((description.length ?? 0) == 0)
        return(
            <Typography variant="caption" sx={{fontSize: {fontSize}, fontStyle: "italic"}}>The event has no description</Typography>
        )

    return(
        <Box sx={{position: "relative"}}>
            <Typography sx={{ lineHeight: 2, fontSize: {fontSize}, textAlign: "justify" }}>
                {description.slice(0, SHORT_LENGTH)}
                {isLong && !expanded && "..."}
            </Typography>

            {isLong && !expanded && (
                <Box sx={{
                    position: "absolute",
                    bottom: 50,
                    left: 0,
                    right: 0,
                    height: "100px",
                    background: `linear-gradient(transparent, white)`,
                }} />
            )}

            <Collapse in={expanded} timeout={400}>
                <Typography sx={{ lineHeight: 2, fontSize: {fontSize}, textAlign: "justify" }}>
                    {description.slice(SHORT_LENGTH)}
                </Typography>
            </Collapse>

            {isLong && (
                <Box sx={{display: "flex", justifyContent: "center"}}>
                    <Button
                        onClick={() => setExpanded(!expanded)}
                        variant="text"
                        sx={{ my: 2, p: 0, textTransform: "none", fontWeight: 600 }}
                    >
                        {expanded ? (
                            <><ExpandLessIcon fontSize="small" /> Read less</>
                        ) : (
                            <><ExpandMoreIcon fontSize="small" /> Read more</>
                        )}
                    </Button>
                </Box>
            )}
        </Box>
    )
}