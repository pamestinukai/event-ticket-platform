import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

interface ConflictDialogProps {
    open: boolean;
    onClose: () => void;
    onRetry: () => void;
}

export function ConflictDialog({ open, onClose, onRetry }: ConflictDialogProps) {
    return (
        <Dialog
            open={open}
            onClose={onClose}
            aria-labelledby="alert-dialog-title"
            aria-describedby="alert-dialog-description"
            role="alertdialog"
        >
            <DialogTitle id="alert-dialog-title">Edit conflict</DialogTitle>
            <DialogContent>
                <DialogContentText id="alert-dialog-description">
                    Someone else modified this record while you were editing. Refresh to get the latest version or try saving changes again.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => window.location.reload()}>Refresh</Button>
                <Button onClick={onRetry} autoFocus>Retry</Button>
            </DialogActions>
        </Dialog>
    );
}