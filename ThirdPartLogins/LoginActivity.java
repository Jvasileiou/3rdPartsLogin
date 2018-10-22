package com.project.jvvas.moneyskills;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;


public class  LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG_TW = "TWITTER";
    public static final String TAG_FB = "FACELOG";
    public static final String TAG_GMAIL = "GMAILLOG";
    private static final int RC_SIGN_IN = 1;
    private static int numberTwitter = 0;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private TwitterAuthClient mTwitterClient;

	private Button buttonLogin;
	private ProgressBar progressBar;
    private ImageView imageViewEyeHide , imageViewEyeShow ;
    private TextView textViewSignUp , textViewForgotPassword;
    private EditText editTextEmail, editTextPassword;
    private ImageView mFacebookImg,mGoogleImg, mTwitterImg;


    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove Bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();

        // https://github.com/twitter/twitter-kit-android/wiki/Log-In-with-Twitter
        // https://github.com/twitter/twitter-kit-android/wiki
        // Configure twitter SDK
        TwitterAuthConfig authConfig = new TwitterAuthConfig
                (   getString(R.string.CONSUMER_KEY) ,
                        getString(R.string.CONSUMER_SECRET_KEY)     );

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this).twitterAuthConfig(authConfig).build();

        // Initialize Twitter
        Twitter.initialize(twitterConfig);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // set Layout
        setContentView(R.layout.activity_login);

		// Initialize the Buttons
		initializeButtons();

        // ----------- Click On -----------
        imageViewEyeHide.setOnClickListener(this);
        imageViewEyeShow.setOnClickListener(this);
        textViewSignUp  .setOnClickListener(this);
        buttonLogin     .setOnClickListener(this);
        mFacebookImg    .setOnClickListener(this);
        mGoogleImg      .setOnClickListener(this);
        mTwitterImg     .setOnClickListener(this);
        textViewForgotPassword.setOnClickListener(this);


        // Create hash key for Facebook Login
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.project.jvvas.moneyskills" ,
                     PackageManager.GET_SIGNATURES) ;
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                int d = Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

	public void initializeButtons()
	{
		imageViewEyeShow = findViewById(R.id.imageView_eyeShow);
        imageViewEyeShow.setVisibility(View.GONE);
        imageViewEyeHide = findViewById(R.id.imageView_eyeHide);
        textViewSignUp   = findViewById(R.id.textView_Signup);
        buttonLogin      = findViewById(R.id.button_ResetPassword);
        editTextEmail    = findViewById(R.id.editText_RegisteredEmail);
        editTextPassword = findViewById(R.id.editText_Password);
        progressBar      = findViewById(R.id.progress_Bar);
        mFacebookImg     = findViewById(R.id.image_FacebookLogin);
        mGoogleImg       = findViewById(R.id.image_GoogleLogin);
        // Custom Twiiter Button --> http://developers-club.com/posts/250907/
        mTwitterImg      = findViewById(R.id.image_TwitterLogin);
        textViewForgotPassword = findViewById(R.id.textView_ForgotPassword);
	}	
	
    @Override
    public void onStart()
    {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUIFacebookAuth();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.textView_Signup:
                startActivity(new Intent(this, SignUpActivity.class));
                break;

            case R.id.button_ResetPassword:
                userLogin();
                break;

            case R.id.image_FacebookLogin:
                loginWithFacebook();
                break;

            case R.id.image_GoogleLogin:
                signIn();
                break;

            case R.id.image_TwitterLogin:
                loginWithTwitter();
                break;

            case R.id.textView_ForgotPassword:
                resetPassword();
                break;

            case R.id.imageView_eyeHide:
                showPassword();
                break;

            case R.id.imageView_eyeShow:
                hidePassword();
                break;

        }
    }

    private void showPassword()
    {
        imageViewEyeShow.setVisibility(View.VISIBLE);
        imageViewEyeHide.setVisibility(View.GONE);

        editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private void hidePassword()
    {
        imageViewEyeHide.setVisibility(View.VISIBLE);
        imageViewEyeShow.setVisibility(View.GONE);

        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void resetPassword()
    {
        startActivity(new Intent(LoginActivity.this , PasswordActivity.class));
        finish();
    }

    // G-MAIL - FACEBOOK - TWITTER ---- > Take the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(numberTwitter == 1 ){
            mTwitterClient.onActivityResult(requestCode, resultCode, data);
        }


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)  // G-MAIL
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException e)
            {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else // FACEBOOK
        {
            // Pass the activity result back to the FACEBOOK SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }




    //////////////////////////////  TWITTER  ////////////////////////////////////////////




    public void loginWithTwitter()
    {
        numberTwitter = 1 ;

        mTwitterClient = new TwitterAuthClient();
        mTwitterClient.authorize(LoginActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(LoginActivity.this, "Signed into twitter succesfully with " + result, Toast.LENGTH_SHORT).show();
                signToFirebaseWithTwitterSession(result.data);
                //updateButtons();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(LoginActivity.this, "failure", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void signToFirebaseWithTwitterSession(TwitterSession session)
    {
        Log.d(TAG_TW , "handleTwitterSession:" + session);

        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_TW , "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();


                            final String fullNameTwitter = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString() ;
                            final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString() ;

                            // Store the additional fields in db
                            User userTwitter = new User(fullNameTwitter , email , "00" , "00" , "00");

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(userTwitter)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);

                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(), "User Registered successful", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            progressBar.setVisibility(View.GONE);

                            updateUITwitterAuth();
                            //updateUITwitterAuth(user);
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);

                            // If sign in fails, display a message to the user.
                            Log.w(TAG_TW , "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUITwitterAuth(null);
                        }
                    }
                });
    }

    public void updateUITwitterAuth(){
        Toast.makeText(LoginActivity.this, "You're logged in ", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this,SingleMultiPlayer.class);
        startActivity(intent);
        finish();
    }



    //////////////////////////////// G-MAIL ///////////////////////////////////////////


    // When pressed sign in button G-MAIL
    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG_GMAIL, "firebaseAuthWithGoogle:" + acct.getId());

        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_GMAIL, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();


                            final String fullNameGmail = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString() ;
                            final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString() ;

                            // Store the additional fields in db
                            User userTwitter = new User(fullNameGmail , email , "00" , "00" , "00");

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(userTwitter)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);

                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(), "User Registered successful", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                            progressBar.setVisibility(View.GONE);

                            updateUIGmailAuth();
                            //updateUIGmailAuth(user);
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);

                            // If sign in fails, display a message to the user.
                            Log.w(TAG_GMAIL, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();
                            //updateUIGmailAuth(null);
                        }

                    }
                });
    }

    public void updateUIGmailAuth()
    {
        Toast.makeText(LoginActivity.this, "You're logged in ", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this,SingleMultiPlayer.class);
        startActivity(intent);
        finish();
    }





    //////////////////////////////// FACEBOOK ///////////////////////////////////////////




    public void loginWithFacebook(){

        // Request permissions
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                Arrays.asList("email", "public_profile")); // add-request for more stuff
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_FB, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.d(TAG_FB, "facebook:onCancel");
            }
            @Override
            public void onError(FacebookException error) {
                Log.d(TAG_FB, "facebook:onError", error);
            }
        });
    }

    public void updateUIFacebookAuth()
    {
        Toast.makeText(LoginActivity.this, "You're logged in ", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this,SingleMultiPlayer.class);
        startActivity(intent);
        finish();
    }


    // After a user successfully signs in
    // exchange it for a Firebase credential
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG_FB, "handleFacebookAccessToken:" + token);

        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_FB, "signInWithCredential:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            final String fullNameFb = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString() ;
                            final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString() ;

                            // Store the additional fields in db
                            User user = new User(fullNameFb , email , "00" , "00" , "00");

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);

                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(), "User Registered successful", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            progressBar.setVisibility(View.GONE);

                            updateUIFacebookAuth();
                        } else {
                            progressBar.setVisibility(View.GONE);

                            // If sign in fails, display a message to the user.
                            Log.w(TAG_FB, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            //mFacebookImg.setEnabled(true);
                            //updateUIFacebookAuth(null);
                        }
                    }
                });
    }



    //////////////////////////////// APP LOGIN  ///////////////////////////////////////////


    private void userLogin(){

        String email    =  editTextEmail.getText().toString().trim();
        String password =  editTextPassword.getText().toString().trim();

        if(email.isEmpty()){
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid email.");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length() < 6 ){
            editTextPassword.setError("Minimum length of password is 6.");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);

                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, SingleMultiPlayer.class);

                    // clear all activitities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}