package com.eleks.rnd.warehouse.glass;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.sourceforge.zbar.Symbol;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class Flow {

    private static final String TAG = "Flow";

    public static final String EXTRA_ITEM = "parsed_item";
    public static final int VOICE_DATE_REQUEST = 42;

    private final Context mContext;
    private final Item mCurrentItem;

    private static Map<Context, Flow> mContextFlow = new WeakHashMap<Context, Flow>();

    public static Flow with(Context ctx) {
        if (mContextFlow.containsKey(ctx)) {
            return mContextFlow.get(ctx);
        } else {
            Flow f = new Flow(ctx);
            mContextFlow.put(ctx, f);
            return f;
        }
    }

    private Flow(Context ctx) {
        mContext = ctx;
        mCurrentItem = new Item();
    }

    public void scanned(Symbol sym) {
        Log.d(TAG, "Scanned: " + sym.getType() + " text: " + sym.getData());

        if (sym.getType() == Symbol.QRCODE) {
            String data = sym.getData();
            mCurrentItem.copyFrom(Item.parseFrom(data));
            if (mCurrentItem.getExpiry() == null) {
                getDateViaVoice();
            } else {
                scannedFullItem();
            }
        } else {
            String data = sym.getData();
            mCurrentItem.setId(data);
            getDateViaVoice();
        }
    }

    private void scannedFullItem() {
        Intent i = new Intent(mContext, ScannedItemActivity.class);
        i.putExtra(EXTRA_ITEM, mCurrentItem);
        mContext.startActivity(i);
    }

    private void getDateViaVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say expiry date: month and day");
        ((Activity) mContext).startActivityForResult(intent, Flow.VOICE_DATE_REQUEST);
    }

    public void completeWithDate(Intent intent) {
        List<String> results = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        Log.d(TAG, "" + results);
        Date d = get5thDayFromNow();
        if (results.size() > 0) {
            String spokenText = results.get(0);
            d = parse(spokenText);
        }
        mCurrentItem.setExpiry(d);
        scannedFullItem();
    }

    /**
     * 
     * @param text
     *            free form text
     * @return null or parsed date
     */
    private static Date parse(String text) {
        Log.d(TAG, "============= PARSING: " + text);
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(text);
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();
            Log.d(TAG, "PARSED: " + dates);
            if (dates.size() > 0) {
                return dates.get(0);
            }
        }
        return null;
    }

    private static Date get5thDayFromNow() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 5);
        return c.getTime();
    }

}
