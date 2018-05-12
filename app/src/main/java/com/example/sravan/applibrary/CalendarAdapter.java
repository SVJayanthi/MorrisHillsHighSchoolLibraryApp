package com.example.sravan.applibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sravan.applibrary.objects.Event;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sravan on 1/8/2018.
 */

//Builds all recycler views to display events on the calendar and queries for the data from Firebase server
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    //Instantiate objects
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private Context mContext;
    private int mCount;


    final private ListItemClickListener mOnClickListener;

    //Listen for when an item is clicked
    public interface ListItemClickListener {
        void onListItemClick(int clickItemIndex, String key);
    }


     private List<Event> mEvents = new ArrayList<>();

    //Sends query for list of events to the server and recieves calendar data
    public CalendarAdapter(Context context, ListItemClickListener listener, List<Event> eventList, int count) {
        this.mContext = context;
        this.mOnClickListener = listener;
        this.mEvents = eventList;
        this.mCount = count;
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
    }

    //Sets up view holder inflater to display all the books
    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.events_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    //Creates the view holder for each of the books queried
    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        if (mEvents.get(position)==null) {
            return;
        }
        Event event = mEvents.get(position);

        String positioning = (mEvents.get(position).getBookKey()).replace(' ', '-').toLowerCase();
        StorageReference photoRef = mStorageRef.child("users/" + positioning + ".jpg");


        GlideApp.with(mContext)
                .load(photoRef)
                .fitCenter()
                .into(holder.imageEvent);


        holder.title.setText(event.getEvent());
        java.util.Date time = new java.util.Date((long) Long.parseLong(event.getDate())*1000);
        holder.date.setText(time.toString());
        holder.info1.setText(event.getEventInfo());
        holder.info2.setText(event.getLocation());
    }



    @Override
    public int getItemCount() {
        return mCount;
    }



    //Inner class to hold the views needed to display a single item in the recycler view
    class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView imageEvent;
        TextView title;
        TextView date;
        TextView info1;
        TextView info2;

        //Constructor for viewholder
        public CalendarViewHolder(View itemView) {
            super(itemView);

            imageEvent = (ImageView) itemView.findViewById(R.id.event_image);
            title = (TextView) itemView.findViewById(R.id.ec_title);
            date = (TextView) itemView.findViewById(R.id.ec_date);
            info1 = (TextView) itemView.findViewById(R.id.ec_infoL);
            info2 = (TextView) itemView.findViewById(R.id.ec_infoR);

            itemView.setOnClickListener(this);
        }


        //Recieves position of item clicked and sends the book id to a new book display page
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            String key = mEvents.get(clickedPosition).getKey();
            mOnClickListener.onListItemClick(clickedPosition, key);
        }

    }
}
