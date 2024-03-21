package com.zhuchao.android.car.tts;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

/**
 * (external\svox\)PicoTts.apk and PicoLangInstaller.apk must be installed first
 */

public class TextSpeaker {
	private TextToSpeech mTextToSpeech;
	private final Context mContext;
	private final OnTextSpeakerResult mOnTextSpeakerResult;

	private static TextSpeaker mTextSpeaker;
	public static Locale mLocale = Locale.US;
	public static float mRate = 1.0f;
	public static float mPitch = 1.0f;
	private static String mFisrtString = null;
	public static void initDefault(Context context) {
		mTextSpeaker = new TextSpeaker(context, Locale.getDefault(), null);
	}

	public static void speakDirect(Context context, String text) {
		if (context == null){
			return;
		}

		if (mTextSpeaker != null && mLocale != null) {			
			if (!mLocale.equals(Locale.getDefault())){
				mTextSpeaker = null;
			}
		}
		
		if (mTextSpeaker == null) {
			initDefault(context);
			mFisrtString = text;
		} else if (mTextSpeaker != null) {
			if (mTextSpeaker.mTextToSpeech != null) {
				mTextSpeaker.mTextToSpeech.speak(text,
						TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	public static TextSpeaker getInstance(Context context, Locale locale,
			OnTextSpeakerResult onResult) {
		// if (mTextSpeaker == null) {
		mTextSpeaker = new TextSpeaker(context, locale, onResult);
		// }
		return mTextSpeaker;
	}

	TextSpeaker(Context context, Locale locale, OnTextSpeakerResult onResult) {
		mContext = context;
		if (locale != null)
			mLocale = locale;
		mOnTextSpeakerResult = onResult;
		mTextToSpeech = new TextToSpeech(context, mOnInitListener);
	}

	public void speak(String text) {
		if (mTextToSpeech != null) {
			mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	public boolean isSpeaking() {
		if (mTextToSpeech != null) {
			return mTextToSpeech.isSpeaking();
		} else {
			return false;
		}
	}

	private final OnInitListener mOnInitListener = new OnInitListener() {
		@Override
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
				mTextToSpeech.setSpeechRate(mRate);
				mTextToSpeech.setPitch(mPitch);
				int result = mTextToSpeech.setLanguage(mLocale);
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					Toast.makeText(mContext, "Data loss or unsupported",
							Toast.LENGTH_LONG).show();
					if (mOnTextSpeakerResult != null)
						mOnTextSpeakerResult.OnInit(-1);
				} else if (result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
					if (mFisrtString != null){
						if (mTextToSpeech != null) {
							mTextToSpeech.speak(mFisrtString,
									TextToSpeech.QUEUE_FLUSH, null);
						}
						mFisrtString = null;
					}					
				} else {
					if (mOnTextSpeakerResult != null)
						mOnTextSpeakerResult.OnInit(0);
				}
			}
		}
	};

	public void stop() {
		if (mTextToSpeech != null)
			mTextToSpeech.stop();
	}

	public void release() {
		if (mTextToSpeech != null) {
			mTextToSpeech.stop();
			mTextToSpeech.shutdown();
			mTextToSpeech = null;
		}
		mTextSpeaker = null;
	}

	public interface OnTextSpeakerResult {
		void OnInit(int status);
	}
}