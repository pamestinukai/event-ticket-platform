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
                    Someone else changed this record while you were editing it. Reload to discard your
                    changes and load their version, or overwrite it with your changes.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => window.location.reload()}>Reload latest</Button>
                <Button onClick={onRetry} color="error" autoFocus>Overwrite anyway</Button>
            </DialogActions>
        </Dialog>
    );
}