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

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;

import java.io.IOException;

import se.norenh.rkfread.RKFCard;
import se.norenh.rkfread.RKFObject;

/**
 * Async task used to read the Mifare card sectors and to extract the balance
 */
public class ReadCardTask extends AsyncTask<Void, Void, Void> {
    private final Tag mTag;
    private final CardReadTaskCallback mCallback;
    private RKFCard mCard;

    public ReadCardTask(Tag tag, CardReadTaskCallback callback) {
        mTag = tag;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final MifareClassic mfc = MifareClassic.get(mTag);

        try {
            mfc.connect();
            final int sectorCount = mfc.getSectorCount();
            mCard = new RKFCard();
            for (int sector = 0; sector < sectorCount; sector++) {
                final Key key = BTMifareKeys.getKey(sector);

                boolean auth = mfc.authenticateSectorWithKeyA(sector, key.A);
                if(!auth) {
                    auth = mfc.authenticateSectorWithKeyB(sector, key.B);
                }

                if(auth) {
                    readBlocks(mfc, sector);
                }
            }

            if(mCard != null) {
                mCard.parseCard();
            }
        }
        catch (TagLostException e) {
            mCard = null;
            return null;
        }
        catch (IOException e) {
            mCard = null;
            return null;
        }
        finally {
            try {
                mfc.close();
            }
            catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    private void readBlocks(MifareClassic mfc, int sectorIndex) throws IOException {
        int startBlockIndex = mfc.sectorToBlock(sectorIndex);
        for(int block = startBlockIndex; block < (startBlockIndex + 3); block++){
            final byte[] data = mfc.readBlock(block);
            mCard.addBlock(sectorIndex, (block%4), data);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(mCard != null) {
            if(mCard.dynPurse != null) {
                final RKFObject value = mCard.dynPurse.get("Value");
                mCallback.onCardReadComplete(value.getValue() / 100.0);
            }
            else {
                mCallback.onCardReadError();
            }
        }
        else {
            mCallback.onCardReadError();
        }
    }
}
