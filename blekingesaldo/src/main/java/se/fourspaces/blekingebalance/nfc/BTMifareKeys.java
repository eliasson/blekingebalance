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
package se.fourspaces.blekingebalance.nfc;

import se.fourspaces.blekingebalance.HexUtils;
import se.fourspaces.blekingebalance.nfc.Key;

/**
 * Authentication keys for Mifare cards. From http://pastebin.com/svGjN30Q
 *
 * Tests shows that Blekingetrafiken uses:
 *
 *  00-03 A: 434F4D4D4F41
 *  00-03 B: 434F4D4D4F42
 *  04-11,15 A: 47524F555041
 *  04-11,15 B: 47524F555042
 *  12,13,15 A: 505249564141
 *  12,13,15 B: 505249564142
 */
public abstract class BTMifareKeys {
    public static Key getKey(int sector) {
        if(sector >= 0 && sector <= 3) {
            return new Key(
                    HexUtils.hexStringToByteArray("434F4D4D4F41"),
                    HexUtils.hexStringToByteArray("434F4D4D4F42"));
        }
        else if((sector >= 4 && sector <= 11) || (sector == 14)) {
            return new Key(
                    HexUtils.hexStringToByteArray("47524F555041"),
                    HexUtils.hexStringToByteArray("47524F555042"));
        }
        else if(sector == 12 || sector == 13 || sector == 15) {
            return new Key(
                    HexUtils.hexStringToByteArray("505249565441"),
                    HexUtils.hexStringToByteArray("505249565442"));
        }
        else {
            throw new RuntimeException("Unknown sector given");
        }
    }
}
