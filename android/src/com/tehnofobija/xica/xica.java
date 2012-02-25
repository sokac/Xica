package com.tehnofobija.xica;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class xica extends Activity {
	public static final String SP = "postavke";
	private ProgressDialog dialog;
	private String jmbag;
	private String jmbg;
	private SharedPreferences settings;
	protected getXica provjera;
	protected getRacun gRacun;
	protected ImageView imView; // slika korisnika, drugacije rijesiti?
	
		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				setContentView(R.layout.main);
				focusedW();
		}
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
			if((requestCode==1 || requestCode==2) && resultCode==1){
				focusedW();
			}
		}
		public void focusedW(){
				settings = getSharedPreferences(SP, 0);
				jmbag=settings.getString("jmbag", "");
				jmbg=settings.getString("jmbg", "");
				if(jmbag.length()!=10 && jmbg.length()!=13){  // first time?
					Toast.makeText(getApplicationContext(), R.string.wrongSettings, Toast.LENGTH_SHORT).show();
					Intent i1=new Intent(this, editSettings.class);
					startActivityForResult(i1, 1);
				}else{
					//
					dialog = new ProgressDialog(xica.this);
					provjera= new getXica();
					provjera.execute("run");
				}
		}
		
	    /** CREATE MENU **/
	    public boolean onCreateOptionsMenu(Menu menu){
	    	MenuInflater inflater = getMenuInflater();
	    	inflater.inflate(R.menu.menu, menu);    	
	    	return true;
		}
	    
	    
	    public boolean onOptionsItemSelected (MenuItem item){
			switch(item.getItemId()){
			case R.id.edit:
				Intent i1=new Intent(this, editSettings.class);
				startActivityForResult(i1, 1);
				return true;
			case R.id.about:
				final Dialog dialogR = new Dialog(xica.this);
				
				dialogR.setContentView(R.layout.about);
				dialogR.setTitle(getResources().getText(R.string.about));
				dialogR.show();
			}
			return false;
	    }
		
		protected class getXica extends AsyncTask<String, Void, String> {
			protected void onPreExecute() {
				dialog.setMessage(getResources().getText(R.string.checking));
				dialog.show();
			}
			protected String doInBackground(final String... args) {
				// HTTP REQUEST
				HttpClient httpclient = new DefaultHttpClient();  
					HttpPost httppost = new HttpPost("http://sokac.net/xica/xica.php");
					try {
							// Add your data  
							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
							nameValuePairs.add(new BasicNameValuePair("jmbag", jmbag));  
							nameValuePairs.add(new BasicNameValuePair("jmbg", jmbg));
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
				
							// Execute HTTP Post Request  
							HttpResponse response = httpclient.execute(httppost);  
							return EntityUtils.toString(response.getEntity());
					} catch (IOException e) {  
							// TODO Auto-generated catch block  
						e.printStackTrace();
						return "net";
					}
				// another thing!
					
			}
			protected void onPostExecute(final String reply) {
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				if(reply.length()<20 || reply.startsWith("FAIL")){
					if(reply.compareTo("net")==0){
						Toast.makeText(getApplicationContext(), R.string.errorConnectivity, Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getApplicationContext(), R.string.errorHandling, Toast.LENGTH_SHORT).show();
						Intent i1=new Intent(xica.this, editSettings.class);
								startActivityForResult(i1, 1);
					}
					return ;
				}
				JSONObject xicaStanje;
				try {
					xicaStanje = (JSONObject) new JSONTokener(reply).nextValue();
					
					// RENDERING
					TextView korisnik = (TextView)findViewById(R.id.korisnik);
					korisnik.setText(xicaStanje.getString("korisnik"));

					TextView razina=(TextView)findViewById(R.id.razina);
					razina.setText(""+xicaStanje.getString("razina"));
					
					
					TextView xstat=(TextView)findViewById(R.id.xstat);
					double potroseno_sada=xicaStanje.getDouble("potroseno_sada");
					double potroseno_kraj_mj=xicaStanje.getDouble("potroseno_kraj_mj");
					double stanje=xicaStanje.getDouble("stanje");
					double y=stanje - (potroseno_kraj_mj - potroseno_sada);
					
					DecimalFormatSymbols zarez=new DecimalFormatSymbols();
					zarez.setDecimalSeparator(',');
					DecimalFormat df = new DecimalFormat("#.00", zarez);
					
					
					xstat.setText(""+df.format(y)+" kn");
					
					if(y>=0)
						xstat.setTextColor(0xff00ff00);
					else
						xstat.setTextColor(0xffff0000);
					
					
					TextView stanjeTV = (TextView)findViewById(R.id.stanje);
					stanjeTV.setText(""+df.format(stanje)+" kn");
					
					TextView ostalo=(TextView)findViewById(R.id.ostalo);
					ostalo.setText(""+df.format(xicaStanje.getDouble("ostalo"))+" kn");
					imView = (ImageView)findViewById(R.id.slika);
					getSlika slika= new getSlika();
					slika.execute("http://www.cap.srce.hr"+xicaStanje.getString("slika"));
					

					// RACUNI
					JSONArray racuni=xicaStanje.getJSONArray("racuni");
					LinearLayout racuniT=(LinearLayout)findViewById(R.id.racuni);
					racuniT.removeAllViews();
					TextView racunTV;
					LinearLayout llracun;
					TableLayout tlracun;
					
					for(int i=racuni.length(); i>0; i--){
						View child = getLayoutInflater().inflate(R.layout.mainracun, null);
						llracun=(LinearLayout)child.findViewById(R.id.llracun);
						tlracun=(TableLayout)child.findViewById(R.id.tlzartikli);
						llracun.setTag(""+i);
						tlracun.setId(0x10020000+i);
						
						racunTV=(TextView)llracun.findViewById(R.id.restoran);
						racunTV.setText(""+racuni.getJSONObject(i-1).getString("restoran"));
						
						racunTV=(TextView)llracun.findViewById(R.id.vrijemer);
						racunTV.setText(""+racuni.getJSONObject(i-1).getString("vrijeme"));

						racunTV=(TextView)llracun.findViewById(R.id.iznosr);
						racunTV.setText(""+racuni.getJSONObject(i-1).getString("iznos")+"kn");
						
						// tap na racun
						llracun.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								gRacun= new getRacun();
								gRacun.setRacun(Integer.parseInt(v.getTag().toString()));
								gRacun.execute("run");
								ImageView desno=(ImageView)v.findViewWithTag("desnomax");
								desno.setImageResource(R.drawable.desno_overlay);
								v.setBackgroundResource(R.drawable.racun_overlay);
								v.setOnClickListener(new OnClickListener() {
									private int rid, isActive=1;
									@Override
									public void onClick(View v) {
										rid=Integer.parseInt(v.getTag().toString());
										ImageView desno=(ImageView)v.findViewWithTag("desnomax");
										// TODO Auto-generated method stub
										if(isActive==1){
											TableLayout lista=(TableLayout)findViewById(0x10020000+rid);
											lista.setVisibility(0x00000008);
											desno.setImageResource(R.drawable.desno);
											v.setBackgroundResource(R.drawable.racun);
											isActive=0;
										}else{
											TableLayout lista=(TableLayout)findViewById(0x10020000+rid);
											lista.setVisibility(0x00000000);
											desno.setImageResource(R.drawable.desno_overlay);
											v.setBackgroundResource(R.drawable.racun_overlay);
											isActive=1;
										}
									}
								});
							}
						});
						racuniT.addView(child);
					}
					//
					// END
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					Toast.makeText(getApplicationContext(), R.string.errorConnectivity, Toast.LENGTH_SHORT).show();
				}
			}
		}

		private class getRacun extends AsyncTask<String, Void, String> {
			private int racun;
			private TableLayout lista;
			
			public final void setRacun(int racun){
				this.racun=racun;
				this.lista=(TableLayout)findViewById(0x10020000+racun);
			}
			protected void onPreExecute() {
				TextView artiklTV= new TextView(getApplicationContext());
				artiklTV.setText(getString(R.string.loading));
				artiklTV.setGravity(1);
				artiklTV.setPadding(0, 10, 0, 10);
				lista.removeAllViews();
				lista.addView(artiklTV);
			}
			protected String doInBackground(final String... args) {
				// HTTP REQUEST
				HttpClient httpclient = new DefaultHttpClient();  
				HttpPost httppost = new HttpPost("http://sokac.net/xica/xica.php");
				try {
						// Add your data  
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);  
						nameValuePairs.add(new BasicNameValuePair("jmbag", jmbag));  
						nameValuePairs.add(new BasicNameValuePair("jmbg", jmbg));
						nameValuePairs.add(new BasicNameValuePair("racun", Integer.toString(racun)));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
						
						// Execute HTTP Post Request  
						HttpResponse response = httpclient.execute(httppost);  
						return EntityUtils.toString(response.getEntity());
				} catch (IOException e) {  
						// TODO Auto-generated catch block  
					return "net";
				}
			}
			protected void onPostExecute(final String reply) {
				if(reply.length()<50){
					Toast.makeText(getApplicationContext(), R.string.errorConnectivity, Toast.LENGTH_SHORT).show();
					return ;
				}
				JSONObject xicaRacun;
				try {
					xicaRacun = (JSONObject) new JSONTokener(reply).nextValue();
					TextView artiklTV;
					TableRow artiklTR;
					lista.removeAllViews();
					lista.setPadding(10, 10, 10, 10);
					// RACUNI
					JSONArray artikli=xicaRacun.getJSONArray("artikli");
					for(int i=0; i<artikli.length(); i++){
						// LIST ARTIKALA
						artiklTR = (TableRow)getLayoutInflater().inflate(R.layout.mainartikl, null);
						artiklTV=(TextView)artiklTR.findViewById(R.id.tvartikl);
						artiklTV.setText(""+artikli.getJSONObject(i).getString("naziv"));
						
						artiklTV=(TextView)artiklTR.findViewById(R.id.tviznos);
						artiklTV.setText(artikli.getJSONObject(i).getString("ukupno")+"kn");
						
						artiklTV=(TextView)artiklTR.findViewById(R.id.tvkol);
						artiklTV.setText(artikli.getJSONObject(i).getString("komada")+" kom");
						
						lista.addView(artiklTR);
						
					}
					// END
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Error: "+R.string.errorConnectivity, Toast.LENGTH_SHORT).show();
				}
			}
		}
		protected class getSlika extends AsyncTask<String, Void, Void>{
			protected Bitmap bmImg;
			protected Void doInBackground(final String... args) {
				URL myFileUrl;
				try {
					myFileUrl = new URL(args[0]);
					HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					bmImg = BitmapFactory.decodeStream(is);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(Void x){
				imView.setImageBitmap(bmImg);
			}
		}
}