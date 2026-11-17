package com.sensel.driverapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.sensel.driverapp.R;
import com.sensel.driverapp.adapter.SyncAdapter;
import com.sensel.driverapp.db.AppDatabase;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.db.model.VehicleVetting;
import com.sensel.driverapp.service.SyncIntentService;
import com.sensel.driverapp.utility.CheckNetwork;
import com.sensel.driverapp.utility.ToastHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.ACTION_SYNC;
import static com.sensel.driverapp.utility.ApplicationConstants.BROADCAST_UPDATE_SCREEN;

public class SyncActivity extends AppCompatActivity {

    private static final String TAG = SyncActivity.class.getName();

    @BindView(R.id.buttonSync)
    Button buttonSync;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progress)
    ProgressBar progress;

    @BindView(R.id.rect)
    LinearLayout rect;

    private SharedPreferencesUtility sharedPreferencesUtility;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        sharedPreferencesUtility = new SharedPreferencesUtility(this);
        // Get local broadcast manager object.
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        buttonSync.setEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        getDataFromDB();
    }

    private void getDataFromDB() {
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        List<VehicleVetting> vettingList = database.vehicleVettingModel().loadAllVetting();
        SyncAdapter syncAdapter = new SyncAdapter(SyncActivity.this, vettingList);
        recyclerView.setAdapter(syncAdapter);
    }

    public void syncToServer(View view) {
        if (CheckNetwork.isNetworkAvailable(SyncActivity.this)) {
            buttonSync.setEnabled(false);
            Intent intent = new Intent(this, SyncIntentService.class);
            //intent.putExtra(CONST_DATA, vehicleVetting); //cause error
            intent.setAction(ACTION_SYNC);
            startService(intent);
        } else {
            ToastHelper.showToast(SyncActivity.this, "Please check internet connection");
        }
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        if (mMessageReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mMessageReceiver != null)
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BROADCAST_UPDATE_SCREEN));
        super.onResume();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ToastHelper.showToast(SyncActivity.this, "Sync operation completed, refreshing screen.");
            getDataFromDB();
            buttonSync.setEnabled(true);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
