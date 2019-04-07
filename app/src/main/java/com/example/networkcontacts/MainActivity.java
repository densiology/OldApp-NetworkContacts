package com.example.networkcontacts;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MainActivity extends Activity
{
	private ContactAdapter adapter; //initialized in onCreate; the bridge between a List Item and ArrayList
	private Context context; //the Context of this activity
	protected String selectedNum; // for the long click; so it can be accessed by the dialog box
	protected ArrayList<String> numbersAll; // for multiple texts; so it can be accessed by the dialog box
	
	EditText editText; //Declared as a field so we can use it in button event.
	protected String strOnEditText; // Updated everytime a change in char occurs
	
	protected ToggleButton btnSmart;
	protected ToggleButton btnGlobe;
	protected ToggleButton btnSun;
	protected ToggleButton btnOther;
	
	protected static boolean checkSmart; //the statuses of the toggle buttons
	protected static boolean checkGlobe;
	protected static boolean checkSun;
	protected static boolean checkOther;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		context = MainActivity.this;
		
		//initialize class variables
		this.selectedNum = new String();
		this.strOnEditText = new String();
		this.numbersAll = new ArrayList<String>();
		
		/* Set references to the UI elements. */
		ListView listView = (ListView)findViewById(R.id.contact_listview);
		editText = (EditText)findViewById(R.id.filter_edittext);
		Button buttonSend = (Button)findViewById(R.id.send_button);
		
		/* When item is tapped, toggle checked properties of CheckBox and Contact. */
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View item, int position, long id)
			{
				Contact contact = adapter.getItem(position);
				contact.toggleChecked();
				ContactAdapter.ContactHolder viewHolder = (ContactAdapter.ContactHolder) item.getTag();
				viewHolder.getCheckBox().setChecked(contact.isChecked());
			}
		});
		
		// add PhoneStateListener
		PhoneCallListener phoneListener = new PhoneCallListener();
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
		
		/* When item is long clicked, prompt if call or send message. */
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View item, int position, long id)
			{
				ContactAdapter.ContactHolder viewHolder = (ContactAdapter.ContactHolder) item.getTag();
				selectedNum = viewHolder.getTextViewNumber().getText().toString();
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("Choose action for " + viewHolder.getTextViewName().getText().toString())
				.setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						if (which == 0)
						{
							// Insert code here for call
							Intent callIntent = new Intent(Intent.ACTION_CALL);
							callIntent.setData(Uri.parse("tel:" + selectedNum));
							startActivity(callIntent);
						}
						else if (which == 1)
						{
							// Insert code here for text
							ArrayList<String> number = new ArrayList<String>();
							number.add(selectedNum);
							sendSms(number);
						}
					}
				})
				.show();
				return true;
			}
		});
		
		/* Sets the whole List View. */
		try
		{
			// Construct an adapter, sending it the List Item layout and the ArrayList of contact data.
			adapter = new ContactAdapter(this, R.layout.list_item, buildContactList());
			// Bind the List View and the adapter together.
			listView.setAdapter(adapter);
		}
		catch(NullPointerException e)
		{
		}
		
		/* When the Button is clicked.. */
		buttonSend.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				editText.setText(""); // When this textbox is empty, all data in adapter will appear.
				// collect all the selected numbers
				ArrayList<String> numbers = new ArrayList<String>();
				String strNames = new String();
				for(int i = 0; i < adapter.getCount(); i++)
				{
					Contact contact = adapter.getItem(i);
					if(contact.isChecked())
					{
						// enter code here for multiple texts
						numbers.add(contact.getNumber());
						strNames = strNames + contact.getName() + "\n";
					}
				}
				
				if (numbers.size() > 0)
				{
					// 'numbers' cannot be accessed on the onClick, so here's a turnaround
					numbersAll = numbers;
					// fix the string to be used in the dialog box
					strNames = strNames.substring(0, strNames.lastIndexOf("\n"));
					// show confirmation dialog
					new AlertDialog.Builder(MainActivity.this)
					.setMessage("Send a text message to contact(s) below?\n\n" + strNames)
					.setPositiveButton(R.string.confirmation_yes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							sendSms(numbersAll);
						}
					})
					.setNegativeButton(R.string.confirmation_no, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					})
					.show();
				}
				else
				{
					// show toast for "please select a contact"
					Toast.makeText(getApplicationContext(), R.string.no_selected, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
		/* Set filtering via EditText. */
		// Enables the List View to have text filter capability.
		listView.setTextFilterEnabled(true);
		// Sets Listener on each char in the EditText; a TextWatcher should be passed.
		// The TextWatcher only affects (read/write) the adapter data, not the ArrayList data.
		editText.addTextChangedListener(new TextWatcher(){
			// Executes when there is a change in chars.
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				strOnEditText = s.toString();
				if (count < before)
				{
					// We're deleting char so we need to reset the adapter data.
					adapter.resetData();
				}
				// Modifies the adapter data according to the char/s; Sends a constraint to the Filter.
				adapter.getFilter().filter(strOnEditText);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		this.btnSmart = (ToggleButton)findViewById(R.id.smart_button);
		this.btnGlobe = (ToggleButton)findViewById(R.id.globe_button);
		this.btnSun = (ToggleButton)findViewById(R.id.sun_button);
		this.btnOther = (ToggleButton)findViewById(R.id.other_button);
		
		if (this.btnSmart.isChecked() == true) { checkSmart = true; }
		else { checkSmart = false; }
		if (this.btnGlobe.isChecked() == true) { checkGlobe = true; }
		else { checkGlobe = false; }
		if (this.btnSun.isChecked() == true) { checkSun = true; }
		else { checkSun = false; }
		if (this.btnOther.isChecked() == true) { checkOther = true; }
		else { checkOther = false; }
	}

	public void clickSmart(View view)
	{
		ToggleButton btnTogSmart = (ToggleButton)view;
		if (btnTogSmart.isChecked() == true) { checkSmart = true; }
		else { checkSmart = false; }
		adapter.getFilter().filter(strOnEditText);
	}
	
	public void clickGlobe(View view)
	{
		ToggleButton btnTogGlobe = (ToggleButton)view;
		if (btnTogGlobe.isChecked() == true) { checkGlobe = true; }
		else { checkGlobe = false; }
		adapter.getFilter().filter(strOnEditText);
	}
	
	public void clickSun(View view)
	{
		ToggleButton btnTogSun = (ToggleButton)view;
		if (btnTogSun.isChecked() == true) { checkSun = true; }
		else { checkSun = false; }
		adapter.getFilter().filter(strOnEditText);
	}
	
	public void clickOther(View view)
	{
		ToggleButton btnTogOther = (ToggleButton)view;
		if (btnTogOther.isChecked() == true) { checkOther = true; }
		else { checkOther = false; }
		adapter.getFilter().filter(strOnEditText);
	}
	
	// send sms method
	private void sendSms(ArrayList<String> numbers)
	{
		String wholeNumbers = new String();
		for (String number : numbers)
		{
			wholeNumbers = wholeNumbers + number + ";";
		}
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
	    sendIntent.putExtra("address", wholeNumbers);
	    sendIntent.setType("vnd.android-dir/mms-sms");
	    startActivity(sendIntent);
	}
	
	//monitor phone call activities
	private class PhoneCallListener extends PhoneStateListener
	{
		private boolean isPhoneCalling = false;
 
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			if (TelephonyManager.CALL_STATE_RINGING == state)
			{
				// phone ringing
			}
			if (TelephonyManager.CALL_STATE_OFFHOOK == state)
			{
				// active
				isPhoneCalling = true;
			}
			if (TelephonyManager.CALL_STATE_IDLE == state)
			{
				// run when class initial and phone call ended, 
				// need detect flag from CALL_STATE_OFFHOOK
				if (isPhoneCalling)
				{
					// restart app
					Intent i = getBaseContext().getPackageManager()
							   .getLaunchIntentForPackage(getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					isPhoneCalling = false;
				}
			}
		}
	}
	
	/* Method for getting all the contacts and putting them in an ArrayList */
	public ArrayList<Contact> buildContactList()
	{
		Cursor namesCursor = getNamesCursor(); //Get the list of names from database and put it in a Names Cursor.
		ArrayList<Contact> contactList = new ArrayList<Contact>(); //The whole ArrayList that will be passed.
		
		// Data containers are already declared here so it won't be declared repeatedly inside the loop.
		String name;
		String photoId;
		String contactId;
		Bitmap photoBitmap;
		String number;
		String type;
		Cursor photoCursor;
		Cursor numberCursor;

		// Loop though the entire Name Cursor.
		while(namesCursor.moveToNext())
		{
			// Extract the Name, Photo ID and Contact ID from each row of the Name Cursor.
			name = namesCursor.getString(namesCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			photoId = namesCursor.getString(namesCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
			contactId = namesCursor.getString(namesCursor.getColumnIndex(ContactsContract.Contacts._ID));
			
			// Extract the Photo (based on a Photo ID) and put it in a Photo Cursor.
			photoCursor = getPhotoCursor(photoId);
			// If the contact has no photo, the Photo Cursor is null.
			if(photoCursor == null)
			{
				photoBitmap = null;
			}
			else
			{
				photoCursor.moveToFirst();
				// Extract the Photo from that Photo Cursor.
				byte[] photoBlob = photoCursor.getBlob(photoCursor.getColumnIndex(CommonDataKinds.Photo.PHOTO));
				// Close the Photo Cursor after the Photo is extracted from the Photo Cursor.
				photoCursor.close();
				// Convert that Photo to a Bitmap type.
				photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
			}
			
			// Extract the Number and Type (based on a Contact ID) and put it in a Number Cursor.
			numberCursor = getNumberCursor(contactId);
			// Loop through the entire Number Cursor (sometimes, a contact has more than 1 phone number).
			while(numberCursor.moveToNext())
			{
				// Extract the Number and Type.
				number = numberCursor.getString(numberCursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
				type = decodeNumType(numberCursor.getString(numberCursor.getColumnIndex(CommonDataKinds.Phone.TYPE)));
				// Add the whole collected Contact data to the end of the ArrayList
				contactList.add(new Contact(photoBitmap, name, number, type));
			}
			// Close the Number Cursor after all Numbers/Types have been extracted from Number Cursor.
			numberCursor.close();
		}
		// Close the Names Cursor after looping is finished from the Names Cursor.
		namesCursor.close();
		
		// Finally, return the complete ArrayList.
		return contactList;
	}
	
	/* Classifies the Number type */
	public String decodeNumType(String num)
	{
		if (num.equals("1")) { return "Home: "; }
		else if (num.equals("2")) { return "Mobile: "; }
		else if (num.equals("3")) { return "Work: "; }
		else if (num.equals("4")) { return "Fax (Work): "; }
		else if (num.equals("5")) { return "Fax (Home): "; }
		else if (num.equals("6")) { return "Pager: "; }
		else if (num.equals("7")) { return "Other: "; }
		else if (num.equals("8")) { return "Callback: "; }
		else if (num.equals("9")) { return "Car: "; }
		else if (num.equals("10")) { return "Main (Company): "; }
		else if (num.equals("11")) { return "ISDN: "; }
		else if (num.equals("12")) { return "Main: "; }
		else if (num.equals("13")) { return "Fax (Other): "; }
		else if (num.equals("14")) { return "Radio: "; }
		else if (num.equals("15")) { return "Extended Telephone: "; }
		else if (num.equals("16")) { return "Direct Teletype: "; }
		else if (num.equals("17")) { return "Mobile (Work): "; }
		else if (num.equals("18")) { return "Pager (Work): "; }
		else if (num.equals("19")) { return "Assistant: "; }
		else if (num.equals("20")) { return "MMS Number: "; }
		else { return "Number: "; }
	}
	
	
	/* Method for getting ALL the names and IDs from database and putting then in a Cursor. */
	public Cursor getNamesCursor()
	{
		// The name of the table.
		Uri mUri = ContactsContract.Contacts.CONTENT_URI;
		// Limit the columns. (SELECT ...)
		String[] mProjection = new String[]{
				ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID
        };
		// Limit the rows. (WHERE ... = ...)
		// IN_VISIBLE_GROUP is a tag indicating if contact should be visible in any UI.
		// IN_VISIBLE_GROUP excludes blank numbers.
		String mSelectionClause = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
		// Replaces ? in the mSelectionClause.
		String[] mSelectionArgs = null;
		// Sort order. COLLATE is an SQL operator in overriding the sort order.
		String mSortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		
		Cursor mCursor = getContentResolver().query(
				mUri,
				mProjection,
				mSelectionClause,
				mSelectionArgs,
				mSortOrder);
		return mCursor;
	}
	
	/* Method for getting a photo of a contact person based on a photo ID and putting it in a Cursor. */
	public Cursor getPhotoCursor(String photoID)
	{
		// The name of the table.
		Uri mUri = ContactsContract.Data.CONTENT_URI;
		// Limit the columns. (SELECT ...)
		String[] mProjection = new String[]{
				CommonDataKinds.Photo.PHOTO
		};
		// Limit the rows. (WHERE ... = ...)
		String mSelectionClause = ContactsContract.Data._ID + " = ?";
		// Replaces ? in the mSelectionClause.
		String[] mSelectionArgs = new String[]{
				photoID 
		};
		// Sort order.
		String mSortOrder = null;
		
		try
		{
			Cursor mCursor = getContentResolver().query(
					mUri,
					mProjection,
					mSelectionClause,
					mSelectionArgs,
					mSortOrder);
			return mCursor;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	/* Method for getting the number/type of a contact person based on the contact ID and putting it in a Cursor. */
	public Cursor getNumberCursor(String contactID)
	{
		// The name of the table.
		Uri mUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		// Limit the columns. (SELECT ...)
		String[] mProjection = new String[]{
				CommonDataKinds.Phone.NUMBER,
				CommonDataKinds.Phone.TYPE
		};
		// Limit the rows. (WHERE ... = ...)
		String mSelectionClause = ContactsContract.Data.CONTACT_ID + " = ?";
		// Replaces ? in the mSelectionClause.
		String[] mSelectionArgs = new String[]{
				contactID 
		};
		// Sort order.
		String mSortOrder = null;
		
		Cursor mCursor = getContentResolver().query(
				mUri,
				mProjection,
				mSelectionClause,
				mSelectionArgs,
				mSortOrder);
		return mCursor;
	}
}