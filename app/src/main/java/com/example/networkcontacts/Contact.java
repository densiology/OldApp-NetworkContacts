package com.example.networkcontacts;

import android.graphics.Bitmap;

public class Contact
{
	public Bitmap photo;
	public String name;
	public String number;
	public String type;
	public boolean checked = false;

	public Contact()
	{
		super();
	}

	public Contact(Bitmap photo, String name, String number, String type)
	{
		super();
		this.photo = photo;
		this.name = name;
		this.number = number;
		this.type = type;
	}

	public Bitmap getPhoto()
	{
		return photo;
	}

	public void setPhoto(Bitmap photo)
	{
		this.photo = photo;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber(String number)
	{
		this.number = number;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
	
	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	public boolean isChecked()
	{
		return checked;
	}
	
	public void toggleChecked()
	{
		checked = !checked;
	}
	
	public byte getNetwork()
	{
		int refinedNumberLength;
		String phoneNumber;
		String strThreeDigits;
		int threeDigits;
		byte networkType;
		
		// First, remove all spaces and dashes of a phone number.
		//refinedNumber = refinedNumber.replace(" ", "");
		//refinedNumber = refinedNumber.replace("-", "");
		String numberThis = number.replaceAll("\\D+","");
		// Second, check if it is a valid cellphone number.
		// If it is a valid cellphone number, extract the 3 digit prefix.
		refinedNumberLength = numberThis.length();
		if(refinedNumberLength >= 10)
		{
			phoneNumber = numberThis.substring(refinedNumberLength - 10);
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
}
