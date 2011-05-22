package com.quasicontrol.pacdroidlive;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

import com.quasicontrol.live.WPUtil;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;


public class PacDroidLiveSettings extends PreferenceActivity {
	public final int GOT_IMAGE =1;

	protected CheckBoxPreference customImgChk;
	protected Uri mUri;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		//setContentView(R.xml.prefs);
		 Preference email_dev = this.findPreference("email_dev");
	     Preference donate = this.findPreference("donate");
	     customImgChk = (CheckBoxPreference) this.findPreference("custom_monster_img");
	     
	     email_dev.setOnPreferenceClickListener(new EmailPrefClickListener(this.getApplicationContext()));
	     donate.setOnPreferenceClickListener(new DonatePrefClickListener(this.getApplicationContext()));
	     customImgChk.setOnPreferenceClickListener(new CustomImagePreferenceClickListener(this.getApplicationContext()));
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
	private class EmailPrefClickListener implements OnPreferenceClickListener
	{
    	Context c = null;
    	public EmailPrefClickListener(Context c)
    	{
    		this.c = c;
    	}
		public boolean onPreferenceClick(Preference preference) {
			
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "support@quasicontrol.com");		
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Fruit Hunter: Bug Report/Feature Request");
			emailIntent.setType("plain/text");
			startActivity(emailIntent);

			return true;
		}
    }
	private class DonatePrefClickListener implements OnPreferenceClickListener
	{
    	Context c = null;
    	public DonatePrefClickListener(Context c)
    	{
    		this.c = c;
    	}
		public boolean onPreferenceClick(Preference preference) {
			
			String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=5WJNUY8BWJJ7G&lc=US&item_name=Quasicontrol%20Applications&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);

			return true;
		}
    }
	
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
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
    	intent.setType("image/*");
    	mUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
    			"pic_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
    	intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mUri);

    	try {
    		intent.putExtra("return-data", true);
    		startActivityForResult(intent,GOT_IMAGE );
    	} catch (ActivityNotFoundException e) {
    		e.printStackTrace();
    	}
    } 
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != RESULT_OK) {
    		customImgChk.setChecked(false);
    		return;
    	}
    	if (requestCode == GOT_IMAGE) {
    		Bitmap image = WPUtil.loadPublicImage(mUri.getPath());
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
