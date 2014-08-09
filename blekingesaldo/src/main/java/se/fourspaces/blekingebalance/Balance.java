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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import se.fourspaces.blekingebalance.nfc.CardReadTaskCallback;
import se.fourspaces.blekingebalance.nfc.ReadCardTask;
import se.fourspaces.blekingebalance.ui.CardView;

public class Balance extends Activity implements CardReadTaskCallback {
    public final static String LOG_TAG = "BlekingeBalance";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private NfcAdapter mAdapter;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;
    private PendingIntent mPendingIntent;
    private CardView mCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        mCardView = (CardView) findViewById(R.id.card);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mAdapter == null) {
            mCardView.setState(CardView.CardState.Unsupported);
            finish();
            return;
        }

        if(!mAdapter.isEnabled()) {
            mCardView.setState(CardView.CardState.Disabled);
        }
        else {
            setupForegroundDispatch();
            handleIntent(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.balance, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAdapter != null) {
            if(mAdapter.isEnabled()) {
                mAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mTechLists);
            }
            else {
                mCardView.setState(CardView.CardState.Disabled);
            }
        }
        else {
            mCardView.setState(CardView.CardState.Unsupported);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
       handleIntent(intent);
    }

    @Override
    public void onCardReadComplete(Double balance) {
        mCardView.setBalance(balance);
        mCardView.setState(CardView.CardState.Complete);
    }

    @Override
    public void onCardReadError() {
        mCardView.setState(CardView.CardState.Error);
    }

    private void setupForegroundDispatch() {
        try {
            IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            filter.addDataType(MIME_TEXT_PLAIN);

            mIntentFilters = new IntentFilter[] {
                    filter,
            };

            mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        catch(IntentFilter.MalformedMimeTypeException e) {
            Log.e(LOG_TAG, "Malformed mime type");
        }
    }

    private void handleIntent(Intent intent) {
        final String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            if(MIME_TEXT_PLAIN.equals(intent.getType())) {
                final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new ReadCardTask(tag, this).execute();
            }
        }
        else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            mCardView.setState(CardView.CardState.InProgress);
            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new ReadCardTask(tag, this).execute();
        }
        else {
            mCardView.setState(CardView.CardState.Idle);
        }
    }
}
