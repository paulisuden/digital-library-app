package com.pm.library.controllers.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pm.library.R;
import com.pm.library.business.entity.Book;
import com.pm.library.business.persistence.BookDB;
import com.pm.library.business.api.interfaces.IGoogleBooksApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterFragment extends Fragment {

    private EditText txttitle, txtisbn, txtauthor, txtyear, txtprice;
    private Button btnSave, btnSelectPhoto, btnSearch;
    private BookDB bookDB;
    private ImageView imagePreview;
    private Uri imageUri;
    private ProgressBar progressBar;
    private IGoogleBooksApi googleBooksApi;

    // abrir galeria
    private final ActivityResultLauncher<Intent> selectImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imagePreview.setImageURI(imageUri);
                }
            });


    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        txttitle = view.findViewById(R.id.reg_txttitle);
        txtisbn = view.findViewById(R.id.reg_txtisbn);
        txtauthor = view.findViewById(R.id.reg_txtauthor);
        txtyear = view.findViewById(R.id.reg_txtpublicationyear);
        txtprice = view.findViewById(R.id.reg_txtprice);
        btnSave = view.findViewById(R.id.reg_btnsave);
        btnSelectPhoto = view.findViewById(R.id.reg_btnselectphoto);
        imagePreview = view.findViewById(R.id.reg_imgpreview);
        btnSearch = view.findViewById(R.id.reg_btnsearch);
        progressBar =  view.findViewById(R.id.reg_progressbar);

        bookDB = new BookDB(getContext(), "BooksDB.db", null, 1);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create()) //traduce el json a un json object
                .build();

        googleBooksApi = retrofit.create(IGoogleBooksApi.class);


        btnSave.setOnClickListener(v -> save());

        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            selectImage.launch(intent);
        });

        btnSearch.setOnClickListener(v -> searchBook());

        return view;
    }

    private void save() {
        Book book = new Book();
        book.setTitle(txttitle.getText().toString());
        book.setIsbn(txtisbn.getText().toString());
        book.setAuthor(txtauthor.getText().toString());
        book.setPublicationYear(Integer.parseInt(txtyear.getText().toString()));
        book.setPrice(Double.parseDouble(txtprice.getText().toString()));

        if (imageUri != null) {
            String path = saveImageInternally(imageUri);
            book.setImage(path);
        }
        else {
            book.setImage("vacio");
        }

        bookDB.add(book);
        Toast.makeText(getContext(), "Libro guardado", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ListFragment())
                .addToBackStack(null)
                .commit();

        BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_nav);

        if (navView != null) {
            navView.setSelectedItemId(R.id.nav_list);
        }

        clear();
    }

    private String saveImageInternally(Uri imageUri) {
        try {
            String fileName = "book_" + System.currentTimeMillis() + ".jpg";
            File destination = new File(requireContext().getFilesDir(), fileName);

            //abre la imagen
            InputStream is = requireContext().getContentResolver().openInputStream(imageUri);
            //archivo vacio para guardar la imagen
            FileOutputStream fos  = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            //libera recursos
            fos.flush();
            fos.close();
            is.close();

            return destination.getAbsolutePath();

        } catch (IOException e) {
            Toast.makeText(getContext(),"Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void clear() {
        txttitle.setText("");
        txtisbn.setText("");
        txtauthor.setText("");
        txtyear.setText("");
        txtprice.setText("");
        imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
        imageUri = null;
    }

    private void searchBook() {
        String title = txttitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese un titulo para buscar el libro", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        btnSearch.setEnabled(false);

        googleBooksApi.searchBooks(title, 1, "es").enqueue(new Callback<JsonObject>() { //enqueue ejecuta en segundo pllano
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    BookData data = parseBookData(response.body());
                    if (data != null) {
                        fillFields(data);
                        Toast.makeText(getContext(), "Datos cargados. Revise si son correctos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No se encontraron datos", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                Toast.makeText(getContext(), "Error. Intentelo nuevamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BookData parseBookData(JsonObject jsonObject) {
        try {

            if (jsonObject.get("totalItems").getAsInt() == 0) {
                return null;
            }

            jsonObject = jsonObject
                    .getAsJsonArray("items")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("volumeInfo");

            BookData data = new BookData();
            data.title = jsonObject.get("title").getAsString();

            if (jsonObject.get("publishedDate").getAsString().length() > 4) {
                data.year = jsonObject.get("publishedDate").getAsString().substring(0, 4);
            }

            if (jsonObject.has("authors")) {
                data.author = jsonObject.getAsJsonArray("authors").get(0).getAsString();
            }

            if (jsonObject.has("industryIdentifiers")) {
                JsonArray isbn = jsonObject.getAsJsonArray("industryIdentifiers");
                for (int i = 0; i < isbn.size(); i++) {
                    JsonObject id = isbn.get(i).getAsJsonObject();
                    if ("ISBN_13".equals(id.get("type").getAsString())) {
                        data.isbn = id.get("identifier").getAsString();
                        break;
                    }
                    if ("ISBN_10".equals(id.get("type").getAsString())) {
                        data.isbn = id.get("identifier").getAsString();
                    }
                }
            }

            return data;

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al parsear los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void fillFields(BookData data) {
        if (data.title != null) {
            txttitle.setText(data.title);
        }
        if (data.author != null) {
            txtauthor.setText(data.author);
        }
        if (data.isbn != null) {
            txtisbn.setText(data.isbn);
        }
        if (data.year != null) {
            txtyear.setText(data.year);
        }
    }

    private static class BookData {
        String title, author, isbn, year = "";
    }
}