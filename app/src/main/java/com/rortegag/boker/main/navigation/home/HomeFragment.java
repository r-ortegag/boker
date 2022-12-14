package com.rortegag.boker.main.navigation.home;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.books.v1.Books;
import com.google.api.services.books.v1.BooksRequestInitializer;
import com.google.api.services.books.v1.model.Volume;
import com.rortegag.boker.R;
import com.rortegag.boker.models.book.Book;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<Book> bookList;

    private TextView
            txtSynopsisRecommended,
            txtSynopsisLatestSearches,
            txtGenreRecommended,
            txtGenreLatestSearches;
    private ProgressBar progressRecommended, progressLatestSearches;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        bookList = new ArrayList<>();
        txtSynopsisRecommended = root.findViewById(R.id.txtSynopsisRecommended);
        txtGenreRecommended = root.findViewById(R.id.txtGenreRecommended);
        txtSynopsisLatestSearches = root.findViewById(R.id.txtSynopsisLatestSearches);
        txtGenreLatestSearches = root.findViewById(R.id.txtGenreLatestSearches);
        progressRecommended = root.findViewById(R.id.progressRecommended);
        progressLatestSearches = root.findViewById(R.id.progressLatestSearches);

        LoadBooksTask loadBooksTask = new LoadBooksTask();

        loadBooksTask.execute();

        return root;
    }

    private class LoadBooksTask extends AsyncTask<Void, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(Void... voids) {
            try {
                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                Books googleBooks = new Books.Builder(httpTransport, jsonFactory, null)
                        .setGoogleClientRequestInitializer(new BooksRequestInitializer())
                        .setApplicationName("Boker")
                        .setGoogleClientRequestInitializer(new BooksRequestInitializer("AIzaSyAx6mvx4-J-TFCAHBNYWsdL0oUyKudAov4"))
                        .build();
                List<Volume> googleVolumes = getListVolume(googleBooks);
                String title, isbn = null, genre, author, synopsis, enhancedSynopsis;
                for (Volume volume : googleVolumes) {
                    try {
                        title = volume.getVolumeInfo().getTitle();
                    } catch (NullPointerException e) {
                        title = null;
                    }
                    try {
                        if(volume.getVolumeInfo().getIndustryIdentifiers().get(0).getType().contentEquals("ISBN_13")) {
                            isbn = volume.getVolumeInfo().getIndustryIdentifiers().get(0).getIdentifier();
                        } else if (volume.getVolumeInfo().getIndustryIdentifiers().get(1).getType().contentEquals("ISBN_13")) {
                            isbn = volume.getVolumeInfo().getIndustryIdentifiers().get(1).getIdentifier();
                        }
                    } catch (NullPointerException e) {
                        isbn = null;
                    }
                    try {
                        genre = volume.getVolumeInfo().getCategories().get(0);
                    } catch (NullPointerException e) {
                        genre = null;
                    }
                    try {
                        author = volume.getVolumeInfo().getAuthors().get(0);
                    } catch (NullPointerException e) {
                        author = null;
                    }
                    try {
                        synopsis = volume.getVolumeInfo().getDescription();
                    } catch (NullPointerException e) {
                        synopsis = null;
                    }
                    try {
                        if (title != null && isbn != null && genre != null && author != null && synopsis != null && !title.isEmpty() && !isbn.isEmpty() && !genre.isEmpty() && !author.isEmpty() && !synopsis.isEmpty()) {
                            try {
                                enhancedSynopsis = synopsis.replace(". ", ".\n\n");
                            } catch (NullPointerException e) {
                                enhancedSynopsis = synopsis;
                            }
                            bookList.add(new Book(title, isbn, genre, author, enhancedSynopsis));
                        }
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), "Error when entering when displaying the book data.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (GeneralSecurityException | IOException e) {
                Toast.makeText(getContext(), "Error when getting data from Google Books API.", Toast.LENGTH_SHORT).show();
            }

            return bookList;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            progressRecommended.setVisibility(View.GONE);
            txtSynopsisRecommended.setText(books.get(0).getSynopsis());
            txtGenreRecommended.setText(books.get(0).getGenre());
            progressLatestSearches.setVisibility(View.GONE);
            txtSynopsisLatestSearches.setText(books.get(1).getSynopsis());
            txtGenreLatestSearches.setText(books.get(1).getGenre());
        }

        private List<Volume> getListVolume(Books googleBooks) {
            List<Volume> googleVolumes = null;
            try {
                do {
                    googleVolumes = googleBooks.volumes()
                            .list("language:english")
                            .setMaxResults(2L)
                            .execute()
                            .getItems();
                } while (googleVolumes == null || googleVolumes.isEmpty());
            } catch (IOException e) { e.printStackTrace(); }
            return googleVolumes;
        }
    }
}