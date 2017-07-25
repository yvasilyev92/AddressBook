package com.deitel.addressbook;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;


//MainActivity manages the app's fragments and coordinates the interactions between them.

//On Phones, MA always displays one Fragment at a time, starting with ContactsFragment.
//On Tablets, MA displays ContactsFragment on left, and DetailFragment or AddEditFragment on right.

//MainActivity uses FragmentTransaction to add and remove the App's Fragments.
//MA also implements 3 interfaces:
//1) ContactsFragmentListener contains callback methods that the ContactsFragment uses to tell MA
//when the user selects a contact in the contact list or adds a new contact.
//2) DetailFragmentListener contains callback methods that the DetailFragment uses to tell MA
// when the user deletes a contact or wishes to edit an existing one.
//3) AddEditFragmentListener contains a callback method that the AddEditFragment uses to tell the MA
//when the user saves a new contact or saves changes to an existing contact.



public class MainActivity extends AppCompatActivity implements
        ContactsFragment.ContactsFragmentListener,
        DetailFragment.DetailFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    //CONTACT_URI is used as a key in a key-value pair thats passed between
    //the MainActivity and its fragments.
    //Its a key for storing a contact's Uri in a Bundle passed to a fragment.
    public static final String CONTACT_URI = "contact_uri";


    //contactsFragment is used to tell ContactsFragment to update the displayed list of contacts
    //after a contact is added or deleted.
    private ContactsFragment contactsFragment;


    //onCreate is used to inflate MA's GUI.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //if layout contains fragmentContainer then use the phone layout.
        if (savedInstanceState != null && findViewById(R.id.fragmentContainer) != null){

            //create ContactsFragment
            contactsFragment = new ContactsFragment();
            //use a FragmentTransaction to add the ContactsFragment to the UI.
            //We call FragmentManager's beginTransaction method to obtain a FragmentTransaction.
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //Then we use 'add' to specify that when the FragmenTransaction completes, the
            //ContactsFragment should be attached to the View with the ID specified as the 1st arg.
            transaction.add(R.id.fragmentContainer, contactsFragment);
            //commit to finalize transaction and display ContactsFragment.
            transaction.commit();
        }
        //else use the Tablet layout.
        else {
            contactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.contactsFragment);
        }
    }


    //Now we implement ContactsFragmentListener interface methods onContactSelected and onAddContact.

    //onContactSelected is called by ContactsFragment to notify MainActivity when the user selects
    //a contact to display.
    @Override
    public void onContactSelected(Uri contactUri){

        //if the app is running on a phone, call displayContact, which replaces the ContactFragment
        //in the fragmentContainer with the DetailFragment that shows a contacts info.
        if (findViewById(R.id.fragmentContainer) != null)
            displayContact(contactUri, R.id.fragmentContainer);
        //else if its on Tablet, calls FragmentManager 'popBackStack' method to pop(remove)
        //the top Fragment on the backstack (if there is one), and call displayContact()
        //which replaces contents of rightPaneContainer with the DetailFragment thats shows
        //the contacts info.
        else {
            getSupportFragmentManager().popBackStack();
            displayContact(contactUri, R.id.rightPaneContainer);
        }
    }

    //onAddContact is called by the ContactsFragment to notify the MA when the user chooses
    //to add a new contact.
    @Override
    public void onAddContact(){

        //if the app is running on a phone, call displayAddEditFragment to display the AddEditFragment
        //in the fragmentContainer.
        if (findViewById(R.id.fragmentContainer) != null)
            displayAddEditFragment(R.id.fragmentContainer, null);
        //else if on tablet, display it in the rightPaneContainer.
        else
            displayAddEditFragment(R.id.rightPaneContainer, null);
    }


    //We create method displayContact to create a DetailFragment that displays the
    //selected contact. You can pass any args to a Fragment by the args in a Bundle of
    //key-value pairs. -We doe this to pass the selected contact's Uri so that the DetailFragment
    //knows which contact to get from the ContentProvider.
    private void displayContact(Uri contactUri, int viewID){

        //create a DetailFragment.
        DetailFragment detailFragment = new DetailFragment();

        //create our Bundle.
        Bundle arguments = new Bundle();
        //call 'putParcelable' method to store a key-value pair containing the
        //CONTACT_URI (String) as the key and contactUri (Uri) as the value.
        //Class Uri implements Parcelable interfae so a Uri can be stored in a Bundle.
        arguments.putParcelable(CONTACT_URI, contactUri);
        //then pass the Bundle to the Fragment's setArguments method.
        detailFragment.setArguments(arguments);

        //Then we get a FragmentTransaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //We call 'replace' to specify that when FragmentTransaction completes, the DetailFragment
        //should replace the contents of the View specified as the 1st arg.
        transaction.replace(viewID, detailFragment);
        //Then we call 'addToBackStack' to push (add) the DetailFragment onto the backstack.
        //this allows the user to touch the back button to pop the Fragment from the backstack.
        transaction.addToBackStack(null);
        transaction.commit(); //display DetailFragment


        //'addToBackStack' arg is an optional name for a back state. This can be used to pop multiple
        //fragments from the back stack to return to a prior state after multiple fragments have been added
        //to the back stack. By default only the topmost Fragment is popped.



    }






    //We created displayAddEditFragment for displaying the fragment for adding or editing an existing contact.
    //displayAddEditFragment receives a View's resource ID specifying where to attach the AddEditFragment
    //and a Uri representing the contact to edit.
    //If contactUri is null that means a new contact is being added. If not then we are editing.
    private void displayAddEditFragment(int viewID, Uri contactUri){


        //First we create our AddEditFragment.
        AddEditFragment addEditFragment = new AddEditFragment();

        //If contactUri is null that means a new contact is being added.
        //If contactUri is not null that means we are editing.
        if (contactUri != null){
            //create our Bundle.
            Bundle arguments = new Bundle();
            //call 'putParcelable' method to store a key-value pair containing the
            //CONTACT_URI (String) as the key and contactUri (Uri) as the value.
            arguments.putParcelable(CONTACT_URI, contactUri);
            //then pass the Bundle to the Fragment's setArguments method.
            addEditFragment.setArguments(arguments);
        }

        //Create a FragmentTransaction,
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //replace the contents of our viewID with our AddEditFragment
        transaction.replace(viewID, addEditFragment);
        //Then we call 'addToBackStack' to push (add) the AddEditFragment onto the backstack.
        //this allows the user to touch the back button to pop the Fragment from the backstack.
        transaction.addToBackStack(null);
        transaction.commit(); //display AddEditFragment
    }


    //Now we implement DetailFragmentListener's interface methods onContactDeleted & onEditContact.

    //Method onContactDeleted is called by DetailFragment to notify the MainActivity when the user
    //deletes a contact.
    @Override
    public void onContactDeleted(){
        //We pop(remove) the DetailFragment from the back stack so that the now
        //deleted contact's info is no longer displayed.
        getSupportFragmentManager().popBackStack();
        //and we call ContactFragments updateContactList to refresh the contacts list.
        contactsFragment.updateContactList();
    }

    //Method onEditContact is called by the DetailFragment to notify MA when the user touches
    //the app bar item to edit a contact. The DetailFragment passes a Uri representing the contact
    //to edit so it can be displayed in AddEditFragment for editing.
    @Override
    public void onEditContact(Uri contactUri){

        //if we are using a phone layout, display the AddEditFragment is fragmentContainer
        if (findViewById(R.id.fragmentContainer) != null)
            displayAddEditFragment(R.id.fragmentContainer, contactUri);
        //else its a Tablet and should display in rightPaneContainer.
        else
            displayAddEditFragment(R.id.rightPaneContainer, contactUri);
    }



    //Now we implement AddEditFragment's interface method onAddEditCompleted.

    //Method onAddEditCompleted is called by AddEditFragment to notify the MA when the user
    //saves a new contact or saves changes to an existing one.
    @Override
    public void onAddEditCompleted(Uri contactUri){

        //We do this by popping (removing) the AddEditFragment from the back stack so it is no
        //longer visible.
        getSupportFragmentManager().popBackStack();
        //then we call ContactsFragment updateContactList to refresh the contacts list.
        contactsFragment.updateContactList();


        //if the app is running on a tablet then fragmentContainer is null,
        //and we pop (remove) the back stack again to remove the DetailFragment if there is one.
        if (findViewById(R.id.fragmentContainer) == null){
            getSupportFragmentManager().popBackStack();
            //and display the contact that was just added or edited.
            displayContact(contactUri, R.id.rightPaneContainer);
        }
    }


}
