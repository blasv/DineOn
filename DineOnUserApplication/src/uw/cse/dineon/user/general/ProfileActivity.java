package uw.cse.dineon.user.general;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import uw.cse.dineon.library.UserInfo;
import uw.cse.dineon.user.DineOnUserActivity;
import uw.cse.dineon.user.DineOnUserApplication;
import uw.cse.dineon.user.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Activity that manages the profile and settings fragments.
 * 
 * @author mhotan
 */
public class ProfileActivity extends DineOnUserActivity implements 
		ProfileEditFragment.InfoChangeListener {

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private static final String TAG = ProfileActivity.class.getSimpleName();
	private Fragment frag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frame = new FrameLayout(this);
		frame.setId(10101010);
		setContentView(frame, 
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
	//	setContentView(R.layout.activity_profile_and_settings);
		if(savedInstanceState == null) {
		frag = ProfileImageFragment.newInstance(DineOnUserApplication.getUserInfo());
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			ft.add(10101010, frag, "image_frag");
			ft.addToBackStack(null);
			ft.commit();	
		}
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// Note that override this method does not mean the actualy
		//  UI Menu is updated this is done manually
		//  See basic_menu under res/menu for ids
		inflater.inflate(R.menu.profile_menu, menu);
		//Hides the 
		final android.view.MenuItem ITEM = menu.findItem(R.id.option_bill);
		ITEM.setEnabled(true);
		ITEM.setVisible(true);

		final android.view.Menu M = menu;

		//Sets the necessary onClickListeners for the menu
		//items with an action layout.
		List<android.view.MenuItem> customActionBarButtons = new ArrayList<android.view.MenuItem>();
		customActionBarButtons.add(menu.findItem(R.id.option_bill));

		setOnClick(M, customActionBarButtons);

		return true;
	}
	
	/**
	 * Creates the onClick listeners for the specified menu items.
	 * 
	 * @param m the parent menu
	 * @param items the list of MenuItems to create listeners for
	 */
	private void setOnClick(final android.view.Menu m, List<android.view.MenuItem> items) {
		for (final android.view.MenuItem ITEM : items) {
			ITEM.getActionView().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {   
					m.performIdentifierAction(ITEM.getItemId(), 0);
				}
			});
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option_edit_profile:
			
			Fragment frag = 
				ProfileEditFragment.newInstance(DineOnUserApplication.getUserInfo());
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			
			ft.replace(10101010, frag);
			ft.addToBackStack(null);
			
			ft.commit();
			break;
		case R.id.option_logout:
			ParseUser.logOut();
			startLoginActivity();
			break;
		default:
			//Unknown
			Log.e(TAG, "None of the specified action items were selected.");
		}
		return true;
	}

	@Override
	public void onUserInfoUpdate(UserInfo user) {
		
		user.saveInBackGround(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(getApplicationContext(),
							"User Info Updated!", Toast.LENGTH_LONG)
							.show();
				} else {
					Log.e(TAG, e.getMessage() + " #" + e.getCode());
				}
			}
		});
	}

	@Override
	public UserInfo getInfo() {
		return DineOnUserApplication.getUserInfo();
	}
}
