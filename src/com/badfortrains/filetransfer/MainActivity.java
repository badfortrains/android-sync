package com.badfortrains.filetransfer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import android.app.ActionBar.LayoutParams;
import android.app.ListActivity;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends ListActivity 
		implements LoaderManager.LoaderCallbacks<Cursor> {

	// This is the Adapter being used to display the list's data
	SimpleCursorAdapter mAdapter;

	// These are the Contacts rows that we will retrieve
	static final String[] PROJECTION = new String[] {
		MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE };

	// This is the select criteria
	static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";

	private static final String TAG = "FileTest";
	private WebServer server;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create a progress bar to display while the list loads
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		progressBar.setIndeterminate(true);
		getListView().setEmptyView(progressBar);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		// For the cursor adapter, specify which columns go into which views
		String[] fromColumns = { MediaStore.Audio.Media.TITLE };
		int[] toViews = { android.R.id.text1 }; // The TextView in
												// simple_list_item_1

		Log.v(TAG, "STARTING");
		
		Uri uri = Uri.parse("content://media/external/file/1437");
		Cursor musiccursor = managedQuery(uri, null, null, null, null);

		if (musiccursor.moveToFirst()) {
		    String title; 
		    int titleColumn = musiccursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE); 
		    title = musiccursor.getString(titleColumn);
		    Log.v(TAG,"FOUND IT is music? "+title);
		}   
		
		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, null, fromColumns,
				toViews, 0);
		setListAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
		
       server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
	}
	
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

	// Called when a new Loader needs to be created
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				PROJECTION, SELECTION, null, null);
	}

	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);
	}

	// Called when a previously created loader is reset, making the data
	// unavailable
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Delete the item
//		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
//			    id);
//		this.getContentResolver().delete(uri, null, null);
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
            	Log.v(TAG,Uri.fromFile(new File(path)).toString());
//                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                intent.setData(Uri.fromFile(new File(path)));
//                sendBroadcast(intent);	
            	File file = new File(path);
            	File sd=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            	File dest = new File(sd, "test");
            	boolean success = file.renameTo(dest);
            	file = dest;
            	Log.v(TAG,"FILE IS " + file.length());
            	Log.v(TAG,"RENAME IS " + success);
            	Log.v(TAG,new File(sd, "test").getPath());
            	   MediaScannerConnection.scanFile(getApplicationContext(),
            		          new String[] { file.toString() }, new String[] {"audio/*"},
            		          new MediaScannerConnection.OnScanCompletedListener() {
            		      public void onScanCompleted(String path, Uri uri) {
            		          Log.v("ExternalStorage", "Scanned " + path + ":");
            		          Log.v("ExternalStorage", "-> uri=" + uri);
            		      }
            		 });
            }

            	
            
			return new Response(Status.OK,MIME_HTML, "<html><body><form name='up' method='post' enctype='multipart/form-data'><input type='file' name='file' /><br /><input type='submit'name='submit' value='Upload'/></form></body></html>");
        }
    }
}