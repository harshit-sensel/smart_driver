package com.sensel.driverapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensel.driverapp.BuildConfig;
import com.sensel.driverapp.R;
import com.sensel.driverapp.adapter.AutocompleteAdapter;
import com.sensel.driverapp.api.ServiceFactory;
import com.sensel.driverapp.api.VehicleApiService;
import com.sensel.driverapp.db.AppDatabase;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.db.model.VehicleVetting;
import com.sensel.driverapp.request.AutocompleteDropdownValuesRequest;
import com.sensel.driverapp.request.CheckListRequest;
import com.sensel.driverapp.request.ChecklistValues;
import com.sensel.driverapp.request.FieldValueRequest;
import com.sensel.driverapp.request.StaticDetails;
import com.sensel.driverapp.response.AutocompleteDropdownResponse;
import com.sensel.driverapp.response.Checklist;
import com.sensel.driverapp.response.DynamicCheckListResponse;
import com.sensel.driverapp.response.Field;
import com.sensel.driverapp.response.FieldValues;
import com.sensel.driverapp.response.ImageDetails;
import com.sensel.driverapp.response.MultipleImageDetailsWrapper;
import com.sensel.driverapp.response.Rule;
import com.sensel.driverapp.service.LocationUpdatesService;
import com.sensel.driverapp.utility.CheckNetwork;
import com.sensel.driverapp.utility.LocationUtils;
import com.sensel.driverapp.utility.ToastHelper;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sensel.driverapp.utility.ApplicationConstants.GET_SELECTED_IMAGES_REQUEST;
import static com.sensel.driverapp.utility.ApplicationConstants.IMAGE_LIST;
import static com.sensel.driverapp.utility.ApplicationConstants.PARENT_POSITION;
import static com.sensel.driverapp.utility.ApplicationConstants.POSITION;
import static com.sensel.driverapp.utility.ApplicationConstants.REMARK;
import static com.sensel.driverapp.utility.ApplicationConstants.RULE_ID;

public class DynamicFormActivity extends AppCompatActivity {
    private Context mContext;
    private static final String TAG = DynamicFormActivity.class.getName();
    private static final String AUTOCOMPLETE_TAG = "ValueFromList";
    private SharedPreferencesUtility sharedPreferencesUtility;
    private LoginConfigs configSelected;

    private String finalLat = "";
    private String finalLong = "";

    private DynamicCheckListResponse dynamicCheckListResponse; //main data coming from server
    private Map<String, ChecklistValues> checklistValuesMap = new LinkedHashMap<>();
    private Map<String, MultipleImageDetailsWrapper> imagesMap = new HashMap<>();
    private List<Object> checkAndFieldsList = new ArrayList<>();
    private CheckListAdapter checkListAdapter;
    private String remarks;

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @BindView(R.id.rvChecklistTitle)
    RecyclerView rvChecklistTitle;

    @BindView(R.id.btSubmit)
    Button btSubmit;

    @BindView(R.id.progress)
    ProgressBar progress;

    @BindView(R.id.rect)
    LinearLayout rect;
    public DynamicFormActivity(Activity activity, Context c) {
        mContext = c;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_form);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferencesUtility = new SharedPreferencesUtility(this);
        configSelected = (LoginConfigs) getIntent().getSerializableExtra("APP_NAME");

        getSupportActionBar().setTitle(configSelected.name);

        rect.setOnClickListener(null);

        init();

        if (CheckNetwork.isNetworkAvailable(this)) {
            getCheckList();
        } else {
            ToastHelper.showToast(this, "Device is offline, getting checklist from local storage.");
        }

        myReceiver = new MyReceiver();
        // Check that the user hasn't revoked permissions by going to Settings.
        if (LocationUtils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }

