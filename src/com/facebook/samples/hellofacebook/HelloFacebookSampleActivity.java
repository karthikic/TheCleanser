/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.samples.hellofacebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.*;

import java.io.IOException;
import java.util.*;

public class HelloFacebookSampleActivity extends FragmentActivity {

    private static final String PERMISSION = "publish_actions";
   
    private Button exit;
    private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";
    private Intent j;
    private Button postStatusUpdateButton;
    private Button postPhotoButton;
    private Button pickFriendsButton;
    private Button pickPlaceButton;
    private LoginButton loginButton;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private PendingAction pendingAction = PendingAction.NONE;
    private ViewGroup controlsContainer;
    private GraphUser user;
    private GraphPlace place;
    private List<GraphUser> tags;
    private boolean canPresentShareDialog;
    Bitmap bmp;
    Editable value;
    private int cameraData=0;
    int diff_pos = 1;
    String tag="";

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        
    }
    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        setContentView(R.layout.main);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                HelloFacebookSampleActivity.this.user = user;
                updateUI();
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                handlePendingAction();
            }
        });

        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);

        

        postPhotoButton = (Button) findViewById(R.id.postPhotoButton);
        postPhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	
            	int k=tag_select();
           /* 	if(k==1)
            	{
            	j = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(j, cameraData);
            	// onClickPostPhoto();
            }*/
            }	
        });
        exit=(Button)findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				finish();
			}
		});
        

       
        controlsContainer = (ViewGroup) findViewById(R.id.main_ui_container);

        final FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            // If we're being re-created and have a fragment, we need to a) hide the main UI controls and
            // b) hook up its listeners again.
            controlsContainer.setVisibility(View.GONE);
            
        }

        // Listen for changes in the back stack so we know if a fragment got popped off because the user
        // clicked the back button.
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fm.getBackStackEntryCount() == 0) {
                    // We need to re-show our UI.
                    controlsContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
        if (resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			bmp = (Bitmap) extras.get("data");
			onClickPostPhoto();
			
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    
    private int tag_select()
    {
    	final CharSequence[] items = { "BBMP", "TOI" };
    
		AlertDialog.Builder builder = new AlertDialog.Builder(HelloFacebookSampleActivity.this);
		final AlertDialog alert = builder.create();
		builder.setTitle("Select Tag");
		
		

		builder.setSingleChoiceItems(items, diff_pos,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if ("BBMP".equals(items[which])) {
							
							diff_pos = 0;
							tag="#BBMP_Complaint";
							dialog.dismiss();
							input();
					//		j = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						//	startActivityForResult(j, cameraData);
							
						} else if ("TOI".equals(items[which])) {
							
							diff_pos = 1;
							tag="#TOI_Complaint";
							dialog.dismiss();
							input();
						//	j = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
							//startActivityForResult(j, cameraData);

						}
						
					}
				});
		builder.show();

	
    	return 1;
    	
    	
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(HelloFacebookSampleActivity.this)
                    .setTitle(R.string.cancelled)
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
        updateUI();
    }

    public void input()
    {
    	
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Description");
    	alert.setMessage("Message");
    	alert.setMessage("Message");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	  value = input.getText();
    	  // Do something with value!
    	  dialog.dismiss();
    	  j = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
  		startActivityForResult(j, cameraData);
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    	    // Canceled.
    	  }
    	});

    	alert.show();
    	
    }
    private void updateUI() {
        Session session = Session.getActiveSession();
        boolean enableButtons = (session != null && session.isOpened());

        
        postPhotoButton.setEnabled(enableButtons);
        

        if (enableButtons && user != null) {
            profilePictureView.setProfileId(user.getId());
            greeting.setText(getString(R.string.hello_user, user.getFirstName()));
        } else {
            profilePictureView.setProfileId(null);
            greeting.setText(null);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                postPhoto();
                break;
           
        }
    }

    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            title = getString(R.string.success);
            String id = result.cast(GraphObjectWithId.class).getId();
            alertMessage = getString(R.string.successfully_posted_post, message, id);
        } else {
            title = getString(R.string.error);
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

  
    private FacebookDialog.ShareDialogBuilder createShareDialogBuilder() {
        return new FacebookDialog.ShareDialogBuilder(this)
                .setName("Hello Facebook")
                .setDescription("The 'Hello Facebook' sample application showcases simple Facebook integration")
                .setLink("http://developers.facebook.com/android");
    }

    

    private void onClickPostPhoto() {
        performPublish(PendingAction.POST_PHOTO, false);
    }

    private void postPhoto() {
        if (hasPublishPermission()) {
           // Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon);
        	
            Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), bmp, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    showPublishResult(getString(R.string.photo_post), response.getGraphObject(), response.getError());
                }
            });
            double latitude=0.0;
            double longitude=0.0;
            
            GPSTracker gps;
            gps = new GPSTracker(HelloFacebookSampleActivity.this);
            if(gps.canGetLocation()){
                
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                 
                // \n is for new line
                //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();    
            }else{
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
           
            Bundle params = request.getParameters();
            //params.putString("message", "#prasad1234");


Geocoder geocoder;
List<Address> addresses;
geocoder = new Geocoder(this, Locale.getDefault());
try {
	addresses = geocoder.getFromLocation(latitude, longitude, 1);
	String address = addresses.get(0).getAddressLine(0);
	String city = addresses.get(0).getAddressLine(1);
	String country = addresses.get(0).getAddressLine(2);
	
	
	 params.putString("message",tag+"\n"+"Address: " +address+" "+city+" "+country +"\nDescription :"+value);
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}




           
            request.setParameters(params);
            Request.executeBatchAsync(request);
            //request.executeAsync();
        } else {
            pendingAction = PendingAction.POST_PHOTO;
        }
    }


    private void showPickerFragment(PickerFragment<?> fragment) {
        fragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
            @Override
            public void onError(PickerFragment<?> pickerFragment, FacebookException error) {
                String text = getString(R.string.exception, error.getMessage());
                Toast toast = Toast.makeText(HelloFacebookSampleActivity.this, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        controlsContainer.setVisibility(View.GONE);

        // We want the fragment fully created so we can use it immediately.
        fm.executePendingTransactions();

        fragment.loadData(false);
    }

   

  
    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    private void performPublish(PendingAction action, boolean allowNoSession) {
        Session session = Session.getActiveSession();
        if (session != null) {
            pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSION));
                return;
            }
        }

        if (allowNoSession) {
            pendingAction = action;
            handlePendingAction();
        }
    }
}
