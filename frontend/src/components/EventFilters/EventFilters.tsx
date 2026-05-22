import {
  Box,
  Button,
  Chip,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import type { CategoryResponse } from '../../types/CategoryResponse';

export interface EventFilters {
  categoryId: number | null;
  dateFrom: string;
  dateTo: string;
  cities: string[];
  priceMin: string;
  priceMax: string;
}

export const DEFAULT_FILTERS: EventFilters = {
  categoryId: null,
  dateFrom: '',
  dateTo: '',
  cities: [],
  priceMin: '',
  priceMax: '',
};

function countActive(f: EventFilters): number {
  return [
    f.categoryId != null,
    f.dateFrom !== '',
    f.dateTo !== '',
    f.cities.length > 0,
    f.priceMin !== '',
    f.priceMax !== '',
  ].filter(Boolean).length;
}

interface EventFiltersProps {
  filters: EventFilters;
  onChange: (filters: EventFilters) => void;
  categories: CategoryResponse[];
  availableCities: string[];
}

export function EventFiltersPanel({
  filters,
  onChange,
  categories,
  availableCities,
}: EventFiltersProps) {
  const set = (patch: Partial<EventFilters>) =>
    onChange({ ...filters, ...patch });

  const activeCount = countActive(filters);
  const categoryName = categories.find(
    (c) => c.id === filters.categoryId,
  )?.name;

  return (
    <Box sx={{ mb: 3 }}>
      <Stack
        direction='row'
        sx={{ alignItems: 'center', mb: 1.5, gap: 1 }}
      >
        <FilterListIcon
          fontSize='small'
          sx={{ color: 'text.secondary' }}
        />
        <Typography
          variant='body2'
          sx={{ fontWeight: 600, color: 'text.secondary' }}
        >
          Filters
        </Typography>
        {activeCount > 0 && (
          <Button
            size='small'
            onClick={() => onChange(DEFAULT_FILTERS)}
            sx={{ ml: 'auto', textTransform: 'none' }}
          >
            Clear all ({activeCount})
          </Button>
        )}
      </Stack>

      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        flexWrap='wrap'
        useFlexGap
        spacing={1.5}
        sx={{ mb: activeCount > 0 ? 1.5 : 0 }}
      >
        {/* Category */}
        <FormControl size='small' sx={{ minWidth: 160 }}>
          <InputLabel>Category</InputLabel>
          <Select
            value={filters.categoryId ?? ''}
            label='Category'
            onChange={(e) =>
              set({
                categoryId:
                  e.target.value === ''
                    ? null
                    : Number(e.target.value),
              })
            }
          >
            <MenuItem value=''>All categories</MenuItem>
            {categories.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Date from */}
        <TextField
          size='small'
          label='From date'
          type='date'
          value={filters.dateFrom}
          onChange={(e) => set({ dateFrom: e.target.value })}
          slotProps={{ inputLabel: { shrink: true } }}
          sx={{ minWidth: 155 }}
        />

        {/* Date to */}
        <TextField
          size='small'
          label='To date'
          type='date'
          value={filters.dateTo}
          onChange={(e) => set({ dateTo: e.target.value })}
          slotProps={{ inputLabel: { shrink: true } }}
          sx={{ minWidth: 155 }}
        />

        {/* City multi-select */}
        <FormControl size='small' sx={{ minWidth: 160 }}>
          <InputLabel id='city-filter-label'>City</InputLabel>
          <Select
            labelId='city-filter-label'
            multiple
            value={filters.cities}
            label='City'
            onChange={(e) =>
              set({
                cities:
                  typeof e.target.value === 'string'
                    ? [e.target.value]
                    : (e.target.value as string[]),
              })
            }
            renderValue={(selected) =>
              (selected as string[]).join(', ')
            }
          >
            {availableCities.length === 0 ? (
              <MenuItem disabled>No cities available</MenuItem>
            ) : (
              availableCities.map((city) => (
                <MenuItem key={city} value={city}>
                  {city}
                </MenuItem>
              ))
            )}
          </Select>
        </FormControl>

        {/* Price min */}
        <TextField
          size='small'
          label='Min price (€)'
          type='number'
          value={filters.priceMin}
          onChange={(e) => set({ priceMin: e.target.value })}
          slotProps={{ htmlInput: { min: 0, step: 1 } }}
          sx={{ minWidth: 130 }}
        />

        {/* Price max */}
        <TextField
          size='small'
          label='Max price (€)'
          type='number'
          value={filters.priceMax}
          onChange={(e) => set({ priceMax: e.target.value })}
          slotProps={{ htmlInput: { min: 0, step: 1 } }}
          sx={{ minWidth: 130 }}
        />
      </Stack>

      {/* Active filter chips */}
      {activeCount > 0 && (
        <Stack
          direction='row'
          flexWrap='wrap'
          useFlexGap
          spacing={0.75}
        >
          {filters.categoryId != null && (
            <Chip
              size='small'
              label={`Category: ${categoryName ?? filters.categoryId}`}
              onDelete={() => set({ categoryId: null })}
              color='primary'
              variant='outlined'
            />
          )}
          {filters.dateFrom && (
            <Chip
              size='small'
              label={`From: ${filters.dateFrom}`}
              onDelete={() => set({ dateFrom: '' })}
              color='primary'
              variant='outlined'
            />
          )}
          {filters.dateTo && (
            <Chip
              size='small'
              label={`To: ${filters.dateTo}`}
              onDelete={() => set({ dateTo: '' })}
              color='primary'
              variant='outlined'
            />
          )}
          {filters.cities.map((city) => (
            <Chip
              key={city}
              size='small'
              label={`City: ${city}`}
              onDelete={() =>
                set({
                  cities: filters.cities.filter((c) => c !== city),
                })
              }
              color='primary'
              variant='outlined'
            />
          ))}
          {filters.priceMin && (
            <Chip
              size='small'
              label={`Min: €${filters.priceMin}`}
              onDelete={() => set({ priceMin: '' })}
              color='primary'
              variant='outlined'
            />
          )}
          {filters.priceMax && (
            <Chip
              size='small'
              label={`Max: €${filters.priceMax}`}
              onDelete={() => set({ priceMax: '' })}
              color='primary'
              variant='outlined'
            />
          )}
        </Stack>
      )}
    </Box>
  );
}
