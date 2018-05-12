# MorrisHillsHighSchoolLibraryApp

To run the application:
Download MorrisHillsHighSchoolLibraryApp\app\release\app-release.apk
onto Android phone or emulator.

To accesss code:
Open MorrisHillsHighSchoolLibraryApp\app\src\main\java\com\example\sravan\applibrary
Open MorrisHillsHighSchoolLibraryApp\app\src\main\res


Synopsis
The Phi Beta Lamba HS Library App is fully functional Library app that allows Users to browse through books and checkout and return books. The information for each of the books is stored on a server so different Users on different phones can access the same information on the book (rating, author, etc.) and its availibility to check out. Each User creates their own account which is stored on a server with personal information used to recommend books. Once the User has created an account and logged in, the home screen will display a selection of personalized books from the server recommended for the User to read. Trending books are highlighted based off what recent Users on different phones have been looking at. If the user wishes to check out a book, the information about the book is read from the server to make sure there is at least one book available for the user to read. Once it is time to return the book, the user can return the novel and the server will update for the available book for other Users who wish to reserve the book. An interactive map of the library is provided for the User to see and search the books in the library. The User can search books based off a variety of categories such as the average rating, trending books, or available books. The User can select a book to analyze its details and interact with the book. The User can share their interest on social media or rate the book for future User's reference. The Calendar displays a list of the upcoming due dates for books and events at the library. There is also resources tab that lists sources for the User to conduct professional research from. 

Code Example
Sample of recieving data about a book from the server to display for the User.
@Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        DatabaseReference mBookReference = mBooksDatabaseReference.child(id);

        mBookReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.getValue(Book.class);
                mBookUser = book;

                title.setText(book.getTitle());
                author.setText(book.getAuthor());
                description.setText(book.getDescription());
                int availablen = book.getBookNumbers() - book.getBookOut();
                String availibility = Integer.toString(availablen);
                available.setText("Available- " + availibility);
                int star = 0;
                if (book.getBookReviews()!=0) {
                    star = (book.getBookStars()) / (book.getBookReviews());
                }
                ratingBar.setNumStars(star);
                progressBar.setMax(book.getBookNumbers());
                progressBar.setProgress(book.getBookOut());
                grade = book.getTargetGrade();
                placeImage();

                setBooksAdapter();
                setCommentsAdapter();
            }
        });
    }
    
   
License
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
