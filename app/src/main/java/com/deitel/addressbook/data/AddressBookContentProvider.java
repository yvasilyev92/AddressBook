package com.deitel.addressbook.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.content.UriMatcher;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.deitel.addressbook.R;
import com.deitel.addressbook.data.DatabaseDescription.Contact;

//AddressBookContentProvider is a subclass of ContentProvider that
//defines query/insert/update/delete operations on our database.

public class AddressBookContentProvider extends ContentProvider {

    //First we define an instance variable dbHelper which will be our reference to AddressBookDatabaseHelper object.
    //We will use it to enable this ContentProvider to get readable/writable access to our DB.
    private AddressBookDatabaseHelper dbHelper;


    //Then we define variable "uriMatcher" which is a UriMatcher object.
    //A ContentProvider uses a UriMatcher to help determine which operation to perform
    //in its query/insert/update/delete methods.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    //Our UriMatcher will return the integer constants "ONE_CONTACT" AND "CONTACTS".
    //The ContentProvider will use these constants in switch statements in its operation methods.
    private static final int ONE_CONTACT = 1; //manipulate one contact
    private static final int CONTACTS = 2;    //manipulate contacts table


    //We configure a static block that adds Uris to the UriMatcher. This block executes once
    //when the class AddressBookContentProvider is loaded into memory.
    static {

        //The UriMatcher method "addURI" takes 3 arguments:
        //1) a String representing the ContentProvider's Authority.
        //2) a String representing a path - each Uri used to invoke the ContentProvider contains "content://"
        //followed by the authority and a path that the CP uses to determine the task to perform.
        //3) an int code that the UriMatcher returns when a Uri supplied to a CP matches a Uri stored in the UriMatcher.


        //we add a Uri in the form content://com.deitel.addressbook.data/contacts/#
        //# is a wildcard that matches a string of numeric characters. In our case it is a
        //primary-key value for one contact in the contacts table.
        //When a Uri matches this format, the UriMatcher returns the constant ONE_CONTACT
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Contact.TABLE_NAME + "/#", ONE_CONTACT);

