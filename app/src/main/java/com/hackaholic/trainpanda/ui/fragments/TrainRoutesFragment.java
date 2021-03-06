package com.hackaholic.trainpanda.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.GsonBuilder;
import com.hackaholic.trainpanda.R;
import com.hackaholic.trainpanda.ServiceHandler.ServiceHandler;

import com.hackaholic.trainpanda.custom.ComplexPreferences;
import com.hackaholic.trainpanda.helpers.EnumType;
import com.hackaholic.trainpanda.helpers.GetPostClass;
import com.hackaholic.trainpanda.helpers.PrefUtils;
import com.hackaholic.trainpanda.ui.activities.SlidingActivity;
import com.hackaholic.trainpanda.utility.CustomDialogBoxEditPNR;
import com.hackaholic.trainpanda.utility.ExpandableLayout;
import com.hackaholic.trainpanda.utility.ExpandableLayoutItem;
import com.hackaholic.trainpanda.utility.ExpandableLayoutListView;
import com.hackaholic.trainpanda.utility.ExpandablePanel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import Model.LIVE_STATUS;
import Model.PNR;
import Model.TRAIN_NAMES;
import Model.TopCategories;
import Model.TopMenuItems;
import Model.TopSubCategories;

public class TrainRoutesFragment extends Fragment implements OnClickListener {

