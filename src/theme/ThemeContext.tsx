import React, { createContext, useContext, useMemo, useState, useEffect } from 'react';
import { useColorScheme } from 'react-native';
import {
  MD3LightTheme,
  MD3DarkTheme,
  PaperProvider,
  MD3Theme,
} from 'react-native-paper';
import {
  useMaterial3Theme,
  isDynamicThemeSupported,
  Material3Theme,
  Material3Scheme,
} from '@pchmn/expo-material3-theme';
import {
  MaterialYouPaletteKey,
  getPresetMaterial3Theme,
} from './colors';
import { typography } from './typography';
import { dbService } from '../db/database';

export type AppMD3Colors = MD3Theme['colors'] & Material3Scheme & {
  success: string;
};

export type AppMD3Theme = Omit<MD3Theme, 'colors'> & {
  colors: AppMD3Colors;
};

interface ThemeContextType {
  theme: AppMD3Theme;
  paperTheme: AppMD3Theme;
  colors: AppMD3Colors;
  isDark: boolean;
  typography: typeof typography;
  paletteKey: MaterialYouPaletteKey;
  setPaletteKey: (key: MaterialYouPaletteKey) => Promise<void>;
  isDynamicSupported: boolean;
}

const ThemeContext = createContext<ThemeContextType>({
  theme: MD3LightTheme as unknown as AppMD3Theme,
  paperTheme: MD3LightTheme as unknown as AppMD3Theme,
  colors: MD3LightTheme.colors as unknown as AppMD3Colors,
  isDark: false,
  typography,
  paletteKey: 'blue',
  setPaletteKey: async () => {},
  isDynamicSupported: false,
});

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const systemScheme = useColorScheme();
  const isDark = systemScheme === 'dark';

  // Extract dynamic colors on Android 12+ or fallback to default ocean blue
  const { theme: dynamicSystemTheme } = useMaterial3Theme({
    fallbackSourceColor: '#1B5E9E',
  });

  const [paletteKey, setLocalPaletteKey] = useState<MaterialYouPaletteKey>('blue');

  useEffect(() => {
    const syncSettings = () => {
      const currentSettings = dbService.getSettings();
      if (currentSettings.selectedThemePalette && currentSettings.selectedThemePalette !== paletteKey) {
        setLocalPaletteKey(currentSettings.selectedThemePalette);
      }
    };

    syncSettings();
    const unsubscribe = dbService.subscribe(syncSettings);
    return () => unsubscribe();
  }, [paletteKey]);

  const setPaletteKey = async (key: MaterialYouPaletteKey) => {
    setLocalPaletteKey(key);
    await dbService.updateSettings({ selectedThemePalette: key });
  };

  // Determine active Material 3 color theme
  const activeM3Theme: Material3Theme = useMemo(() => {
    if (paletteKey === 'dynamic') {
      return dynamicSystemTheme;
    }
    return getPresetMaterial3Theme(paletteKey);
  }, [paletteKey, dynamicSystemTheme]);

  // Merge extracted M3 color scheme with Paper MD3LightTheme / MD3DarkTheme
  const paperTheme: AppMD3Theme = useMemo(() => {
    const baseTheme = isDark ? MD3DarkTheme : MD3LightTheme;
    const m3Scheme = isDark ? activeM3Theme.dark : activeM3Theme.light;

    return {
      ...baseTheme,
      colors: {
        ...baseTheme.colors,
        ...m3Scheme,
        // Semantic roles and contrast pairings
        success: isDark ? '#81C784' : '#2E7D32',
      },
    };
  }, [isDark, activeM3Theme]);

  const contextValue: ThemeContextType = useMemo(
    () => ({
      theme: paperTheme,
      paperTheme,
      colors: paperTheme.colors,
      isDark,
      typography,
      paletteKey,
      setPaletteKey,
      isDynamicSupported: isDynamicThemeSupported,
    }),
    [paperTheme, isDark, paletteKey]
  );

  return (
    <ThemeContext.Provider value={contextValue}>
      <PaperProvider theme={paperTheme as unknown as MD3Theme}>
        {children}
      </PaperProvider>
    </ThemeContext.Provider>
  );
};

export const useAppTheme = () => useContext(ThemeContext);
