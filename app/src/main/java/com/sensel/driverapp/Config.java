package com.sensel.driverapp;

/**
 * Created by User on 01-12-2016.
 */

public class Config {

    public static String DOMAINURL1 = "https://fleetsmart3.ui.sensel.in";
    public static String DOMAINURL2 = "http://fleetsmart3.ui.sensel.in";
    public static String DOMAINURL3 = "http://54.164.125.228:8080";
    public static String DOMAINURL4 = "http://demo.fleetsmart.in";

    // File upload url (replace the ip with your server address)
    public static final String FILE_UPLOAD_URL_VOLLEY = DOMAINURL1+"/SenselRestService.svc/rest/v3/";
    public static final String FILE_UPLOAD_URL_VOLLEY1 = DOMAINURL3+"/SenselRestService.svc/rest/";

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "DriverApp";

    public static final String ADMINPANELHTTPSURL = "https://adminpanel.mysensel.com/Forms/Service.asmx";
    public static final String ADMINPANELHTTPURL = "http://adminpanel.mysensel.com/Forms/Service.asmx";


    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME_CHECKLIST = "Checklist";

    public static final String IMAGE_DIRECTORY_NAME_UNLOAD_QTY = "Unload_Qty";
}
