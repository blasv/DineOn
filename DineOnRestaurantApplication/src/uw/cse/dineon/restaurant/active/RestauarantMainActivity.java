package uw.cse.dineon.restaurant.active;

import java.util.List;

import uw.cse.dineon.library.CustomerRequest;
import uw.cse.dineon.library.DiningSession;
import uw.cse.dineon.library.Order;
import uw.cse.dineon.library.Restaurant;
import uw.cse.dineon.library.UserInfo;
import uw.cse.dineon.restaurant.DineOnRestaurantActivity;
import uw.cse.dineon.restaurant.R;
import uw.cse.dineon.restaurant.active.DiningSessionDetailFragment.DiningSessionDetailListener;
import uw.cse.dineon.restaurant.active.DiningSessionListFragment.DiningSessionListListener;
import uw.cse.dineon.restaurant.active.OrderDetailFragment.OrderDetailListener;
import uw.cse.dineon.restaurant.active.RequestDetailFragment.RequestDetailListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * This activity supports the main features for this restaurant
 * It presents a swipable interface for Restaurants to view 
 * 
 * 1. Pending Order
 * 2. Pending Requests
 * 3. Current Customers
 *  
 * @author mhotan
 */
public class RestauarantMainActivity extends DineOnRestaurantActivity implements
OrderListFragment.OrderItemListener,
RequestListFragment.RequestItemListener,
DiningSessionListListener,
DiningSessionDetailListener,
OrderDetailListener, 
RequestDetailListener {

	private static final String TAG = RestauarantMainActivity.class.getSimpleName(); 

	private static String[] content;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private ScreenSlidePagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_main);

		content = getResources().
				getStringArray(R.array.fragment_titles);
		
		mPager = (ViewPager) findViewById(R.id.pager_restaurant_main);
		// Reset the adapter
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
	}

	@Override
	protected void updateUI() {
		super.updateUI();
	}

	@Override
	protected void addDiningSession(DiningSession session) {
		super.addDiningSession(session);

		// Update our UI for the current dining session
		DiningSessionListFragment frag = mPagerAdapter.getCurrentDiningSessionListFragment();
		if (frag != null) {
			frag.addDiningSession(session);
		}
	}

	@Override
	protected void removeUser(UserInfo user) {
		// Update our UI for the current dining session
		if (user == null) {
			super.removeUser(user);
			return;
		}
		
		DiningSession session = mRestaurant.getDiningSession(user);
		if (session == null || session.getUsers().size() > 1) { 
			super.removeUser(user);
			return;
		}
		
		// Only update our state when the user is the last one left.
		DiningSessionListFragment frag = mPagerAdapter.getCurrentDiningSessionListFragment();
		if (frag != null) {
			frag.removeDiningSession(session);
		}

		super.removeUser(user);
	}

	@Override
	protected void addOrder(Order order) {
		super.addOrder(order);

		// Update our UI for the current added Order 
		OrderListFragment frag = mPagerAdapter.getCurrentOrderListFragment();
		if (frag != null) {
			frag.addOrder(order);
		}
	}

	@Override
	protected void completeOrder(Order order) {

		// Update our UI for the current added Order 
		OrderListFragment frag = mPagerAdapter.getCurrentOrderListFragment();
		if (frag != null) {
			frag.deleteOrder(order);
		}
		
		// TODO if the current detail fragment is showing the current order
		// hide the fragment. and focus the screen.
		

		super.completeOrder(order);
	}

	@Override 
	protected void addCustomerRequest(CustomerRequest request) {
		super.addCustomerRequest(request);

		// Update our UI for the current added Request
		RequestListFragment frag = mPagerAdapter.getCurrentRequestListFragment();
		if (frag != null) {
			frag.addRequest(request);
		}	
	}

	@Override
	protected void removeCustomerRequest(CustomerRequest request) {

		// Update our UI for the current added Request
		RequestListFragment frag = mPagerAdapter.getCurrentRequestListFragment();
		if (frag != null) {
			frag.deleteRequest(request);
		}	

		super.removeCustomerRequest(request);
	}

	//////////////////////////////////////////////////////////////////////
	////	Listener for OrderDetailFragment.OrderDetailListener
	////	For Fragment call backs  
	//////////////////////////////////////////////////////////////////////

	/**
	 * Attempts to replace the current fragment in the container if the device is large enough.
	 * If there is no fragment in the container then this expands the container.
	 * If this method is unable to show the fragment it will start the activity class argument
	 * @param frag Fragment to attempt to show.
	 * @param activityclass Activity class to start 
	 */
	private void displayOrStartActivity(Fragment frag, Class<?> activityclass) {
		// Attempt to create a new fragment and present it
		if (replaceFragmentInContainer(frag)) {
			return;
		}

		// We were unable to show a fragment.
		Intent intent = new Intent(getApplicationContext(), activityclass);
		startActivity(intent);
	}

	@Override
	public void onRequestSelected(CustomerRequest request) {
		if (request == null) { // Safety Check
			return;
		}

		// We must assign the correct reference to the Request to display.
		mRestaurant.setTempRequest(request);
		displayOrStartActivity(new RequestDetailFragment(), RequestDetailActivity.class);
	}

	@Override
	public void onOrderSelected(Order order) {
		if (order == null) { // Safety Check
			return;
		}

		// We must assign 
		mRestaurant.setTempOrder(order);
		displayOrStartActivity(new OrderDetailFragment(), OrderDetailActivity.class);
	}

	@Override
	public void onDiningSessionSelected(DiningSession ds) {
		if (ds == null) { // Safety Check
			return;
		}

		mRestaurant.setTempDiningSession(ds);
		displayOrStartActivity(new DiningSessionDetailFragment(), 
				DiningSessionDetailActivity.class);
	}

	@Override 
	public List<DiningSession> getCurrentSessions() {
		return super.getCurrentSessions();
	}

	/**
	 * Replaces any fragment in the container with the inputed fragment.
	 * If no container exists then nothing is added.
	 * @param frag Fragment to add.
	 * @return true if something was added or replaced, false other wise
	 */
	private boolean replaceFragmentInContainer(Fragment frag) {
		View view = findViewById(R.id.container);
		if (view != null) { // We are in a 7in or above tablet
			FrameLayout container = (FrameLayout) view;
			container.setVisibility(View.VISIBLE);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			ft.replace(R.id.container, frag);
			ft.commit();
			return true;
		}
		return false;
	}

	@Override
	public void onAssignStaffToRequest(CustomerRequest request, String staff) {
		// TODO Implement Add a field in customer request
		// Assigns a staff to customer request.
		request.setWaiter(staff);
	}

	@Override
	public void onRemoveRequest(CustomerRequest request) {
		// Use this and the super class to appropiately remove
		// the customer request
		removeCustomerRequest(request);
	}	

	@Override 
	public List<CustomerRequest> getCurrentRequests() {
		return super.getCurrentRequests();
	}

	@Override 
	public List<Order> getPendingOrders() {
		return super.getPendingOrders();
	}

	@Override
	public void onProgressChanged(Order order, int progress) {
		// TODO Implement
		Log.i(TAG, "Progress of order: " + order + " changed to " + progress);
		
		if (progress != 100) {
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_complete_order);
		builder.setMessage(R.string.message_complete_order_question);
		final Order ORDER = order;
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				completeOrder(ORDER);
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(true);
		builder.create().show();
	}

	@Override
	public void onOrderComplete(Order order) {
		// Use this and the super class to appropriately complete
		// the Order
		completeOrder(order);
	}

	@Override
	public void sendShoutOut(UserInfo user, String message) {
		// TODO Send push notification back to  
		Toast.makeText(this, 
				getString(R.string.shout_out) + user.getName() 
				+ getString(R.string.quote) 
				+ message 
				+ getString(R.string.quote), 
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSendTaskToStaff(CustomerRequest request, String staff,
			String urgency) {
		// TODO Auto-generated method stub
		Toast.makeText(this, 
				getString(R.string.sending_customer_request) 
				+ request.getDescription() 
				+ getString(R.string.to) + staff, Toast.LENGTH_SHORT).show();
	}
	

	@Override
	public CustomerRequest getRequest() {
		return mRestaurant.getTempCustomerRequest();
	}

	@Override
	public Order getOrder() {
		return mRestaurant.getTempOrder();
	}
	
	@Override
	public DiningSession getDiningSession() {
		return mRestaurant.getTempDiningSession();
	}

	//////////////////////////////////////////////////////////////////////
	////	Methods overriden from DineOnRestaurantActivity
	//////////////////////////////////////////////////////////////////////

	/**
	 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
	 * sequence.
	 */
	public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

		private final Fragment[] mFragments;

		/**
		 * This is a PageAdapter to control Restaurant displays
		 * that show orders, customer requests, and sessions.
		 * @param fragmentManager Fragment manager of this activity
		 */
		public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
			mFragments = new Fragment[3];
		}

		@Override
		public Fragment getItem(int position) {

			Restaurant restaurant = mRestaurant;
			assert (restaurant != null); // WTF if that is null?

			// Narrow in position
			position = Math.min(Math.max(position, 0), content.length - 1);

			Fragment f;
			switch (position) {
			case 0:
				f = new OrderListFragment();
				break;
			case 1:
				f = new RequestListFragment();
				break;
			case 2: // Should be 2
				f = new DiningSessionListFragment();
				break;
				// TODO Add more options
			default:
				Log.wtf(TAG, "ScreenSlidePagerAdapter weird index requested: " + position);
				return null;
			}
			mFragments[position] = f;
			return mFragments[position];
		}

		@Override
		public int getCount() {
			return content.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			position = Math.max(Math.min(position, content.length - 1), 0);
			return content[position];
		}

		/**
		 * Returns the a reference to the current Order list fragment. 
		 * @return Order List fragment if it exists, else null
		 */
		public OrderListFragment getCurrentOrderListFragment() {
			return (OrderListFragment) mFragments[0];
		}

		/**
		 * Returns a reference to the current Request List fragment. 
		 * @return Request List fragment if it exists, else null
		 */
		public RequestListFragment getCurrentRequestListFragment() {
			return (RequestListFragment) mFragments[1];
		}

		/**
		 * Returns current Dining Session list fragment instance.
		 * @return Dining Session list fragment if it exists, else null
		 */
		public DiningSessionListFragment getCurrentDiningSessionListFragment() {
			return (DiningSessionListFragment) mFragments[2];
		}
	}






}
