package com.deitel.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.deitel.addressbook.data.DatabaseDescription.Contact;

//The AddEditFragment class provides a GUI for adding new contacts or editing
//existing ones.
//The class implements LoaderManager.LoaderCallbacks<Cursor> interface to respond
//to LoaderManager events.

public class AddEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    //AddEditFragmentListener nested interface contains the callback method onAddEditCompleted.
    //MainActivity implements this interface to be notified when the user saves a new contact
    //or saves changes to an existing one.
    public interface AddEditFragmentListener{
        //called when a contact is saved.
        void onAddEditCompleted(Uri contactUri);
    }

    //CONTACT_LOADER identifies the Loader that queries the AddressBookCP to retrieve one contact
    //for editing.
    private static final int CONTACT_LOADER = 0;

    //'listener' refers to AddEditFragmentListener thats notified when the user saves a new/updated contact.
    private AddEditFragmentListener listener;
    //contactUri represents to the contact to edit.
    private Uri contactUri;


    //addingNewContact specifies whether a new contact is added is being added (true)
    // or an existing one is being edited (false).
    private boolean addingNewContact = true;

    //EditTexts for contact info.
    private TextInputLayout nameTextInputLayout;
    private TextInputLayout phoneTextInputLayout;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout streetTextInputLayout;
    private TextInputLayout cityTextInputLayout;
    private TextInputLayout stateTextInputLayout;
    private TextInputLayout zipTextInputLayout;
    private FloatingActionButton saveContactFAB;
    private CoordinatorLayout coordinatorLayout; //used with SnackBars.



    //Overridden Fragment lifecycle methods onAttach and onDetach set instance variable 'listener'
    //to the host Activity when AddEditFragment is attached and set 'listener' to null
    //when AddEditFragment is detached.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddEditFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }



    //onCreateView is called when the Fragment's view needs to be created.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        //fragment has menu items to display.
        setHasOptionsMenu(true);

        //inflate the GUI and get references to Fragment's TextInputLayouts.
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        nameTextInputLayout = (TextInputLayout) view.findViewById(R.id.nameTextInputLayout);
        //set TextWatcher to detect when text in name field changes, once text is detected
        //then the saveButtonFAB appears. Otherwise it is hidden so user cant add a contact wih no name.
        nameTextInputLayout.getEditText().addTextChangedListener(nameChangedListener);
        phoneTextInputLayout = (TextInputLayout) view.findViewById(R.id.phoneTextInputLayout);
        emailTextInputLayout = (TextInputLayout) view.findViewById(R.id.emailTextInputLayout);
        streetTextInputLayout = (TextInputLayout) view.findViewById(R.id.streetTextInputLayout);
        cityTextInputLayout = (TextInputLayout) view.findViewById(R.id.cityTextInputLayout);
        stateTextInputLayout = (TextInputLayout) view.findViewById(R.id.stateTextInputLayout);
        zipTextInputLayout = (TextInputLayout) view.findViewById(R.id.zipTextInputLayout);
        saveContactFAB = (FloatingActionButton) view.findViewById(R.id.saveFloatingActionButton);

        //set custom OnClickListener so when saveContactFAB is clicked, the keyboard is hidden
        //and method saveContact() is executed.
        saveContactFAB.setOnClickListener(saveContactButtonClicked);

        //call updateSaveButtonFAB() , it is initially hidden until text appears in Name field.
        updateSaveButtonFAB();

        //initialize CoordinatorLayout. Its used to display SnackBars with brief messages.
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        //After inflating GUI/getting references, we next use Fragment method getArguments
        //to get the Bundle of arguments. When we launch AddEditFragment from the MainActivity, we
        //pass null for the Bundle arg because the user is adding a new contacts info.
        Bundle arguments = getArguments();


        //if arguments is not null then the user is editing an existing contact.
        if (arguments != null){
            //so we set addingNewContact to false
            addingNewContact = false;
            //and read the contact's URI from the Bundle by calling method getParcelable.
            contactUri = arguments.getParcelable(MainActivity.CONTACT_URI);
        }

        //if the contactUri from the existing contact the user is trying to edit is not null,
        //then we use Fragment's getLoaderManager to initialize a Loader that the AddEditFragment
        //will use to get the data for the contact being editing.
        if (contactUri != null)
            getLoaderManager().initLoader(CONTACT_LOADER, null, this);

        return view;
    }



    //custom TextWatcher nameChangedListener detects when text in the nameInputLayout's EditText
    //changes so it can determine whether to hide or show the saveContactFAB.
    private final TextWatcher nameChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonFAB();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };




    //updateSaveButtonFAB method is created and used to show the saveContactFAB
    //only if the name field is not empty.
    private void updateSaveButtonFAB(){

        String input = nameTextInputLayout.getEditText().getText().toString();

        if (input.trim().length() != 0)
            saveContactFAB.show();
        else
            saveContactFAB.hide();

    }



    //we create a custom OnClickListener for our Fragment's saveContactFAB button,
    //so that when its clicked, the keybord is hidden and saveContact() is executed,
    //which is supposed to save contact to the database.
    private final View.OnClickListener saveContactButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    getView().getWindowToken(),0);
            saveContact();
        }
    };





    //the saveContact method saves contact info to the database.
    private void saveContact(){

        //First we create a ContentValues object.
        ContentValues contentValues = new ContentValues();
        //and add to it key-value pairs representing column names and values to be inserted into
        //or updated in the database.
        contentValues.put(Contact.COLUMN_NAME, nameTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_PHONE, phoneTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_EMAIL, emailTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_STREET, streetTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_CITY, cityTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_STATE, stateTextInputLayout.getEditText().getText().toString());
        contentValues.put(Contact.COLUMN_ZIP, zipTextInputLayout.getEditText().getText().toString());


        //if the addingNewContact is true, we are adding a new contact to the DB,
        if (addingNewContact){

            //so we use ContentResolver method 'insert' to invoke 'insert' on the AddressBookCP
            //and place the new contact into the database.
            Uri newContactUri = getActivity().getContentResolver().insert(Contact.CONTENT_URI, contentValues);

            //if the insert is successful it will yield us a non-null URI.
            if (newContactUri != null){
                //so we display a SnackBar indicating the add was successful
                Snackbar.make(coordinatorLayout, R.string.contact_added, Snackbar.LENGTH_LONG).show();
                //and notify AddEditFragmentListener of the new contact.
                listener.onAddEditCompleted(newContactUri);
            }
            else {
                //if add not successful
                Snackbar.make(coordinatorLayout, R.string.contact_not_added, Snackbar.LENGTH_LONG).show();
            }
        }
        //if addingNewContact is false, we are editing an existing contact
        else {
            //we use the Activity's ContentResolver to invoke 'update' on the AddressBookCP
            //and store the edited contact's data.
            int updatedRows = getActivity().getContentResolver().update(contactUri,contentValues,null,null);

            //a successful update will yield an integer greater than zero.

            //If update is successful,
            if (updatedRows > 0){
                //we notfiy AddEditFragmentListener with the contact that was edited
                listener.onAddEditCompleted(contactUri);
                //and display a Snackbar indicating a successful update.
                Snackbar.make(coordinatorLayout,R.string.contact_updated, Snackbar.LENGTH_LONG).show();
            }
            else {
                //if update was NOT successful.
                Snackbar.make(coordinatorLayout,R.string.contact_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }
    }




    //Now we begin implementing LoaderManager.LoaderCallbacks<Cursor> interface.
    //These methods are used only in AddEditFragment only when the user is editing
    //an existing contact..


    //onCreateLoader creates a CursorLoader for the specific contact being editing.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        //Create a CursorLoader based on the id argument. Since only one Loader a switch
        //isnt needed but its good practice.
        switch (id){
            case CONTACT_LOADER:
                return new CursorLoader(getActivity(), //content in which Loader lifecycle is being managed
                        contactUri,                    //Uri of contact to display
                        null,                          //null projection to return all columns
                        null,                          //null selection to return all rows
                        null,                          //no selection arguments
                        null);                         //no sort order.
            default:
                return null;
        }
    }



    //onLoadFinished is called by LoaderManager when a Loader finishes loading its data, so you
    //can process the results in the Cursor argument.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        //onLoadFinished checks whether the cursor is non-null, which is how it checks
        //if the contact exists in the database. If cursor is non-null we call its
        //moveToFirst method which moves the cursor the first row.
        if (data != null && data.moveToFirst()){

            //Then we get the column index for each data item.
            int nameIndex = data.getColumnIndex(Contact.COLUMN_NAME);
            int phoneIndex = data.getColumnIndex(Contact.COLUMN_PHONE);
            int emailIndex = data.getColumnIndex(Contact.COLUMN_EMAIL);
            int streetIndex = data.getColumnIndex(Contact.COLUMN_STREET);
            int cityIndex = data.getColumnIndex(Contact.COLUMN_CITY);
            int stateIndex = data.getColumnIndex(Contact.COLUMN_STATE);
            int zipIndex = data.getColumnIndex(Contact.COLUMN_ZIP);

            //fill our EditTexts with the retrieved data.
            nameTextInputLayout.getEditText().setText(data.getString(nameIndex));
            phoneTextInputLayout.getEditText().setText(data.getString(phoneIndex));
            emailTextInputLayout.getEditText().setText(data.getString(emailIndex));
            streetTextInputLayout.getEditText().setText(data.getString(streetIndex));
            cityTextInputLayout.getEditText().setText(data.getString(cityIndex));
            stateTextInputLayout.getEditText().setText(data.getString(stateIndex));
            zipTextInputLayout.getEditText().setText(data.getString(zipIndex));

            //hide or show the save button depending if Name field is empty or not.
            updateSaveButtonFAB();
        }
    }


    //onLoaderReset is called by the LoaderManager when a Loader is reset its data no longer available.
    //At this point the app should immediately disconnect from the data.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
