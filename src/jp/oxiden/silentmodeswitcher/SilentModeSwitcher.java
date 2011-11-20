package jp.oxiden.silentmodeswitcher;

import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.TimePicker;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SilentModeSwitcher extends PreferenceActivity {

	final String CONFIG1_ENABLE_KEY = "config1_enable";
	final String CONFIG1_ALERTTIME = "config1_alermtime";// 切り替え時間
	final int DEFAULT_ALARM_HOUR = 9;
	final int DEFAULT_ALARM_MINUTE = 0;
	final String CONFIG_WEEKMODE = "config_weekmode";// 曜日
	final String CONFIG_MANNERMODE = "config_mannermode";// マナーモードの種類
	
	Context m_context = SilentModeSwitcher.this;// EnableSilentMode用

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			Logger.debug("onCreate");
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);

			// Preferences用リスナ
			CheckBoxPreference prefConfig1Enable = (CheckBoxPreference) this.findPreference(CONFIG1_ENABLE_KEY);
			prefConfig1Enable.setOnPreferenceChangeListener(new onPreferenceChangeListener());
			Preference prefConfig1Alarmtime = this.findPreference(CONFIG1_ALERTTIME);
			prefConfig1Alarmtime.setOnPreferenceClickListener(new PreferenceClickListener());
			// クリックに応じてイベント処理しない(アラーム起動時に設定値を取得して使用する)ためコメントアウト
			// Preference prefConfigWeekMode =
			// this.findPreference(CONFIG_WEEKMODE);
			// prefConfigWeekMode.setOnPreferenceChangeListener(new
			// onPreferenceChangeListener());
			// Preference prefConfigMannerMode =
			// this.findPreference(CONFIG_MANNERMODE);
			// prefConfigMannerMode.setOnPreferenceChangeListener(new
			// onPreferenceChangeListener());

			// 設定が有効であればアラームを設定しておく
			startAlerm();
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void onDestroy() {
		Logger.debug("onDestroy");
		super.onDestroy();
	}
	
	public void setContext(Context context) {
		m_context = context;
	}

	// --------------------------Preference--------------------------
	private class onPreferenceChangeListener implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// preference.setSummary((CharSequence)newValue);
			Logger.debug("key=" + preference.getKey());

			if (preference.getKey().equals(CONFIG1_ENABLE_KEY)) {
				Logger.debug("CONFIG1_ENABLE_KEY = " + newValue.toString());
				boolean config = ((Boolean) newValue).booleanValue();
				// チェックボックスのON/OFFに応じて切替日時をON/OFFする
				Preference prefConfig1Alarmtime = SilentModeSwitcher.this.findPreference(CONFIG1_ALERTTIME);
				prefConfig1Alarmtime.setEnabled(config);
				// アラームマネージャ
				setAlerm(config, 1);
				return true;
				// } else if (preference.getKey().equals(CONFIG_WEEKMODE)
				// || preference.getKey().equals(CONFIG_MANNERMODE)) {
				// // 設定が有効であればアラームを設定しておく
				// startAlerm();
				// return true;
			}
			return false;
		}

	}

	private class PreferenceClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Logger.debug("key=" + preference.getKey());
			Integer[] alermtime = getAlermtime();
			final TimePickerDialog tpdAlermTime = new TimePickerDialog(SilentModeSwitcher.this,
					new TimePickerSetListener(), alermtime[0], alermtime[1], true // is24HourView
			);
			tpdAlermTime.show();
			return true;
		}
	}

	private class TimePickerSetListener implements OnTimeSetListener {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Logger.debug("TimePickerSetListener=" + hourOfDay + ":" + minute);
			setAlermtime(hourOfDay, minute);
			startAlerm();
		}
	}

	public void startAlerm(Boolean start_only) {
		Boolean enable = getEnable();
		Logger.debug("startAlerm: " + Boolean.valueOf(enable) + ", start_only=" + start_only);
		if (enable) {
			if (!start_only) {
				setAlerm(false, 1);
			}
			setAlerm(true, 1);
		}
	}

	public void startAlerm() {
		startAlerm(false);
	}

	// --------------------------Preference--------------------------

	// Preferenceから設定値を取得(CONFIG1_ENABLE_KEY)
	// デフォルト：false
	private Boolean getEnable() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(m_context);
		Boolean ret = pref.getBoolean(CONFIG1_ENABLE_KEY, false);
		Logger.debug("getEnable: " + ret);
		return ret;
	}

	// Preferenceから設定値を取得(CONFIG_WEEKMODE)
	// デフォルト：""
	public String getWeekMode() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(m_context);
		String ret = pref.getString(CONFIG_WEEKMODE, "");
		Logger.debug("getWeekMode: " + ret);
		return ret;
	}

	// Preferenceから設定値を取得(CONFIG_MANNERMODE)
	// デフォルト：""
	public String getMannerMode() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(m_context);
		String ret = pref.getString(CONFIG_MANNERMODE, "");
		Logger.debug("getMannerMode: " + ret);
		return ret;
	}

	// Preferenceから設定値を取得(CONFIG1_ALERTTIME)
	// デフォルト：[DEFAULT_ALARM_HOUR, DEFAULT_ALARM_MINUTE]
	public Integer[] getAlermtime() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(m_context);
		Logger.debug("getAlermtime: " + pref.getString(CONFIG1_ALERTTIME, ""));
		String[] ret = pref.getString(CONFIG1_ALERTTIME, DEFAULT_ALARM_HOUR + ":" + DEFAULT_ALARM_MINUTE).split(":", 2);
		Logger.debug("getAlermtime: " + ret[0] + "," + ret[1]);
		int hour = Integer.parseInt(ret[0]);
		int minute = Integer.parseInt(ret[1]);
		Logger.debug("getAlermtime: " + hour + ":" + minute);
		return new Integer[] { hour, minute };
	}

	// Preferenceに設置値を保存(CONFIG1_ALERTTIME)
	private void setAlermtime(int hour, int minute) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SilentModeSwitcher.this);
		Logger.debug("setAlermtime: " + hour + ":" + minute);
		Editor editor = pref.edit();
		editor.putString(CONFIG1_ALERTTIME, hour + ":" + minute);
		editor.commit();
		return;
	}

	// アラームセット/リセット
	private void setAlerm(boolean enable, int config_number) {
		Logger.debug("setAlerm: enable=" + enable);
		if (enable) {
			// アラームセット
			GregorianCalendar cal = getAlermDate();
			Intent i = new Intent(m_context, EnableSilentMode.class);
			PendingIntent pi = PendingIntent.getBroadcast(m_context, 0, i, 0);
			AlarmManager am = (AlarmManager) m_context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);

			Logger.debug("scheduled @ " + cal.get(GregorianCalendar.HOUR_OF_DAY) + ":" + cal.get(GregorianCalendar.MINUTE) + ":" + cal.get(GregorianCalendar.SECOND));
		} else {
			// アラーム解除
			Intent i = new Intent(m_context, EnableSilentMode.class);
			PendingIntent pi = PendingIntent.getBroadcast(m_context, 0, i, 0);
			AlarmManager am = (AlarmManager) m_context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(pi);
		}
	}

	// アラーム時間取得
	private GregorianCalendar getAlermDate() {
		Integer[] config_time = getAlermtime();
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.HOUR_OF_DAY, config_time[0]);
		cal.set(GregorianCalendar.MINUTE, config_time[1]);
		cal.set(GregorianCalendar.SECOND, 0);
		cal.set(GregorianCalendar.MILLISECOND, 0);
		if (cal.before(new GregorianCalendar())) {
			cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
		}
//		// for debug -------------------------------------
//		cal = new GregorianCalendar();
//		cal.add(GregorianCalendar.SECOND, 20);
//		// for debug -------------------------------------
		Logger.debug("getAlermDate: " + cal.get(GregorianCalendar.HOUR_OF_DAY) + ":" + cal.get(GregorianCalendar.MINUTE) + ":" + cal.get(GregorianCalendar.SECOND));
		return cal;
	}

}