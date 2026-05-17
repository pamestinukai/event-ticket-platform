import {Box, Button, Typography} from "@mui/material";
import {Header} from "../Header/Header.tsx";
import {Footer} from "../Footer/Footer.tsx";

export interface ErrorScreenProps {
    error : string;
}

export function ErrorScreen({error} :ErrorScreenProps) {
    return (
        <Box>
            <Header/>
            <Box sx={{ display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", height: "85vh", gap: 2 }}>
                <Typography variant="h5" sx={{fontWeight: 700}}>Something went wrong</Typography>
                <Typography color="text.secondary">{error}</Typography>
                <Button variant="contained" onClick={() => window.history.back()}>Go back</Button>
            </Box>
            <Footer/>
        </Box>
    )
}