	private ArrayList<String> al_booking_status;
	private ArrayList<String> al_current_status;
	private ArrayList<String> al_no;
	private String totalPassengers = "";
	ExpandableLayoutListView expandableLayoutListView;
	private TextView train_route_train_name;
	private TextView train_route_train_code;
	private ListView train_route_listview, pnr_listview;
	private AutoCompleteTextView train_route_actv_train_number;
	private ProgressBar progressBar_train_routes;
	ProgressDialog pb;
	private TextView train_route_tv_go;
	String tNO, PNR,DOJ;
	LIVE_STATUS objLive;
	String newDOJ;
	ExpandablePanel panel;
	LinearLayout pnr_expand, ll_pnr_third, ll_pnr_fourth;
	ArrayList<String> al_code;
	private final String[] array = {"Hello", "World", "Android", "is", "Awesome", "World", "Android", "is", "Awesome", "World", "Android", "is", "Awesome", "World", "Android", "is", "Awesome"};
	PNR cuurentPNR;
	ArrayList<String> al_lat;
	ArrayList<String> al_scharr;
	ArrayList<String> al_fullname;
	ArrayList<String> al_schdep;
	ArrayList<String> al_state;
	ArrayList<String> al_no1;
	ArrayList<String> al_lng;
	ExpandableLayout pnrSLider;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null && args.containsKey("trainNo")) {
			tNO = args.getString("trainNo");

		}
		if (args != null && args.containsKey("pnr")) {
			PNR = args.getString("pnr");
			Log.e("pnr", PNR);
		}
		if (args != null && args.containsKey("doj")) {
			DOJ = args.getString("doj");
			Log.e("doj", DOJ);

			try{

				DateFormat originalFormat = new SimpleDateFormat("dd-m-yyyy", Locale.ENGLISH);
				DateFormat targetFormat = new SimpleDateFormat("yyyymmdd");
				Date date = originalFormat.parse(DOJ);
				newDOJ = targetFormat.format(date);  // 20120821

				Log.e("new DOJ",""+newDOJ.toString());
			}catch (Exception e){
				Log.e("exc in date conv",e.toString());
			}
		}

	}

	private  void closePNRSlider(){
		pnrSLider.hide();
		View content = pnrSLider.getHeaderLayout();
		ImageView icon = (ImageView)content.findViewById(R.id.expand);

		icon.setBackgroundResource(R.drawable.finaldown);
	}

	private  void openPNRSlider(){
		View content = pnrSLider.getHeaderLayout();
		ImageView icon = (ImageView)content.findViewById(R.id.expand);
		icon.setBackgroundResource(R.drawable.finalup);
		pnrSLider.show();
	}

	public TrainRoutesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		TextView title = (TextView) getActivity().findViewById(R.id.lk_profile_header_textview);
		title.setText("PNR : " + PNR);

		ImageView imgToolbarOption = (ImageView) getActivity().findViewById(R.id.imgToolbarOption);
		imgToolbarOption.setVisibility(View.VISIBLE);

		imgToolbarOption.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				CustomDialogBoxEditPNR cdbox = new CustomDialogBoxEditPNR(getActivity(),android.R.style.Theme_Translucent_NoTitleBar);
				cdbox.show();
			}
		});


		View rootView = inflater.inflate(R.layout.train_route, container, false);

		pnrSLider = (ExpandableLayout)rootView.findViewById(R.id.first);

		pnrSLider.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				View content = pnrSLider.getHeaderLayout();
				ImageView icon = (ImageView)content.findViewById(R.id.expand);
				if(pnrSLider.isOpened()){
					icon.setBackgroundResource(R.drawable.uu);
				}else{
					icon.setBackgroundResource(R.drawable.dd);
				}
			}
		});


		initializeViews(rootView);


		ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getActivity(), "user_pref", 0);
		cuurentPNR = complexPreferences.getObject("current-pnr", PNR.class);


		ListView pnr_listview1 = (ListView) rootView.findViewById(R.id.pnr_listview);

		//Static Code
		if(PNR.equals("1234567890")){
			pnr_listview1.setAdapter(
					new MyAdapterPNR(cuurentPNR, getActivity(),true));
		}//Static code ends
		else {
			pnr_listview1.setAdapter(
					new MyAdapterPNR(cuurentPNR, getActivity(),false));
		}

		openPNRSlider();

		return rootView;

	}

	private void initializeViews(View rootView) {
		initializeTextViews(rootView);
		initiaiizeListViews(rootView);
		progressBar_train_routes = (ProgressBar) rootView.findViewById(R.id.progressBar_train_routes);
		//AutoCompleteTextview for source stations
		train_route_actv_train_number = (AutoCompleteTextView) rootView.findViewById(R.id.train_route_actv_train_number);
		train_route_actv_train_number.setThreshold(1);
		train_route_actv_train_number.setText(tNO);
		//pnr_expand = (LinearLayout)rootView.findViewById(R.id.ll_pnr_first);


		pnr_listview = (ListView) rootView.findViewById(R.id.pnr_listview);

		train_route_tv_go.performClick();
	}




	private void initiaiizeListViews(View rootView) {


		train_route_listview = (ListView) rootView.findViewById(R.id.train_route_listview);
		train_route_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {

				/*String code = al_code.get(position);
				PrefUtils.setCurrentStationCode(getActivity(),code);
				Intent i = new Intent(getActivity(),SlidingActivity.class);
				startActivity(i);*/

				String code = al_code.get(position);
				String FullStationName = al_fullname.get(position);


				String latitude = al_lat.get(position);
				String longitude = al_lng.get(position);

				//Setting which tab by default we have to open
				PrefUtils.setTab(getActivity(), "Tab 1");

				PrefUtils.setCurrentStationCode(getActivity(), code);

				PrefUtils.setCurrentLatitude(getActivity(), latitude);
				PrefUtils.setCurrentLongitude(getActivity(), longitude);

				Bundle bun = new Bundle();
				bun.putString("stName", FullStationName+" ("+code+")");


				SlidingFragment fragment = new SlidingFragment();
				fragment.setArguments(bun);
				FragmentManager fragmentManager22 = getFragmentManager();
				fragmentManager22.beginTransaction().replace(R.id.lk_profile_fragment, fragment).commit();

				/*PrefUtils.setCurrentStationCode(getActivity(),code);
				FragmentManager fragmentManager2 = getFragmentManager();

				fragmentManager2.beginTransaction()
						.replace(R.id.lk_profile_fragment,new SlidingFragment()).commit();*/

			}
		});


		//To set alarm
		train_route_listview.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(getActivity(), "Set Alarm ?", Toast.LENGTH_LONG).show();
				return true;
			}
		});
	}

	private void initializeTextViews(View rootView) {
		train_route_train_name = (TextView) rootView.findViewById(R.id.train_route_train_name);
		train_route_train_code = (TextView) rootView.findViewById(R.id.train_route_train_code);
		train_route_tv_go = (TextView) rootView.findViewById(R.id.train_route_tv_go);
		train_route_tv_go.setOnClickListener(this);
	}


