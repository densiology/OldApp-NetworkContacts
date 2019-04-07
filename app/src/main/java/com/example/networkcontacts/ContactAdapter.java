/**
 * This adapter is the 'bridge connection' between a list of data and the UI layout of a single item.
 * When instantiated without filter, this adapter will bridge ALL the list of data.
 * When instantiated with filter, this adapter will bridge selected items in the list of data.
**/

package com.example.networkcontacts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Contact> implements Filterable
{
	private Context context; //The activity context.
	private int itemLayoutId; //The layout ID of each item.
	
	private ArrayList<Contact> contactList; //The list of data that will be in the adapter.
	private ArrayList<Contact> origContactList; //Initialized in onCreate. Contains all the data.
	
	private Filter contactFilter; //A Filter which can constrain data.
	
	/* The constructor. 
	 * Receives the activity context, layout of each item, and the list of data to be shown */ 
	public ContactAdapter(Context context, int layoutResourceId, ArrayList<Contact> data)
	{
		super(context, layoutResourceId, data); //Call superclass (mandatory for ArrayAdapter<T>)
		this.context = context;
		this.itemLayoutId = layoutResourceId;
		this.contactList = data;
		this.origContactList = data;
	}

	/* Override from class ArrayAdapter<T>. Called every time adapter refers to each item in the list.
	 * Receives the position, view and parent of the item in the list view */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View rowView = convertView; //View of the item.
		ContactHolder holder = null; //Static holder of the UI elements (TextView, ImgView, etc.)
		
		// Get the whole ArrayList to be displayed and extract the specified item to a Contact.
		Contact contact = contactList.get(position);
		
		/* Getting references to the UI elements. */
		// If the view has not yet been occupied..
		if(rowView == null)
		{
			// Create an inflater.
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			// Inflate the item layout in the view.
			rowView = inflater.inflate(itemLayoutId, parent, false);
			// This holder will 'hold' the UI elements.
			holder = new ContactHolder();
			// Get references to the UI elements.
			holder.imgViewTelecom = (ImageView)rowView.findViewById(R.id.telecom_view);
			holder.imgViewPhoto = (ImageView)rowView.findViewById(R.id.photo_view);
			holder.txtViewName = (TextView)rowView.findViewById(R.id.name_view);
			holder.txtViewNumber = (TextView)rowView.findViewById(R.id.number_view);
			holder.txtViewType = (TextView)rowView.findViewById(R.id.type_view);
			holder.chkBoxSend = (CheckBox)rowView.findViewById(R.id.check_box);
			// Set a foothold on this view.
			// optimization: tag the row with its child views, so we don't have to call findViewById
			// later when we reuse the row
			rowView.setTag(holder);
			
			// If CheckBox is toggled, update the contact it is tagged with.
			holder.chkBoxSend.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					CheckBox cb = (CheckBox)v;
					Contact contact = (Contact)cb.getTag();
					contact.setChecked(cb.isChecked());
				}
			});
		}
		// If the view has been occupied..
		else
		{
			// To initialize references to the UI elements, get those that are already on foothold.
			holder = (ContactHolder)rowView.getTag();
		}
		
		// Tag the CheckBox with the Contact it is displaying, so that we can
		// access the contact in onClick() when the CheckBox is toggled.
		holder.chkBoxSend.setTag(contact);
		
		
		/* Use those references to output all the data of a contact. */
		if(contact.photo == null)
		{
			holder.imgViewPhoto.setImageResource(R.drawable.no_photo);
		}
		else
		{
			holder.imgViewPhoto.setImageBitmap(contact.photo);
		}
		holder.txtViewName.setText(contact.name);
		holder.txtViewNumber.setText(contact.number);
		holder.txtViewType.setText(contact.type);
		holder.chkBoxSend.setChecked(contact.isChecked());

		/* Output the network logos (Smart, Globe, Sun, Other) */
		String extractedNumber = (String)holder.txtViewNumber.getText();
		byte rawNetworkType = determineNetwork(extractedNumber);
		switch(rawNetworkType)
		{
			case 1:
				holder.imgViewTelecom.setImageResource(R.drawable.smart_logo);
				break;
			case 2:
				holder.imgViewTelecom.setImageResource(R.drawable.globe_logo);
				break;
			case 3:
				holder.imgViewTelecom.setImageResource(R.drawable.sun_logo);
				break;
			case 4:
				holder.imgViewTelecom.setImageResource(R.drawable.other_logo);
				break;
		}
		
		// Returns the newly-occupied view.
		return rowView;
	}
	
	/* Override from ArrayAdapter. Returns the number of contacts on the adapter. */
	@Override
	public int getCount()
	{
		return contactList.size();
	}
	
	/* Override from ArrayAdapter. Returns a contact contained on the adapter. */
	@Override
	public Contact getItem(int position)
	{
		return contactList.get(position);
	}
	
	/* Override from ArrayAdapter. Returns itemId of a contact on the adapter. */
	@Override
	public long getItemId(int position)
	{
		return contactList.get(position).hashCode();
	}
	
	/* Override from the Filterable class. Should send a Filter that can constrain data. */
	@Override
	public Filter getFilter()
	{
		// If the filter method has not yet been used, construct one.
		if (contactFilter == null)
		{
			contactFilter = new ContactFilter();
		}
		// Return the constructed Filter.
		return contactFilter;
	}
	
	/* Method for determining the network of a mobile number
	 * 1 = SMART, 2 = GLOBE, 3 = SUN, 4 = OTHER */
	public byte determineNetwork(String refinedNumber)
	{
		int refinedNumberLength;
		String phoneNumber;
		String strThreeDigits;
		int threeDigits;
		byte networkType;
		
		// First, remove all spaces and dashes of a phone number.
		//refinedNumber = refinedNumber.replace(" ", "");
		//refinedNumber = refinedNumber.replace("-", "");
		refinedNumber = refinedNumber.replaceAll("\\D+","");
		// Second, check if it is a valid cellphone number.
		// If it is a valid cellphone number, extract the 3 digit prefix.
		refinedNumberLength = refinedNumber.length();
		if(refinedNumberLength >= 10)
		{
			phoneNumber = refinedNumber.substring(refinedNumberLength - 10);
			strThreeDigits = phoneNumber.substring(0, 3);
			threeDigits = Integer.parseInt(strThreeDigits);
		}
		else
		{
			threeDigits = 0;
		}
		switch(threeDigits)
		{
			case 813: case 907: case 908: case 909: case 910: case 912: case 918: case 919: 
			case 920: case 921: case 928: case 929: case 930: case 938: case 939: case 946: 
			case 947: case 948: case 949: case 989: case 998: case 999:
				networkType = 1;
				break;
			case 817: case 905: case 906: case 915: case 916: case 917: case 926: case 927: 
			case 935: case 936: case 937: case 994: case 996: case 997: 
				networkType = 2;
				break;
			case 922: case 923: case 925: case 932: case 933: case 934: case 942: case 943: 
				networkType = 3;
				break;
			default:
				networkType = 4;
				break;
		}
		return networkType;
	}
	
	/* Method to make adapter data go unfiltered. */
	public void resetData()
	{
		contactList = origContactList;
	}
	
	/* The Filter class. This has the capability to constrain data. */
	private class ContactFilter extends Filter
	{
		/* Invoked to filter data according to a constraint. */
		@Override
		protected FilterResults performFiltering(CharSequence constraint)
		{
			// Holds the results of a filtering operation.
			FilterResults results = new FilterResults();
			// Convert the constraint to all lower case.
			constraint = constraint.toString().toLowerCase();
			
			/* We implement here the filter logic. */
			if (constraint == null || constraint.length() == 0)
			{
				/* We perform filtering operation */
				// Initialize the results ArrayList.
				ArrayList<Contact> nContactList = new ArrayList<Contact>();
				for (Contact c: origContactList)
				{
					if (MainActivity.checkSmart == true)
					{
						if (c.getNetwork() == 1)
						{ nContactList.add(c); }
					}
					if (MainActivity.checkGlobe == true)
					{
						if (c.getNetwork() == 2)
						{ nContactList.add(c); }
					}
					if (MainActivity.checkSun == true)
					{
						if (c.getNetwork() == 3)
						{ nContactList.add(c); }
					}
					if (MainActivity.checkOther == true)
					{
						if (c.getNetwork() == 4)
						{ nContactList.add(c); }
					}
				}
				results.values = nContactList;
				results.count = nContactList.size();
			}
			else
			{
				/* We perform filtering operation */
				// Initialize the results ArrayList.
				ArrayList<Contact> nContactList = new ArrayList<Contact>();
				// Loop around each contact data currently on the adapter.
				// It can also loop around origContactList (edited), but every time a letter is typed
				// it had to loop around the whole list, a waste of CPU cycle.
				for (Contact c : origContactList)
				{
					// If name contains the constraint, add it to the results ArrayList.
					if (c.getName().toLowerCase().contains(constraint))
					{
						if (MainActivity.checkSmart == true)
						{
							if (c.getNetwork() == 1)
							{ nContactList.add(c); }
						}
						if (MainActivity.checkGlobe == true)
						{
							if (c.getNetwork() == 2)
							{ nContactList.add(c); }
						}
						if (MainActivity.checkSun == true)
						{
							if (c.getNetwork() == 3)
							{ nContactList.add(c); }
						}
						if (MainActivity.checkOther == true)
						{
							if (c.getNetwork() == 4)
							{ nContactList.add(c); }
						}
					}
				}
				// Store it to the FilterResults variable.
				results.values = nContactList;
				results.count = nContactList.size();
			}
			return results;
		}

		/* Invoked to set the adapter the filtered results. */
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			// Now we have to inform the adapter about the new list filtered
			contactList = (ArrayList<Contact>) results.values;
			notifyDataSetChanged();
		}
	}
	
	/* This class declares UI holder variables.
	 * It is static such that it will not be declared repeatedly inside getItem() */
	static class ContactHolder
	{
		ImageView imgViewTelecom;
		ImageView imgViewPhoto;
		TextView txtViewName;
		TextView txtViewNumber;
		TextView txtViewType;
		CheckBox chkBoxSend;
		
		public CheckBox getCheckBox()
		{
			return chkBoxSend;
		}
		
		public TextView getTextViewName()
		{
			return txtViewName;
		}
		
		public TextView getTextViewNumber()
		{
			return txtViewNumber;
		}
	}
}
