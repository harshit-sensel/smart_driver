package com.sensel.driverapp;

/**
 * Created by User on 29-04-2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;


public class NotificationAdapter extends ArrayAdapter<RowItem> {

    Context context;

    public NotificationAdapter(Context context, int resourceId,
                               List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {

        ImageView imageView;
        TextView txtTitle;
       // TextView txtinfo;
        WebView webView;
        TextView txtdate;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.notificationlist, null);
            holder = new ViewHolder();

            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
           // holder.txtinfo=(TextView) convertView.findViewById(R.id.info);
            holder.txtdate = (TextView) convertView.findViewById(R.id.date);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.webView = (WebView) convertView.findViewById(R.id.webview);
            holder.imageView = imageView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (rowItem != null) {
            holder.txtTitle.setText(rowItem.getTitle());
            holder.txtdate.setText(rowItem.getAddress());
            holder.imageView.setImageResource(rowItem.NgetImageId());

            if (holder.webView != null) {
                final WebSettings webSettings = holder.webView.getSettings();
                webSettings.setDefaultFixedFontSize(14);
                webSettings.setJavaScriptEnabled(true);
                holder.webView.setVerticalScrollBarEnabled(false);
                holder.webView.setHorizontalScrollBarEnabled(false);

                holder.webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        if (request != null && request.getUrl() != null) {
                            String url = request.getUrl().toString();
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return true;
                    }
                });

                String desc = rowItem.getDesc() != null ? rowItem.getDesc() : "";
                String htmlContent = "<!DOCTYPE html><html><head>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
                        "<style>" +
                        "body { margin: 0; padding: 0; font-family: sans-serif; font-size: 14px; color: #212121; word-wrap: break-word; line-height: 1.4; }" +
                        "table { width: 100% !important; border-collapse: collapse; margin-top: 5px; }" +
                        "td, th { padding: 4px; border: 1px solid #cccccc; font-size: 13px; }" +
                        "a[href^='http://'], a[href^='https://'] { color: #1976D2; font-weight: bold; text-decoration: underline; }" +
                        "a:not([href^='http']) { color: inherit; text-decoration: none; cursor: default; }" +
                        "</style></head><body>" +
                        desc +
                        "</body></html>";

                holder.webView.loadDataWithBaseURL("", htmlContent, "text/html", "UTF-8", "");
            }
        }

        return convertView;
    }

    public boolean isHtml(String input)
    {
        Pattern htmlPattern = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
        boolean isHTML = htmlPattern.matcher(input).matches();
        return isHTML;
    }
}
