package uw.cse.dineon.restaurant.profile;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import uw.cse.dineon.library.Menu;
import uw.cse.dineon.library.MenuItem;
import uw.cse.dineon.library.RestaurantInfo;
import uw.cse.dineon.library.image.DineOnImage;
import uw.cse.dineon.library.image.ImageObtainable;
import uw.cse.dineon.library.image.ImageCache.ImageGetCallback;
import uw.cse.dineon.restaurant.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Fragment that presents a editable list of menu items of this restaurant.
 * 
 * @author mhotan
 */
@SuppressWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class MenuItemsFragment extends ListFragment {

	/**
	 * Adapter for restaurant menu Item adapter. Use this to add new Menuitems
	 */
	private RestaurantMenuItemAdapter mAdapter;

	private static final String TAG = "MenuItemsFragment";

	private List<String> menuTitles;

	/**
	 * Activity listener.
	 */
	private MenuItemListener mListener;

	private Menu currentMenu;

	public AlertDialog newItemAlert; // for testing. Otherwise can't access
	public AlertDialog newMenuAlert; // for testing. Otherwise can't access

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// IF there are arguments
		// then check if there is a restaurant info instance
		// info can be null
		RestaurantInfo info = mListener.getInfo();

		// If arguments existed and it included a Restaurant Info
		if (isValid(info)) {

			List<Menu> menus = info.getMenuList();
			
			// TODO Handle multiple menus
			if (menus.size() < 1) {
				Menu defaultMenu = new Menu("Default");
				info.getMenuList().add(defaultMenu);
				defaultMenu.saveInBackGround(null);
				Log.d(TAG, "No menu exists, created a default menu!");
			}
			currentMenu = menus.get(0);

			// Make list of menu titles for future reference
			menuTitles = new ArrayList<String>();
			for (Menu m : menus) {
				menuTitles.add(m.getName());
			}
			
			List<MenuItem> menuitems = currentMenu.getItems();
			mAdapter = new RestaurantMenuItemAdapter(getActivity(), menuitems);
			setListAdapter(mAdapter);
		} else {
			List<String> defList = new ArrayList<String>();
			defList.add("Illegal Restaurant Info State");
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1, defList);
			setListAdapter(adapter);
		}
		updateTitle();
	}

	/**
	 * Updates the title to reflect the current menu.
	 */
	private void updateTitle() {
		getActivity().getActionBar().setTitle("Menu: " + currentMenu.getName());
	}

	@Override
	public void onCreateOptionsMenu(android.view.Menu menu,
			MenuInflater inflater) {
		// TODO Add your menu entries here
		inflater.inflate(R.menu.menu_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(android.view.Menu menu) {
		if (currentMenu == null) {

		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if (item.getItemId() == R.id.menu_add_menu_item) {
			makeNewMenuItem();
			return true;
		} else if (item.getItemId() == R.id.menu_switch_menu) {
			switchMenus();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Helper function to prompt the user for new menu item details and handle
	 * saving/consistency.
	 */
	private void makeNewMenuItem() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle("Add New Menu Item");
		alert.setMessage("Input Menu Item Details");
		final View AV = getLayoutInflater(getArguments()).inflate(
				R.layout.alert_new_menuitem, null);

		alert.setView(AV);
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface d, int arg1) {
				String title = ((EditText) AV
						.findViewById(R.id.input_menuitem_title)).getText()
						.toString();
				String desc = ((EditText) AV
						.findViewById(R.id.input_menuitem_desc)).getText()
						.toString();
				String priceString = ((EditText) AV
						.findViewById(R.id.input_menuitem_price)).getText()
						.toString();
				if (title.trim().equals("") || priceString.equals("")) {
					Toast.makeText(getActivity(), "Please input Title and Price",
							Toast.LENGTH_SHORT).show();
					return;
				}

				double price = Double.parseDouble(priceString);

				MenuItem mi = new MenuItem(mAdapter.getCount() + 1, price,
						title, desc);

				currentMenu.addNewItem(mi);
				mAdapter.add(mi);
				mAdapter.notifyDataSetChanged();

				mListener.onMenuItemAdded(mi);
				Log.v(TAG, "Adding new menu item");
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// Do nothing
			}
		});
		this.newItemAlert = alert.show();
	}

	/**
	 * Helper function to prompt user to pick a menu (or create new one).
	 */
	private void switchMenus() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle("Add New Menu Item");
		alert.setMessage("Input Menu Item Details");
		final View AV = getLayoutInflater(getArguments()).inflate(
				R.layout.alert_select_menu, null);

		alert.setView(AV);
		final Spinner SPINNER = (Spinner) AV
				.findViewById(R.id.spinner_select_menu);
		final ArrayAdapter<String> ADAPTER = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, menuTitles);
		SPINNER.setAdapter(ADAPTER);

		SPINNER.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v,
					int pos, long id) {
				if(pos > mListener.getInfo().getMenuList().size()) {
					Log.e(TAG, "Invalid menu index selected!");
				} else {
					currentMenu = mListener.getInfo().getMenuList().get(pos);
					mAdapter.notifyDataSetInvalidated();
					mAdapter = new RestaurantMenuItemAdapter(getActivity(),
							currentMenu.getItems());
					setListAdapter(mAdapter);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				Log.v(TAG, "Nothing selected");
			}
		});

		// Handle button to unhide new menu stuff
		Button showAddMenuButton = (Button) AV
				.findViewById(R.id.button_new_menu);
		showAddMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AV.findViewById(R.id.container_new_menu).setVisibility(
						View.VISIBLE);
			}
		});

		ImageButton addMenu = (ImageButton) AV
				.findViewById(R.id.button_save_menu);
		addMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String newTitle = ((TextView) AV
						.findViewById(R.id.input_new_menu_title)).getText()
						.toString();
				((TextView) AV.findViewById(R.id.input_new_menu_title))
				.setText("");
				if (newTitle.trim().equals("")) {
					Toast.makeText(getActivity(), "Please input title",
							Toast.LENGTH_SHORT).show();
					return;
				}

				Menu newMenu = new Menu(newTitle);
				menuTitles.add(newTitle);
				mListener.getInfo().addMenu(newMenu);
				SPINNER.setSelection(SPINNER.getCount() - 1);
				// Switch spinner to last item added
			}
		});

		alert.setPositiveButton("Select",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface d, int x) {
				updateTitle();
			}
		});

		this.newMenuAlert = alert.show();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof MenuItemListener) {
			mListener = (MenuItemListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet MenuItemsFragment.MenuItemListener");
		}
	}

	/**
	 * Checks whether the current restaurant info is a valid. restaurant info
	 * instance
	 * 
	 * @param info
	 *            info to be analyzed
	 * @return true if it is valid
	 */
	private boolean isValid(RestaurantInfo info) {
		if (info == null) {
			return false;
		}
		return true;
	}

	// ////////////////////////////////////////////////////
	// // Following is the interface in which activities
	// // that wish to attach this Fragment must implement
	// // Intended to use for user input
	// ////////////////////////////////////////////////////

	/**
	 * Interface for all owning activities to implement.
	 * 
	 * @author mhotan
	 */
	public interface MenuItemListener extends ImageObtainable {

		/**
		 * Notifies that the user chooses to delete the current menu item.
		 * 
		 * @param item
		 *            menu item to delete
		 */
		void onMenuItemDeleted(MenuItem item);

		/**
		 * Notifies that the user chose to add the current menu item.
		 * 
		 * @param item
		 *            menu item to add
		 */
		void onMenuItemAdded(MenuItem item);

		// XXX Seems like this might be redundant right now

		/**
		 * Notifies that the user chose to modify the current item.
		 * 
		 * @param item
		 *            menu item to modify
		 */
		void onMenuItemModified(MenuItem item);

		/**
		 * The user has just added an image to this menu item.
		 * @param item Item to change
		 * @param b Bitmap to use.
		 */
		void onImageAddedToMenuItem(MenuItem item, Bitmap b);

		/**
		 * @return RestaurantInfo
		 */
		RestaurantInfo getInfo();

	}

	/**
	 * Adapter to handle the modification of menu items.
	 * 
	 * @author mhotan
	 */
	private class RestaurantMenuItemAdapter extends ArrayAdapter<MenuItem> {

		/**
		 * Context to use this adapter.
		 */
		private final Context mContext;

		private final NumberFormat mCurrencyFormatter;

		/**
		 * Creates a menu item adapter for displaying, modifying, and deleting
		 * menu items.
		 * 
		 * @param ctx
		 *            Context to use adapter
		 * @param items
		 *            items to add to this adapter
		 */
		public RestaurantMenuItemAdapter(Context ctx, List<MenuItem> items) {
			super(ctx, R.layout.listitem_menuitem_editable, items);
			mContext = ctx;
			// Update the time
			mCurrencyFormatter = NumberFormat.getCurrencyInstance();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position >= this.getCount()) {
				super.getView(position, convertView, parent);
			}
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = inflater.inflate(R.layout.listitem_menuitem_editable,
						parent, false);
			}

			// Obtain the view used for this menu item
			ImageView imageView = (ImageView) view
					.findViewById(R.id.image_thumbnail_menuitem);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			TextView title = (TextView) view
					.findViewById(R.id.label_menuitem_title);
			//			ImageButton delete = (ImageButton) view
			//					.findViewById(R.id.button_menuitem_delete);
			TextView description = (TextView) view
					.findViewById(R.id.label_menuitem_desc);
			TextView price = (TextView) view
					.findViewById(R.id.label_menuitem_price);

			// Get the item at the established position
			final MenuItem ITEM = super.getItem(position);

			// Set all the regular descriptive stuff
			title.setText(ITEM.getTitle());
			description.setText(ITEM.getDescription());
			price.setText(mCurrencyFormatter.format(ITEM.getPrice()));

			// 
			DineOnImage image = ITEM.getImage();
			if (image != null) {
				mListener.onGetImage(image, new InitialGetImageCallback(imageView));
			}

			// Set an onlick listener to handle the changing of images.
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ImageView imageView = (ImageView) v;
					AlertDialog getImageDialog = getRequestImageDialog(
							new MenuItemImageGetCallback(ITEM, imageView));
					getImageDialog.show();
				}
			});

			return view;
		}

		/**
		 * Get an alert dialog to present the user with the option to take pictures.
		 * 
		 * @param callback Callback to accept pictures
		 * @return Get a dailog that will handle getting images for a menu item
		 */
		private AlertDialog getRequestImageDialog(final ImageGetCallback callback) {
			AlertDialog.Builder builder = new  AlertDialog.Builder(mContext);
			builder.setTitle(R.string.dialog_title_getimage);
			builder.setMessage(R.string.dialog_message_getimage_for_menuitem);
			builder.setPositiveButton(R.string.dialog_option_take_picture, 
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mListener.onRequestTakePicture(callback);
					dialog.dismiss();
				}
			});
			builder.setNeutralButton(R.string.dialog_option_choose_picture, 
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mListener.onRequestGetPictureFromGallery(callback);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			return builder.create();
		}

		/**
		 * An image get callback to to populate menu item view. 
		 * @author mhotan
		 */
		private class MenuItemImageGetCallback implements ImageGetCallback {

			private final MenuItem mItem;
			private final ImageView mView;

			/**
			 * A callback to handle the retrieving of images.
			 * @param item Item to get image for.
			 * @param view View to hole image.
			 */
			public MenuItemImageGetCallback(MenuItem item, ImageView view){
				mItem = item;
				mView = view;
			}

			@Override
			public void onImageReceived(Exception e, Bitmap b) {
				if (e == null) {
					mView.setImageBitmap(b);
					mListener.onImageAddedToMenuItem(mItem, b);
				} else {
					String message = getActivity().getResources().
							getString(R.string.message_unable_get_image);
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				}
			}
		}

		/**
		 * Get the pre set image for this menuitem.
		 * @author mhotan
		 */
		private class InitialGetImageCallback implements ImageGetCallback {

			private ImageView mView;

			/**
			 * prepares callback for placing an image in the view.
			 * @param view
			 */
			public InitialGetImageCallback(ImageView view) {
				mView = view;
			}

			@Override
			public void onImageReceived(Exception e, Bitmap b) {
				if (e == null && mView != null) {
					mView.setImageBitmap(b);
				}
			}
		}
	}
}
