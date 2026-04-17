package com.pm.library.controllers.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pm.library.R;
import com.pm.library.business.entity.Book;

import java.io.File;
import java.util.ArrayList;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(@NonNull Context context, @NonNull ArrayList<Book> objects) {
        super(context, R.layout.item_book, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        Book book = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_book, parent, false);
        }

        TextView title = view.findViewById(R.id.title);
        TextView author = view.findViewById(R.id.author);
        TextView price = view.findViewById(R.id.price);
        ImageView photo = view.findViewById(R.id.photo);

        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        price.setText("$" + book.getPrice());
        String path = book.getImage();

        switch (path) {
            case "vacio":
                photo.setImageResource(R.drawable.vacio);
                break;
            case "harrypotter":
                photo.setImageResource(R.drawable.harrypotter);
                break;
            case "elprincipito":
                photo.setImageResource(R.drawable.elprincipito);
                break;
            case "boulevard":
                photo.setImageResource(R.drawable.boulevard);
                break;
            default:
                if (new File(path).exists()) {
                    photo.setImageBitmap(BitmapFactory.decodeFile(path));
                } else {
                    photo.setImageResource(R.drawable.vacio);
                }
        }

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        photo.startAnimation(animation);

        return view;
    }

    public void updateList(ArrayList<Book> newList) {
        this.clear();
        if (newList != null) {
            this.addAll(newList);
        }
        this.notifyDataSetChanged();
    }


}