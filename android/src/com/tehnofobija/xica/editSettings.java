package com.tehnofobija.xica;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class editSettings extends Activity{
	public static final String SP = "postavke";
	private EditText jmbag;
	private EditText jmbg;
	private ImageButton saveButton;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings);
        
        // get settings
        final SharedPreferences settings = getSharedPreferences(SP, 0);
        String jmbagc=settings.getString("jmbag", "");
        String jmbgc=settings.getString("jmbg", "");
        
        // get elements
        jmbag = (EditText) findViewById(R.id.jmbag);
        jmbg = (EditText) findViewById(R.id.jmbg);
        
        // set the values!
        jmbag.setText(jmbagc);
        jmbg.setText(jmbgc);
        
        this.saveButton=(ImageButton) this.findViewById(R.id.save);
        this.saveButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(jmbag.getText().length()==10 && jmbg.getText().length()==13){
        			SharedPreferences.Editor editor = settings.edit();
        			editor.putString("jmbag", jmbag.getText().toString());
        			editor.putString("jmbg", jmbg.getText().toString());
        			editor.commit();
            		setResult(1);
            		finish();
        		}
        		else{
        			Toast.makeText(getApplicationContext(), R.string.wrongSettings, Toast.LENGTH_SHORT).show();
        		}
        	}
        });
    }
}
