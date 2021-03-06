package com.hackaholic.trainpanda.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hackaholic.trainpanda.R;
import com.hackaholic.trainpanda.ServiceHandler.ServiceHandler;
import com.hackaholic.trainpanda.helpers.API;
import com.hackaholic.trainpanda.helpers.EnumType;
import com.hackaholic.trainpanda.helpers.GetPostClass;
import com.hackaholic.trainpanda.helpers.PrefUtils;
import com.hackaholic.trainpanda.utility.ExpandableLayout;
import com.hackaholic.trainpanda.utility.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherFragment extends Fragment
{
	ProgressDialog pb2,pb;
	private TextView weather_tv_description;
	private TextView weather_tv_humidity;
	private TextView weather_tv_low;
	private TextView weather_tv_max;
	private TextView weather_tv_min;
	private TextView weather_tv_pressure;
	private TextView weather_tv_rain;
	private TextView weather_tv_temprature;
	private TextView weather_tv_wind,txtPressure,txtTempMIN,txtTempMAX;
	ImageView imageCloud;
	String ST_CODE;
	ExpandableTextView txtFamousFor,txtLetestEvents;
	String LATITUDE,LONGITUDE;
	LinearLayout famousLinear,latestLinear;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View row=inflater.inflate(R.layout.weather, container,false);


		LATITUDE = PrefUtils.getCurrentLatitude(getActivity());
		LONGITUDE = PrefUtils.getCurrentLongitude(getActivity());

		initializeTextViews(row);

		famousLinear = (LinearLayout)row.findViewById(R.id.famousLinear);
		latestLinear = (LinearLayout)row.findViewById(R.id.latestLinear);

		fetchStationINFO();

		return row;
	}
	
	private void initializeTextViews(View row)
	{
		txtLetestEvents=(ExpandableTextView)row.findViewById(R.id.txtLetestEvents);
		txtFamousFor=(ExpandableTextView)row.findViewById(R.id.txtFamousFor);

		txtTempMIN =(TextView)row.findViewById(R.id.txtTempMIN);
		txtTempMAX =(TextView)row.findViewById(R.id.txtTempMAX);

		imageCloud = (ImageView)row.findViewById(R.id.imageCloud);

		txtPressure=(TextView)row.findViewById(R.id.txtPressure);

		weather_tv_description=(TextView)row.findViewById(R.id.weather_tv_description);
		weather_tv_humidity=(TextView)row.findViewById(R.id.weather_tv_humidity);
		//weather_tv_low=(TextView)row.findViewById(R.id.weather_tv_low);
		//weather_tv_max=(TextView)row.findViewById(R.id.weather_tv_max);
		//weather_tv_min=(TextView)row.findViewById(R.id.weather_tv_min);
		//weather_tv_pressure=(TextView)row.findViewById(R.id.weather_tv_pressure);
		//weather_tv_rain=(TextView)row.findViewById(R.id.weather_tv_rain);
		weather_tv_temprature=(TextView)row.findViewById(R.id.weather_tv_temprature);
		weather_tv_wind=(TextView)row.findViewById(R.id.weather_tv_wind);


		txtLetestEvents.setTypeface(PrefUtils.getTypeFace(getActivity()));
		txtFamousFor.setTypeface(PrefUtils.getTypeFace(getActivity()));

		txtTempMIN.setTypeface(PrefUtils.getTypeFace(getActivity()));
		txtTempMAX.setTypeface(PrefUtils.getTypeFace(getActivity()));

		txtPressure.setTypeface(PrefUtils.getTypeFace(getActivity()));
		weather_tv_description.setTypeface(PrefUtils.getTypeFace(getActivity()));
		weather_tv_humidity.setTypeface(PrefUtils.getTypeFace(getActivity()));

		weather_tv_temprature.setTypeface(PrefUtils.getTypeFace(getActivity()));
		weather_tv_wind.setTypeface(PrefUtils.getTypeFace(getActivity()));
	}

	private void fetchStationINFO(){

		pb =new ProgressDialog(getActivity());
		pb.setMessage("Loading details...");
		pb.show();

		ST_CODE = PrefUtils.getCurrentStationCode(getActivity());

		String url= API.BASE_URL+"stations?filter[where][code]="+ST_CODE;


		Log.e("Url : ", "" + url);

		new GetPostClass(url, EnumType.GET) {
			@Override
			public void response(String result) {
				pb.dismiss();
				Log.e("######res station info", result);

				try {

					if(result!=null)
					{
						try
						{
							JSONArray stationInfoArray = new JSONArray(result);
							JSONObject jsonObject=stationInfoArray.getJSONObject(0);

							String famousFor = jsonObject.getString("famousFor");

							if(famousFor.toString().trim().length()==0){
								famousLinear.setVisibility(View.GONE);
								txtFamousFor.setVisibility(View.GONE);
								txtFamousFor.setText("No Data Found");
							}else{
								famousLinear.setVisibility(View.VISIBLE);
								txtFamousFor.setVisibility(View.VISIBLE);
								txtFamousFor.setText(famousFor);
							}

							latestLinear.setVisibility(View.GONE);
							txtLetestEvents.setVisibility(View.GONE);



							fetchWeather(jsonObject.getString("latitude"),jsonObject.getString("longitude"));

						}
						catch(Exception e)
						{
							famousLinear.setVisibility(View.GONE);
							latestLinear.setVisibility(View.GONE);
							txtFamousFor.setVisibility(View.GONE);
							txtLetestEvents.setVisibility(View.GONE);
							txtLetestEvents.setText("No Data Found");
							txtFamousFor.setText("No data Found");
							Log.e("$$$$$$exc", e.toString());

						}
					}
					else
					{
						printMessage("Response is null...!");
					}



				} catch (Exception e) {
					Log.e("$$$$$$exc", e.toString());
				}

			}

			@Override
			public void error(String error) {
				pb.dismiss();
//                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
			}
		}.call2();
	}


	private void  fetchWeather(String latitude,String longtitude){

        pb2 =new ProgressDialog(getActivity());
        pb2.setMessage("Loading details...");
        pb2.show();


		String url="http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon=" + longtitude;
		Log.e("Url : ", "" + url);

        new GetPostClass(url, EnumType.GET) {
            @Override
            public void response(String result) {
                pb2.dismiss();
                Log.e("########res weather ", result);

                try {

					if(result!=null)
					{
						try
						{
							JSONObject jsonObject=new JSONObject(result);
							//Get coord
							JSONObject cordJsonObject=jsonObject.getJSONObject("coord");
							String longitude=cordJsonObject.getString("lon");
							String latitude=cordJsonObject.getString("lat");
							//Get sys
							JSONObject sysJsonObject=jsonObject.getJSONObject("sys");
							String message=sysJsonObject.getString("message");
							String country=sysJsonObject.getString("country");
							String sunrise=sysJsonObject.getString("sunrise");
							String sunset=sysJsonObject.getString("sunset");

							//Get Weather
							JSONArray weatherJsonArray=jsonObject.getJSONArray("weather");
							JSONObject jsonObject2=weatherJsonArray.getJSONObject(0);
							String id=jsonObject2.getString("id");
							String main=jsonObject2.getString("main");
							String description=jsonObject2.getString("description");
							String icon=jsonObject2.getString("icon");

							//Get Base
							String base=jsonObject.getString("base");

							//Get Main
							JSONObject mainJsonObject=jsonObject.getJSONObject("main");
							String temp=mainJsonObject.getString("temp");
							String temp_min=mainJsonObject.getString("temp_min");
							String temp_max=mainJsonObject.getString("temp_max");
							String pressure=mainJsonObject.getString("pressure");

							String humidity=mainJsonObject.getString("humidity");

							//Get Wind
							JSONObject windJsonObject=jsonObject.getJSONObject("wind");
							String speed=windJsonObject.getString("speed");
							String deg=windJsonObject.getString("deg");

							//Get clouds
							JSONObject cloudsJsonObject=jsonObject.getJSONObject("clouds");
							String all=cloudsJsonObject.getString("all");

							//Get miscellaneous
							String dt=jsonObject.getString("dt");
							String idm=jsonObject.getString("id");
							String name=jsonObject.getString("name");
							String code=jsonObject.getString("cod");


							//Set Data

							Glide.with(getActivity()).load("http://www.openweathermap.org/img/w/"+icon+".png").thumbnail(0.1f).into(imageCloud);
						//	Picasso.with(getActivity().getBaseContext()).load("http://www.openweathermap.org/img/w/"+icon+".png").placeholder(R.drawable.a6).into(imageCloud);

							weather_tv_wind.setText("Wind - "+speed+" KM/H"+" ");
							txtPressure.setText("Pressure - "+pressure);


							txtTempMIN.setText("Temp Min. "+temp_min.substring(0,2)+"\u00b0"+" C");
							txtTempMAX.setText("Temp Max. "+temp_max.substring(0,2)+"\u00b0"+" C");

							weather_tv_humidity.setText("Humidity - "+humidity+" %"+" ");
							weather_tv_temprature.setText(temp.substring(0,2)+"\u00b0"+" C");
							weather_tv_description.setText("" + description.toUpperCase()+" ");

					/*weather_tv_min.setText("Min Temp - "+temp_min.substring(0,2)+" ");
					weather_tv_max.setText("Max Temp - "+temp_max.substring(0,2)+" ");
					weather_tv_pressure.setText("Pressure - "+pressure+" ");
					weather_tv_description.setText("Description - "+description+" ");

					weather_tv_temprature.setText(temp.substring(0,2));*/




						}
						catch(Exception e)
						{
							Log.e("$$$$$$exc", e.toString());
							e.printStackTrace();
						}
					}
					else
					{
						printMessage("Response is null...!");
					}



                } catch (Exception e) {
                    Log.e("$$$$$$exc", e.toString());
                }

            }

            @Override
            public void error(String error) {
                pb2.dismiss();
//                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
			}
        }.call2();
    }


	private class MyBackgroundAsync extends AsyncTask<String, Void, String>
	{
		private ProgressDialog dialog;
		private Context context;
		
		public MyBackgroundAsync(Context context)
		{
			this.context=context;
		}
		
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			dialog=new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Please Wait...!");
			dialog.show();
		}
		
		@Override
		protected String doInBackground(String... params)
		{			
			return hitServer(params[0]);
		}
		
		private String hitServer(String value)
		{
			String response=null;
			//String url="http://api.openweathermap.org/data/2.5/weather?q="+value+"&units=metric";
			String url="http://api.openweathermap.org/data/2.5/weather?lat="+LATITUDE+"&lon="+LONGITUDE;
			Log.e("Url : ", "" + url);
			try
			{
				ServiceHandler handler=new ServiceHandler();
				response=handler.makeServiceCall(url,ServiceHandler.GET);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			dialog.dismiss();
			Log.e("response",result.toString());

			if(result!=null)
			{
				try
				{
					JSONObject jsonObject=new JSONObject(result);
					//Get coord
					JSONObject cordJsonObject=jsonObject.getJSONObject("coord");
					String longitude=cordJsonObject.getString("lon");
					String latitude=cordJsonObject.getString("lat");
					//Get sys
					JSONObject sysJsonObject=jsonObject.getJSONObject("sys");
					String message=sysJsonObject.getString("message");
					String country=sysJsonObject.getString("country");
					String sunrise=sysJsonObject.getString("sunrise");
					String sunset=sysJsonObject.getString("sunset");
					
					//Get Weather
					JSONArray weatherJsonArray=jsonObject.getJSONArray("weather");
					JSONObject jsonObject2=weatherJsonArray.getJSONObject(0);
					String id=jsonObject2.getString("id");
					String main=jsonObject2.getString("main");
					String description=jsonObject2.getString("description");
					String icon=jsonObject2.getString("icon");
					
					//Get Base
					String base=jsonObject.getString("base");
					
					//Get Main 
					JSONObject mainJsonObject=jsonObject.getJSONObject("main");
					String temp=mainJsonObject.getString("temp");
					String temp_min=mainJsonObject.getString("temp_min");
					String temp_max=mainJsonObject.getString("temp_max");
					String pressure=mainJsonObject.getString("pressure");
					String sea_level=mainJsonObject.getString("sea_level");
					String grnd_level=mainJsonObject.getString("grnd_level");
					String humidity=mainJsonObject.getString("humidity");
					
					//Get Wind
					JSONObject windJsonObject=jsonObject.getJSONObject("wind");
					String speed=windJsonObject.getString("speed");
					String deg=windJsonObject.getString("deg");
					
					//Get clouds
					JSONObject cloudsJsonObject=jsonObject.getJSONObject("clouds");
					String all=cloudsJsonObject.getString("all");
					
					//Get miscellaneous
					String dt=jsonObject.getString("dt");
					String idm=jsonObject.getString("id");
					String name=jsonObject.getString("name");
					String code=jsonObject.getString("cod");
					
					//Set Data

					/*weather_tv_min.setText("Min Temp - "+temp_min.substring(0,2)+" ");
					weather_tv_max.setText("Max Temp - "+temp_max.substring(0,2)+" ");
					weather_tv_pressure.setText("Pressure - "+pressure+" ");
					weather_tv_description.setText("Description - "+description+" ");
					weather_tv_wind.setText("Wind - "+speed+"Kmph"+" ");
					weather_tv_humidity.setText("Humidity - "+humidity+"%"+" ");
					weather_tv_temprature.setText(temp.substring(0,2));*/
				
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				printMessage("Response is null...!");
			}
		}
	}
	
	private void printMessage(String msg)
	{
		Toast.makeText(getActivity(), "" + msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		
		
        
	}
	
}
