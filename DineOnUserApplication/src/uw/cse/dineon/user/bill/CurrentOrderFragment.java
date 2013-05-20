package uw.cse.dineon.user.bill;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.parse.ParseException;
import com.parse.SaveCallback;

import uw.cse.dineon.library.CurrentOrderItem;
import uw.cse.dineon.library.DiningSession;
import uw.cse.dineon.library.MenuItem;
import uw.cse.dineon.library.Order;
import uw.cse.dineon.user.DineOnUserApplication;
import uw.cse.dineon.user.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author mhotan
 */
public class CurrentOrderFragment 
extends Fragment implements OnClickListener {

	private static final String TAG = CurrentBillFragment.class.getSimpleName();
	
	private static final double TAX = 0.08;

	/**
	 * an argument that can be used to pass this bundle explicit.
	 * order as a list of Strings that currently represent Menu items
	 */
	public static final String ARGUMENT_ORDER = "Order";

	/**
	 * Current adapter for holding values to store on our list.
	 */
	private OrderArrayAdapter mAdapter;
	
	private View mListView;
	
	private NumberFormat mFormatter;

	/**
	 * Activity which serves as a Listener. 
	 */
	private OrderUpdateListener mListener;

	private TextView mSubtotal, mTax, mTotal;
	private Button mReqButton;
	private Button mPlaceOrderButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_view_order,
				container, false);

		this.mFormatter = NumberFormat.getCurrencyInstance();

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OrderUpdateListener) {
			mListener = (OrderUpdateListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet CurrentOrderFragment.OrderUpdateListener");
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Attempt to extract argument if this fragment was created with them
		ListView listview = (ListView) getView().findViewById(R.id.list_order);
		this.mListView = getView();

		mSubtotal = (TextView) getView().findViewById(R.id.value_subtotal);
		mTax = (TextView) getView().findViewById(R.id.value_tax);
		mTotal = (TextView) getView().findViewById(R.id.value_total);
		mPlaceOrderButton = (Button) getView().findViewById(R.id.button_place_order);
		//mPlaceOrderButton.setOnClickListener(this);
		mReqButton = (Button) getView().findViewById(R.id.button_request);
		mReqButton.setOnClickListener(this);
		
		// Create the adapter to handles 
		if (this.mListener != null) {
			HashMap<MenuItem, CurrentOrderItem> orderItems = this.mListener.getOrder();
			List<MenuItem> items = new ArrayList<MenuItem>();
			double totalPrice = 0.0;
			for (MenuItem m : orderItems.keySet()) {
				items.add(m);
				totalPrice += m.getPrice() * orderItems.get(m).getQuantity();
			}
			mAdapter = new OrderArrayAdapter(this.getActivity(), items, orderItems);
			
			mSubtotal.setText(mFormatter.format(totalPrice));
			mTax.setText(mFormatter.format(totalPrice * TAX));
			mTotal.setText(mFormatter.format(totalPrice * (1 + TAX)));
		}
		listview.setAdapter(mAdapter);
	}

	/**
	 * @param newItem TODO Replace with OrderItem
	 */
	public void addNewItem(MenuItem newItem) {
		mAdapter.add(newItem);

	}

	/**
	 * @param item MenuItem to remove from this
	 */
	public void removeItem(MenuItem item) {
		mAdapter.remove(item); // Remove the item
		mAdapter.notifyDataSetChanged(); // Notify the data set changed
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.button_request) {
			getRequestDescription();
		//else
		//	mListener.onPlaceOrder("ORDER REQUESTED PLACE OBJECT HERE!");
		}
	}
	
	/**
	 * @param str String request to send to Restaurant.
	 */
	private void sendRequest(String str) {
		if(getActivity() instanceof CurrentOrderActivity) {
			CurrentOrderActivity act = (CurrentOrderActivity)getActivity();
			Log.v(TAG, "About to send Req");
			act.onRequestMade(str);
		}
	}
	
	/**
	 * Helper that brings up alert box for sending customer requests.
	 */
	private void getRequestDescription() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle("Add New Menu Item");
		alert.setMessage("Input Menu Item Details");
		final View AV = getLayoutInflater(getArguments()).inflate(
				R.layout.alert_build_request, null);
		alert.setView(AV);
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface d, int arg1) {
				String desc = ((EditText) AV
						.findViewById(R.id.input_request_description)).getText()
						.toString();
				sendRequest(desc);
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//Do nothing
			}
		});
		alert.show();
	}
	
	public String getSubtotal() {
		return ((TextView)mListView.findViewById(R.id.value_subtotal)).
				getText().toString();
	}

	public String getTax() {
		return ((TextView) mListView.findViewById(R.id.value_tax)).
				getText().toString();
	}
	
	public String getTotal() {
		return ((TextView) mListView.findViewById(R.id.value_total)).
				getText().toString();
	}
	
	/**
	 * Listener associated with this containing fragment.
	 * <b>This allows any containing activity to receive
	 * messages from this interface's Fragment.</b>
	 * TODO Add modify as see fit to communicate back to activity
	 * @author mhotan
	 */
	public interface OrderUpdateListener {

		/**
		 * User wishes place current order.
		 * @param order to place
		 */
		public void onPlaceOrder(Order order);

		/**
		 * User wishes to increment the quantity of a particular item on their order.
		 * TODO Enforce assertion that item is actually placed in that order
		 * @param item Menu item to increment
		 */
		public void onIncrementItemOrder(MenuItem item);

		/**
		 * User wishes to decrement the quantity of a particular item on their order.
		 * TODO Enforce assertion that item is actually placed in that order
		 * @param item Menu item to decrement
		 */
		public void onDecrementItemOrder(MenuItem item);

		/**
		 * User wishes to remove a particular item on their order.
		 * TODO Enforce assertion that item is actually placed in that order
		 * @param item Menu item to remove
		 */
		public void onRemoveItemFromOrder(MenuItem item);
		
		/**
		 * Get the current items in the user's order.
		 * @return hash map of items
		 */
		public HashMap<MenuItem, CurrentOrderItem> getOrder();
		
		/**
		 * Once an order is placed clear the current order.
		 */
		public void resetCurrentOrder();

	}

	/**
	 * Simple adapter that handles custom list item layout and 
	 * their interaction handlers
	 * TODO Change Type String to Order.
	 * TODO Change layout of item 
	 * @author mhotan
	 */
	private class OrderArrayAdapter extends ArrayAdapter<MenuItem> {

		/**
		 * Owning context.
		 */
		private final Context mContext;

		/**
		 * List of menu items.
		 * TODO Change String to MenuItem
		 */
		private final List<MenuItem> mItems;
		
		private final HashMap<MenuItem, CurrentOrderItem> mOrderMapping;

		/**
		 * This is a runtime mapping between "More Info buttons"
		 * and there respective restaurants.
		 * TODO Change String to restaurant;
		 * NOTE (MH): Not exactly sure if this works
		 */
		private final HashMap<View, MenuItem> mViewToItem;

		/**
		 * Mapping to increment and decrement button
		 * to the text view it alters.
		 */
		private final Map<Button, TextView> mButtonToTextView;

		private final OnItemClickListener privateListener;

		/**
		 * Creates an array adapter to display a Order.
		 * @param ctx Context of owning activity
		 * @param order List of menu items to display
		 * @param orderItems Map of MenuItems to their current order.
		 */
		public OrderArrayAdapter(Context ctx, List<MenuItem> order, 
				HashMap<MenuItem, CurrentOrderItem> orderItems) {
			super(ctx, R.layout.listitem_orderitem, order);

			mContext = ctx;
			mItems = new ArrayList<MenuItem>(order);

			mViewToItem = new HashMap<View, MenuItem>();
			mButtonToTextView = new HashMap<Button, TextView>();

			privateListener = new OnItemClickListener();
			
			mPlaceOrderButton.setOnClickListener(privateListener);
			//mReqButton.setOnClickListener(privateListener);
			
			this.mOrderMapping = new HashMap<MenuItem, CurrentOrderItem>(orderItems);
		}

		@Override
		public View getView(int position, View covnertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.listitem_orderitem, parent, false);

			// TODO Here is where we adjust the contents of the list row
			// with attributes determined by the order item

			// Get the Order Item by associating with the position
			position = Math.max(0, Math.min(position, this.mItems.size() - 1));
			MenuItem item = mItems.get(position);	

			// Assign the buttons to this order
			Button incButton = (Button) rowView.findViewById(R.id.button_increment_item);
			Button decButton = (Button) rowView.findViewById(R.id.button_decrement_item);
			TextView itemQuantity = (TextView) rowView.findViewById(R.id.label_item_quantity);
			ImageButton deleteButton = (ImageButton) rowView.findViewById(R.id.button_delete);
			TextView label = (TextView) rowView.findViewById(R.id.label_order_item);

			// set the quantity
			int itemCount = this.mOrderMapping.get(item).getQuantity();
			itemQuantity.setText("" + itemCount);
			
			// set the label and description
			label.setText(item.getTitle() + "\n" + item.getDescription());

			// Place mapping from all the clickable view to the Order item
			mViewToItem.put(incButton, item);
			mViewToItem.put(decButton, item);
			mViewToItem.put(deleteButton, item);
			mViewToItem.put(label, item);
			mViewToItem.put(itemQuantity, item);

			// Place a mapping from the increment 
			// and decrement button to the Text View representing the
			// item quantity
			mButtonToTextView.put(incButton, itemQuantity);
			mButtonToTextView.put(decButton, itemQuantity);

			// Set the listeners for the buttons that alter the order state
			incButton.setOnClickListener(privateListener);
			decButton.setOnClickListener(privateListener);
			deleteButton.setOnClickListener(privateListener);
			// Set the listener when the user selects the item in the current order
			label.setOnClickListener(privateListener);

			return rowView;
		}

		/**
		 * Private click listener for this list items.
		 * TODO Can also be used by having a this object hold references for 
		 * all the Item
		 * @author mhotan
		 */
		private class OnItemClickListener implements View.OnClickListener {

			@Override
			public void onClick(View v) {

				MenuItem item = mViewToItem.get(v);
				int toAdd = -1;

				double priceChange = 0.0;
				switch (v.getId()) {
				case R.id.button_increment_item:
					toAdd = 1;
				case R.id.button_decrement_item:
					TextView curValStr = mButtonToTextView.get(v);
					assert (curValStr != null);

					// Obtain the potential new Value
					Integer curVal = Integer.parseInt(curValStr.getText().toString());
					int newVal = Math.max(0, curVal + toAdd); // Can't have negative amounts

					// Notify the listener appropiately
					priceChange = 0.0;
					if (newVal - curVal > 0) {
						mListener.onIncrementItemOrder(item);
						mAdapter.mOrderMapping.get(item).incrementQuantity();
						priceChange = item.getPrice();
					} else if (newVal - curVal < 0) {
						mListener.onDecrementItemOrder(item);
						mAdapter.mOrderMapping.get(item).decrementQuantity();
						priceChange = item.getPrice() * -1;
					}

					mButtonToTextView.get(v).setText("" + newVal);

					break;
				case R.id.button_delete:
					priceChange = mAdapter.mOrderMapping.get(item).getQuantity() 
							* item.getPrice() * -1;
					mListener.onRemoveItemFromOrder(item);
					mAdapter.remove(item);
					mAdapter.mOrderMapping.remove(item);
					mAdapter.mItems.remove(item);
					//mAdapter.mOrderMapping.remove(item);
					// TODO Do something to remove this item
					break;
				case R.id.label_order_item:
					// TODO Add some way to show focus
					break;
					
				case R.id.button_place_order:
					// check to see if user is currently in a dining session
					DiningSession session = DineOnUserApplication.cachedUser.getDiningSession();
					if (session != null) {
						// create and save the order
						List<MenuItem> items = new ArrayList<MenuItem>();
						for (MenuItem m : mAdapter.mOrderMapping.keySet()) {
							for (int i = 0; i < mAdapter.mOrderMapping.get(m).getQuantity(); i++) {
								items.add(m);
							}
						}
						final Order newOrder = new Order(session.getTableID(),  
								DineOnUserApplication.cachedUser.getUserInfo(), 
								items);
						
						// save the new order
						newOrder.saveInBackGround(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								// TODO Auto-generated method stub
								if (e == null) {
									// successful, send the push notification
									mListener.onPlaceOrder(newOrder);
								} else {
									Log.d(TAG, "Couldn't save the new order: " + e.getMessage());
								}
							}
						});
						
					} else {
						Toast.makeText((Context)mListener, "Must checkin before placing order.", 
								Toast.LENGTH_SHORT).show();
					}
					
					break;
					
				default:
					break;
				}
				if (priceChange != 0.0) {
					mSubtotal = (TextView) mListView.findViewById(R.id.value_subtotal);
					mTax = (TextView) mListView.findViewById(R.id.value_tax);
					mTotal = (TextView) mListView.findViewById(R.id.value_total);
					double newTotal = priceChange 
							+ Double.parseDouble(mSubtotal.getText().toString().substring(1));
					mSubtotal.setText(mFormatter.format(newTotal));
					mTax.setText(mFormatter.format((newTotal * TAX)));
					mTotal.setText(mFormatter.format((newTotal * (1 + TAX))));
				}
			}

		}

	}
}
