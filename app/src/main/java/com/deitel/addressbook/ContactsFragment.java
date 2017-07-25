package com.deitel.addressbook;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.deitel.addressbook.data.DatabaseDescription.Contact;

//ContactsFragment displays the contact list in a RecyclerView and provides
//a FAB that the user can touch to add a new contact.
//It uses a Loader to query the AddressBookCP to receive a Cursor
//that the ContactsAdapter uses to supply data to the RecyclerView.
//It implements interface LoaderManager.LoaderCallbacks<Cursor> so it can respond to method calls
//from the LoaderManager to create the Loader and process the results of AddressBookCP.


public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    //The first thing we do is define the nested interface ContactsFragmentListener
    //which contains the callback methods the MainActivity implements to be notified
    //when the user selects a contact or touches the FAB to add a new contact.
    public interface ContactsFragmentListener{

        //called when contact selected.
        void onContactSelected(Uri contactUri);
        //called when FAB add button touched.
        void onAddContact();
    }

    //We declare a constant thats used to identify the Loader when processing results returned
    //from the AddressBookCP. In our case we have only one Loader so we set it to zero.
    //If a class uses multiple Loaders each should use a unique integer so you can identify which Loader
    //to manipulate in the LoaderManager.LoaderCallback callback methods.
    private static final int CONTACTS_LOADER = 0;

    //The listener variable will refer to the object that implements the interface (MainActivity).
    //Used to inform the MainActivity when a contact is selected.
    private ContactsFragmentListener listener;
    //The contactsAdapter variable will refer to the ContactsAdapter that binds data to our RV.
    private ContactsAdapter contactsAdapter;





    //overridden Fragment method onCreateView inflates & configures the Fragments GUI.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        //fragment has menu items to display.
        setHasOptionsMenu(true);

        //inflate GUI and get reference to the RecyclerView.
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        //recyclerView should display items in a vertical list.
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));


        //create the ContactsAdapter that will populate the RecyclerView.
        //The argument to this constructor is an implementation of ContactsAdapter.ContactClickListener interface
        //specifying that when a user touches a contact, the ContactFragmentListener's onContactSelected
        //should be called with the Uri of the contact to display in DetailFragment.
        contactsAdapter = new ContactsAdapter(
                new ContactsAdapter.ContactClickListener(){
                    @Override
                    public void onClick(Uri contactUri){
                        listener.onContactSelected(contactUri);
                    }
                }
        );


        // then we set the adapter.
        recyclerView.setAdapter(contactsAdapter);
        //attach a custom ItemDecorator to draw dividers between list items.
        recyclerView.addItemDecoration(new ItemDivider(getContext()));
        //setHasFixedSize to true, which improves performance if RV layout size never changes.
        recyclerView.setHasFixedSize(true);




        //get our FAB add button.
        FloatingActionButton addButton = (FloatingActionButton) view.findViewById(R.id.addButton);
        //configure the FAB button listener to display the AddEditFragment if its clicked.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddContact();
            }
        });


        //lastly return our view.
        return view;
    }



    //ContactFragment overrides Fragment lifecycle methods onAttach and onDetach to set
    //instance variable 'listener'. listener is set to the host Activity when ContactsFragment
    //is attached and is set to null when ContactsFragment is detached.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ContactsFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }




    //Fragment lifecycle onActivityCreated is called a Fragment's host Activity has been created
    //and the Fragment's onCreateView method completes execution - at this point the Fragment's GUI
    //is part of the Activity's view heirarchy.
    //We use this method to tell the LoaderManager to initialize a Loader - doing this after the view
    //heirarchy exists is important because the RV must exist before we can display the loaded data.
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Here we use Fragment method getLoaderManager to obtain the Fragments LoaderManager object.
        //and we use its initLoader method to receive 3 arguments:
        //1) an integer ID used to identify the Loader,
        //2) a Bundle containing arguments for the Loader's constuctor, or null if no arguments.
        //3) a reference to the implementation of the interface LoaderManager.LoaderCallbacks<Cursor>
        //(this represents the ContactsAdapter). We implement this methods in THIS fragment.
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);

        //If there isnt an active loader already with the specified ID then the initLoader
        //asynchronously calls the onCreateLoadermethod to create and start a Loader for that ID.
        //If there is one then initLoader immediately calls the onLoadFinished method.
    }


    //We create the updateContactList method to notify the ContactsAdapter when the data changes.
    //This method is called when new contacts are added and when existing contacts are updated/deleted.
    public void updateContactList(){
        contactsAdapter.notifyDataSetChanged();
    }





    //Now we begin implementing LoaderManager.LoaderCallback<Cursor> callback methods.


    //LoaderManager calls onCreateLoader to create and return a new Loader for the specified ID,
    //which the LoaderManager manages in the context of the Fragment's or Activity's lifecycle.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //We only use one Loader so a switch state is unnecessary but its good practice.
        //As you see we create a CursorLoader based on the id arguement, that queries the AddressBookCP
        //to get the list of contacts then make the results available as a Cursor.


        switch (id){
            case CONTACTS_LOADER:
                return new CursorLoader(getActivity(), //context in which Loader lifecycle is managed.
                        Contact.CONTENT_URI, //URI of contacts table
                        null,                //null projection returns all columns
                        null,                //null selection returns all rows
                        null,                //no selection arguments
                        Contact.COLUMN_NAME + " COLLATE NOCASE ASC"); //sort order ascending/alphabetical
            default:
                return null;
        }
    }



    //onLoadFinished is called by LoaderManager when a Loader finishes loading its data, so you
    //can process the results in the Cursor argument.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //In our case we call ContactAdapter's swapCursor method with the Cursor as its argument
        //so the ContactsAdapter can refresh the RV based on the new Cursor contents.
        contactsAdapter.swapCursor(data);
    }

    //onLoaderReset is called by the LoaderManager when a Loader is reset its data no longer available.
    //At this point the app should immediately disconnect from the data.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //In our case we call the ContactAdapter's swapCursor method with the null argument
        //to indicate there is no data to bind to the RV.
        contactsAdapter.swapCursor(null);
    }



}
