package uw.cse.dineon.user.restaurantselection;

import java.util.List;

import uw.cse.dineon.library.DineOnUser;
import uw.cse.dineon.library.RestaurantInfo;
import uw.cse.dineon.library.image.DineOnImage;
import uw.cse.dineon.library.image.ImageCache.ImageGetCallback;
import uw.cse.dineon.library.image.ImageObtainable;
import uw.cse.dineon.user.DineOnUserApplication;
import uw.cse.dineon.user.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Fragment that presents the information about a particular restaurant.
 * @author mhotan
 */
public class RestaurantInfoFragment extends Fragment {

	private static final int IMAGEVIEW_WIDTH = 250;
	private static final int IMAGEVIEW_HEIGHT = 250;
	
	private static final String TAG = RestaurantInfoFragment.class.getSimpleName(); 

	private RestaurantInfoListener mListener;

	private RestaurantInfo mRestaurant;

	// UI Components
	private TextView mRestNameLabel, mAddressLabel, mHoursLabel;
	private RatingBar mRatingBar;
	private LinearLayout mGallery;

	@Override
	public void onCreate(Bundle onSavedInstance) {
		super.onCreate(onSavedInstance);
		setRetainInstance(true);

		// Get the list from the activity.
		mRestaurant = mListener.getCurrentRestaurant();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_restaurant_info,
				container, false);

		mRestNameLabel = (TextView) view.findViewById(R.id.label_restaurant_info);
		mAddressLabel = (TextView) view.findViewById(R.id.label_restaurant_address);
		mHoursLabel = (TextView) view.findViewById(R.id.label_restaurant_hours_header);
		mRatingBar = (RatingBar) view.findViewById(R.id.ratingbar_restaurant);
		mGallery = (LinearLayout) view.findViewById(R.id.gallery_restaurant_images);

		// Update the display
		setRestaurantForDisplay(mRestaurant);

