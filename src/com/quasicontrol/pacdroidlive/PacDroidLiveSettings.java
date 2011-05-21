package com.quasicontrol.pacdroidlive;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;


public class PacDroidLiveSettings extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		//setContentView(R.xml.prefs);
		 Preference email_dev = this.findPreference("email_dev");
	     email_dev.setOnPreferenceClickListener(new EmailPrefClickListener(this.getApplicationContext()));
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
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pac Droid: Bug Report/Feature Request");
			emailIntent.setType("plain/text");
			startActivity(emailIntent);

			return true;
		}
    }

}