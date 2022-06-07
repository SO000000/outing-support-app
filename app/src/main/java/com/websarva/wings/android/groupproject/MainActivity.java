package com.websarva.wings.android.groupproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// <参考文献>
//	反復アラームのスケジュール設定(https://developer.android.com/training/scheduling/alarms?hl=ja#type)
//  TimePicker(https://developer.android.com/reference/android/widget/TimePicker)
//  Android のマテリアル デザイン(https://developer.android.com/guide/topics/ui/look-and-feel?hl=ja)


public class MainActivity extends AppCompatActivity {
	/**
	 * ログに記載するタグ用の文字列。
	 */
	private static final String DEBUG_TAG = "AsyncSample";

	private static final String WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=ja";
	/**
	 * お天気APIにアクセスすするためのAPIキー。
	 */
	private static final String APP_ID = "--------------";

	private ImageButton mailbutton,weatherbutton,alarmbutton;

	private int count = 0;

	public String putCityName;
	public String putWeather;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//		目的地を入力するEditText
		EditText editText = findViewById(R.id.destination);
		String text = editText.getText().toString();

//		editextに入力したtextを取得するbutton
//		天気を表示するためのボタン
		weatherbutton = (ImageButton) findViewById(R.id.weatherButton);
		weatherbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
//				ボタンが繰り返し押すことで，現在地，経由地，目的地をそれぞれ表示する
				if(count == 0) {
					EditText current = findViewById(R.id.current_location);
					String q_current = current.getText().toString();
					String urlFull_current = WEATHERINFO_URL + "&q=" + q_current + "&appid=" + APP_ID;
					receiveWeatherInfo(urlFull_current);
					count = 1;
				} else if (count == 1) {
					EditText waypoint = findViewById(R.id.waypoint);
					String q_waypoint = waypoint.getText().toString();
					String urlFull_destination = WEATHERINFO_URL + "&q=" + q_waypoint + "&appid=" + APP_ID;
					receiveWeatherInfo(urlFull_destination);
					count = 2;
				} else if (count == 2) {
					EditText destination = findViewById(R.id.destination);
					String q_destination = destination.getText().toString();
					String urlFull_destination = WEATHERINFO_URL + "&q=" + q_destination + "&appid=" + APP_ID;
					receiveWeatherInfo(urlFull_destination);
					count = 0;
				}
			}
		});

//		メール画面を開くボタン
		mailbutton = (ImageButton) findViewById(R.id.mailButton);
		mailbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				MailActivity();
			}
		});

		alarmbutton = (ImageButton) findViewById(R.id.alarmButton);
		alarmbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AlarmActivity();
			}
		});

	}
	public void MailActivity() {
		Intent intent = new Intent(this, MailActivity.class);
		startActivity(intent);
	}

	public void AlarmActivity() {
		Intent intent = new Intent(this, AlarmActivity.class);
		startActivity(intent);
	}

	//	mapbuttonが押された時の処理
	public void onMapShowCurrentButtonClick(View view) {
		// 入力欄に入力されたキーワード文字列を取得
		EditText current = findViewById(R.id.current_location);
		String present = current.getText().toString();
		EditText etSearchWord = findViewById(R.id.destination);
		String searchWord = etSearchWord.getText().toString();

		try {
			// 入力されたキーワードをURLエンコード。
			searchWord = URLEncoder.encode(searchWord, "UTF-8");
			// マップアプリと連携するURI文字列を生成。
			// 起動した際,現在地から目的地までのルートが表示される
			String uriStr = "geo:0,0?q=" + present + "から" + searchWord;
			// URI文字列からURIオブジェクトを生成。
			Uri uri = Uri.parse(uriStr);
			// Intentオブジェクトを生成。
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			// アクティビティを起動。
			startActivity(intent);
		}
		catch(UnsupportedEncodingException ex) {
			Log.e("MainActivity", "検索キーワード変換失敗", ex);
		}
	}