		return view;
	}



	/**
	 * Sets display features for this fragment to this argument.
	 * @param restaurant Restaurant to present
	 */
	public void setRestaurantForDisplay(RestaurantInfo restaurant) {
		if (restaurant == null) {
			Log.e(TAG, "Unable to populate fragment with null restaurant");
			return;
		}

		// Update the reference 
		if (restaurant == mRestaurant) {
			Log.w(TAG, "Attempted to set the same restaurant multiple times for same display");
			return;
		}
		
		this.mRestaurant = restaurant;

		mRestNameLabel.setText(mRestaurant.getName());
		mAddressLabel.setText(mRestaurant.getAddr());
		mHoursLabel.setText("Hell yeah... 24/7!");
		
		// TODO Fix this so it is not random.
		mRatingBar.setRating((float)(Math.random() * mRatingBar.getNumStars()));
		
		// Dump the current views in the galler
		mGallery.removeAllViews();
		
		List<DineOnImage> images = mRestaurant.getImageList();
		for (DineOnImage image : images) {
			// Create a view for each image.
			final Context CTX = getActivity();
			final ViewGroup CONTAINER = getStanderdLinearLayout(CTX); 
			// Fill with place holder
			CONTAINER.addView(getLoadingImageProgressDialog(CTX));
			
			// Ask the listener to get the image
			mListener.onGetImage(image, new ImageGetCallback() {
				
				@Override
				public void onImageReceived(Exception e, Bitmap b) {
					// Upon completion remove all the views
					CONTAINER.removeAllViews();
					if (e == null) {
						// Add a view for each image
						CONTAINER.addView(produceView(CTX, b));
					} else {
						// Produce a view for unable to dl
						CONTAINER.addView(
								produceView(CTX, R.drawable.restaurant_photo_placeholder));	
					}
					CONTAINER.invalidate();
				}
			});
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof RestaurantInfoListener) {
			mListener = (RestaurantInfoListener) activity;
		} 
		else {
			throw new ClassCastException(activity.toString()
					+ " must implement MyListFragment.OnItemSelectedListener");
		}
	}

	/**
	 * Determines whether the given restaurant is a favorite of the current user.
	 * @param ib ImageButton
	 * @param ri RestaurantInfo 
	 */
	public void determineFavorite(ImageButton ib, RestaurantInfo ri) {
		DineOnUser dou = DineOnUserApplication.getDineOnUser();
		if(dou == null) {
			Log.d(TAG, "Your DineOnUser was null.");
			return;
		}
		if(this.mRestaurant == null) {
			Log.d(TAG, "The RestaurantInfo was null for some reason.");
			return;
		}
		assignFavoriteImageResource(ib, dou, ri);
		ib.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ImageButton ib = (ImageButton) v;
				if(ib.getTag().equals("notFavorite")) {
					favoriteRestaurant(mRestaurant);
				}
				else if(ib.getTag().equals("isFavorite")) {
					unFavoriteRestaurant(mRestaurant);
				}	
			}
		});

	}

	/**
	 * Assigns the given restaurant as a favorite of the given user.
	 * @param ib ImageButton
	 * @param dou DineOnUser to add a favorite to
	 * @param ri RestaurantInfo to add to the user's favorites
	 */
	public void assignFavoriteImageResource(ImageButton ib,
			DineOnUser dou,
			RestaurantInfo ri) {
		if(!dou.isFavorite(ri)) {
			ib.setImageResource(R.drawable.addtofavorites);
			ib.setTag("notFavorite");
		} else {
			ib.setImageResource(R.drawable.is_favorite);
			ib.setTag("isFavorite");
		}
	}

	/**
	 * Add restaurant as a favorite to the current user.
	 * @param ri RestaurantInfo
	 */
	public void favoriteRestaurant(RestaurantInfo ri) {
		DineOnUserApplication.getDineOnUser().addFavorite(ri);
		ImageButton ib = (ImageButton) this.getView().findViewById(R.id.button_user_favorites);
		if(ib != null) {
			this.assignFavoriteImageResource(ib, DineOnUserApplication.getDineOnUser(), ri);
		}
		DineOnUserApplication.getDineOnUser().saveInBackGround(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if(e != null) {
					Log.d(TAG, "the callback for saving favorited failed.\n" + e.getMessage());
				}
			}
		});
	}

	/**
	 * Delete restaurant from current user's favorite list.
	 * @param ri RestaurantInfo
	 */
	public void unFavoriteRestaurant(RestaurantInfo ri) {
		DineOnUserApplication.getDineOnUser().removeFavorite(ri);
		ImageButton ib = (ImageButton) this.getView().findViewById(R.id.button_user_favorites);
		if(ib != null) {
			this.assignFavoriteImageResource(ib, DineOnUserApplication.getDineOnUser(), ri);
		}
		DineOnUserApplication.getDineOnUser().saveInBackGround(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if(e != null) {
					Log.d(TAG, "the callback for saving unfavorited failed.\n" + e.getMessage());
				}
			}
		});
	}

	/**
	 * Gets a new instance of the correct layout of that is capable of holding an
	 * ImageView made with produceView.  This is particular to this fragment.
	 * @param ctx Context to create Layout in.
	 * @return LinearLayout that contain an ImageView
	 */
	private static LinearLayout getStanderdLinearLayout(Context ctx) {
		LinearLayout layout = new LinearLayout(ctx);
		layout.setLayoutParams(new LayoutParams(IMAGEVIEW_WIDTH, IMAGEVIEW_HEIGHT));
		layout.setGravity(Gravity.CENTER);
		return layout;
	}
	
	/**
	 * This produces an image view that contains the bitmap.
	 * @param ctx Context to create image view in
	 * @param b Bitmap to create a view with.  
	 * @return ImageView with image centered and cropped appropiately.
	 */
	private static ImageView produceView(Context ctx, Bitmap b) {
		ImageView imageView = new ImageView(ctx);
		imageView.setLayoutParams(new LayoutParams(
				IMAGEVIEW_WIDTH, 
				IMAGEVIEW_HEIGHT));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setImageBitmap(b);
		return imageView;
	}
	
	/**
	 * This produces an image view that contains the bitmap.
	 * @param ctx Context to create image view in
	 * @param resId Resource id of image
	 * @return ImageView with image centered and cropped appropiately.
	 */
	private static ImageView produceView(Context ctx, int resId) {
		ImageView imageView = new ImageView(ctx);
		imageView.setLayoutParams(new LayoutParams(
				IMAGEVIEW_WIDTH, 
				IMAGEVIEW_HEIGHT));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setImageResource(resId);
		return imageView;
	}
	
	/**
	 * Returns general image loading progess dialog.
	 * @param ctx Context to create progress bar in.
	 * @return Indeterminate progress bar.
	 */
	private static View getLoadingImageProgressDialog(Context ctx) {
		ProgressBar p = new ProgressBar(ctx);
		p.setIndeterminate(true);
		return p;
	}

	/**
	 * Interface for Activity callbacks.
	 * @author mhotan
	 */
	public interface RestaurantInfoListener extends ImageObtainable {

		/**
		 * Notifies activity that user request to make a reservation.
		 * @param reservation Reservation the user.
		 */
		void onMakeReservation(String reservation);

		/**
		 * Returns the current Restaurant to be displayed.
		 * @return null if no restaurant available, other wise the resaurant.
		 */
		RestaurantInfo getCurrentRestaurant();

		/**
		 * @param r RestaurantInfo
		 */
		void setCurrentRestaurant(RestaurantInfo r);

	}

}
