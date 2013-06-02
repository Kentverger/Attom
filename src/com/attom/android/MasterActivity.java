package com.attom.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract.Constants;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MasterActivity extends Activity {

	private static Camera mCamera;
	private CameraPreview mPreview;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	String GCM_SENDER_ID = "817681874144";

	static final String TAG = "GCMDemo";

	GoogleCloudMessaging gcm;

	SharedPreferences prefs;
	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


		prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		setContentView(R.layout.activity_master);

		regid = prefs.getString(PROPERTY_REG_ID, null);
		if (regid == null) {
			registerBackground();
		}else{
			Log.d(TAG, regid);
		}

		gcm = GoogleCloudMessaging.getInstance(this);
		

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview_master);
		preview.addView(mPreview);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); 
		}
		catch (Exception e){
			Log.d("Error", "" + e.getMessage());
		}
		return c;
	}

	/** A basic Camera preview class */
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCameraLocal;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			mCameraLocal = camera;

			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCameraLocal.setPreviewDisplay(holder);
				mCameraLocal.startPreview();
			} catch (IOException e) {
				Log.d("ERROR", "Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

			if (mHolder.getSurface() == null){
				return;
			}

			try {
				mCameraLocal.stopPreview();
			} catch (Exception e){
			}

			try {
				mCameraLocal.setPreviewDisplay(mHolder);
				mCameraLocal.startPreview();

			} catch (Exception e){
				Log.d("ERROR", "Error starting camera preview: " + e.getMessage());
			}
		}
	}
	private void registerBackground() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					regid = gcm.register(GCM_SENDER_ID);
					msg = "Device registered, registration id=" + regid;
					
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(PROPERTY_REG_ID, regid);
					editor.commit();
					
					Log.d(TAG, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}
			
		}.execute(null, null, null);
	}

	public static class GcmBroadcastReceiver extends BroadcastReceiver {
	    static final String TAG = "GCMDemo";
	    public static final int NOTIFICATION_ID = 1;
	    Context ctx;
	    

	    @Override
	    public void onReceive(Context context, Intent intent) {
	        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
	        ctx = context;
	        String messageType = gcm.getMessageType(intent);
	        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	        	Toast.makeText(ctx, intent.getExtras().toString(), Toast.LENGTH_SHORT).show();
	        } else {
	        	
	        	PictureCallback mPicture = new PictureCallback() {

	        	    @Override
	        	    public void onPictureTaken(byte[] data, Camera camera) {
	        	    	
	        			File pictureFileDir = getDir();

	        			if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

	        				Log.d(TAG, "Ups no pudimos guardar la imagen");
	        				Toast.makeText(ctx, "Ups no pudimos guardar la imagen",
	        						Toast.LENGTH_LONG).show();
	        				return;

	        			}
	        	    	
	        			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
	        			String date = dateFormat.format(new Date());
	        			String photoFile = "Picture_" + date + ".jpg";

	        			String filename = pictureFileDir.getPath() + File.separator + photoFile;
	        	    	
	        	    	File pictureFile = new File(filename);

	        	        if (pictureFile == null){
	        	            return;
	        	        }

	        	        try {
	        	            FileOutputStream fos = new FileOutputStream(pictureFile);
	        	            fos.write(data);
	        	            fos.close();
	        	            Toast.makeText(ctx, "La foto se a guardado correctamente", Toast.LENGTH_LONG).show();
	        	            ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
	        	        } catch (FileNotFoundException e) {
	        	            Log.d(TAG, "File not found: " + e.getMessage());
	        	        } catch (IOException e) {
	        	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        	        }
	        	    }
	        	};
	        	
	        	mCamera.takePicture(null, null, mPicture);
	        }
	        setResultCode(Activity.RESULT_OK);
	    }

	}
	private static File getDir() {
		File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "CameraAPIDemo");
	}
	
}
