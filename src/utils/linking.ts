import { Linking } from 'react-native';
import * as Clipboard from 'expo-clipboard';
import { showShortToast } from './toast';

/**
 * Copies text to clipboard and shows toast "Copied: {value}"
 */
export async function copyToClipboard(value: string): Promise<void> {
  if (!value) return;
  await Clipboard.setStringAsync(value);
  showShortToast(`Copied: ${value}`);
}

/**
 * Normalize Pakistani number:
 * - strip non-digits
 * - drop "00" prefix (e.g. 00923001234567 -> 923001234567)
 * - leading "0" replaced with country code 92 (e.g. 03001234567 -> 923001234567)
 * - already 92... kept as 92...
 */
export function normalizeWhatsAppNumber(rawNumber: string): string {
  if (!rawNumber) return '';
  let digits = rawNumber.replace(/\D/g, '');
  if (digits.startsWith('00')) {
    digits = digits.substring(2);
  }
  if (digits.startsWith('0')) {
    digits = '92' + digits.substring(1);
  }
  return digits;
}

/**
 * Opens WhatsApp with the given phone number
 */
export async function openWhatsApp(rawNumber: string): Promise<void> {
  const normalized = normalizeWhatsAppNumber(rawNumber);
  if (!normalized) {
    showShortToast('No valid number');
    return;
  }

  const deepUrl = `whatsapp://send?phone=${normalized}`;
  const webUrl = `https://wa.me/${normalized}`;

  try {
    const canOpenDeep = await Linking.canOpenURL(deepUrl);
    if (canOpenDeep) {
      await Linking.openURL(deepUrl);
      return;
    }

    const canOpenWeb = await Linking.canOpenURL(webUrl);
    if (canOpenWeb) {
      await Linking.openURL(webUrl);
      return;
    }

    showShortToast('WhatsApp not installed');
  } catch (error) {
    // Try opening web fallback directly
    try {
      await Linking.openURL(webUrl);
    } catch {
      showShortToast('WhatsApp not installed');
    }
  }
}

/**
 * Opens phone dialer pre-filled with number
 */
export async function openDialer(rawNumber: string): Promise<void> {
  if (!rawNumber || !rawNumber.trim()) {
    showShortToast('No phone number saved');
    return;
  }

  const cleaned = rawNumber.replace(/[^\d+]/g, '');
  if (!cleaned) {
    showShortToast('No phone number saved');
    return;
  }

  const telUrl = `tel:${cleaned}`;
  try {
    const canOpen = await Linking.canOpenURL(telUrl);
    if (canOpen) {
      await Linking.openURL(telUrl);
    } else {
      showShortToast('No dialer available');
    }
  } catch {
    showShortToast('No dialer available');
  }
}

/**
 * Opens GitHub URL in browser
 */
export async function openGitHub(): Promise<void> {
  const url = 'https://github.com/afnan-nex';
  try {
    await Linking.openURL(url);
  } catch {
    showShortToast('Could not open GitHub link');
  }
}
