package com.deitel.addressbook.data;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.deitel.addressbook.data.DatabaseDescription.Contact;

/**
 * Created by Yevgeniy on 6/30/2017.
 */

//The AddressBookDatabaseHelper class extends SQLiteOpenHelper, which helps apps create
//databases and manage database version changes.
//We use to create our Database and enable our ContentProvider to access it.

class AddressBookDatabaseHelper extends SQLiteOpenHelper {

    //First we define the name of our Database.
    private static final String DATABASE_NAME = "AddressBook.db";

    //Then we define the Database version number starting at 1.
    private static final int DATABASE_VERSION = 1;



    //Then we define our Constructor. Which simply calls the superclass constructor that takes 4 args.
    public AddressBookDatabaseHelper(Context context){

        //content = The Context in which the DB is being opened or created.
        //DB name - this can be null if you wish to use an in-memory database.
        //the CursorFactory to use - we put null to indicate that we wish to use the default SQLiteCursorFactory.
        //the DB version number.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //SQLiteOpenHelper requires you override its abstract onCreate and onUpgrade methods.


    //The onCreate method creates our Database, it is called if the DB does not yet exist.
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Here we specify the table to create using constants from our Contact class.
        //Remember "_ID" was auto-created by the BaseColumns interface we implemented.
        final String CREATE_CONTACTS_TABLE = "CREATE TABLE " + Contact.TABLE_NAME + "(" +
                Contact._ID + " integer primary key, " +
                Contact.COLUMN_NAME + " TEXT, " +
                Contact.COLUMN_PHONE + " TEXT, " +
                Contact.COLUMN_EMAIL + " TEXT, " +
                Contact.COLUMN_STREET + " TEXT, " +
                Contact.COLUMN_CITY + " TEXT, " +
                Contact.COLUMN_STATE + " TEXT, " +
                Contact.COLUMN_ZIP + " TEXT);";

        //Finally we use the "execSQL" command to execute our just-created "CREATE TABLE" command.
        db.execSQL(CREATE_CONTACTS_TABLE);
    }


    //The onUpgrade method is called if the Database on a device is newer version
    //and must be upgraded.
    //It normally defines how to upgrade the DB when the schema changes. We leave it empty since we dont need to.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
