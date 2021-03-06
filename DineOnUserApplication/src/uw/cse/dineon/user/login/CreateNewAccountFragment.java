package uw.cse.dineon.user.login;

import uw.cse.dineon.user.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Fragment view that show to the user a screen to place credentials.
 * These credentials are the used to create an account.
 * @author mhotan
 */
public class CreateNewAccountFragment extends Fragment {

	private OnCreateNewAccountListener mListener;

	// Input from users
	private EditText mUsername, mEmail, mPassword, mPasswordRepeat; 

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_create_new_account,
				container, false);

		mUsername = (EditText) view.findViewById(R.id.input_createnewaccount_username);
		mEmail = (EditText) view.findViewById(R.id.input_createnewaccount_email);
		mPassword = (EditText) view.findViewById(R.id.input_createnewaccount_password);
		mPasswordRepeat = (EditText) view.findViewById(
				R.id.input_createnewaccount_repeat_password);
		
		Button createAccountButton = (Button) view.findViewById(R.id.button_create_account);
		createAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createAccout();
			}
		});
		
		Button facebookButton = (Button) view.findViewById(R.id.button_createnewaccount_facebook);
		facebookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Fix
				mListener.onLoginWithFacebook();
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnCreateNewAccountListener) {
			mListener = (OnCreateNewAccountListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet MyListFragment.OnItemSelectedListener");
		}
	}
	
	/**
	 * Attempts to create an account with current state of the view.
	 */
	private void createAccout() {
		String username = mUsername.getText().toString();
		String email = mEmail.getText().toString();
		String password = mPassword.getText().toString();
		String passwordRepeat = mPasswordRepeat.getText().toString();
		
		mListener.onCreateNewAccount(username, email, password, passwordRepeat);
	}

	/**
	 * Listener for this class.
	 * @author mhotan
	 */
	public interface OnCreateNewAccountListener {

		/**
		 * Attempts to create a new password.
		 * @param username String
		 * @param email String
		 * @param password String
		 * @param passwordRepeat String
		 */
		void onCreateNewAccount(String username, String email, 
				String password, String passwordRepeat);
		
		/**
		 * User decides that he or she rather log in via Facebook.
		 */
		void onLoginWithFacebook();
		
	}

}