private void hitServerLiveStatus() {

	String url = "http://api.railwayapi.com/live/train/" + tNO + "/doj/" + newDOJ + "/apikey/" + getResources().getString(R.string.key1) + "/";
	Log.e("####live status url",url);

	pb =new ProgressDialog(getActivity());
	pb.setMessage("Fetching live status...");
	pb.show();


	new GetPostClass(url , EnumType.GET) {
		@Override
		public void response(String response) {
			pb.dismiss();
			//Log.e("live status", response.toString());
			objLive = new GsonBuilder().create().fromJson(response.toString(), LIVE_STATUS.class);

			int startPos=0;
			int endPos=0;
			for(int i=0;i<al_code.size();i++){

				if(cuurentPNR.from_station.code.equalsIgnoreCase(al_code.get(i))){
					Log.e("PNR ST",cuurentPNR.from_station.code);
					Log.e("START ST",al_code.get(i));
					Log.e("START pos", "" + i);
					startPos = i;
				}

				if(cuurentPNR.to_station.code.equalsIgnoreCase(al_code.get(i))){
					Log.e("PNR ST",cuurentPNR.to_station.code);
					Log.e("END ST",al_code.get(i));
					Log.e("END pos",""+i);
					endPos = i;
				}
			}




			//Static Code
			if(PNR.equals("1234567890")){
				train_route_listview.setAdapter(new MyAdapter(getActivity(), objLive,
						al_lat, al_scharr, al_fullname, al_schdep, al_state, al_no, al_code, al_lng, 1, 5));

			}//end
			else{
				train_route_listview.setAdapter(new MyAdapter(getActivity(), objLive,
						al_lat, al_scharr, al_fullname, al_schdep, al_state, al_no, al_code, al_lng, startPos, endPos));

			}


			// Automatically close the pnr slider after 3 Seconds
			new CountDownTimer(2500,1000){

				@Override
				public void onTick(long l) {

				}

				@Override
				public void onFinish() {
					closePNRSlider();
				}
			}.start();



		}

		@Override
		public void error(String error) {
			pb.dismiss();
			Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
		}
	}.call2();



	}


	private class TrainRouteAsync extends AsyncTask<String, Void, String>
	{
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog=new ProgressDialog(getActivity());
			dialog.setCancelable(false);
			dialog.setMessage("Please Wait...!");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params)
		{
			return hitServer(params[0]);
		}

		private String hitServer(String url)
		{
			String response=null;
			try
			{
				ServiceHandler handler=new ServiceHandler();
				response=handler.makeServiceCall(url, ServiceHandler.GET);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);

			dialog.dismiss();

			//Initialize Arraylist
			 al_lat=new ArrayList<String>();
			 al_scharr=new ArrayList<String>();
			 al_fullname=new ArrayList<String>();
			 al_schdep=new ArrayList<String>();
			 al_state=new ArrayList<String>();
			 al_no=new ArrayList<String>();


			 al_code=new ArrayList<String>();

			al_lng=new ArrayList<String>();
			if(result!=null)
			{
				try
				{
					JSONObject jsonObject=new JSONObject(result);
					System.out.println("Response from server :: "+result);
					JSONObject trainJsonObject=jsonObject.getJSONObject("train");
					String train_number=trainJsonObject.getString("number");
					String train_name=trainJsonObject.getString("name");



					//Get Route Array
					JSONArray routeJsonArray=jsonObject.getJSONArray("route");
					for(int i=0;i<routeJsonArray.length();i++)
					{
						JSONObject jsonObject2=routeJsonArray.getJSONObject(i);
						al_lat.add(jsonObject2.getString("lat"));
						al_scharr.add(jsonObject2.getString("scharr"));
						al_fullname.add(jsonObject2.getString("fullname"));
						al_schdep.add(jsonObject2.getString("schdep"));
						al_state.add(jsonObject2.getString("state"));
						al_no.add(jsonObject2.getString("no"));
						al_code.add(jsonObject2.getString("code"));
						//System.out.println(al_code+"ALCODE");
						al_lng.add(jsonObject2.getString("lng"));
					}


					train_route_train_name.setText(" "+train_name+" ");
					train_route_train_code.setText(" "+train_number+" ");
				} 
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}

			hitServerLiveStatus();


		}
	}



	private class MyAdapterPNR extends BaseAdapter
	{
		private PNR valuesPNR;
		private int rows;
		private Context context;
		boolean isStaticCode;
		public MyAdapterPNR(PNR currentpnr,Context context,boolean staticode)
		{
			this.isStaticCode = staticode;
			this.valuesPNR = currentpnr;
			this.context=context;
			Log.e("pnr : ", "" + this.valuesPNR.pnr);
			Log.e("Cchart preaperd : ", "" + this.valuesPNR.chart_prepared);
			Log.e("train name : ", "" + this.valuesPNR.train_name);
			Log.e("Total Passengers : ", "" + this.valuesPNR.total_passengers);
		}
		@Override
		public int getCount()
		{
			if(isStaticCode)
				return 1;
			else
			return valuesPNR.passengers.size();
		}
		@Override
		public Object getItem(int position)
		{
			if(isStaticCode)
				return 0;
			else
				return	valuesPNR.passengers.get(position);
		}
		@Override
		public long getItemId(int position)
		{
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row=convertView;

				LayoutInflater inflater=(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
				row=inflater.inflate(R.layout.single_row_pnr,parent,false);


				TextView single_row_pnr_tv_passengers=(TextView)row.findViewById(R.id.single_row_pnr_tv_passengers);
				TextView single_row_pnr_tv_seats=(TextView)row.findViewById(R.id.single_row_pnr_tv_seats);
				TextView single_row_pnr_tv_status=(TextView)row.findViewById(R.id.single_row_pnr_tv_status);

				single_row_pnr_tv_passengers.setTypeface(PrefUtils.getTypeFace(getActivity()));
				single_row_pnr_tv_seats.setTypeface(PrefUtils.getTypeFace(getActivity()));
				single_row_pnr_tv_status.setTypeface(PrefUtils.getTypeFace(getActivity()));


				if(isStaticCode){
					single_row_pnr_tv_passengers.setText("Passenger 1");
					single_row_pnr_tv_seats.setText("W/L 45");
					single_row_pnr_tv_status.setText("W/L 10");
				}else {
					single_row_pnr_tv_passengers.setText("Passenger " + valuesPNR.passengers.get(position).no);
					single_row_pnr_tv_seats.setText(valuesPNR.passengers.get(position).booking_status + "");
					single_row_pnr_tv_status.setText(valuesPNR.passengers.get(position).current_status + "");
				}


			return row;
		}
	}













	private class MyAdapter extends BaseAdapter
	{

		private Context context;
		private ArrayList<String> al_lat;
		private ArrayList<String> al_scharr;
		private ArrayList<String> al_fullname;
		private ArrayList<String> al_schdep;
		private ArrayList<String> al_state;
		private ArrayList<String> al_no;
		private ArrayList<String> al_code;
		private ArrayList<String> al_lng;
		LIVE_STATUS valuesLiveStatus;
		int STARTPOS,ENDPOS;

		public MyAdapter(Context context,LIVE_STATUS obj,ArrayList<String> al_lat,ArrayList<String> al_scharr,
				ArrayList<String> al_fullname,ArrayList<String> al_schdep,ArrayList<String> al_state,
				ArrayList<String> al_no,ArrayList<String> al_code,ArrayList<String> al_lng,int start,int end)
		{
			this.valuesLiveStatus = obj;
			this.context=context;
			this.al_lat=al_lat;
			this.al_scharr=al_scharr;
			this.al_fullname=al_fullname;
			this.al_schdep=al_schdep;
			this.al_state=al_state;
			this.al_no=al_no;
			this.al_code=al_code;
			this.al_lng=al_lng;
			this.STARTPOS = start;
			this.ENDPOS = end;
		}

		@Override
		public int getCount() 
		{
			Log.e("size of tarian st", "" + al_lat.size());
			return al_lat.size();
		}

		@Override
		public Object getItem(int position)
		{
			return al_lat.get(position);
		}

		@Override
		public long getItemId(int position) 
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			boolean isDestination=false;
			View row=convertView;
			MyHolder holder;

			if(row==null)
			{
				holder=new MyHolder();

				LayoutInflater inflater=(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
				row=inflater.inflate(R.layout.single_row_train_route, parent,false);


				holder.mainParent = (RelativeLayout)row.findViewById(R.id.mainParent);

				holder.train_route_arival_time=(TextView) row.findViewById(R.id.train_route_arival_time);
				holder.train_route_departure_time=(TextView) row.findViewById(R.id.train_route_departure_time);
				holder.train_route_avg_delay_time=(TextView) row.findViewById(R.id.train_route_avg_delay_time);
				holder.train_route_distance_next_station=(TextView) row.findViewById(R.id.train_route_distance_next_station);
				holder.train_route_halt_time=(TextView) row.findViewById(R.id.train_route_halt_time);
				holder.train_route_train_name=(TextView) row.findViewById(R.id.train_route_train_name);
				holder.liveStatus=(TextView) row.findViewById(R.id.liveStatus);
				holder.train_route_avg_delay_time=(TextView) row.findViewById(R.id.train_route_avg_delay_time);

				holder.train_route_arival_time.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.train_route_departure_time.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.train_route_avg_delay_time.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.train_route_distance_next_station.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.train_route_halt_time.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.train_route_train_name.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.liveStatus.setTypeface(PrefUtils.getTypeFace(getActivity()));
				holder.train_route_avg_delay_time.setTypeface(PrefUtils.getTypeFace(getActivity()));


				row.setTag(holder);
				//Log.e("ALCODE ",""+al_code);
				LinearLayout linearRoute = (LinearLayout)row.findViewById(R.id.linearRoute);


			}
			else
			{
				holder=(MyHolder) row.getTag();	
			}


			holder.train_route_arival_time.setText(" - "+al_scharr.get(position)+" ");
			holder.train_route_departure_time.setText(" - "+al_schdep.get(position)+" ");
			holder.train_route_train_name.setText(""+al_fullname.get(position)+" ");


			if(position>=STARTPOS && position<=ENDPOS){
				holder.mainParent.setBackgroundColor(Color.parseColor("#152846"));
			}else{
				//#424c53
				holder.mainParent.setBackgroundColor(Color.parseColor("#ffffff"));
			}


			holder.train_route_avg_delay_time.setText(""+valuesLiveStatus.route.get(position).status);



			if(!al_scharr.get(position).trim().equals("Source") && !al_schdep.get(position).trim().equals("Destination") )
			{
				holder.train_route_halt_time.setText(calculateHaltTime(al_scharr.get(position).trim(),
						al_schdep.get(position).trim()));
			}
			else if(al_scharr.get(position).trim().equals("Source") )
			{
				holder.train_route_halt_time.setText("S");
			}
			else if(al_schdep.get(position).trim().equals("Destination") )
			{
				holder.train_route_halt_time.setText("D");
			}
			else 
			{
				holder.train_route_halt_time.setText(".....");
			}


			// Setting the live status
			SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
			String traintime="";
			try {
				traintime = valuesLiveStatus.route.get(position).scharr;
			}catch (Exception e){
				Log.e("---- EXCe1",""+ e.toString());
			}


			try {


				Date schTime = sdfTime.parse(traintime);

				Date nowTime = new Date();

				String strTime = sdfTime.format(nowTime);

				if (schTime.after(nowTime)) {
					holder.liveStatus.setText("Crossed");
				} else if (schTime.before(nowTime)) {
					holder.liveStatus.setText("Upcoming");
				} else if (schTime.equals(nowTime)) {
					holder.liveStatus.setText("Current");
				}

				Log.e("##### MY time",""+strTime);
			}catch (Exception e){
				Log.e("---- EXCe2",""+ e.toString());
				holder.liveStatus.setText("" +valuesLiveStatus.route.get(position).scharr);
			}









			return row;
		}
	}

	private static class MyHolder
	{
		TextView train_route_arival_time,train_route_departure_time,
		train_route_avg_delay_time,train_route_distance_next_station,train_route_halt_time
		,train_route_train_name,liveStatus;

		RelativeLayout mainParent;
	}

	private String calculateHaltTime(String start_time,String end_time)
	{
		String str="";
		try 
		{
			DateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date start = format.parse(start_time);
			Date end = format.parse(end_time);

			long difference = end.getTime() - start.getTime();

			long hours = TimeUnit.MILLISECONDS.toHours(difference);
			long minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60;

			if(String.valueOf(hours).length()==1 && String.valueOf(minutes).length()==1)
			{
				str="0"+hours+":0"+minutes;
			}
			else if(String.valueOf(hours).length()==1 && String.valueOf(minutes).length()==2)
			{
				str="0"+hours+":"+minutes;
			}
			else if(String.valueOf(hours).length()==2 && String.valueOf(minutes).length()==1)
			{
				str=""+hours+":0"+minutes;
			}
			else if(String.valueOf(hours).length()==2 && String.valueOf(minutes).length()==2)
			{
				str=""+hours+":"+minutes;
			}
			else
			{
				str="";
			}
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return str;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.train_route_tv_go:
			if(!train_route_actv_train_number.getText().toString().trim().equals(""))
			{
				String url="http://api.railwayapi.com/route/train/"+train_route_actv_train_number.getText().toString().trim()+"/apikey/"+getResources().getString(R.string.key1)+"/";
				new TrainRouteAsync().execute(url);
			}
			else
			{
				Toast.makeText(getActivity(), "Please Enter Train Number...!", Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

}
