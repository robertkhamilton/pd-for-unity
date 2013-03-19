package com.bronsonzgeb.android.unity;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

public class Wrapper {
	private Activity activity;
	private Map<String, Integer> openFiles;
	
	public Wrapper( Activity currentActivity ) {
		activity = currentActivity;
		
		openFiles = new HashMap<String, Integer>();
	}
	
	public void openFile( String filename ) {
		Resources res = activity.getResources();
		File patchFile = null;
		
		try {
			String baseFilename = filename.split("\\.(?=[^\\.]+$)")[0];
			int resourceId = res.getIdentifier( baseFilename, "raw", activity.getPackageName());
			InputStream in = res.openRawResource(resourceId);
			patchFile = IoUtils.extractResource(in, filename, activity.getCacheDir());
			int handle = PdBase.openPatch(patchFile);
			openFiles.put(filename, handle);
		} catch (IOException e) {
			Log.e("Unity", e.toString());
		} finally {
			if (patchFile != null) patchFile.delete();
		}
	}
	
	public void closeFile( String filename ) {
		Integer handle = openFiles.get( filename );
		if ( handle != null ) {
			PdBase.closePatch( handle );
			openFiles.remove( filename );
		}
	}
	
	public void sendFloat( float f, String receiver ) {
		PdBase.sendFloat( receiver, f );
	}
	
	public void startAudio() {
		try {
//			int srate = PdBase.suggestSampleRate();
			int srate = AudioParameters.suggestSampleRate();
//			int nic = PdBase.suggestInputChannels();
			int nic = AudioParameters.suggestInputChannels();
//			int noc = PdBase.suggestOutputChannels();
			int noc = AudioParameters.suggestOutputChannels();
			float millis = 50.0f;
			int tpb = (int) (0.001f * millis * srate / PdBase.blockSize()) + 1;
			
			PdAudio.initAudio(srate, 0, noc, 1, true);
			PdAudio.startAudio(activity);
		} catch (IOException e) {
			Log.e("Unity", e.toString());
		}
	}
	
	public void initPd() {
//		PdPreferences.initPreferences(activity.getApplicationContext());
//		PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).registerOnSharedPreferenceChangeListener(activity);
//		if (!activity.bindService(new Intent(activity, PdService.class), pdConnection, Activity.BIND_AUTO_CREATE)) {
//			Log.e("Unity", "Failed to bind service");
//		}
		AudioParameters.init(activity);
		Log.d("Unity", "Init Pd");
	}
}
