package com.eleks.rnd.warehouse.glass;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.glass.widget.MessageDialog;

public class ScannedItemActivity extends Activity {
    private static final String TAG = "ItemA";

    private static final long BEFORE_DIALOG_TIME = 3000;

    protected static final long AFTER_DIALOG_TIME = 3000;

    private Handler mHandler;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        final Item item = (Item) getIntent().getSerializableExtra(Flow.EXTRA_ITEM);

        TextView mId = (TextView) findViewById(R.id.output_id);
        mId.setText(item.getId());

        TextView mExpiry = (TextView) findViewById(R.id.output_expiry);
        mExpiry.setText(item.getExpiryString());

        mImage = (ImageView) findViewById(R.id.background);

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showItemAddingDialog(item);
            }
        }, BEFORE_DIALOG_TIME);
    }

    private void showItemAddingDialog(Item item) {
        String secondary = item.getId() + " :: " + item.getExpiryString();
        MessageDialog localDialog = new MessageDialog.Builder(ScannedItemActivity.this)
                .setTemporaryIcon(R.drawable.ic_sync_50).setTemporaryMessage("adding item")
                .setTemporarySecondaryMessage(secondary).setIcon(R.drawable.ic_done_50).setMessage("success")
                .setSecondaryMessage(secondary).setDismissable(true).setAutoHide(true)
                .setListener(new MessageDialog.SimpleListener() {
                    public boolean onConfirmed() {
                        Log.d(TAG, "onConfirm");
                        return true;
                    }

                    public void onDismissed() {
                        Log.d(TAG, "onDismissed");
                    }

                    public void onDone() {
                        Log.d(TAG, "onDone");
                        mImage.setImageResource(getRandomFood());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ScannedItemActivity.this.finish();
                            }
                        }, AFTER_DIALOG_TIME);
                    }
                }).build();
        localDialog.show();
    }

    private int getRandomFood() {
        int random = ((int) (Math.random() * 1000)) % 4;
        switch (random) {
        case 0:
            return R.drawable.food_0;
        case 1:
            return R.drawable.food_1;
        case 2:
            return R.drawable.food_2;
        case 3:
            return R.drawable.food_3;
        default:
            return R.drawable.food_0;
        }
    }
}
