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
	private EditText username;
	private EditText password;
	private ImageButton saveButton;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings);
        
        // get settings
        final SharedPreferences settings = getSharedPreferences(SP, 0);
        String usernamec=settings.getString("username", "");
        String passwordc=settings.getString("password", "");
        
        // get elements
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        
        // set the values!
        username.setText(usernamec);
        password.setText(passwordc);
        
        this.saveButton=(ImageButton) this.findViewById(R.id.save);
        this.saveButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(username.getText().length() > 3 && password.getText().length() > 3){
        			SharedPreferences.Editor editor = settings.edit();
        			editor.putString("username", username.getText().toString());
        			editor.putString("password", password.getText().toString());
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
