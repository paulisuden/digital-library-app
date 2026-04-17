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
import android.widget.Toast;

import com.pm.library.R;
import com.pm.library.business.entity.Book;
import com.pm.library.business.persistence.BookDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ManageFragment extends Fragment {

    EditText txttitle, txtisbn, txtauthor, txtyear, txtprice;
    Button btnUpdate, btnDelete, btnEditPhoto;
    BookDB bookDB;
    int id;
    ImageView imagePreview;
    Uri imageUri;

    private final ActivityResultLauncher<Intent> selectImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imagePreview.setImageURI(imageUri);
                }
            });


    public ManageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);

        txttitle = view.findViewById(R.id.man_txttitle);
        txtisbn = view.findViewById(R.id.man_txtisbn);
        txtauthor = view.findViewById(R.id.man_txtauthor);
        txtyear = view.findViewById(R.id.man_txtpublicationyear);
        txtprice = view.findViewById(R.id.man_txtprice);
        btnUpdate = view.findViewById(R.id.man_btnupdate);
        btnDelete = view.findViewById(R.id.man_btndelete);
        btnEditPhoto = view.findViewById(R.id.man_btneditphoto);
        imagePreview = view.findViewById(R.id.man_editimgpreview);

        bookDB = new BookDB(getContext(), "BooksDB.db", null, 1);

        // Recibir datos del Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            id = bundle.getInt("id");
            txttitle.setText(bundle.getString("title"));
            txtisbn.setText(bundle.getString("isbn"));
            txtauthor.setText(bundle.getString("author"));
            txtyear.setText(bundle.getInt("publication_year") + "");
            txtprice.setText(bundle.getDouble("price") + "");

            String path = bundle.getString("image");
            switch (path) {
                case "vacio":
                    imagePreview.setImageResource(R.drawable.vacio);
                    break;
                case "harrypotter":
                    imagePreview.setImageResource(R.drawable.harrypotter);
                    break;
                case "boulevard":
                    imagePreview.setImageResource(R.drawable.boulevard);
                    break;
                case "elprincipito":
                    imagePreview.setImageResource(R.drawable.elprincipito);
                    break;
                default:
                    File file = new File(path);
                    if (file.exists()) {
                        // Convertimos la ruta del archivo a una URI que el ImageView entienda
                        imageUri = Uri.fromFile(file);
                        imagePreview.setImageURI(imageUri);
                    } else {
                        imagePreview.setImageResource(R.drawable.vacio);
                    }
            }
        }

        btnEditPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            selectImage.launch(intent);
        });

        btnUpdate.setOnClickListener(v -> update());
        btnDelete.setOnClickListener(v -> delete());

        return view;
    }

    private void update() {
        Book book = new Book();
        book.setTitle(txttitle.getText().toString());
        book.setIsbn(txtisbn.getText().toString());
        book.setAuthor(txtauthor.getText().toString());
        book.setPublicationYear(Integer.parseInt(txtyear.getText().toString()));
        book.setPrice(Double.parseDouble(txtprice.getText().toString()));

        if (imageUri != null) {
            // nos fijamos si la actualizaron a la imagen:
            if (imageUri.toString().startsWith("content://")) { // viene de la galeria
                String path = saveImageInternally(imageUri);
                book.setImage(path);
            } else {
                book.setImage(imageUri.getPath()); // ya esta en la db
            }
        } else {
            book.setImage("empty");
        }

        bookDB.update(id, book);
        Toast.makeText(getContext(), "Actualizado", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack(); // vuelve atrás
    }

    private void delete() {
        bookDB.delete(id);
        Toast.makeText(getContext(), "Eliminado", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ListFragment())
                .addToBackStack(null)
                .commit();
    }

    private String saveImageInternally(Uri imageUri) {
        try {
            String fileName = "book_" + System.currentTimeMillis() + ".jpg";
            File destination = new File(requireContext().getFilesDir(), fileName);

            InputStream is = requireContext().getContentResolver().openInputStream(imageUri);
            FileOutputStream fos = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.flush();
            fos.close();
            is.close();

            return destination.getAbsolutePath();

        } catch (IOException e) {
            Toast.makeText(getContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}