package cl.json.social;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;

import cl.json.ShareFile;

/**
 * Created by Ralf Nieuwenhuizen on 10-04-17.
 */
public class InstagramShare extends SingleShareIntent {

    private static final String PACKAGE = "com.instagram.android";
    private static final String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.instagram.android";

    public InstagramShare(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public void open(ReadableMap options) throws ActivityNotFoundException {
            super.open(options);

            if (!ShareIntent.hasValidKey("url", options)) {
                Log.e("RNShare", "No url provided");
                return;
            }
            String url = options.getString("url");

            Boolean isUrlScheme = url.startsWith("instagram://");
            if (isUrlScheme) {
                openInstagramUrlScheme(url);
                return;
            }

            if (!ShareIntent.hasValidKey("type", options)) {
                Log.e("RNShare", "No type provided");
                return;
            }
            String type = options.getString("type");
            String extension = this.getExtension(type);
            Boolean isImage = type.startsWith("image");

            this.shareToInstagramFeed(url, chooserTitle, isImage, extension);
    }

    protected void openInstagramUrlScheme(String url) {
            Uri uri = Uri.parse(url);
            this.getIntent().setAction(Intent.ACTION_VIEW);
            this.getIntent().setData(uri);
            super.openIntentChooser();
    }

    private String getExtension(String url) {
            String[] ext = url.split("/");
            return ext[ext.length -1];
    }

    protected void shareToInstagramFeed(String url, String chooserTitle, Boolean isImage, String extension) {
        Boolean shouldUseInternalStorage = ShareIntent.hasValidKey("useInternalStorage", options) && options.getBoolean("useInternalStorage");
        ShareFile shareFile = new ShareFile(url, "image/" + extension, "image", shouldUseInternalStorage, this.reactContext) ;
        Uri uri = shareFile.getURI();

        Activity activity = this.reactContext.getCurrentActivity();

        Intent feedIntent = new Intent("com.instagram.share.ADD_TO_FEED");
        
        feedIntent.setType("image/*");
        feedIntent.putExtra(Intent.EXTRA_STREAM, uri);
        feedIntent.setPackage(PACKAGE);
        
        Intent chooserIntent = Intent.createChooser(feedIntent, "Share to feed");
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       
        activity.grantUriPermission(PACKAGE, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      
        this.reactContext.startActivity(chooserIntent);
         TargetChosenReceiver.sendCallback(true, true, this.getIntent().getPackage());
    }

    @Override
    protected String getPackage() {
        return PACKAGE;
    }

    @Override
    protected String getDefaultWebLink() {
        return null;
    }

    @Override
    protected String getPlayStoreLink() {
        return PLAY_STORE_LINK;
    }
}
