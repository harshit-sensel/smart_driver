package com.sensel.driverapp;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class WebAccessor {

    public static WebAccessor getNewInstance()
    {
        return  new WebAccessor();
    }

    public String uploadImageService(RequestQueue volleyRequestQueue, String methodName, String imageFile, String imageName, String sessionid)
    {
        final StringBuffer result = new StringBuffer();

        try{
            File uploadFile = new File(imageFile);
            if(!uploadFile.exists())
                return "";
            String[] methodParam = methodName.split(",");

            final CountDownLatch cd = new CountDownLatch(1);

            StringBuffer methodUrl = new StringBuffer(Config.FILE_UPLOAD_URL_VOLLEY + methodParam[0].trim()+"?sessionid=" + sessionid +"&fileName=" + imageName);

            ImageUploadWithVolley imageUploadReq = new ImageUploadWithVolley(methodUrl.toString(), new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    cd.countDown();
                    StringBuffer errorMessage = new StringBuffer("[ERROR_ON_IMAGE_UPLOAD]");
                    try
                    {
                        if(error != null)
                        {
                            if(error.networkResponse != null)
                            {
                                errorMessage.append("  Status_code:" + error.networkResponse.statusCode);
                                errorMessage.append("  Data:" + error.networkResponse.data);
                            }
                            errorMessage.append("  Cause:" + error.getCause());
                            errorMessage.append("  Message:" + error.getMessage());
                        }
                    }
                    catch(Exception e)
                    {
                        errorMessage.append("  Exception:" + e.getCause());
                    }
                    result.append(errorMessage);
//                    if(error == null) return;
//                    System.out.print(error.toString());
//                    Log.d("abd", "Error: " + error
//                            + ">>" + error.networkResponse.statusCode
//                            + ">>" + error.networkResponse.data
//                            + ">>" + error.getCause()
//                            + ">>" + error.getMessage());
                }
            }, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    cd.countDown();
                    result.append("success");
                    System.out.print(response);
                }
            }, uploadFile);
            RetryPolicy policy = new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            imageUploadReq.setRetryPolicy(policy);
            volleyRequestQueue.add(imageUploadReq);

            cd.await();
        }
        catch(Exception e)
        {
            return "";
        }
        Log.e("Webaccessor",result.toString());
        return result.toString();
    }

}
