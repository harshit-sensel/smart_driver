package com.sensel.driverapp;
import android.content.Context;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Mallikarjun on 11-10-2015.
 */
public class WebserviceCall {

    public String SOAP_ACTION = "http://tempuri.org/SendSMS";
    public String OPERATION_NAME = "SendSMS";
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    SoapObject request = null, objMessages = null;
    SoapSerializationEnvelope envelope;
    Context context;
    WebserviceCall() {

    }

    public  String Getsessionid(String deviceid){
        SOAP_ACTION = "http://tempuri.org/GetSessionIDByIMEI";
        OPERATION_NAME = "GetSessionIDByIMEI ";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    // MethodName variable is define for which webservice function  will call
    public String IsBirthday(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/IsBirthday";
        OPERATION_NAME = "IsBirthday";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEInumber");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }
    public String CheckIMEI(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/CheckIMEI";
        OPERATION_NAME = "CheckIMEI";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEInumber");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.ADMINPANELHTTPSURL,Config.ADMINPANELHTTPURL,OPERATION_NAME,piarray,SOAP_ACTION);
    }


    public String CheckIMEIForNonTotal(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/CheckIMEIForNonTotal";
        OPERATION_NAME = "CheckIMEIForNonTotal";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEInumber");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //For Driver incentives
    public String GetDriver_Incentives(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/GetDriverpoints";
        OPERATION_NAME = "GetDriverpoints";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //For Driver incentives
    public String GetTransporter_Incentives(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/GetTransportorpoints";
        OPERATION_NAME = "GetTransportorpoints";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //For Driver incentives
    public String BestDriver_Incentives(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/GetBestDriverByIMEI";
        OPERATION_NAME = "GetBestDriverByIMEI";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    public String InsertDriverFeedback(String deviceid,String customercode, String latlng,String cooperation,String attitude,String clean,String PPE,String unload,String comment)
    {
        SOAP_ACTION = "http://tempuri.org/InsertDriverFeedback";
        OPERATION_NAME = "InsertDriverFeedback";
        PropertyInfo[] piarray=new PropertyInfo[9];
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        pi = new PropertyInfo();
        pi.setName("latlng");
        pi.setValue(latlng);
        pi.setType(String.class);
        piarray[1]=pi;
        pi = new PropertyInfo();
        pi.setName("customercode");
        pi.setValue(customercode);
        pi.setType(String.class);
        piarray[2]=pi;
        pi = new PropertyInfo();
        pi.setName("cooperative");
        pi.setValue(cooperation);
        pi.setType(String.class);
        piarray[3]=pi;
        pi = new PropertyInfo();
        pi.setName("attitude");
        pi.setValue(attitude);
        pi.setType(String.class);
        piarray[4]=pi;
        pi = new PropertyInfo();
        pi.setName("cleanness");
        pi.setValue(clean);
        pi.setType(String.class);
        piarray[5]=pi;
        pi = new PropertyInfo();
        pi.setName("PPE");
        pi.setValue(PPE);
        pi.setType(String.class);
        piarray[6]=pi;
        pi = new PropertyInfo();
        pi.setName("unload_procedure");
        pi.setValue(unload);
        pi.setType(String.class);
        piarray[7]=pi;
        pi = new PropertyInfo();
        pi.setName("comments");
        pi.setValue(comment);
        pi.setType(String.class);
        piarray[8]=pi;
        return callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //For Driver incentives
    public String GetDriver_Language(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/GetLanguage";
        OPERATION_NAME = "GetLanguage";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //For Driver incentives
    public String GetEmergencyVehicle(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/GetEmergencyVehiclesNew";
        OPERATION_NAME = "GetEmergencyVehiclesNew";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //For Driver incentives
    public String KeepActivityLog(String deviceid,String activity,String lat,String lng) {
        SOAP_ACTION = "http://tempuri.org/Keepuseractivitylog";
        OPERATION_NAME = "Keepuseractivitylog";

        int version=BuildConfig.VERSION_CODE;//

        PropertyInfo[] piarray=new PropertyInfo[4];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("imei");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;

        pi = new PropertyInfo();
        pi.setName("page");
        pi.setValue(activity+"#"+version);
        pi.setType(String.class);
        piarray[1]=pi;

        pi = new PropertyInfo();
        pi.setName("lat");
        pi.setValue(lat);
        pi.setType(String.class);
        piarray[2]=pi;

        pi = new PropertyInfo();
        pi.setName("lng");
        pi.setValue(lng);
        pi.setType(String.class);
        piarray[3]=pi;

        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    //GetTotalVehicles
    public String GetTotalVehicles() {
        SOAP_ACTION = "http://tempuri.org/GetTotalVehicles";
        OPERATION_NAME = "GetTotalVehicles";
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,null,SOAP_ACTION);
    }

    //GetDestination
    public String GetDestinations(String deviceid) {
        SOAP_ACTION = "http://tempuri.org/GetRouteSourceDestinationsByIMEI";
        OPERATION_NAME = "GetRouteSourceDestinationsByIMEI";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0]=pi;
        return  callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    public String GetAppVersion(String packageName) {
        SOAP_ACTION = "http://tempuri.org/GetAppCurrentVersion";
        OPERATION_NAME = "GetAppCurrentVersion";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("packageName");
        pi.setValue(packageName);
        pi.setType(String.class);
        piarray[0]=pi;
       return  callWebservice(Config.ADMINPANELHTTPSURL,Config.ADMINPANELHTTPURL,OPERATION_NAME,piarray,SOAP_ACTION);
    }


    public  String callWebservice(String address,String alternateAddress, String methodName,PropertyInfo[] pi,String soapAction)
    {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, methodName);
        if(pi!=null) {
            for (int i = 0; i < pi.length; i++) {
                request.addProperty(pi[i]);
            }
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(address);
        if (envelope.bodyIn instanceof SoapFault) {
            SoapFault soapFault = (SoapFault) envelope.bodyIn;
            String requestStr = httpTransport.requestDump;

        } else {
            SoapObject soapObject = ((SoapObject) envelope.bodyIn);
            String requestStr = httpTransport.requestDump;
        }
        if(!address.contains("adminpanel.mysensel.com"))
         httpTransport = new HttpTransportSE(address+"/SenselWebService.asmx");
        Object response = null;
        try {
            httpTransport.call(soapAction, envelope);
             response = envelope.getResponse();
        } catch (Exception exception) {
            response = exception.toString();
            try{
                if(!alternateAddress.isEmpty()) {
                    httpTransport = new HttpTransportSE(alternateAddress);
                    if(!alternateAddress.contains("adminpanel.mysensel.com"))
                        httpTransport = new HttpTransportSE(alternateAddress+"/SenselWebService.asmx");
                    httpTransport.call(soapAction, envelope);
                     response = envelope.getResponse();
                }
            }catch (Exception e){}
        }
        if(response!=null)
        return response.toString();
        else
            return "";
    }
}



