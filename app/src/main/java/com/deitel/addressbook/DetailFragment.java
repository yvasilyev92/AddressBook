package com.deitel.addressbook;

/**
 * Created by Yevgeniy on 6/30/2017.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deitel.addressbook.data.DatabaseDescription.Contact;

//The DetailFragment class displays one contact's information and provides menu items on
//the app bar that enable the user to edit or delete that contact.
//It implements the LoaderManager.LoaderCallbacks<Cursor> interface to respond to LoaderManager events.

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    //nested interface DetailFragmentListener contains callback methods that MainActivity implements
    //to be notfied when the user deletes a contact and when a user touches the edit menu item
    //to edit a contact.
    public interface DetailFragmentListener{

        //called when a contact is deleted.
        void onContactDeleted();

        //pass Uri of contact to edit to the DetailFragmentListener.
        void onEditContact(Uri contactUri);
    }


    //CONTACT_LOADER identifes a Loader that queries the AddressBookCP to retrieve one contact to display
    private static final int CONTACT_LOADER = 0;

    //listener refers to the DetailFragmentListener(MainActivity) thats notified when the user deletes
    //a contact or chooses to edit one.
    private DetailFragmentListener listener;
    //contactUri is Uri of contact to be displayed.
    private Uri contactUri;

    //variables for our Fragment's TextViews
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;
    private TextView streetTextView;
    private TextView cityTextView;
    private TextView stateTextView;
    private TextView zipTextView;


    //Overridden Fragment lifecycle methods onAttach and onDetach set the instance variable 'listener'
    //to the host Activity when DetailFragment is attached and set it to null when its detached.

    //set DetailFragmentListener when fragment attached.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DetailFragmentListener) context;
    }
    //remove DetailFragmentListener when fragment detached.
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }





    //onCreateView is called when DetailFragment's view needs to be created.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        //this fragment has menu options to display.
        setHasOptionsMenu(true);

        //We use Fragment method getArguments
        //to get the Bundle of arguments. When we launch DetailFragment from the MainActivity, we
        //pass a Urifor the Bundle arg because the user is displaying an existing contact.

        //getArguments to extract the selected contact's Uri.
        Bundle arguments = getArguments();

        if (arguments != null)
            //we read the contact's URI from the Bundle by calling method getParcelable.
            //Bundle is a key-value pair. CONTACT_URI is a key, the value is a Uri.
            contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);

        //Next we inflate the GUI and initialize all our TextViews.
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        phoneTextView = (TextView) view.findViewById(R.id.phoneTextView);
        emailTextView = (TextView) view.findViewById(R.id.emailTextView);
        streetTextView = (TextView) view.findViewById(R.id.streetTextView);
        cityTextView = (TextView) view.findViewById(R.id.cityTextView);
        stateTextView = (TextView) view.findViewById(R.id.stateTextView);
        zipTextView = (TextView) view.findViewById(R.id.zipTextView);

        //We then use Fragment's getLoaderManager to initialize a Loader that the DetailFragment
        //will use to get data for the contact to display.
        //initLoader's args get sent to onCreateLoader(int,Bundle,Callbacks),
        //our int is zero, no need for a Bundle, and this Frag is the callback.
        getLoaderManager().initLoader(CONTACT_LOADER, null, this);
        return view;
    }



    //DetailFragment displays in the app bar options for editing the current contact
    //or deleting it.


    //onCreateOptionsMenu inflates the resource file fragment_details_menu.xml that we created.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    //onOptionsItemSelected is where we handle menu item selections.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //We use the MenuItems resource ID to determine which menu item was selected,
        switch (item.getItemId()){

            //if Edit option we call DetailFragmentListener's onEditContact method with the
            //contactUri, MainActivity passes this to AddEditFragment.
            case R.id.action_edit:
                listener.onEditContact(contactUri);
                return true;
            //if Delete option we call method deleteContact. Which is DialogFragment
            case R.id.action_delete:
                deleteContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }





    //We create method deleteContact to display a DialogFragment to user to confirm
    //that the current selected contact should be deleted.
    private void deleteContact(){
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }


    //Here we create our "confirmDelete" Dialog, which uses ContentResolver's 'delete' method
    //to invoke 'delete' and remove our contact from the Database.
    DialogFragment confirmDelete = new DialogFragment(){
        @Override
        public Dialog onCreateDialog(Bundle bundle) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);
            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //The delete method receives the Uri of the content to delete.
                    //The last 2 args are the WHERE clause and we leave them null because
                    //the rowID of the contact to delete is embedded in the Uri, and that row ID
                    //is extracted from the Uri by the AddressBookCP delete method.
                    getActivity().getContentResolver().delete(contactUri,null,null);
                    //Then we call the DetailFragmentListener's onContacteleted method so MA
                    //can remove the DetailFragment from the screen.
                    listener.onContactDeleted();
                }
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();

        }
    };



    //Now we implement LoaderManager.LoaderCallbacks<Cursor> interface methods.


    //LoaderManager calls onCreateLoader to create and return a new Loader for the specified ID,
    //which the LoaderManager manages in the context of the Fragment's or Activity's lifecycle.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //onCreateLoader creates a CursorLoader for the specific contact being displayed,
        //based on id argument.
        CursorLoader cursorLoader;


        switch (id){
            case CONTACT_LOADER:
                cursorLoader = new CursorLoader(getActivity(), //context in which Loader lifecycle is being managed
                        contactUri,                            //Uri of contact to display
                        null,                                  //null projection returns all columns
                        null,                                  //null selection returns all rows
                        null,                                  //no selection args
                        null);                                 //no sort order.
                break;
            default:
                cursorLoader = null;
                break;
        }
        return cursorLoader;

    }



    //onLoadFinished is called by LoaderManager when a Loader finishes loading its data, so you
    //can process the results in the Cursor argument.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        //we check if the contact is non-null, if this is true then a contact
        //matching the contactUri was found in the database
        if (data != null && data.moveToFirst()){
            //so we get the contacts info from the Cursor,
            //by getting the column index for each item.
            int nameIndex = data.getColumnIndex(Contact.COLUMN_NAME);
            int phoneIndex = data.getColumnIndex(Contact.COLUMN_PHONE);
            int emailIndex = data.getColumnIndex(Contact.COLUMN_EMAIL);
            int streetIndex = data.getColumnIndex(Contact.COLUMN_STREET);
            int cityIndex = data.getColumnIndex(Contact.COLUMN_CITY);
            int stateIndex = data.getColumnIndex(Contact.COLUMN_STATE);
            int zipIndex = data.getColumnIndex(Contact.COLUMN_ZIP);
            //and display it in the GUI.
            nameTextView.setText(data.getString(nameIndex));
            phoneTextView.setText(data.getString(phoneIndex));
            emailTextView.setText(data.getString(emailIndex));
            streetTextView.setText(data.getString(streetIndex));
            cityTextView.setText(data.getString(cityIndex));
            stateTextView.setText(data.getString(stateIndex));
            zipTextView.setText(data.getString(zipIndex));
        }

    }


    //onLoaderReset is called by the LoaderManager when a Loader is reset its data no longer available.
    //At this point the app should immediately disconnect from the data.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //method onLoaderReset is not needed in DetailFragment so it does nothing.

    }
}
