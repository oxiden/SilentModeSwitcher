package jp.oxiden.silentmodeswitcher;

import java.util.Date;

import android.widget.Toast;

import android.app.Notification;
import android.app.NotificationManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.media.AudioManager;

public class EnableSilentMode extends BroadcastReceiver {

	final String CONFIG_WEEKMODE_M2F = "weekmode_m2f";
	final String CONFIG_WEEKMODE_M2S = "weekmode_m2s";
	final String CONFIG_WEEKMODE_EVERYDAY = "weekmode_everyday";
	final String CONFIG_MANNERMODE_VIBRATE = "mannermode_vibrate";
	final String CONFIG_MANNERMODE_SILIENT = "mannermode_silent";

	SilentModeSwitcher m_sms;
	Context m_context;

	//
	// 指定時間に起動されるコールバック関数
	//
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			// SilentModeSwitcherクラスを使う準備
			m_sms = new SilentModeSwitcher();
			m_sms.setContext(context);
			// サイレントモードへの切り替え
			m_context = context;
			Logger.debug("==========Scheduled timing has come===========");
			Toast.makeText(context, getMsg(R.string.on_alerm_notice), Toast.LENGTH_LONG).show();
			setSilentMode();
			// 再スケジュールする
			m_sms.startAlerm(true);
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}

	private void setSilentMode() {
		// 土日祝は除く
		if (!isWeekDay()) {
			Logger.debug("return cause today is holiday.");
			return;
		}

		notify("サイレントモードに移行しました。今日も一日がんばりましょう。(がんばりましょう)");

		// マナーモードにするよ。
		AudioManager am = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(getMannerMode());
	}

	// 通知領域に通知
	private void notify(String message) {
		Notification notice = new Notification(android.R.drawable.btn_default, getMsg(R.string.app_name), System
				.currentTimeMillis());

		Context context = m_context;// getApplicationContext();
		CharSequence contentTitle = getMsg(R.string.app_name);
		CharSequence contentText = message;
		Intent notifyIntent = new Intent(context, SilentModeSwitcher.class);
		PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
		notice.setLatestEventInfo(context, contentTitle, contentText, intent);

		NotificationManager notify_manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final int NOTFICATION_ID = 1;
		notify_manager.notify(NOTFICATION_ID, notice);
	}

	// 平日ならtrueを返す
	private Boolean isWeekDay() {
		Date now = new Date();
		int now_day = now.getDay() % 7;
		String mode = m_sms.getWeekMode();
		if (mode.equals(CONFIG_WEEKMODE_M2F)) {
			return (now_day > 0 && now_day < 6);
		} else if (mode.equals(CONFIG_WEEKMODE_M2S)) {
			return (now_day > 0 && now_day < 7);
		} else if (mode.equals(CONFIG_WEEKMODE_EVERYDAY)) {
			return true;
		}
		return false;
	}

	private Integer getMannerMode() {
		String mode = m_sms.getMannerMode();
		if (mode.equals(CONFIG_MANNERMODE_VIBRATE)) {
			return AudioManager.RINGER_MODE_VIBRATE;
		} else if (mode.equals(CONFIG_MANNERMODE_SILIENT)) {
			return AudioManager.RINGER_MODE_SILENT;
		}
		return AudioManager.RINGER_MODE_NORMAL;
	}

	// ストリングテーブル参照
	private String getMsg(int string_id) {
		// return m_context.getResources().getString(string_id);
		return (String) (CharSequence) m_context.getText(string_id);
	}
}