//観光ボタンが押された時の処理
	public void onSpotshow(View view) {
		// 入力欄に入力されたキーワード文字列を取得。
		EditText gSearchWord = findViewById(R.id.destination);
		String gsearchWord = gSearchWord.getText().toString();

		try {
			// 入力されたキーワードをURLエンコード。
			gsearchWord = URLEncoder.encode(gsearchWord, "UTF-8");
			// マップアプリと連携するURI文字列を生成。
			String uriStr = "https://www.google.com/search?q=" + gsearchWord + "+おすすめスポット";
			// URI文字列からURIオブジェクトを生成。
			Uri uri = Uri.parse(uriStr);
			// Intentオブジェクトを生成。
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			// アクティビティを起動。
			startActivity(intent);
		}
		catch(UnsupportedEncodingException ex) {
			Log.e("MainActivity", "検索キーワード変換失敗", ex);
		}
	}

	/**
	 * リストビューに表示させる天気ポイントリストデータを生成するメソッド。
	 *
	 * @return 生成された天気ポイントリストデータ。
	 */

	/**
	 * お天気情報の取得処理を行うメソッド。
	 *
	 * @param urlFull お天気情報を取得するURL。
	 */
	@UiThread
	private void receiveWeatherInfo(final String urlFull) {
		Looper mainLooper = Looper.getMainLooper();
		Handler handler = HandlerCompat.createAsync(mainLooper);
		WeatherInfoBackgroundReceiver backgroundReceiver = new WeatherInfoBackgroundReceiver(handler, urlFull);
		ExecutorService executorService  = Executors.newSingleThreadExecutor();
		executorService.submit(backgroundReceiver);
	}

	/**
	 * 非同期でお天気情報APIにアクセスするためのクラス。
	 */
	private class WeatherInfoBackgroundReceiver implements Runnable {
		/**
		 * ハンドラオブジェクト。
		 */
		private final Handler _handler;
		/**
		 * お天気情報を取得するURL。
		 */
		private final String _urlFull;

		/**
		 * コンストラクタ。
		 * 非同期でお天気情報Web APIにアクセスするのに必要な情報を取得する。
		 *
		 * @param handler ハンドラオブジェクト。
		 * @param urlFull お天気情報を取得するURL。
		 */
		public WeatherInfoBackgroundReceiver(Handler handler , String urlFull) {
			_handler = handler;
			_urlFull = urlFull;
		}

		@WorkerThread
		@Override
		public void run() {
			// HTTP接続を行うHttpURLConnectionオブジェクトを宣言。finallyで解放するためにtry外で宣言。
			HttpURLConnection con = null;
			// HTTP接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
			InputStream is = null;
			// 天気情報サービスから取得したJSON文字列。天気情報が格納されている。
			String result = "";
			try {
				// URLオブジェクトを生成。
				URL url = new URL(_urlFull);
				// URLオブジェクトからHttpURLConnectionオブジェクトを取得。
				con = (HttpURLConnection) url.openConnection();
				// 接続に使ってもよい時間を設定。
				con.setConnectTimeout(1000);
				// データ取得に使ってもよい時間。
				con.setReadTimeout(1000);
				// HTTP接続メソッドをGETに設定。
				con.setRequestMethod("GET");
				// 接続。
				con.connect();
				// HttpURLConnectionオブジェクトからレスポンスデータを取得。
				is = con.getInputStream();
				// レスポンスデータであるInputStreamオブジェクトを文字列に変換。
				result = is2String(is);
			}
			catch(MalformedURLException ex) {
				Log.e(DEBUG_TAG, "URL変換失敗", ex);
			}
			// タイムアウトの場合の例外処理。
			catch(SocketTimeoutException ex) {
				Log.w(DEBUG_TAG, "通信タイムアウト", ex);
			}
			catch(IOException ex) {
				Log.e(DEBUG_TAG, "通信失敗", ex);
			}
			finally {
				// HttpURLConnectionオブジェクトがnullでないなら解放。
				if(con != null) {
					con.disconnect();
				}
				// InputStreamオブジェクトがnullでないなら解放。
				if(is != null) {
					try {
						is.close();
					}
					catch(IOException ex) {
						Log.e(DEBUG_TAG, "InputStream解放失敗", ex);
					}
				}
			}
			WeatherInfoPostExecutor postExecutor = new WeatherInfoPostExecutor(result);
			_handler.post(postExecutor);
		}

		/**
		 * InputStreamオブジェクトを文字列に変換するメソッド。 変換文字コードはUTF-8。
		 *
		 * @param is 変換対象のInputStreamオブジェクト。
		 * @return 変換された文字列。
		 * @throws IOException 変換に失敗した時に発生。
		 */
		private String is2String(InputStream is) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuffer sb = new StringBuffer();
			char[] b = new char[1024];
			int line;
			while(0 <= (line = reader.read(b))) {
				sb.append(b, 0, line);
			}
			return sb.toString();
		}
	}

	/**
	 * 非同期でお天気情報を取得した後にUIスレッドでその情報を表示するためのクラス。
	 */
	private class WeatherInfoPostExecutor implements Runnable {
		/**
		 * 取得したお天気情報JSON文字列。
		 */
		private final String _result;

		/**
		 * コンストラクタ。
		 *
		 * @param result Web APIから取得したお天気情報JSON文字列。
		 */
		public WeatherInfoPostExecutor(String result) {
			_result = result;
		}

		@UiThread
		@Override
		public void run() {
			// 都市名。
			String cityName = "";
			// 天気。
			String weather = "";
			// 緯度
			String latitude = "";
			// 経度。
			String longitude = "";
			try {
				// ルートJSONオブジェクトを生成。
				JSONObject rootJSON = new JSONObject(_result);
				// 都市名文字列を取得。
				cityName = rootJSON.getString("name");
				// 緯度経度情報JSONオブジェクトを取得。
				JSONObject coordJSON = rootJSON.getJSONObject("coord");
				// 緯度情報文字列を取得。
				latitude = coordJSON.getString("lat");
				// 経度情報文字列を取得。
				longitude = coordJSON.getString("lon");
				// 天気情報JSON配列オブジェクトを取得。
				JSONArray weatherJSONArray = rootJSON.getJSONArray("weather");
				// 現在の天気情報JSONオブジェクトを取得。
				JSONObject weatherJSON = weatherJSONArray.getJSONObject(0);
				// 現在の天気情報文字列を取得。
				weather = weatherJSON.getString("description");
			}
			catch(JSONException ex) {
				Log.e(DEBUG_TAG, "JSON解析失敗", ex);
			}

			putCityName = cityName;
			putWeather = weather;

			// 画面に表示する「〇〇の天気」文字列を生成。
			String telop = cityName + "の天気";
			// 天気の詳細情報を表示する文字列を生成。
			String desc = "現在は" + weather + "です。";
			// 天気情報を表示するTextViewを取得。
			TextView tvWeatherTelop = findViewById(R.id.tvWeatherTelop);
			TextView tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
			// 天気情報を表示。
			tvWeatherTelop.setText(telop);
			tvWeatherDesc.setText(desc);
		}
	}

	public void SpotShow(View view){
		Intent intent = new Intent(MainActivity.this, com.websarva.wings.android.groupproject.SpotShow.class);

		intent.putExtra("etSearchWord3",putCityName);
		intent.putExtra("weather",putWeather);

		startActivity(intent);


	}
}

