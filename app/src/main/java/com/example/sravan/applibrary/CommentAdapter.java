package com.example.sravan.applibrary;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sravan.applibrary.objects.Comments;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sravan on 1/8/2018.
 */

//Builds all recycler views to display comments on the calendar and queries for the data from Firebase server
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CalendarViewHolder> {


    private Context mContext;
    private int mCount;

    private List<Comments> mComments = new ArrayList<>();

    //Sends query for list of commentss to the server and recieves calendar data
    public CommentAdapter(Context context, List<Comments> commentList, int count) {
        this.mContext = context;
        this.mComments = commentList;
        this.mCount = count;
    }

    //Sets up view holder inflater to display all the books
    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.user_comment, parent, false);
        return new CalendarViewHolder(view);
    }

    //Creates the view holder for each of the books queried
    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        if (mComments.get(position)==null) {
            return;
        }
        Comments comment = mComments.get(position);



        holder.title.setText(comment.getTitle());
        java.util.Date time = new java.util.Date((long) comment.getTimestamp()*1000);
        holder.date.setText(time.toString());
        holder.ratingBar.setNumStars(comment.getBookStars());
        holder.author.setText(comment.getAuthor());
        holder.text.setText(comment.getReview());
    }



    @Override
    public int getItemCount() {
        return mCount;
    }



    //Inner class to hold the views needed to display a single item in the recycler view
    class CalendarViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        RatingBar ratingBar;
        TextView author;
        TextView date;
        TextView text;

        //Constructor for viewholder
        public CalendarViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.comment_title);
            ratingBar = (RatingBar) itemView.findViewById(R.id.comment_rating);
            author = (TextView) itemView.findViewById(R.id.comment_author);
            date = (TextView) itemView.findViewById(R.id.comment_date);
            text = (TextView) itemView.findViewById(R.id.comment);
        }

    }
}
