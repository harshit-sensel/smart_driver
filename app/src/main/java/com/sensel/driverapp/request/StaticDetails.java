package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StaticDetails {

    @SerializedName("TankManufacturer")
    @Expose
    private String TankManufacturer = "";

    @SerializedName("TruckMaker")
    @Expose
    private String TruckMaker = "";
    @SerializedName("RegistrationDate")
    @Expose
    private String RegistrationDate = "";
    @SerializedName("VettingDate")
    @Expose
    private String VettingDate = "";
    @SerializedName("DriverName")
    @Expose
    private String DriverName = "";
    @SerializedName("TankNo")
    @Expose
    private String TankNo = "";
    @SerializedName("RearAxleWeight")
    @Expose
    private String RearAxleWeight = "";
    @SerializedName("TankManufacturingDate")
    @Expose
    private String TankManufacturingDate = "";

    @SerializedName("DriverDOB")
    @Expose
    private String DriverDOB = "";

    @SerializedName("TruckLoadedWeight")
    @Expose
    private String TruckLoadedWeight = "";

    @SerializedName("OverloadCombinedGVW")
    @Expose
    private String OverloadCombinedGVW = "";

    @SerializedName("MaxEffectiveLoadWeight")
    @Expose
    private String MaxEffectiveLoadWeight = "";

    @SerializedName("Tare_Empty_Weight")
    @Expose
    private String Tare_Empty_Weight = "";

    @SerializedName("LastHydroTestDate")
    @Expose
    private String LastHydroTestDate = "";

    @SerializedName("TankCapLitres")
    @Expose
    private String TankCapLitres = "";

    @SerializedName("TruckGVW")
    @Expose
    private String TruckGVW = "";

    @SerializedName("LastTechnicianVisit")
    @Expose
    private String LastTechnicianVisit = "";

    @SerializedName("DriverLicense")
    @Expose
    private String DriverLicense = "";

    @SerializedName("VehicleType")
    @Expose
    private String VehicleType = "";

    @SerializedName("VehicleId")
    @Expose
    private String VehicleId = "";

    @SerializedName("CombinedGVW")
    @Expose
    private String CombinedGVW = "";

    @SerializedName("GVWR")
    @Expose
    private String GVWR = "";

    @SerializedName("TankCapKgs")
    @Expose
    private String TankCapKgs = "";

    @SerializedName("TankOverload")
    @Expose
    private String TankOverload = "";

    @SerializedName("EPbyTruckGVW")
    @Expose
    private String EPbyTruckGVW = "";

    @SerializedName("ModelNo")
    @Expose
    private String ModelNo = "";

    @SerializedName("TrailerNo")
    @Expose
    private String TrailerNo = "";

    @SerializedName("TrailerGVW")
    @Expose
    private String TrailerGVW = "";

    @SerializedName("AGVW")
    @Expose
    private String AGVW = "";

    @SerializedName("EnginePower")
    @Expose
    private String EnginePower = "";

    @SerializedName("FrontAxleWeight")
    @Expose
    private String FrontAxleWeight = "";

    @SerializedName("EPbyCombinedGVW")
    @Expose
    private String EPbyCombinedGVW = "";

    @SerializedName("IMEI")
    @Expose
    private String imei;

    @SerializedName("Location")
    @Expose
    private String Location = "";

    @SerializedName("Latitude")
    @Expose
    private String Latitude = "";

    @SerializedName("Longitude")
    @Expose
    private String Longitude = "";

    @SerializedName("Summary")
    @Expose
    private String Summary = "";

    @SerializedName("InspectorName")
    @Expose
    private String InspectorName;

    @SerializedName("DateTime")
    @Expose
    private String DateTime;

    @SerializedName("AppId")
    @Expose
    private String AppId;

    //for internal use only
    @Expose
    private String AppName;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public String getAppId() {
        return AppId;
    }

    public void setAppId(String appId) {
        AppId = appId;
    }

    public String getInspectorName() {
        return InspectorName;
    }

    public void setInspectorName(String inspectorName) {
        InspectorName = inspectorName;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public String getTankManufacturer() {
        return TankManufacturer;
    }

    public void setTankManufacturer(String tankManufacturer) {
        TankManufacturer = tankManufacturer;
    }

    public String getTruckMaker() {
        return TruckMaker;
    }

    public void setTruckMaker(String truckMaker) {
        TruckMaker = truckMaker;
    }

    public String getRegistrationDate() {
        return RegistrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        RegistrationDate = registrationDate;
    }

    public String getVettingDate() {
        return VettingDate;
    }

    public void setVettingDate(String vettingDate) {
        VettingDate = vettingDate;
    }

    public String getDriverName() {
        return DriverName;
    }

    public void setDriverName(String driverName) {
        DriverName = driverName;
    }

    public String getTankNo() {
        return TankNo;
    }

    public void setTankNo(String tankNo) {
        TankNo = tankNo;
    }

    public String getRearAxleWeight() {
        return RearAxleWeight;
    }

    public void setRearAxleWeight(String rearAxleWeight) {
        RearAxleWeight = rearAxleWeight;
    }

    public String getTankManufacturingDate() {
        return TankManufacturingDate;
    }

    public void setTankManufacturingDate(String tankManufacturingDate) {
        TankManufacturingDate = tankManufacturingDate;
    }

    public String getDriverDOB() {
        return DriverDOB;
    }

    public void setDriverDOB(String driverDOB) {
        DriverDOB = driverDOB;
    }

    public String getTruckLoadedWeight() {
        return TruckLoadedWeight;
    }

    public void setTruckLoadedWeight(String truckLoadedWeight) {
        TruckLoadedWeight = truckLoadedWeight;
    }

    public String getOverloadCombinedGVW() {
        return OverloadCombinedGVW;
    }

    public void setOverloadCombinedGVW(String overloadCombinedGVW) {
        OverloadCombinedGVW = overloadCombinedGVW;
    }

    public String getMaxEffectiveLoadWeight() {
        return MaxEffectiveLoadWeight;
    }

    public void setMaxEffectiveLoadWeight(String maxEffectiveLoadWeight) {
        MaxEffectiveLoadWeight = maxEffectiveLoadWeight;
    }

    public String getTare_Empty_Weight() {
        return Tare_Empty_Weight;
    }

    public void setTare_Empty_Weight(String tare_Empty_Weight) {
        Tare_Empty_Weight = tare_Empty_Weight;
    }

    public String getLastHydroTestDate() {
        return LastHydroTestDate;
    }

    public void setLastHydroTestDate(String lastHydroTestDate) {
        LastHydroTestDate = lastHydroTestDate;
    }

    public String getTankCapLitres() {
        return TankCapLitres;
    }

    public void setTankCapLitres(String tankCapLitres) {
        TankCapLitres = tankCapLitres;
    }

    public String getTruckGVW() {
        return TruckGVW;
    }

    public void setTruckGVW(String truckGVW) {
        TruckGVW = truckGVW;
    }

    public String getLastTechnicianVisit() {
        return LastTechnicianVisit;
    }

    public void setLastTechnicianVisit(String lastTechnicianVisit) {
        LastTechnicianVisit = lastTechnicianVisit;
    }

    public String getDriverLicense() {
        return DriverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        DriverLicense = driverLicense;
    }

    public String getVehicleType() {
        return VehicleType;
    }

    public void setVehicleType(String vehicleType) {
        VehicleType = vehicleType;
    }

    public String getVehicleId() {
        return VehicleId;
    }

    public void setVehicleId(String vehicleId) {
        VehicleId = vehicleId;
    }

    public String getCombinedGVW() {
        return CombinedGVW;
    }

    public void setCombinedGVW(String combinedGVW) {
        CombinedGVW = combinedGVW;
    }

    public String getGVWR() {
        return GVWR;
    }

    public void setGVWR(String GVWR) {
        this.GVWR = GVWR;
    }

    public String getTankCapKgs() {
        return TankCapKgs;
    }

    public void setTankCapKgs(String tankCapKgs) {
        TankCapKgs = tankCapKgs;
    }

    public String getTankOverload() {
        return TankOverload;
    }

    public void setTankOverload(String tankOverload) {
        TankOverload = tankOverload;
    }

    public String getEPbyTruckGVW() {
        return EPbyTruckGVW;
    }

    public void setEPbyTruckGVW(String EPbyTruckGVW) {
        this.EPbyTruckGVW = EPbyTruckGVW;
    }

    public String getModelNo() {
        return ModelNo;
    }

    public void setModelNo(String modelNo) {
        ModelNo = modelNo;
    }

    public String getTrailerNo() {
        return TrailerNo;
    }

    public void setTrailerNo(String trailerNo) {
        TrailerNo = trailerNo;
    }

    public String getTrailerGVW() {
        return TrailerGVW;
    }

    public void setTrailerGVW(String trailerGVW) {
        TrailerGVW = trailerGVW;
    }

    public String getAGVW() {
        return AGVW;
    }

    public void setAGVW(String AGVW) {
        this.AGVW = AGVW;
    }

    public String getEnginePower() {
        return EnginePower;
    }

    public void setEnginePower(String enginePower) {
        EnginePower = enginePower;
    }

    public String getFrontAxleWeight() {
        return FrontAxleWeight;
    }

    public void setFrontAxleWeight(String frontAxleWeight) {
        FrontAxleWeight = frontAxleWeight;
    }

    public String getEPbyCombinedGVW() {
        return EPbyCombinedGVW;
    }

    public void setEPbyCombinedGVW(String EPbyCombinedGVW) {
        this.EPbyCombinedGVW = EPbyCombinedGVW;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }


    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }
}
