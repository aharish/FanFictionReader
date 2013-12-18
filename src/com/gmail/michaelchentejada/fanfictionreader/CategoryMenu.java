/**
 * 
 */
package com.gmail.michaelchentejada.fanfictionreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.ToggleButton;

/**
 * The class which shows the main categories in the fan fiction site.
 * @author Michael Chen
 */
public class CategoryMenu extends Activity {
	
	private Context context;
	private ProgressDialog progress;
	private ListView listView;
	private parseSite asynctask = new parseSite();
	private final OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
			Log.d("Category Menu","Id:" + id + " & url:" + list.get((int)id).get(Parser.URL));
			Parser.Stories("http://www.fanfiction.net" + list.get((int)id).get(Parser.URL));
			// TODO Auto-generated method stub
			
		}
	};

	private final OnItemSelectedListener sortlistener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			System.out.println(arg2+""+arg3);
			Collections.sort(list, new ListComparator(arg2));
			SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.category_menu_list_item, new String[] {Parser.TITLE,Parser.VIEWS}, new int[] {R.id.category_menu_title,R.id.category_menu_views});
			listView.setAdapter(adapter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {		
		}
	};

	private final OnCheckedChangeListener crossoverListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
			String url = isChecked ? "http://www.fanfiction.net/crossovers"+FANFIC_URLS[id] : "http://www.fanfiction.net"+FANFIC_URLS[id];
			asynctask.cancel(true);
			asynctask =  new parseSite();
			asynctask.execute(url);
		}
	};
	private static final String[] FANFIC_URLS = {"/anime/",
		"/book/",
		"/cartoon/",
		"/comic/",
		"/game/",
		"/misc/",
		"/movie/",
		"/play/",
		"/tv/"}; 
	private ArrayList<HashMap<String, String>> list;
	private int id = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_category_menu);
		
		context = this;
		listView = (ListView) findViewById(R.id.categoryMenuListView);
		View header = (View)getLayoutInflater().inflate(R.layout.category_menu_header, null);
		listView.addHeaderView(header);
		listView.setOnItemClickListener(listener);
		
		Spinner spinner = (Spinner)header.findViewById(R.id.category_menu_header_sort);
		ArrayAdapter<CharSequence> sortAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.SortBy));
		sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(sortAdapter);
		spinner.setOnItemSelectedListener(sortlistener);
		
		ToggleButton crossoverButton = (ToggleButton)findViewById(R.id.category_menu_header_crossover_selector);
		crossoverButton.setOnCheckedChangeListener(crossoverListener);
		
		if (savedInstanceState == null){
			id = getIntent().getIntExtra("Id", 0);
			boolean crossover = getIntent().getBooleanExtra("Crossover", false);
				
			if (id >FANFIC_URLS.length){
				end(getResources().getString(R.string.dialog_unspecified));
			}else{
				setResult(RESULT_OK);
				String url = crossover ? "http://www.fanfiction.net/crossovers"+FANFIC_URLS[id] : "http://www.fanfiction.net"+FANFIC_URLS[id];
				asynctask.execute(url);
			}
			
		}else {
			list = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("List");
			SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.category_menu_list_item, new String[] {Parser.TITLE,Parser.VIEWS}, new int[] {R.id.category_menu_title,R.id.category_menu_views});
			listView.setAdapter(adapter);
		}
}
	
	/**
	 * Saves the current list to avoid having to fetch it again upon screen rotation & pause.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("List", list);
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Ends the current intent. Only called when an error occurs. Passes a string containing the error message
	 * to be displayed as a toast on the previous activity.
	 * @param error The error message to display.
	 */
	private void end(String error){
		Intent end = new Intent();
		end.putExtra("Error", error);
		setResult(RESULT_CANCELED, end);
		finish();
	}
	
	/**
	 * The asynchronous class which obtains the list of categories from fanfiction.net. 
	 * @author Michael Chen
	 *
	 */
	private class parseSite extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>>{

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(context);
			progress.setTitle("");
			progress.setMessage(getResources().getString(R.string.dialog_loading));
			progress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
					
				}
			});
			progress.show();
			super.onPreExecute();
		}
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... url) {
			return Parser.Categories(url[0]);
		}
		
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			// To dismiss the dialog
			progress.dismiss();
			if (result != null) {
				list = result;
				SimpleAdapter adapter = new SimpleAdapter(context, result, R.layout.category_menu_list_item, new String[] {Parser.TITLE,Parser.VIEWS}, new int[] {R.id.category_menu_title,R.id.category_menu_views});
				listView.setAdapter(adapter);				
			}else{
				end(getResources().getString(R.string.dialog_internet));	
			}
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * Stops the asynchronous task if the screen is closed prematurely. 
	 */
	@Override
	protected void onDestroy() {
		asynctask.cancel(true);
		super.onDestroy();
	}
}