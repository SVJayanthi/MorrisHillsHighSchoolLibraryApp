package com.example.sravan.applibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sravan.applibrary.objects.Book;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.auth.api.signin.internal.Storage;
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
import java.util.Objects;

/**
 * Created by Sravan on 1/8/2018.
 */

//Builds all recycler views to display books and queries for the data from Firebase server
public class BooksListAdapter extends RecyclerView.Adapter<BooksListAdapter.BooksViewHolder> {

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    //Instantiate objects
    private Context mContext;
    private int mCount;
    public static String id = null;
    public static int viewHolderCount = 0;



    final private ListItemClickListener mOnClickListener;
    private final static String TAG = BooksListAdapter.class.getSimpleName();

    //Listen for when an item is clicked
    public interface ListItemClickListener {
        void onListItemClick(String clickedBook);
    }

    private List<Book> mBooks;

    //Sends query for list of books to the server and recieves book data
    public BooksListAdapter(Context context, ListItemClickListener listener, List<Book> booksList,int count) {
        this.mContext = context;
        this.mOnClickListener = listener;
        this.mBooks = booksList;
        this.mCount = count;
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

    }

    //Sets up view holder inflater to display all the books
    @Override
    public BooksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.books_item_view, parent, false);
        Log.d(TAG, "ViewHolder Number: " + viewHolderCount);
        viewHolderCount++;
        return new BooksViewHolder(view);
    }

    //Creates the view holder for each of the books queried
    @Override
    public void onBindViewHolder(BooksViewHolder holder, int position) {
        if (mBooks.get(position)==null) {
            return; // bail if returned null
        }
        Book book = mBooks.get(position);

        String positioning = (book.getTitle()).replace(' ', '-').toLowerCase();
        StorageReference photoRef = mStorageRef.child("users/" + positioning + ".jpg");


        GlideApp.with(mContext)
                .load(photoRef)
                .fitCenter()
                .into(holder.imageBook);

        Log.d(TAG, "#" + position);
        holder.author.setText(book.getAuthor());
        holder.titleBook.setText(book.getTitle());
        int star = 0;
        if (book.getBookReviews()!=0) {
            star = (book.getBookStars()) / (book.getBookReviews());
        }
        holder.ratingBar.setNumStars(star);
        holder.progressBar.setMax(book.getBookNumbers());
        holder.progressBar.setProgress(book.getBookOut());

    }

    @Override
    public int getItemCount() { return mCount; }



    //Inner class to hold the views needed to display a single item in the recycler view
    class BooksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        ImageView imageBook;
        TextView titleBook;
        TextView author;
        RatingBar ratingBar;
        ProgressBar progressBar;

        //Constructor for viewholder
        public BooksViewHolder(View itemView) {
            super(itemView);

            imageBook = (ImageView) itemView.findViewById(R.id.book_image);
            titleBook = (TextView) itemView.findViewById(R.id.title_book);
            author = (TextView) itemView.findViewById(R.id.author);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
            itemView.setOnClickListener(this);
        }


        //Recieves position of item clicked and sends the book id to a new book display page
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            String clickedBook = mBooks.get(clickedPosition).getTitle();

            mOnClickListener.onListItemClick(clickedBook);
        }

    }
}
