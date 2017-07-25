package com.deitel.addressbook.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Yevgeniy on 6/30/2017.
 */

//DatabaseDescription defines static fields that are used with this apps ContentProvider
//and ContentResolver classes. The nested class "Contact" defines the names of the columns
//that will be in our database.
// For every database table you typically have a class similar to Contact.

public class DatabaseDescription {


    //Here we define the variable "AUTHORITY", which will be the ContentProvider's authority.
    //An authority is the name that is supplied to a ContentResolver to locate a ContentProvider.
    //It is typically the name of the ContentProvider subclass.
    public static final String AUTHORITY = "com.deitel.addressbook.data";


    //Each Uri that is used to access a specific ContentProvider begins with "content://"
    //followed by the authority. This is the ContentProvider's base Uri.
    //We use the Uri.parse method to create the base Uri.
    private static final Uri BASE_CONTENT_URI =Uri.parse("content://" + AUTHORITY);


    //Now we create a nested class "Contact". It will define the database table name,
    //the Uri used to access the table via ContentProvider, and the table's column names.
    //We implement interface BaseColumns as it defines the constant "_ID" with the value "_id",
    //this is required as each row in DB table must have a primary key.
    public static final class Contact implements BaseColumns{


        //The name of our Database Table.
        public static final String TABLE_NAME = "contacts";

        //The Uri used for accessing the Table via ContentProvider.
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();


        //The column names our database table will have.
        //The table-name and column names will be used by AddressBookDatabaseHelper to create our database.
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_STREET = "street";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_ZIP = "zip";


        //We also create a method buildContactUri which will be used to create a Uri
        //for a specific contact in the database table.
        public static Uri buildContactUri(long id){

            //class ContentUris contains a static utility method for manipulating "content://" Uris.
            //The "withAppendedId" method appends a forward slash (/) and a record ID to the end of a Uri
            //in its first argument.
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
