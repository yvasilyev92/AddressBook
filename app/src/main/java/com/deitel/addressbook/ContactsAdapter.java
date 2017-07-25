package com.deitel.addressbook;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deitel.addressbook.data.DatabaseDescription.Contact;

//ContactsAdapter is subclass of RecyclerView.Adapter that binds contacts to RecyclerView.

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {


    //Here we create the interface ContactClickListener that class ContactsFragment implements
    //to be notified when the user touches a contact in the RecyclerView.
    //Each item in the RecyclerView has a click listener that calls ContactClickListener's onClick
    //and passes the selected contact's uri. Then ContactsFragment notifies the MainActivity that
    //a contact was selected, so MA can display it in DetailFragment.
    public interface ContactClickListener{
        void onClick(Uri contactUri);
    }





    //ViewHolder maintains a reference to a RV's item's TextView and the DB rowID for the contact.
    //This nested subclass is used to implement the view-holder pattern in the context of RecyclerView.
    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView textView;
        private long rowID;


        //Here we configure a RV's item's ViewHolder.
        //ViewHolder constructor stores a reference to RV's item's TextView and sets its on View.OnClickListener
        //which passes the contact's URI to the adapter's ContactClickListener.
        public ViewHolder(View itemView){

            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);

            //attach a listener to itemView.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(Contact.buildContactUri(rowID));
                }
            });
        }

        //set the database row ID for the contact in this ViewHolder.
        public void setRowID(long rowID){
            this.rowID = rowID;
        }

    }



    //ContactsAdapter instance variables
    private Cursor cursor = null;
    private final ContactClickListener clickListener;



    //constructor.
    public ContactsAdapter(ContactClickListener clickListener){
        this.clickListener = clickListener;
    }






    //onCreateViewHolder sets up the new list item and its ViewHolder.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        //here we inflate the GUI for a ViewHolder object. In our case we used the
        //predefined layout android.R.layout.simple_list_item_1 which defines a layout containing
        //one TextView named text1.

        View view = LayoutInflater.from(parent.getContext()).inflate(
                android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }



    //onBindViewHolder sets the text of the list item to display the question tag.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        //We use moveToPosition to move to the contact that corresponds to the current RV'item's position.
        cursor.moveToPosition(position);

        //Then we set the ViewHolder's rowID by using Cursor method getColumnIndex to look up the column number
        // of the Contact._ID column. We then pass this number Cursor getLong method to get the contact's row ID.
        holder.setRowID(cursor.getLong(cursor.getColumnIndex(Contact._ID)));

        //Lastly we set the text for ViewHolder's TextView using a similar process as setting the row ID.
        //we look up the column number for the COLUMN_NAME column, and call getString to get the contacts name.
        holder.textView.setText(cursor.getString(cursor.getColumnIndex(Contact.COLUMN_NAME)));
    }




    //getItemCount returns the number of items that the adapter binds.
    @Override
    public int getItemCount() {

        //We return the total number of rows in the Cursor or Zero if the Cursor is null.
        return (cursor != null) ? cursor.getCount() : 0;
    }



    //We create swapCursor method to replace the adapter's current Cursor and notify the
    //adapter its data has changed.
    //swapCursor is called from the ContactFragment's onLoadFinished and onLoaderReset methods.
    public void swapCursor(Cursor cursor){

        this.cursor = cursor;
        notifyDataSetChanged();
    }

}
