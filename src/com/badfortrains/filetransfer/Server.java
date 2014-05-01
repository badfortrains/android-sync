package com.badfortrains.filetransfer;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Gabriel on 4/30/14.
 */
public class Server {
    private static final String TAG = "FileTest";
    private WebServer server;
    private Context mContext;
    private Media mMedia;

    public void Server(Context context){
        mMedia= new Media(context);

        server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
    }

    public void destroy(){
        if (server != null)
            server.stop();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }

        @Override
        public Response serve(String uri, Method method,
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {
            String answer = "An Answer";
            String path = files.get("file");
            Log.v(TAG, "RESOPONSE");
            if(path != null){
                Log.v(TAG, path);
                Log.v(TAG, Uri.fromFile(new File(path)).toString());
//                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                intent.setData(Uri.fromFile(new File(path)));
//                sendBroadcast(intent);
                File file = new File(path);
                File sd= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                File dest = new File(sd, "test.mp3");
                boolean success = file.renameTo(dest);
                file = dest;
                Log.v(TAG,"FILE IS " + file.length());
                Log.v(TAG,"RENAME IS " + success);
                Log.v(TAG,new File(sd, "test.mp3").getPath());
                mMedia.scan(file);
            }



            return new Response(Response.Status.OK,MIME_HTML, "<html><body><form name='up' method='post' enctype='multipart/form-data'><input type='file' name='file' /><br /><input type='submit'name='submit' value='Upload'/></form></body></html>");
        }
    }
}
