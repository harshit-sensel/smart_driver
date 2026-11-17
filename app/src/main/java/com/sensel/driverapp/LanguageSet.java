package com.sensel.driverapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class LanguageSet {

    public void setLocate(String lang, Context context, Context acticitycontex) {
        Locale locale=new Locale(lang);
        Locale.setDefault(locale);
        Configuration config =new Configuration();
        config.locale=locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        acticitycontex.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());



        SharedPreferences.Editor editor=context.getSharedPreferences("lang_setting", MODE_PRIVATE).edit();
        editor.putString("my_lang",lang);
        Gridmenu.LANGUGE=lang;
        Log.e("setLocate",lang);
        editor.apply();

    }
    public  String whichlanguage(String lan){
        String retlang;
        switch (lan){
            case "English": retlang= "en";
            break;
             case "Tamil":retlang= "ta";
            break;
            case "Kannada":retlang= "kn";
            break;
            case "Malayalam":retlang= "ml";
            break;
            case "Hindi":retlang="hi";
            break;

            default:retlang= "en";
            break;
        }
        return retlang;
    }
}
