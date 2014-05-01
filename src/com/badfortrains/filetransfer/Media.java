package com.badfortrains.filetransfer;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Gabriel on 4/30/14.
 */
public class Media{

    Context mContext;
    String musicJSON = "";

    public Media(Context context){
        mContext = context;
    }

    private void musicJSON(){
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] proj = {
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.TRACK,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM
        };
        String select = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        JSONArray result = new JSONArray();
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, select, null, null);
        while(cursor.moveToNext()){
            JSONObject item = new JSONObject();
            try{
                item.put("id",cursor.getString(0));
                item.put("title",cursor.getString(1));
                item.put("track",cursor.getString(2));
                item.put("artist",cursor.getString(3));
                item.put("album",cursor.getString(4));
            }catch(JSONException e){
                e.printStackTrace();
            }
            result.put(item);
            musicJSON = result.toString();
        }

    }

    public void scan(File file){
        MediaScannerConnection.scanFile(mContext,
                new String[]{file.toString()}, new String[]{"audio/*"},
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v("ExternalStorage", "Scanned " + path + ":");
                        Log.v("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }
}
