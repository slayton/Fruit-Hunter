package com.quasicontrol.pacdroidlive;

import java.io.File;
import java.util.ArrayList;

import com.quasicontrol.live.WPUtil;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;


public class PacDroidLiveSettings extends PreferenceActivity {
	public final int GOT_IMAGE =12345;

	protected CheckBoxPreference customImgChk;
	protected ListPreference themePref;
	protected Uri mUri;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prefs);
	
		Preference buy_theme = this.findPreference("buy_theme");
		buy_theme.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q=pub:\"Quasicontrol Applications\"")));
		//buy_theme.setOnPreferenceClickListener(new BuyThemePrefClickLIstener(this.getApplicationContext()));
		
		
		Preference email_dev = this.findPreference("email_dev");
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"stuart@quasicontrol.com"});		
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pac Droid: Bug Report/Feature Request");
		emailIntent.setType("plain/text");
		email_dev.setIntent(emailIntent);

	    Preference donate = this.findPreference("donate");
	    String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=5WJNUY8BWJJ7G&lc=US&item_name=Quasicontrol%20Applications&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
		Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		donate.setIntent(donateIntent);
		

	    customImgChk = (CheckBoxPreference) this.findPreference("custom_monster_img");
	    customImgChk.setOnPreferenceClickListener(new CustomImagePreferenceClickListener(this.getApplicationContext()));
	     
	     
	    themePref = (ListPreference) this.findPreference("theme_pref");
	    setThemePrefEntries();     
	}
	public void setThemePrefEntries(){
		
		WPUtil.logD("setting up theme prefernce values");
	    ArrayList<String> themes = WPUtil.getInstalledThemes(this.getApplicationContext(), PacDroidLiveWallpaperService.getThemeList());
	    String[] entries = new String[themes.size()];
	    String[] entryValues = new String [themes.size()];
	   
	    themes.toArray(entryValues);
	    
	    /*
	    if (themes.size()==1)
	    	Toast.makeText(PacDroidLiveSettings.this, "No custom themes available, buy some one today",
					Toast.LENGTH_SHORT).show();
	    */
	    for (int i=0; i<themes.size(); i++)
	    	entries[i] = WPUtil.getThemeDisplayName(themes.get(i));
	    	
	    themePref.setEntries(entries);
	    themePref.setEntryValues(entryValues);
	}

	/**
	 * Checks that a preference is a valid numerical value
	 */
	Preference.OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {

		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// Check that the string is an integer
			if (newValue != null && newValue.toString().length() > 0
					&& newValue.toString().matches("\\d*")) {
				return true;
			}
			// If now create a message to the user
			Toast.makeText(PacDroidLiveSettings.this, "Invalid Input",
					Toast.LENGTH_SHORT).show();
			return false;
		}
	};
	
	private class CustomImagePreferenceClickListener implements OnPreferenceClickListener{
		Context c = null;
		public CustomImagePreferenceClickListener(Context c){
			this.c = c;
		}
		public boolean onPreferenceClick(Preference preference) {

			
			if (customImgChk.isChecked())
				getImage();
			return true;
		}
		  
	}
	
	private void getImage() {
		Toast.makeText(PacDroidLiveSettings.this, "For best results chose a square image smaller than 50x50 pixels",
				Toast.LENGTH_SHORT).show();
    	
		
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
    	intent.setType("image/*");
    	mUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"PacDroidLiveWallpaper_tmp" + ".jpg"));
    	intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mUri);

		//Intent intent =  new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		//Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		//intent.setType("image/*");
    	try {
    		intent.putExtra("return-data", true);
    		startActivityForResult(intent,GOT_IMAGE );
    	} catch (ActivityNotFoundException e) {
    		e.printStackTrace();
    	}
    } 
	/*
	protected final void onActivityResult(final int requestCode, final int 
			resultCode, final Intent i) { 

		super.onActivityResult(requestCode, resultCode, i); 
		WPUtil.logD("onActivityResult 1----------------");
		// this matches the request code in the above call 
		if (requestCode == GOT_IMAGE) { 
			WPUtil.logD("onActivityResult 2----------------");

			Uri _uri = i.getData(); 
			// this will be null if no image was selected... 
			if (_uri != null) { 
				WPUtil.logD("onActivityResult 3----------------");

				// now we get the path to the image file 
				Cursor cursor = getContentResolver().query(_uri, new String[] 
				                                                            { android.provider.MediaStore.Images.ImageColumns.DATA }, 
				                                                            null, null, null); 
				cursor.moveToFirst(); 
				String imageFilePath = cursor.getString(0); 
				cursor.close(); 
			} 
		} 
*/
		
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			customImgChk.setChecked(false);
    		return;
    	}
    	if (requestCode == GOT_IMAGE) {
    		Bitmap image = BitmapFactory.decodeFile(mUri.getPath());

    		if (image!=null)
    		{
    			image = WPUtil.resizeBitmap(image, WPUtil.IMAGE_SIZE_X, WPUtil.IMAGE_SIZE_Y);
    			WPUtil.savePrivateBitmap(this.getApplicationContext(), WPUtil.MONSTER_FILE_PATH, image);
    		}
    		else
    		{
    			customImgChk.setChecked(false);
    			Toast.makeText(this.getApplicationContext(), "Failed to grab image! Try selecting with the Gallery app!", Toast.LENGTH_LONG).show();
    		}
    	}
    }
	
	

}
