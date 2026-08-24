import { createMaterial3Theme, Material3Theme, Material3Scheme } from '@pchmn/expo-material3-theme';
import { MaterialYouPaletteKey } from '../types';

export type { Material3Scheme as ColorScheme, Material3Theme, Material3Scheme };
export type { MaterialYouPaletteKey };

export interface MaterialYouThemeOption {
  key: MaterialYouPaletteKey;
  name: string;
  seedColor: string;
  theme?: Material3Theme;
}

export const PRESET_SEED_COLORS: Record<Exclude<MaterialYouPaletteKey, 'dynamic'>, { name: string; seedColor: string }> = {
  blue: {
    name: 'Ocean Blue',
    seedColor: '#1B5E9E',
  },
  green: {
    name: 'Botanical Green',
    seedColor: '#2E6C38',
  },
  purple: {
    name: 'Lavender Purple',
    seedColor: '#6750A4',
  },
  coral: {
    name: 'Sunset Coral',
    seedColor: '#9C4146',
  },
  amber: {
    name: 'Golden Amber',
    seedColor: '#825500',
  },
  teal: {
    name: 'Deep Teal',
    seedColor: '#006874',
  },
};

// Lazily generated M3 themes for presets
const presetThemesCache: Partial<Record<Exclude<MaterialYouPaletteKey, 'dynamic'>, Material3Theme>> = {};

export function getPresetMaterial3Theme(key: Exclude<MaterialYouPaletteKey, 'dynamic'>): Material3Theme {
  if (!presetThemesCache[key]) {
    const config = PRESET_SEED_COLORS[key];
    presetThemesCache[key] = createMaterial3Theme(config.seedColor);
  }
  return presetThemesCache[key]!;
}

export const materialYouPalettes: Record<MaterialYouPaletteKey, MaterialYouThemeOption> = {
  dynamic: {
    key: 'dynamic',
    name: 'Dynamic (System)',
    seedColor: '#1B5E9E', // Indicator seed color for dynamic icon
  },
  blue: {
    key: 'blue',
    name: PRESET_SEED_COLORS.blue.name,
    seedColor: PRESET_SEED_COLORS.blue.seedColor,
  },
  green: {
    key: 'green',
    name: PRESET_SEED_COLORS.green.name,
    seedColor: PRESET_SEED_COLORS.green.seedColor,
  },
  purple: {
    key: 'purple',
    name: PRESET_SEED_COLORS.purple.name,
    seedColor: PRESET_SEED_COLORS.purple.seedColor,
  },
  coral: {
    key: 'coral',
    name: PRESET_SEED_COLORS.coral.name,
    seedColor: PRESET_SEED_COLORS.coral.seedColor,
  },
  amber: {
    key: 'amber',
    name: PRESET_SEED_COLORS.amber.name,
    seedColor: PRESET_SEED_COLORS.amber.seedColor,
  },
  teal: {
    key: 'teal',
    name: PRESET_SEED_COLORS.teal.name,
    seedColor: PRESET_SEED_COLORS.teal.seedColor,
  },
};
