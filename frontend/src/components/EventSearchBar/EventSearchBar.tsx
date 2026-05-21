import { InputAdornment, IconButton, TextField } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";
import { useEffect, useRef, useState } from "react";

const DEBOUNCE_MS = 300;

interface EventSearchBarProps {
  onSearch: (query: string) => void;
}

export function EventSearchBar({ onSearch }: EventSearchBarProps) {
  const [inputValue, setInputValue] = useState("");
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    debounceRef.current = setTimeout(() => onSearch(inputValue), DEBOUNCE_MS);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [inputValue]);

  const commit = () => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    onSearch(inputValue);
  };

  const clear = () => {
    setInputValue("");
    if (debounceRef.current) clearTimeout(debounceRef.current);
    onSearch("");
  };

  return (
    <TextField
      fullWidth
      variant="outlined"
      placeholder="Search by event name, description, or performer..."
      value={inputValue}
      onChange={e => setInputValue(e.target.value)}
      onKeyDown={e => { if (e.key === "Enter") commit(); }}
      inputProps={{ "aria-label": "Search events" }}
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start">
              <IconButton onClick={commit} aria-label="Search" edge="start" size="small">
                <SearchIcon />
              </IconButton>
            </InputAdornment>
          ),
          endAdornment: inputValue ? (
            <InputAdornment position="end">
              <IconButton onClick={clear} aria-label="Clear search" edge="end" size="small">
                <ClearIcon />
              </IconButton>
            </InputAdornment>
          ) : null,
        },
      }}
      sx={{ mb: 3 }}
    />
  );
}
