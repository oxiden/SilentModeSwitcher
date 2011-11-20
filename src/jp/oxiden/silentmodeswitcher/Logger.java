package jp.oxiden.silentmodeswitcher;

import android.util.Log;

public class Logger {

	static String Mytag = "OXIDEN";// this.getClass().getName()

	public static int debug(String s) {
		return Log.d(Mytag, s);
	}

	public static int error(String s) {
		return Log.e(Mytag, s);
	}

	public static int error(Exception e) {
		return Log.e(Mytag, e.getClass() + ": " + e.getLocalizedMessage());
	}

}