        //we add a Uri in the form content://com.deitel.addressbook.data/contacts
        //which represents the entire contacts table. When a Uri matches this format,
        //the UriMatcher returns the constant CONTACTS aka the entire table.
        uriMatcher.addURI(DatabaseDescription.AUTHORITY, Contact.TABLE_NAME, CONTACTS);
    }


    //When a ContentProvider is created, Android calls its onCreate method to configure it.
    @Override
    public boolean onCreate() {
        //We create the AddressBookDatabaseHelper object that enables the CP to access the database.
        //The first time the CP is invoked to write to the database, the AddressBookDatabaseHelper's onCreate
        //method will be called to create the database.
        dbHelper = new AddressBookDatabaseHelper(getContext());
        return true; //CP successfully created.
    }


    //The getType method is a required CP method that simply returns null in this app. This method
    //is typically used when creating and starting Intents for Uri's with specific MIME types.
    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data at the given URI.
        return null;
    }








    //Here we override the CP method "query". It retrieves data from the provider's data source.
    //The method returns a Cursor thats used to interact with he results. "query" receives 5 arguments.
    //1) uri - A uri representing the data to retrieve.
    //2) projection - a String array representing the specific columns to retrieve.
    //If projection is null then all columns will be included in the result.
    //3) selection - a String containing the selection criteria. This is the SQL "Where" clause.
    //4) selectionArgs - a String array containing the args used to replace any argument placeholders(?) in selection.
    //5) sortOrder - a String representing the sort order. This is the SQL "Sort By" clause.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {


        //First we create a SQLiteQueryBuilder for building SQL queries that are submitted to the SQLite DB.
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //We use setTables to specify the table to access. Specifying multiple table can be used as JOIN operation.
        queryBuilder.setTables(Contact.TABLE_NAME);


        //Now we use our UriMatcher to determine the operation to perform. In this app we have 2 queries:
        //1) select a specific contact from the DB to display or edit its details.
        //2) select all contacts in DB to display their names in our ContactsFragment RecyclerView.

        //We use the UriMatcher method "match" to determine which query operation to perform. "match"
        //returns one of the constants that were registered with the UriMatcher.
        switch (uriMatcher.match(uri)){

            //if ONE_CONTACT, only the contact with the ID specified in the Uri should be selected.
            case ONE_CONTACT:
                //we use "appendWhere" to add a "WHERE" clause containing the contact's ID
                //and getLastPathSegment returns the last segment in the Uri.
                // For example if contact ID is 5 then the uri is content://com.deitel.addressbook.data/contacts/5
                queryBuilder.appendWhere(Contact._ID + "=" + uri.getLastPathSegment());
                break;

            //if CONTACTS, the switch terminates without adding anything to the query. Because there is
            //no WHERE clause then all the contacts will be selected.
            case CONTACTS:
                break;
            //For any Uri that isnt a match we throw an exception indicating it was invalid.
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.invalid_query_uri) + uri);
        }


        //To query our database we use SQLiteQueryBuilders query method to perform the query and get a Cursor
        //representing our results.


        //The SQLiteQueryBuilder's query method takes 7 arguments.
        //1) A DB to query. dbHelper's getReadableDatabase() returns a read-only DB object.
        //2) projection - a String array of columns to retrieve
        //3) selection - a String containing selection criteria.
        //4) selectionArgs - a String array for selection placeholders.
        //5) groupBy - the SQL GroupBy clause, if null then no grouping performed.
        //6) having - used with groupBy to specify which groups to include.
        //7) sortOrder - a String representing the sort order.
        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),projection,selection,selectionArgs,
                null,null,sortOrder);

        //Now that we have a Cursor with our results. We must register it to watch for content changes.
        //setNotificationUri indicates that the cursor should be updated if the data it refers to changes.
        //The 1st arg is the ContentResolver that invoked the ContentProvider.
        //The 2nd arg is the Uri used to invoke the ContentProvider.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        //Lastly we return our Cursor containing the query results.
        return cursor;
    }









    //The overridden CP method insert adds a new record to the contacts table. It receives 2 args.
    //1) uri - a Uri representing the table in which the data will be inserted.
    //2) values - a ContentValues object containing key-value pairs in which the
    //the column names are the keys and each keys values is the data to insert in that column.
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Uri newContactUri = null;


        //We use the UriMatcher method "match" to determine which query operation to perform. "match"
        //returns one of the constants that were registered with the UriMatcher.

        switch (uriMatcher.match(uri)){

            //First we check whether the Uri is for the contacts table.
            case CONTACTS:
                //if it is then we add a new contact into the database.
                //We use SQLiteOpenHelper's getWritableDatabase to get a SQLite object
                //for modifying data in the database. Then we use its "insert" method which takes 3 args.
                //Our table,nullColumnHack - which we leave null,the values to insert.

                //a successful insert gives the new contact's row ID, and -1 if it isnt.
                long rowId = dbHelper.getWritableDatabase().insert(Contact.TABLE_NAME, null, values);

                //If the creation of the new contact is successful, we create a Uri representing the new contact,
                //and notify the ContentResolver the DB has changed, so its code can respond to DB changes.
                if (rowId > 0){
                    newContactUri = Contact.buildContactUri(rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                else //if not successful add then we throw an exception as the operation failed.
                    throw new SQLException(getContext().getString(R.string.insert_failed) + uri);
                break;
            //If it is NOT for the contacts table we throw an exception.
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_insert_uri) + uri);
        }

        //Finally we return Uri of our newly added contact.
        return newContactUri;
    }







    //The overridden CP method "update" updates an existing record. It receives 4 args.
    //1) A uri representing the row to update.
    //2) a ContentValues object containing the columns to update and their values.
    //3) a String containing the selection criteria.
    //4) a String array containing args used to replace any placeholders.
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        //create variable numberOfRowsUpdated. 1 if successful, 0 if not.
        int numberOfRowsUpdated;

        //Updates are performed on a single specific contact. So we check only for ONE_CONTACT uri.
        switch (uriMatcher.match(uri)){

            case ONE_CONTACT:
                //if our Uri matches ONE_CONTACT, we get its last path segment
                //which is the contact's unique ID.
                String id = uri.getLastPathSegment();

                //Then we get a Writable DB object and call its update method to update the specified contact.
                //The update method's args are our table, the values to update,
                //the Where clause - in our case the ID of the row to update, and selection args.
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
                        Contact.TABLE_NAME, values, Contact._ID + "=" + id, selectionArgs);
                break;


            //if the Uri is not for the contacts table we throw an exception.
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_update_uri) + uri);
        }


        //if the update is successful we are returned the number of rows updated. Otherwise 0.
        //if successful we notify the ContentResolver that the DB has changed so its code can respond to changes.
        if (numberOfRowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }


        //Lastly we return the number of updated rows.
        return numberOfRowsUpdated;
    }









    //The overridden CP method "delete" removes an existing record from our DB.
    //The delete method takes 3 arguments.
    //1) The Uri representing which row(s) to delete.
    //2) a String containing a WHERE clause specifying which rows to delete.
    //3) a String array containing the arguments used to replace any selection placeholders.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {


        //variable to hold number of rows deleted. 1 if successful deletion, 0 if not.
        int numberOfRowsDeleted;

        //We perform deletion on only a specific single contact. So we check if the Uri is ONE_CONTACT
        switch (uriMatcher.match(uri)){


            case ONE_CONTACT:
                //if ONE_CONTACT uri match, get the contact's ID using getLastPathSegment
                String id = uri.getLastPathSegment();


                //Then get a Writable DB object and call its "delete" method to delete the row.
                //we specify the table name, the WHERE clause which is the ID of row to be deleted,
                //and selectionArgs.

                //A successful delete returns an integer of 1, otherwise 0.
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
                        Contact.TABLE_NAME, Contact._ID + "=" + id, selectionArgs);
                break;

            //if not for ONE_CONTACT then throw exception as operation failed.
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_delete_uri) + uri);
        }

        //if successful deletion, notify the ContentResolver that the DB has changed,
        //so its code can respond to changes.
        if (numberOfRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Finally return the number of rows deleted.
        return numberOfRowsDeleted;
    }



}
