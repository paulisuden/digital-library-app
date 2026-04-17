package com.pm.library.controllers.fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.pm.library.R;
import com.pm.library.business.entity.Book;
import com.pm.library.business.persistence.BookDB;
import com.pm.library.controllers.adapters.BookAdapter;

import java.util.ArrayList;


public class ListFragment extends Fragment {

    ListView listView;
    BookDB bookDB;
    ArrayList<Book> allBooks;
    BookAdapter adapter;
    SearchView searchView;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        searchView = view.findViewById(R.id.search_view);
        listView = view.findViewById(R.id.list_books);
        bookDB = new BookDB(getContext(), "BooksDB.db", null, 1);
        allBooks = new ArrayList<>(bookDB.list());
        adapter = new BookAdapter(getContext(), new ArrayList<>());
        listView.setAdapter(adapter);
        adapter.updateList(allBooks);

        // configuramos q la lista tendra un context menu
        registerForContextMenu(listView);

        listView.setOnItemClickListener((adapterView, v, i, l) -> {
            Book book = (Book) listView.getItemAtPosition(i);
            int id = book.getId();
            ManageFragment fragment = new ManageFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("id", book.getId());
            bundle.putString("title", book.getTitle());
            bundle.putString("author", book.getAuthor());
            bundle.putString("isbn", book.getIsbn());
            bundle.putInt("publication_year", book.getPublicationYear());
            bundle.putDouble("price", book.getPrice());
            bundle.putString("image", book.getImage());
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() >= 3) {
                    filterBookList(s);
                } else {
                    adapter.updateList(allBooks);
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
        });

        return view;
    }

    private void filterBookList(String s) {
        ArrayList<Book> filterList = new ArrayList<>();

        for (Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(s.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(s.toLowerCase())) {
                filterList.add(book);
            }
        }
        adapter.updateList(filterList);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        requireActivity().getMenuInflater().inflate(R.menu.contextual_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo infoMenu =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = infoMenu.position;
        Book book = (Book) listView.getItemAtPosition(position);

        if (item.getItemId() == R.id.mc_delete) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar libro")
                    .setMessage("¿Seguro quiere eliminar el libro " + book.getTitle() + "?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        bookDB.delete(book.getId());
                        ArrayList<Book> freshList = new ArrayList<>(bookDB.list());
                        this.allBooks = freshList;
                        searchView.setQuery("", false);
                        adapter.updateList(freshList);
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else if (item.getItemId() == R.id.mc_qr) {
            String contentQr = "https://openlibrary.org/isbn/" + book.getIsbn();
            try {

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(contentQr, BarcodeFormat.QR_CODE, 400, 400);

                ImageView imageView = new ImageView(requireContext());
                imageView.setImageBitmap(bitmap);
                imageView.setPadding(40,40,40,40);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Código QR")
                        .setView(imageView)
                        .setPositiveButton("Aceptar", null)
                        .show();

            } catch (Exception e) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Error al generar el código QR")
                        .setPositiveButton("Aceptar", null)
                        .show();
            }
        }
        return true;
    }
}