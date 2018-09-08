package gescis.webschool;

import android.*;
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import gescis.webschool.utils.InputStreamVolleyRequest;
import gescis.webschool.utils.TouchImageView;

/**
 * Created by shalu on 30/06/18.
 */

public class ImageActivity extends AppCompatActivity {

    TouchImageView imageView;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 10;
    private static final int REQUEST_PERMISSION_SETTING = 20;
    private static final int PERMISSION_CALLBACK = 29;
    private boolean sentToSettings = false;
    Context context;
    String url, filename;
    Dialog dialog;
    String[] permissionRequired = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);

        context = this;
        imageView = (TouchImageView) findViewById(R.id.imageView);
        Environment.getExternalStorageDirectory();
        if (!checkPermission()) {
            requestPermission();
        }
        ask_permissn();
        if(getIntent()!=null){
            url = getIntent().getStringExtra("url");
            filename = getIntent().getStringExtra("filename");
        }
        Picasso.with(this).load(url).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                if (!checkPermission()) {
                    requestPermission();
                }else{
                    dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.progressdialog);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    download_now(url);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkPermission(){
        int result = ActivityCompat.checkSelfPermission(ImageActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(ImageActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            Toast.makeText(ImageActivity.this, "Storage permission allows to download image. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(ImageActivity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case  EXTERNAL_STORAGE_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(ImageActivity.this, "Permission Granted, Now you can download image.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(ImageActivity.this, "Permission Denied, You cannot download image.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private void ask_permissn() {

        if (ActivityCompat.checkSelfPermission(ImageActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ImageActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (Wschool.sharedPreferences.getBoolean(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setTitle("Needs Storage Permission");
                builder.setMessage("This app needs storage permission to download your image.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", ImageActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(ImageActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }

            Wschool.editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
            Wschool.editor.commit();

        } else {
            //You already have the permission, just go ahead.
        }
    }

    private void download_now(String url) {

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response!=null) {
                                File folder = new File(Environment.getExternalStorageDirectory() + "/Webschool");
                                boolean success = true;
                                if (!folder.exists()) {
                                    success = folder.mkdir();
                                }
                                if (success) {
                                    // Do something on success
                                    File newone = new File(folder, filename);

                                    if(!newone.exists())
                                    {

                                        FileOutputStream fos = new FileOutputStream(newone);
                                        fos.write(response);
                                        fos.close();
                                        dialog.dismiss();
                                        String filepath = "Document downloaded in storage/Webschool/"+filename;
                                        GalleryRefresh(folder);
                                        Toast.makeText(context, "Download complete. Document downloaded in storage/Webschool/"+filename, Toast.LENGTH_LONG).show();
                                    }else
                                    {
                                        dialog.dismiss();
                                        Toast.makeText(context, "File is already saved.", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    // Do something else on failure
                                    dialog.dismiss();
                                }

                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            dialog.dismiss();
                            Toast.makeText(context, "UNABLE TO DOWNLOAD FILE", Toast.LENGTH_LONG).show();
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }

                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                dialog.dismiss();
                Toast.makeText(context, "UNABLE TO DOWNLOAD FILE", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }

        }, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(context, new HurlStack());
        mRequestQueue.add(request);
    }

    public void GalleryRefresh(File folder)
    {
        Uri photoURI = FileProvider.getUriForFile(ImageActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                folder);
        Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, photoURI);
        context.sendBroadcast(intent);
    }
}
