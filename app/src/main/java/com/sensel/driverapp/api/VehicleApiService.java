package com.sensel.driverapp.api;

import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.request.AppVersionRequest;
import com.sensel.driverapp.request.AutocompleteDropdownValuesRequest;
import com.sensel.driverapp.request.CheckListRequest;
import com.sensel.driverapp.request.FirebaseTokenRequest;
import com.sensel.driverapp.request.LoginRequest;
import com.sensel.driverapp.request.NotificationRequest;
import com.sensel.driverapp.request.SaveRequest;
import com.sensel.driverapp.request.VehicleRequest;
import com.sensel.driverapp.response.AppVersionResponse;
import com.sensel.driverapp.response.AutocompleteDropdownResponse;
import com.sensel.driverapp.response.DynamicCheckListResponse;
import com.sensel.driverapp.response.GetCheckList;
import com.sensel.driverapp.response.GetInsertResponse;
import com.sensel.driverapp.response.GetVehicleInfo;
import com.sensel.driverapp.response.LoginResponse;
import com.sensel.driverapp.response.firebase.FirebaseResponse;
import com.sensel.driverapp.response.notification.NotificationResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface VehicleApiService {

    //public static String BASE_URL = "http://ui.vehicle-tracking.co.in/SenselRestService.svc/rest/v3/";
    String LOGIN_URL = " https://ui.mysensel.com/SenselRestService.svc/rest/v3/";
    // public static String BASE_URL ="https://fleetsmart3.ui.sensel.in//SenselRestService.svc/rest/v3/";

    @Headers({"Content-Type: application/json"})
    @POST("GetLoginDetails")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVettingCheckList")
    Call<GetCheckList> getChecklist(@Body CheckListRequest checkListRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVetCheckList")
    Call<DynamicCheckListResponse> getDynamicChecklist(@Body CheckListRequest checkListRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVetDropDownList")
    Call<List<AutocompleteDropdownResponse>> getVetDropDownList(@Body AutocompleteDropdownValuesRequest request);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVetMainDetails")
    Call<String> getVetMainDetails(@Body AutocompleteDropdownValuesRequest request);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVetLoginConfig")
    Call<List<LoginConfigs>> getLoginConfig(@Body CheckListRequest checkListRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/insertVettingCheckList")
    Call<GetInsertResponse> saveCompleteData(@Body SaveRequest saveRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/insertVetDetails")
    Call<GetInsertResponse> saveVetDetails(@Body SaveRequest saveRequest);


    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVehicles")
    Call<GetVehicleInfo> getVehicleInfo(@Body VehicleRequest vehicleRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetVettingNotificationsList")
    Call<NotificationResponse> getNotification(@Body NotificationRequest notificationRequest);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/insertFirebaseToken")
    Call<FirebaseResponse> setNotification(@Body FirebaseTokenRequest firebaseTokenRequest);

    @Multipart
    @POST("SenselRestService.svc/rest/v3/UploadFile")
    Call<String> setImages(@Header("sessionid") String sessionid, @Header("fileName") String fileName, @Part MultipartBody.Part filedata);

    @Multipart
    @POST("SenselRestService.svc/rest/UploadFile")
    Call<String> setdemoImages(@Part MultipartBody.Part filedata);

    @Headers({"Content-Type: application/json"})
    @POST("SenselRestService.svc/rest/v3/GetAppCurrentVersion")
    Call<AppVersionResponse> getAppCurrentVersion(@Body AppVersionRequest appVersionRequest);


}

