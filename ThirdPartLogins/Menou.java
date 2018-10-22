package com.project.jvvas.moneyskills;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Menou extends AppCompatActivity implements View.OnClickListener{

    private int coins;
    private Intent intent ;
    private TextView textViewCoins;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
	private ImageView mImageViewProfil ;
    private Button mLogoutBtn , mTrainingBtn , mSingleBtn , mMultiBtn ;


    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove Bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_single_multi_player);

        // Initialize buttons
		initializeButtons();
      
        // Initialize Firebase auth;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null)
        {
            // Retrieve the coins of the user from the db
            final DatabaseReference ref = database.getReference("Users").child(user.getUid()) ;

            // Attach a listener to read the data at our reference
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User currentUser = dataSnapshot.getValue(User.class) ;
                    coins   = currentUser.coins ;

                    // Create a string from the int 'coins'
                    String stringCoins = Integer.toString(coins);

                    textViewCoins.setText(stringCoins.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

        intent = getIntent();
		
		// ------------------- Click On ---------------
        mImageViewProfil.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
        mTrainingBtn.setOnClickListener(this);
        mSingleBtn.setOnClickListener(this);
        mMultiBtn.setOnClickListener(this);
    }

	public void initializeButtons()
	{
		mImageViewProfil    = (ImageView) findViewById(R.id.imageView_Profil);
        mLogoutBtn          = (Button) findViewById(R.id.Logout_btn);
        mTrainingBtn        = (Button) findViewById(R.id.button_Training);
        mSingleBtn          = (Button) findViewById(R.id.button_SinglePlayer);
        mMultiBtn           = (Button) findViewById(R.id.button_MultiPlayer);
        textViewCoins       = (TextView) findViewById(R.id.textView_Coins) ;
	}
	
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.Logout_btn:
                userLogout();
                break;

            case R.id.imageView_Profil:
                showProfile();
                break;

            case R.id.button_Training:
                // startTraining()
                break;

            case R.id.button_SinglePlayer:
                startSingle();
                break;

            case R.id.button_MultiPlayer:
                // startMulti();
                break;
        }
    }



    public void userLogout()
    {
        //String provider = user.getProviders().get(0);

        // if there is internet connection
        if(isNetworkAvailable())
        {
            // Sign out From Firebase
            mAuth.signOut();

            updateUI();
        }
        else
        {
            Toast.makeText(SingleMultiPlayer.this, "No network connection.", Toast.LENGTH_LONG).show();
        }
		
		// -----------------------   SECOND WAY (WITH THE PROVIDERS)	-----------------------
/*
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String provider = user.getProviders().get(0);

                // Sign out From Firebase
                mAuth.signOut();

                if(provider.equals("facebook.com"))
                {
                    // Sign out from Facebook
                    LoginManager.getInstance().logOut();
                }
                else if(provider.equals("google.com") || provider.equals("twitter.com")) {
                    // Sign out from G-mail
                    FirebaseAuth.getInstance().signOut();
                }

                updateUI();
            }
        });
*/

    }

    private void showProfile()
    {
        // if there is internet connection
        if(isNetworkAvailable())
        {
            Intent profileIntent = new Intent(SingleMultiPlayer.this , ProfilActivity.class);
            startActivity(profileIntent);
            finish();
        }
        else
        {
            Toast.makeText(SingleMultiPlayer.this, "No network connection.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            updateUI();
        }
    }

    public void  updateUI(){
        Toast.makeText(SingleMultiPlayer.this, "You're logged out ", Toast.LENGTH_LONG).show();

        Intent accountIntent = new Intent(SingleMultiPlayer.this,LoginActivity.class);
        startActivity(accountIntent);
        finish();
    }

    public void startSingle()
    {
        Intent accountIntent = new Intent(SingleMultiPlayer.this,GameActivity.class);
        startActivity(accountIntent);
        finish();
    }

    public void startMulti()
    {
        // if there is internet connection
        if(isNetworkAvailable())
        {
            Intent accountIntent = new Intent(SingleMultiPlayer.this,WaitingMulti.class);
            startActivity(accountIntent);
            finish();
        }
        else
        {
            Toast.makeText(SingleMultiPlayer.this, "No network connection.", Toast.LENGTH_LONG).show();
        }

    }

    public void exitBut(View v)
    {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
