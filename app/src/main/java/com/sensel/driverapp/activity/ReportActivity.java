package com.sensel.driverapp.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.sensel.driverapp.R;
import com.sensel.driverapp.adapter.ReportAdapter;
import com.sensel.driverapp.api.ServiceFactory;
import com.sensel.driverapp.api.VehicleApiService;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.request.ReportRequest;
import com.sensel.driverapp.response.report.Getreportdetails;
import com.sensel.driverapp.response.report.ReportResponse;
import com.sensel.driverapp.response.report.VettingReport;
import com.sensel.driverapp.utility.CheckNetwork;
import com.sensel.driverapp.utility.ToastHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sensel.driverapp.utility.ApplicationConstants.CONST_CONFIG_REGULAR;

public class ReportActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.ivBack)
    ImageView ivBack;
    @BindView(R.id.tv_PROName)
    TextView tvPROName;
    @BindView(R.id.rvReport)
    RecyclerView rvReport;
    @BindView(R.id.spinner_option)
    AppCompatSpinner spinnerOption;
    ArrayAdapter typeAdapter;
    String[] option = {"All", "Packed", "Rigid", "Truck+trailer"};
    String VehicleType;
    private static TextView tvFromDate;        //Date
    private static TextView tvToDate;        //Date
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.rect)
    LinearLayout rect;
    @BindView(R.id.btGetReport)
    Button btGetReport;
    @BindView(R.id.layoutVehicleType)
    LinearLayout layoutVehicleType;

    SharedPreferencesUtility sharedPreferencesUtility;
    List<VettingReport> reportList = new ArrayList<>();
    ReportAdapter reportAdapter;

    private LoginConfigs configSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);

        configSelected = (LoginConfigs) getIntent().getSerializableExtra("APP_NAME");
        if (configSelected != null && configSelected.name.equals(CONST_CONFIG_REGULAR)) {
            layoutVehicleType.setVisibility(View.VISIBLE);
        } else {
            layoutVehicleType.setVisibility(View.GONE);
        }

        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        sharedPreferencesUtility = new SharedPreferencesUtility(this);

        ivBack.setOnClickListener(v -> finish());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvReport.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) rvReport.getItemAnimator()).setSupportsChangeAnimations(false);


        spinnerOption.setOnItemSelectedListener(this);
        typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, option);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinnerOption.setAdapter(typeAdapter);

        tvFromDate.setOnClickListener(v -> showDatePickerDialog(v, "From"));
        tvToDate.setOnClickListener(v -> showDatePickerDialog(v, "To"));

        btGetReport.setOnClickListener(v -> {
            if (CheckNetwork.isNetworkAvailable(ReportActivity.this)) {
                if ((tvFromDate.getText().toString().trim().length() > 0) && (tvToDate.getText().toString().trim().length() > 0)) {
                    showdialog();
                    getReportList();
                } else {
                    ToastHelper.showToast(ReportActivity.this, "Please Fill All Details");
                }
            } else {
                ToastHelper.showToast(ReportActivity.this, "Please check internet connection");
            }
        });
    }


    public void showdialog() {
        rect.setVisibility(View.VISIBLE);
       // progress.showNow();
    }

    public void closedialog() {
        rect.setVisibility(View.GONE);
       // progress.hide();
    }

    private void getReportList() {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setFromDt(tvFromDate.getText().toString());
        reportRequest.setToDt(tvToDate.getText().toString());
        reportRequest.setSessionId(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID));
        reportRequest.setVehicleType(VehicleType);
        reportRequest.setAppId(configSelected.appId);

        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        Call call;
        if (configSelected.name.equals(CONST_CONFIG_REGULAR)) {
            call = service.getReport(reportRequest);
        } else {
            call = service.getVetReport(reportRequest);
        }
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                closedialog();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body() instanceof Getreportdetails) {
                        Getreportdetails getreportdetails = (Getreportdetails) response.body();
                        reportList = getreportdetails.getVettingReports();
                        if (reportList.isEmpty()) {
                            Toast.makeText(ReportActivity.this, "No reports are available.", Toast.LENGTH_SHORT).show();
                        }
                        reportAdapter = new ReportAdapter(ReportActivity.this, (List) reportList);
                        rvReport.setAdapter(reportAdapter);
                    } else if (response.body() instanceof List) {
                        List<ReportResponse> responses = (List<ReportResponse>) response.body();
                        if (responses.isEmpty()) {
                            Toast.makeText(ReportActivity.this, "No reports are available.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        reportAdapter = new ReportAdapter(ReportActivity.this, (List) responses);
                        rvReport.setAdapter(reportAdapter);
                    }
                } else {//VehicleTypeChecklist needs to verify using OTP
                    ToastHelper.showToast(ReportActivity.this, "Something went wrong");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                closedialog();
                Log.d("Login Fail ", t.getMessage());
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(this, "select"+aa.getItem(position), Toast.LENGTH_SHORT).show();
        VehicleType = (typeAdapter.getItem(position)).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void showDatePickerDialog(View v, String identifier) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
        ((DatePickerFragment) newFragment).setIdentifier(identifier);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        String identifier;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 0);

            //datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

            // Create a new instance of DatePickerDialog and return it
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //dob.setVisibility(View.VISIBLE);
            if (getIdentifier().equals("From")) {
                tvFromDate.setText("" + year + "-" + (month + 1) + "-" + day);
            } else if (getIdentifier().equals("To")) {
                tvToDate.setText("" + year + "-" + (month + 1) + "-" + day);
            }
        }
    }


}
