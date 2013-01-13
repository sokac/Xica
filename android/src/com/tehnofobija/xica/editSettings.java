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
	private EditText email;
	private EditText password;
	private ImageButton saveButton;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings);
        
        // get settings
        final SharedPreferences settings = getSharedPreferences(SP, 0);
        String emailSaved=settings.getString("email", "");
        String passwordSaved=settings.getString("password", "");
        
        // get elements
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        
        // set the values!
        email.setText(emailSaved);
        password.setText(passwordSaved);
        
        this.saveButton=(ImageButton) this.findViewById(R.id.save);
        this.saveButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(email.getText().length() > 5 && password.getText().length() > 3){
        			SharedPreferences.Editor editor = settings.edit();
        			editor.putString("email", email.getText().toString());
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