    private void init() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvChecklistTitle.setLayoutManager(linearLayoutManager);
        //((SimpleItemAnimator) rvChecklistTitle.getItemAnimator()).setSupportsChangeAnimations(false);
        btSubmit.setOnClickListener(v -> {
            // check if checklist is same and also check if mandatory field are covered or not
            if (checkMandatoryFieldsHasValues() && dynamicCheckListResponse.getChecklistcount() == checklistValuesMap.size()) {
                //if (checkAndFieldsList.size() == checklistValuesMap.size()) {
                showRemarksDialog();
            } else {
                showErrorMessage();
            }
        });
    }

    private boolean checkMandatoryFieldsHasValues() {
        boolean isDataValid = true;
        for (Field field : dynamicCheckListResponse.getFields()) {
            for (FieldValues values : field.getFieldValues()) {
                if (values.getMandatory().equals("1")) {
                    if ((values.getValidation().equals("0") || values.getValidation().equals("1") || values.getValidation().equals("2")
                            || values.getValidation().equals("3") || values.getValidation().equals("4")
                            || values.getValidation().equals("7") || values.getValidation().equals("8"))
                            && (values.getSelectedValue() == null || values.getSelectedValue().isEmpty())) {
                        isDataValid = false;
                        break;
                    } else if (values.getValidation().equals("8") //phone number validation
                            && (values.getSelectedValue() == null || values.getSelectedValue().length() != 10)) {
                        isDataValid = false;
                        break;
                    } else if (values.getValidation().equals("5")
                            && (values.getSelectedTime() == null || values.getSelectedTime().isEmpty())) {
                        //time validation
                        isDataValid = false;
                        break;
                    } else if (values.getValidation().equals("6")
                            && (values.getSelectedTime() == null || values.getSelectedTime().isEmpty()
                            || values.getSelectedValue() == null || values.getSelectedValue().isEmpty())) {
                        //date time validation
                        isDataValid = false;
                        break;
                    }
                }
            }
            if (!isDataValid) {
                break;
            }
        }

        return isDataValid;
    }

    @Override
    public void onBackPressed() {
        showAlertDialog();
    }

    public void showLoadingDialog() {
        rect.setVisibility(View.VISIBLE);
        // progress.showNow();
    }

    public void closeLoadingDialog() {
        rect.setVisibility(View.GONE);
        //progress.hide();
    }

    public void showAlertDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_alert);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvStatusMessage = dialog.findViewById(R.id.tvStatusMessage);
        tvStatusMessage.setText("You may loose your data!");
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        btnYes.setOnClickListener(v -> {
            finish();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void showRemarksDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_remarks);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        dialogMessage.setText("Are you sure to submit report?");
        EditText editTextComments = dialog.findViewById(R.id.editTextComments);
        if (remarks != null && !remarks.isEmpty()) {
            editTextComments.setText(remarks);
        }
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        btnYes.setOnClickListener(v -> {
            remarks = editTextComments.getText().toString();
            if (remarks.isEmpty()) {
                ToastHelper.showToast(DynamicFormActivity.this, "Please enter the remarks.");
                return;
            }
            dialog.dismiss();
            submitData(remarks);
        });

        btnNo.setOnClickListener(v -> {
            remarks = editTextComments.getText().toString();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showErrorMessage() {
        StringBuilder message = new StringBuilder("Please provide details for \n");
        for (Object data : checkAndFieldsList) {
            boolean foundMissingField = false;
            int count = 0;

            if (data instanceof Checklist) {
                Checklist checklist = (Checklist) data;
                for (Rule rule : checklist.getRules()) {
                    count++;
                    if (!checklistValuesMap.containsKey(rule.getRuleId())) {
                        message.append(((Checklist) data).getChecklistName()).append(" > \n").append(count).append(". ").append(rule.getRuleName());
                        foundMissingField = true;
                        break;
                    }
                }
            } else if (data instanceof Field) {
                //check if mandatory fields has values
                Field field = (Field) data;
                for (FieldValues values : field.getFieldValues()) {
                    count++;
                    if (values.getMandatory().equals("1")) {
                        if ((values.getValidation().equals("0") || values.getValidation().equals("1") || values.getValidation().equals("2")
                                || values.getValidation().equals("3") || values.getValidation().equals("4")
                                || values.getValidation().equals("7"))
                                && (values.getSelectedValue() != null && !values.getSelectedValue().isEmpty())) {
                            continue;
                        } else if (values.getValidation().equals("8") && values.getSelectedValue() != null && values.getSelectedValue().length() == 10) {
                            //phone number validation
                            continue;
                        } else if (values.getValidation().equals("5") && values.getSelectedTime() != null && !values.getSelectedTime().isEmpty()) {
                            //time validation
                            continue;
                        } else if (values.getValidation().equals("6") && values.getSelectedTime() != null && !values.getSelectedTime().isEmpty()
                                && values.getSelectedValue() != null && !values.getSelectedValue().isEmpty()) {
                            //date time validation
                            continue;
                        }
                        message.append(field.getHeaderName()).append(" > \n").append(count).append(". ").append(values.getFieldName());

                        if (values.getValidation().equals("6")) {
                            message.append("\n\n").append("Please provide values for both date & time.");
                        } else if (values.getValidation().equals("5")) {
                            message.append("\n\n").append("Please select the time.");
                        } else if (values.getValidation().equals("8")) {
                            message.append("\n\n").append("Please enter a valid 10 digit phone number.");
                        } else if (values.getFieldType().equals("3") && values.getValidation().equals("1")) {
                            message.append("\n\n").append("Please select the value from autocomplete. Random values are not allowed for this field.");
                        } else {
                            message.append("\n\n").append("Field is compulsory, please provide valid input.");
                        }

                        foundMissingField = true;
                        break;
                    }
                }
            }

            if (foundMissingField) {
                break;
            }
        }
        showErrorMessageDialog(message.toString());
    }

    public void showErrorMessageDialog(String message) {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_error_alert);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView tvStatusMessage = dialog.findViewById(R.id.tvStatusMessage);
        tvStatusMessage.setText(message);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        btnYes.setText("OK");
        Button btnNo = dialog.findViewById(R.id.btnNo);
        btnNo.setVisibility(View.GONE);
        btnYes.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCheckList() {
        showLoadingDialog();
        CheckListRequest checkListRequest = new CheckListRequest();
        checkListRequest.setSessionid(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID));
        checkListRequest.setAppId(configSelected.appId);
        checkListRequest.setImei(getIMEI());

        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        service.getDynamicChecklist(checkListRequest).enqueue(new Callback<DynamicCheckListResponse>() {
            @Override
            public void onResponse(Call<DynamicCheckListResponse> call, Response<DynamicCheckListResponse> response) {
                closeLoadingDialog();
                if (response.code() == 200) {
                    loadChecklist(Objects.requireNonNull(response.body()));
                }
            }

            @Override
            public void onFailure(Call<DynamicCheckListResponse> call, Throwable t) {
                Log.d("Login Fail ", t.getMessage());
                closeLoadingDialog();
            }
        });
    }

    private void loadChecklist(DynamicCheckListResponse dynamicCheckListResponse) {
        this.dynamicCheckListResponse = dynamicCheckListResponse; //stored for validation

        checkAndFieldsList.addAll(dynamicCheckListResponse.getFields());
        checkAndFieldsList.addAll(dynamicCheckListResponse.getChecklists());

        checkListAdapter = new CheckListAdapter();
        rvChecklistTitle.setAdapter(checkListAdapter);
    }

    class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {

        private LayoutInflater layoutInflater;

        CheckListAdapter() {
            layoutInflater = LayoutInflater.from(DynamicFormActivity.this);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = layoutInflater.inflate(R.layout.checklist_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Object data = checkAndFieldsList.get(position);
            CheckListQuestionAdapter checkListQuestionAdapter;
            if (data instanceof Checklist) {
                //check box types
                Checklist checklist = (Checklist) data;
                holder.tvHeading.setText(checklist.getChecklistName());
                holder.rvChecklistQuestion.setLayoutManager(new LinearLayoutManager(DynamicFormActivity.this));
                checkListQuestionAdapter = new CheckListQuestionAdapter(DynamicFormActivity.this, (List) checklist.getRules(), position, imagesMap);
                holder.rvChecklistQuestion.setAdapter(checkListQuestionAdapter);
                if (checklist.getListStatus()) {
                    holder.rvChecklistQuestion.setVisibility(View.GONE);
                } else {
                    holder.rvChecklistQuestion.setVisibility(View.VISIBLE);
                }
                holder.tvHeading.setOnClickListener(v -> {
                    if (checklist.getListStatus()) {
                        updateCollapse(position, false);
                    } else {
                        updateCollapse(position, true);
                    }
                });
            } else if (data instanceof Field) {
                Field field = (Field) data;
                holder.tvHeading.setText(field.getHeaderName());
                holder.rvChecklistQuestion.setLayoutManager(new LinearLayoutManager(DynamicFormActivity.this));
                checkListQuestionAdapter = new CheckListQuestionAdapter(DynamicFormActivity.this, (List) field.getFieldValues(), position, imagesMap);
                holder.rvChecklistQuestion.setAdapter(checkListQuestionAdapter);

                if (field.getListStatus()) {
                    holder.rvChecklistQuestion.setVisibility(View.GONE);
                } else {
                    holder.rvChecklistQuestion.setVisibility(View.VISIBLE);
                }
                holder.tvHeading.setOnClickListener(v -> {
                    if (field.getListStatus()) {
                        updateCollapse(position, false);
                    } else {
                        updateCollapse(position, true);
                    }
                });
            }
        }

        //add sub items
        void updateCollapse(int position, Boolean value) {
            for (int i = 0; i < checkAndFieldsList.size(); i++) {
                Object data = checkAndFieldsList.get(i);
                if (data instanceof Checklist) {
                    Checklist checklist = (Checklist) data;
                    if (i == position) {
                        checklist.setListStatus(value);
                    } else {
                        checklist.setListStatus(true);
                    }
                } else {
                    Field field = (Field) data;
                    if (i == position) {
                        field.setListStatus(value);
                    } else {
                        field.setListStatus(true);
                    }
                }
            }
            checkListAdapter.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return checkAndFieldsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tvHeading)
            TextView tvHeading;

            @BindView(R.id.rvChecklistQuestion)
            RecyclerView rvChecklistQuestion;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

    }

    public class CheckListQuestionAdapter extends RecyclerView.Adapter {

        private Context context;
        private List<Object> objectList;
        private LayoutInflater layoutInflater;
        int id = 0;
        int parentPosition;
        private Map<String, MultipleImageDetailsWrapper> imagesMap;

        CheckListQuestionAdapter(Context context, List<Object> items, int position, Map<String, MultipleImageDetailsWrapper> imagesMap) {
            this.context = context;
            this.objectList = items;
            this.imagesMap = imagesMap;
            layoutInflater = LayoutInflater.from(context);
            id = 0;
            this.parentPosition = position;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view;
            switch (viewType) {
                case 0: //radio selection, default
                    view = layoutInflater.inflate(R.layout.question_item_checklist, viewGroup, false);
                    return new CheckListQuestionAdapter.ChecklistViewHolder(view);
                case 1: //text box
                    view = layoutInflater.inflate(R.layout.question_item_textbox, viewGroup, false);
                    return new CheckListQuestionAdapter.TextBoxViewHolder(view);
                case 2: //dropdown
                    view = layoutInflater.inflate(R.layout.question_item_dropdown, viewGroup, false);
                    return new CheckListQuestionAdapter.DropdownViewHolder(view);
                case 3: //autocomplete
                    view = layoutInflater.inflate(R.layout.question_item_autocomplete, viewGroup, false);
                    return new CheckListQuestionAdapter.AutoCompleteViewHolder(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            Object data = objectList.get(position);
            if (data instanceof Rule) {
                return 0;
            } else if (data instanceof FieldValues) {
                FieldValues values = (FieldValues) data;
                switch (values.getFieldType()) {
                    case "1":
                        return 1;
                    case "2":
                        return 2;
                    case "3":
                        return 3;
                    default:
                        return -1;
                }
            } else {
                return -1;
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            Object data = objectList.get(position);

            if (data instanceof Rule) {
                Rule rule = (Rule) data;
                id = id + 1;
                ((ChecklistViewHolder) holder).tvQuestion.setText(id + ". " + rule.getRuleName());
                ((ChecklistViewHolder) holder).tvConf.setText(rule.getCoefficient());

                try {
                    if (imagesMap.containsKey(rule.getRuleId()) && imagesMap.get(rule.getRuleId()) != null && imagesMap.get(rule.getRuleId()).getImages() != null) {
                        ((ChecklistViewHolder) holder).textViewImageCount.setText(imagesMap.get(rule.getRuleId()).getImages().size() + "");
                    } else {
                        ((ChecklistViewHolder) holder).textViewImageCount.setText("0");
                    }
                } catch (Exception e) {
                    Log.e("TAG", "onBindViewHolder: ", e);
                }

                if (!rule.getImagePath().equals("")) {
                    Glide.with(context).load(rule.getImagePath()).into(((ChecklistViewHolder) holder).imUploadImage);
                }

                ((ChecklistViewHolder) holder).imUploadImage.setFocusable(true);

                if (!rule.getOption().equals("")) {

                    if (rule.getOption().equals(rule.getOptions().get(0).getScore())) {
                        ((ChecklistViewHolder) holder).rbOption1.setChecked(true);
                        int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(0).getScore());
                        ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                        ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));
                    } else if (rule.getOption().equals(rule.getOptions().get(1).getScore())) {
                        ((ChecklistViewHolder) holder).rbOption2.setChecked(true);
                        // int score=Integer.parseInt(rule.getCoefficient())*2;
                        int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(1).getScore());
                        ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                        ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));

                    } else if (rule.getOption().equals(rule.getOptions().get(2).getScore())) {
                        ((ChecklistViewHolder) holder).rbOption3.setChecked(true);
                        // int score=Integer.parseInt(rule.getCoefficient());
                        int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(2).getScore());
                        ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                        ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));

                    } else if (rule.getOption().equals(rule.getOptions().get(3).getScore())) {
                        ((ChecklistViewHolder) holder).rbOption4.setChecked(true);
                        int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(rule.getOptions().size() - 1).getScore());
                        // int score= 0;
                        ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                        ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));
                    }

                }

                if (!rule.getRemark().equals("")) {
                    ((ChecklistViewHolder) holder).textViewRemarkHeading.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).tvRemark.setText(rule.getRemark());
                } else {
                    ((ChecklistViewHolder) holder).textViewRemarkHeading.setVisibility(View.GONE);
                }

                if (rule.getOptions().size() == 1) {
                    ((ChecklistViewHolder) holder).rbOption1.setText(rule.getOptions().get(0).getOptionName());
                } else if (rule.getOptions().size() == 2) {
                    ((ChecklistViewHolder) holder).rbOption2.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).rbOption1.setText(rule.getOptions().get(0).getOptionName());
                    ((ChecklistViewHolder) holder).rbOption2.setText(rule.getOptions().get(1).getOptionName());
                } else if (rule.getOptions().size() == 3) {
                    ((ChecklistViewHolder) holder).rbOption2.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).rbOption3.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).rbOption1.setText(rule.getOptions().get(0).getOptionName());
                    ((ChecklistViewHolder) holder).rbOption2.setText(rule.getOptions().get(1).getOptionName());
                    ((ChecklistViewHolder) holder).rbOption3.setText(rule.getOptions().get(2).getOptionName());
                } else if (rule.getOptions().size() == 4) {
                    ((ChecklistViewHolder) holder).rbOption2.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).rbOption3.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).rbOption4.setVisibility(View.VISIBLE);
                    ((ChecklistViewHolder) holder).rbOption1.setText(rule.getOptions().get(0).getOptionName());
                    ((ChecklistViewHolder) holder).rbOption2.setText(rule.getOptions().get(1).getOptionName());
                    ((ChecklistViewHolder) holder).rbOption3.setText(rule.getOptions().get(2).getOptionName());
                    ((ChecklistViewHolder) holder).rbOption4.setText(rule.getOptions().get(3).getOptionName());
                }

                ((ChecklistViewHolder) holder).rbOption1.setOnClickListener(v -> {
                    int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(0).getScore());
                    ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                    ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));
                    score = 3;
                    updateChecklistData(position, parentPosition, rule.getRuleId(), null, null, rule.getOptions().get(0).getScore());
                });

                ((ChecklistViewHolder) holder).rbOption2.setOnClickListener(v -> {
                    int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(1).getScore());
                    ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                    ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));
                    updateChecklistData(position, parentPosition, rule.getRuleId(), null, null, rule.getOptions().get(1).getScore());
                });

                ((ChecklistViewHolder) holder).rbOption3.setOnClickListener(v -> {
                    int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(2).getScore());
                    ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                    ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));
                    updateChecklistData(position, parentPosition, rule.getRuleId(), null, null, rule.getOptions().get(2).getScore());
                });

                ((ChecklistViewHolder) holder).rbOption4.setOnClickListener(v -> {
                    int score = Integer.parseInt(rule.getCoefficient()) * Integer.parseInt(rule.getOptions().get(3).getScore());
                    ((ChecklistViewHolder) holder).tvScore.setText(String.format("%d", score));
                    ((ChecklistViewHolder) holder).tvQuestion.setTextColor(context.getResources().getColor(R.color.done_ckeck));
                    updateChecklistData(position, parentPosition, rule.getRuleId(), null, null, rule.getOptions().get(3).getScore());
                });

                ((ChecklistViewHolder) holder).linearLayoutAttachPhotos.setOnClickListener(v -> {
                    //multiple image selection box
                    Intent intent = new Intent(context, ImageSelectorActivity.class);
                    intent.putExtra(PARENT_POSITION, parentPosition);
                    intent.putExtra(POSITION, position);
                    String ruleId = rule.getRuleId();
                    intent.putExtra(RULE_ID, ruleId);
                    intent.putExtra(REMARK, rule.getRemark());
                    intent.putExtra("APP_NAME", configSelected.name);
                    if (imagesMap.get(ruleId) != null) {
                        intent.putExtra(IMAGE_LIST, imagesMap.get(ruleId).getImages());
                    }
                    startActivityForResult(intent, GET_SELECTED_IMAGES_REQUEST);
                });

                ((ChecklistViewHolder) holder).tvRemark.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        int position = holder.getAdapterPosition();
                        updateChecklistData(position, parentPosition, rule.getRuleId(), s.toString(), null, null);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

            } else if (data instanceof FieldValues) {
                /* Type
                1	TextBox
                2	DropDownList
                3	Autocomplete
                */
                FieldValues values = (FieldValues) data;
                id = id + 1;
                switch (values.getFieldType()) {
                    case "1":
                        ((TextBoxViewHolder) holder).layoutEditDetails.setVisibility(View.VISIBLE);
                        ((TextBoxViewHolder) holder).editText.setVisibility(View.VISIBLE);
                        ((TextBoxViewHolder) holder).textViewDatePicker.setVisibility(View.GONE);

                        ((TextBoxViewHolder) holder).layoutTimePicker.setVisibility(View.GONE);
                        //((TextBoxViewHolder) holder).textViewTimePicker.setVisibility(View.GONE);
                        //((TextBoxViewHolder) holder).titleTimePicker.setVisibility(View.GONE);

                        applyTextBoxValidation(values, id,
                                ((TextBoxViewHolder) holder).layoutEditDetails,
                                ((TextBoxViewHolder) holder).titleEditText,
                                ((TextBoxViewHolder) holder).editText,
                                ((TextBoxViewHolder) holder).textViewDatePicker,

                                ((TextBoxViewHolder) holder).layoutTimePicker,
                                ((TextBoxViewHolder) holder).titleTimePicker,
                                ((TextBoxViewHolder) holder).textViewTimePicker);
                        break;
                    case "2":
                        ((DropdownViewHolder) holder).title.setText(String.format("%d. %s", id, values.getFieldName()));
                        getAutocompleteDropdownValues(values, ((DropdownViewHolder) holder).spinner, null);
                        break;
                    case "3":
                        ((AutoCompleteViewHolder) holder).title.setText(String.format("%d. %s", id, values.getFieldName()));
                        ((AutoCompleteViewHolder) holder).linearLayoutAutocompleteData.setVisibility(View.GONE);
                        getAutocompleteDropdownValues(values, ((AutoCompleteViewHolder) holder).autoComplete,
                                ((AutoCompleteViewHolder) holder).linearLayoutAutocompleteData);
                        break;
                }
            }

        }

        @Override
        public int getItemCount() {
            return objectList.size();
        }

        class ChecklistViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tvQuestion)
            TextView tvQuestion;
            @BindView(R.id.tvRemark)
            TextView tvRemark;
            @BindView(R.id.textViewRemarkHeading)
            TextView textViewRemarkHeading;
            @BindView(R.id.tvConf)
            TextView tvConf;
            @BindView(R.id.tvScore)
            TextView tvScore;
            @BindView(R.id.rbOption1)
            RadioButton rbOption1;
            @BindView(R.id.rbOption2)
            RadioButton rbOption2;
            @BindView(R.id.rbOption3)
            RadioButton rbOption3;
            @BindView(R.id.rbOption4)
            RadioButton rbOption4;
            @BindView(R.id.rgRadoGroup)
            RadioGroup rgRadoGroup;
            @BindView(R.id.imUploadImage)
            ImageView imUploadImage;
            @BindView(R.id.textViewImageCount)
            TextView textViewImageCount;

            @BindView(R.id.linearLayoutAttachPhotos)
            LinearLayout linearLayoutAttachPhotos;

            public ChecklistViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }

        class TextBoxViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.titleEditText)
            TextView titleEditText;

            @BindView(R.id.editText)
            EditText editText;

            @BindView(R.id.textViewDatePicker)
            TextView textViewDatePicker;

            @BindView(R.id.textViewTimePicker)
            TextView textViewTimePicker;

            @BindView(R.id.titleTimePicker)
            TextView titleTimePicker;

            @BindView(R.id.layoutTimePicker)
            FrameLayout layoutTimePicker;

            @BindView(R.id.layoutEditDetails)
            FrameLayout layoutEditDetails;

            public TextBoxViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class DropdownViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.spinner)
            AppCompatSpinner spinner;

            public DropdownViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class AutoCompleteViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.autoComplete)
            AppCompatAutoCompleteTextView autoComplete;
            @BindView(R.id.linearLayoutAutocompleteData)
            LinearLayout linearLayoutAutocompleteData;

            public AutoCompleteViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

    }

    private void getAutocompleteDropdownValues(FieldValues fieldValues, View view, LinearLayout linearLayoutAutocompleteData) {
        //showLoadingDialog();
        AutocompleteDropdownValuesRequest request = new AutocompleteDropdownValuesRequest();
        request.setSessionId(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID));
        request.setAppId(configSelected.appId);
        request.setFieldId(fieldValues.getId());
        request.setImei(getIMEI());

        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        service.getVetDropDownList(request).enqueue(new Callback<List<AutocompleteDropdownResponse>>() {
            @Override
            public void onResponse(Call<List<AutocompleteDropdownResponse>> call, Response<List<AutocompleteDropdownResponse>> response) {
                //closeLoadingDialog();
                if (response.code() == 200) {
                    List<AutocompleteDropdownResponse> values = response.body();
                    //set the list to autocomplete or spinner
                    if (view instanceof AppCompatSpinner) {
                        applySpinnerValidation((AppCompatSpinner) view, fieldValues, values);
                    } else if (view instanceof AppCompatAutoCompleteTextView) {
                        applyAutoCompleteValidation((AppCompatAutoCompleteTextView) view, fieldValues, values, linearLayoutAutocompleteData);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AutocompleteDropdownResponse>> call, Throwable t) {
                Log.d("Login Fail ", t.getMessage());
                //closeLoadingDialog();
            }
        });
    }

    /* Validation
    1	Name
    2	Numeric
    3	AlphaNumeric
    4	Date
    5	Time
    6	DateTime
    7	SplChars(-,/)
    8	PhoneNo
    */
    @SuppressLint("DefaultLocale")
    private void applyTextBoxValidation(FieldValues values, int id,
                                        FrameLayout layoutEditDetails, TextView titleEditText, EditText editText, TextView textViewDatePicker,
                                        FrameLayout layoutTimePicker, TextView titleTimePicker, TextView textViewTimePicker) {
        switch (values.getValidation()) {
            case "1": //Name
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText(values.getSelectedValue());
                editText.setFilters(new InputFilter[]{nameFilter});
                break;
            case "2": //Numeric
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setText(values.getSelectedValue());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "3": //AlphaNumeric
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setText(values.getSelectedValue());
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setFilters(new InputFilter[]{alphaNumericFilter});
                break;
            case "4": //Date
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setVisibility(View.GONE);
                textViewDatePicker.setText(values.getSelectedValue());
                textViewDatePicker.setVisibility(View.VISIBLE);
                textViewDatePicker.setHint("YYYY-MM-DD");
                textViewDatePicker.setOnClickListener(v -> {
                    showDatePickerDialog(textViewDatePicker, values);
                });
                break;
            case "5": //Time
                titleTimePicker.setText(String.format("%d. %s", id, values.getFieldName()));
                layoutEditDetails.setVisibility(View.GONE);
                layoutTimePicker.setVisibility(View.VISIBLE);
                textViewTimePicker.setText(values.getSelectedTime());
                textViewTimePicker.setOnClickListener(v -> {
                    showTimePicker(textViewTimePicker, values);
                });
                break;
            case "6": //DateTime
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setVisibility(View.GONE);
                textViewDatePicker.setText(values.getSelectedValue());
                textViewDatePicker.setVisibility(View.VISIBLE);
                textViewDatePicker.setHint("YYYY-MM-DD");
                textViewDatePicker.setOnClickListener(v -> {
                    showDatePickerDialog(textViewDatePicker, values);
                });

                layoutTimePicker.setVisibility(View.VISIBLE);
                textViewTimePicker.setText(values.getSelectedTime());
                textViewTimePicker.setOnClickListener(v -> {
                    showTimePicker(textViewTimePicker, values);
                });
                break;
            case "7": //SplChars
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setText(values.getSelectedValue());
                editText.setHint("Only alpha-numeric and " + values.getValue() + " characters are allowed");
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                //regEx = "[a-zA-Z 0-9" + values.getValue() + "]+"; //[a-zA-Z 0-9]+
                editText.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
                        if (src.equals("")) { // for backspace
                            return src;
                        }
                        if (src.toString().matches("[a-zA-Z 0-9" + values.getValue() + "]+")) {
                            return src;
                        }
                        return "";
                    }
                }});
                break;
            case "8": //PhoneNo
                titleEditText.setText(String.format("%d. %s", id, values.getFieldName()));
                editText.setText(values.getSelectedValue());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
                editText.setFilters(new InputFilter[]{numericFilter, new LengthFilter(10)});
                break;
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //store values to storage
                values.setSelectedValue(editable.toString());
            }
        });
    }

    private InputFilter nameFilter = (src, start, end, dst, dstart, dend) -> {
        if (src.equals("")) { // for backspace
            return src;
        }
        if (src.toString().matches("[a-zA-Z ]+")) {
            return src;
        }
        return "";
    };

    private InputFilter alphaNumericFilter = (src, start, end, dst, dstart, dend) -> {
        if (src.equals("")) { // for backspace
            return src;
        }
        if (src.toString().matches("[a-zA-Z 0-9]+")) {
            return src;
        }
        return "";
    };

    private InputFilter numericFilter = (src, start, end, dst, dstart, dend) -> {
        if (src.equals("")) { // for backspace
            return src;
        }
        if (src.toString().matches("[ 0-9]+")) {
            return src;
        }
        return "";
    };

    private void applyAutoCompleteValidation(AppCompatAutoCompleteTextView autoComplete, FieldValues fieldValues, List<AutocompleteDropdownResponse> values, LinearLayout linearLayoutAutocompleteData) {

        AutocompleteAdapter adapter = null;
        if (values != null && values.size() > 0) {
            adapter = new AutocompleteAdapter(this, R.layout.item_autocomplete, values);
        }
        autoComplete.setAdapter(adapter);

        //updated this to allow selection of first options in list or only option is list
        if (fieldValues.getSelection() >= 0 && fieldValues.getSelectedValue() != null) {
            autoComplete.setText(fieldValues.getSelectedValue());
        }

        if (fieldValues.getHashMap() != null) {
            linearLayoutAutocompleteData.setVisibility(View.VISIBLE);
            showDynamicValues(fieldValues.getHashMap(), linearLayoutAutocompleteData);
        } else {
            linearLayoutAutocompleteData.removeAllViews();
            linearLayoutAutocompleteData.setVisibility(View.GONE);
        }

        if (fieldValues.getValidation().equals("1")) {
            autoComplete.setTag(AUTOCOMPLETE_TAG);
        } else {
            autoComplete.setTag(fieldValues.getFieldName());
        }

        autoComplete.setInputType(InputType.TYPE_CLASS_TEXT);
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //if validation is 1 then do not allow user to type the value
                if (!autoComplete.getTag().equals(AUTOCOMPLETE_TAG)) {
                    fieldValues.setSelectedValue(editable.toString());
                } else {
                    fieldValues.setSelectedValue(null);
                    linearLayoutAutocompleteData.removeAllViews();
                    linearLayoutAutocompleteData.setVisibility(View.GONE);
                }
            }
        });

        autoComplete.setOnItemClickListener((adapterView, view, pos, l) -> {
            AutocompleteDropdownResponse selected = (AutocompleteDropdownResponse) adapterView.getItemAtPosition(pos);
            fieldValues.setSelectedValue(selected.getValue());
            fieldValues.setSelection(pos);
            if (autoComplete.getTag().equals(AUTOCOMPLETE_TAG)) {
                //call web service to get the data and show in dialog box
                getDataForAutocompleteSelection(selected.getValue(), fieldValues, linearLayoutAutocompleteData);
            }
        });

        //to hide the autocomplete box after setting the text
        autoComplete.dismissDropDown();
    }

    private void applySpinnerValidation(AppCompatSpinner spinner, FieldValues fieldValues, List<AutocompleteDropdownResponse> values) {
        ArrayAdapter adapter = null;
        if (values != null && values.size() > 0) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);
        } else if (fieldValues.getValue() != null) {
            List<String> items = Arrays.asList(fieldValues.getValue().split(","));
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        }

        spinner.setAdapter(adapter);
        spinner.setSelection(fieldValues.getSelection());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Object selected = adapterView.getItemAtPosition(pos);
                if (selected instanceof String) {
                    fieldValues.setSelectedValue((String) selected);
                } else if (selected instanceof AutocompleteDropdownResponse) {
                    fieldValues.setSelectedValue(((AutocompleteDropdownResponse) selected).getValue());
                }
                fieldValues.setSelection(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void showDatePickerDialog(TextView textViewDate, FieldValues values) {
        DialogFragment newFragment = new DatePickerFragment(this, textViewDate, values);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private TextView textViewDate;
        private Context context;
        private FieldValues values;

        DatePickerFragment(Context context, TextView textViewDate, FieldValues values) {
            this.textViewDate = textViewDate;
            this.context = context;
            this.values = values;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, this, year, month, day);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 0);

            // Create a new instance of DatePickerDialog and return it
            return datePickerDialog;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            //String date = String.format("%d-%d-%d", year, (month + 1), day);
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            String date = format.format(calendar.getTime());
            textViewDate.setText(date);
            values.setSelectedValue(date);
        }

    }

    @SuppressLint("DefaultLocale")
    public void showTimePicker(TextView textViewTime, FieldValues values) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) ->
        {
            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
            textViewTime.setText(time);
            values.setSelectedTime(time);
        }, hour, minute, true); //Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void updateChecklistData(int position, int parentPosition, String ruleId, String remark, ArrayList<String> photos, String score) {
        try {
            if (checklistValuesMap.containsKey(ruleId)) {
                ChecklistValues checklistValues = checklistValuesMap.get(ruleId);
                if (remark != null) checklistValues.setComments(remark);
                if (photos != null && !photos.isEmpty()) checklistValues.setPhotos(photos);
                if (score != null) checklistValues.setScore(score);
            } else {
                ChecklistValues checklistValues = new ChecklistValues();
                checklistValues.setRuleId(ruleId);
                if (remark != null) checklistValues.setComments(remark);
                if (photos != null && !photos.isEmpty()) checklistValues.setPhotos(photos);
                if (score != null) checklistValues.setScore(score);
                checklistValuesMap.put(ruleId, checklistValues);
            }

            Rule rule = ((Checklist) checkAndFieldsList.get(parentPosition)).getRules().get(position);
            if (remark != null) rule.setRemark(remark);
            if (score != null) rule.setOption(score);

        } catch (Exception e) {
            Log.e(TAG, "updateChecklistData: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK && requestCode == GET_SELECTED_IMAGES_REQUEST) {
                if (data != null) {
                    int position = data.getIntExtra(POSITION, 0);
                    int parentPosition = data.getIntExtra(PARENT_POSITION, 0);
                    String ruleId = data.getStringExtra(RULE_ID);
                    ArrayList<ImageDetails> imageDetailsList = (ArrayList<ImageDetails>) data.getSerializableExtra(IMAGE_LIST);

                    //add images to images list
                    if (imagesMap.containsKey(ruleId)) {
                        imagesMap.get(ruleId).setImages(imageDetailsList);
                    } else {
                        MultipleImageDetailsWrapper multipleImageDetailsWrapper = new MultipleImageDetailsWrapper();
                        multipleImageDetailsWrapper.setId(ruleId);
                        multipleImageDetailsWrapper.setImages(imageDetailsList);
                        imagesMap.put(ruleId, multipleImageDetailsWrapper);
                    }

                    String remark = data.getStringExtra(REMARK);
                    String score = null;
                    ArrayList<String> photos = new ArrayList<>();
                    for (ImageDetails imageDetail : imageDetailsList) {
                        photos.add(imageDetail.getFileName());
                    }
                    //add images to checklist
                    updateChecklistData(position, parentPosition, ruleId, remark, photos, score);

                    //update the adapter, refresh the images count
                    checkListAdapter.notifyItemChanged(parentPosition);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void submitData(String remarks) {
        showLoadingDialog();
        //String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        //if (provider.contains("network") || provider.contains("gps")) {
        if(isLocationAvailable()){
            if (checkMandatoryFieldsHasValues() && dynamicCheckListResponse.getChecklistcount() == checklistValuesMap.size()) {
                StaticDetails staticDetails = new StaticDetails();
                staticDetails.setLatitude(finalLat);
                staticDetails.setLongitude(finalLong);
                staticDetails.setSummary(remarks);
                staticDetails.setDateTime(getDate());
                staticDetails.setAppId(configSelected.appId);
                //used just for identification and while saving data
                staticDetails.setAppName(configSelected.name);
                staticDetails.setImei(getIMEI());
                //create a request object based on user selection
                List<FieldValueRequest> fieldValues = new ArrayList<>();
                for (Field field : dynamicCheckListResponse.getFields()) {
                    for (FieldValues fieldValue : field.getFieldValues()) {
                        if (fieldValue.getValidation().equals("6")) { //datetime
                            fieldValues.add(new FieldValueRequest(fieldValue.getId(), fieldValue.getSelectedValue() + " " + fieldValue.getSelectedTime()));
                        } else if (fieldValue.getValidation().equals("5")) { //time
                            fieldValues.add(new FieldValueRequest(fieldValue.getId(), fieldValue.getSelectedTime()));
                        } else { //all others
                            fieldValues.add(new FieldValueRequest(fieldValue.getId(), fieldValue.getSelectedValue()));
                        }
                    }
                }

                //so add this data to DB and later sync it to server
                VehicleVetting vehicleVetting = new VehicleVetting();
                vehicleVetting.staticDetails = staticDetails;
                vehicleVetting.checklistValues = new ArrayList<>(checklistValuesMap.values());
                vehicleVetting.imageList = new ArrayList<>(imagesMap.values());
                vehicleVetting.fieldValues = fieldValues;
                vehicleVetting.sessionid = sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID);
                vehicleVetting.isUploaded = false;
                vehicleVetting.appName = configSelected.getName();

                //Save files data to DB
                AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
                database.vehicleVettingModel().insertVetting(vehicleVetting);

                closeLoadingDialog();
                ToastHelper.showToast(this, "Data stored in local storage successfully, will be synced to server when internet connection is available.");
//                Intent i = new Intent(this, ListDashboardActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
                finish();
            } else {
                closeLoadingDialog();
                showErrorMessage();
            }
        } else {
            closeLoadingDialog();
            ToastHelper.showToast(this, "Please Turn On GPS");
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private String getDate() {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime nowDate = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return nowDate.format(formatter);
        } else {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(new Date());
        }
    }

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            //start the update service
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                mService.requestLocationUpdates();
            }
            // mService.removeLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(findViewById(R.id.rootLayout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, (View.OnClickListener) view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(DynamicFormActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(DynamicFormActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                Snackbar.make(findViewById(R.id.rootLayout),
                                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }).show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                finalLat = String.valueOf(location.getLatitude());
                finalLong = String.valueOf(location.getLongitude());
                //Toast.makeText(DynamicFormActivity.this, LocationUtils.getLocationText(location), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getDataForAutocompleteSelection(String value, FieldValues fieldValues, LinearLayout linearLayoutAutocompleteData) {
        showLoadingDialog();
        AutocompleteDropdownValuesRequest request = new AutocompleteDropdownValuesRequest();
        request.setSessionId(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID));
        request.setAppId(configSelected.appId);
        request.setFieldId(fieldValues.getId());
        request.setValue(value);
        request.setImei(getIMEI());

        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        service.getVetMainDetails(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                closeLoadingDialog();
                if (response.code() == 200) {
                    String values = response.body();
                    if (values != null && !values.isEmpty()) {
                        Type type = new TypeToken<HashMap<String, String>>() {
                        }.getType();
                        HashMap<String, String> hashMap = new Gson().fromJson(values, type);
                        if (hashMap.size() > 0) {
                            fieldValues.setHashMap(hashMap);
                            showDynamicValues(hashMap, linearLayoutAutocompleteData);
                            /*LayoutInflater inflater = LayoutInflater.from(DynamicFormActivity.this);
                            for (Map.Entry entry : hashMap.entrySet()) {
                                //create the custom view
                                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_report_dynamic_view, linearLayoutAutocompleteData, false);
                                TextView textView = layout.findViewById(R.id.textViewTitle);
                                textView.setText(entry.getKey().toString());
                                TextView textViewData = layout.findViewById(R.id.textViewData);
                                textViewData.setText(entry.getValue().toString());
                                linearLayoutAutocompleteData.addView(layout);
                            }*/
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Login Fail ", t.getMessage());
                closeLoadingDialog();
            }
        });
    }

    private void showDynamicValues(HashMap<String, String> hashMap, LinearLayout linearLayoutAutocompleteData) {
        linearLayoutAutocompleteData.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(DynamicFormActivity.this);
        for (Map.Entry entry : hashMap.entrySet()) {
            //create the custom view
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_report_dynamic_view, linearLayoutAutocompleteData, false);
            TextView textView = layout.findViewById(R.id.textViewTitle);
            textView.setText(entry.getKey().toString());
            TextView textViewData = layout.findViewById(R.id.textViewData);
            textViewData.setText(entry.getValue().toString());
            linearLayoutAutocompleteData.addView(layout);
        }
    }

    @SuppressLint("MissingPermission")
    public String getIMEI() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = "";
            if (tm != null) {
                device_id = tm.getDeviceId();
            }
            return device_id;
        } else {
            String android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String device_id = "";
            if (android_id != null) {
                device_id = android_id;
            }
            return device_id;
        }
    }
    public boolean isLocationAvailable() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }
}
