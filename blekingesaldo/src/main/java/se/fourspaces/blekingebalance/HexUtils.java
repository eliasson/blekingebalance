/*
 * Copyright 2014 Markus Eliasson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.fourspaces.blekingebalance;

import android.util.Log;


/**
 * Utils to convert HEX written as string.
 */
public abstract class HexUtils {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        String hexStringPattern = "^([0-9A-Fa-f]{2})+$";
        boolean isHexString = s.matches(hexStringPattern);

        if (isHexString) {
            try {
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i + 1), 16));
                }
            } catch (Exception e) {
                Log.d(Balance.LOG_TAG, "Argument(s) for hexStringToByteArray(String s)"
                        + "was not a hex string");
            }
        }
        return data;
    }
}
