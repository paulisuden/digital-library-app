package com.pm.library.business.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pm.library.business.entity.Book;

import java.util.ArrayList;
import java.util.List;

public class BookDB extends SQLiteOpenHelper {
    Context context;
    public BookDB(@Nullable Context context, @Nullable String nameDB,
                  @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, nameDB, factory, version);
        this.context = context;
    }

    @Override
    // Se ejecuta una vez cuando se crea la DB
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE books (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "isbn TEXT, " +
                "author TEXT, " +
                "year INTEGER, " +
                "price REAL, " +
                "image TEXT)";
        db.execSQL(sql);

        String insert = "INSERT INTO books VALUES (null, " +
                "'El principito', " +
                "'485723', " +
                "'Antoine De Saint Exupery', 2008, 145000, 'elprincipito')";
        db.execSQL(insert);

        insert = "INSERT INTO books VALUES (null, " +
                "'Harry Potter', " +
                "'357197', " +
                "'J. K Rowling', 2026, 20000, 'harrypotter')";
        db.execSQL(insert);

        insert = "INSERT INTO books VALUES (null, " +
                "'Boulevard', " +
                "'357197', " +
                "'Flor M. Salvador', 2026, 20000, 'boulevard')";
        db.execSQL(insert);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Busca un libro a partir de su id
     * @param id
     * @return
     */

    public Book element(int id) {
        SQLiteDatabase database = getReadableDatabase(); // db modo lectura
        Cursor cursor = database.rawQuery("SELECT * FROM books WHERE _id = ?",
                new String[]{String.valueOf(id)}
        );
        try {
            // mueve el cursor a la primer fila de la tabla
            if (cursor.moveToNext()) { // si hay datos es true
                return extractBook(cursor);
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.d("TAG", "Error element(id) BookDB" + ex.getMessage());
            throw ex;
        } finally {
            cursor.close(); // libera la memoria
        }
    }

    private Book extractBook(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getInt(0));
        book.setTitle(cursor.getString(1));
        book.setIsbn(cursor.getString(2));
        book.setAuthor(cursor.getString(3));
        book.setPublicationYear(cursor.getInt(4));
        book.setPrice(cursor.getDouble(5));
        book.setImage(cursor.getString(6));
        return book;
    }

    /**
     * Busca un libro a partir de su titulo
     * @param title
     * @return
     */

    public Book element(String title) {
        SQLiteDatabase database = getReadableDatabase(); // db modo lectura
        Cursor cursor = database.rawQuery("SELECT * FROM books WHERE title = ?",
                new String[]{String.valueOf(title)}
        );
        try {
            if (cursor.moveToNext()) {
                return extractBook(cursor);
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.d("TAG", "Error element(title) BookDB" + ex.getMessage());
            throw ex;
        } finally {
            cursor.close();
        }
    }

    /**
     * Busca todos los libros
     * @return
     */
    public List<Book> list() {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT * FROM books ORDER BY title ASC";
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                bookList.add(
                        new Book(cursor.getInt(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getInt(4),
                                cursor.getDouble(5),
                                cursor.getString(6))
                );
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookList;
    }

    /**
     * Añade un nuevo libro
     * @param book
     */
    public void add(Book book) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("isbn", book.getIsbn());
        values.put("year", book.getPublicationYear());
        values.put("price", book.getPrice());
        values.put("image", book.getImage());
        database.insert("books", null, values);
    }

    /**
     * Actualiza los valores de un libro
     * @param id
     * @param book
     */
    public void update(int id, Book book) {
        SQLiteDatabase database = getWritableDatabase();
        String[] parameters = {String.valueOf(id)};
        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("isbn", book.getIsbn());
        values.put("year", book.getPublicationYear());
        values.put("price", book.getPrice());
        values.put("image", book.getImage());

        int rows = database.update("books", values, "_id=?", parameters);

        if (rows == 0) {
            Log.d("DB", "No se actualizó ningún registro");
        }
    }

    /**
     * Se borra un libro
     * @param id
     */

    public void delete(int id) {
        SQLiteDatabase database = getWritableDatabase();
        String[] parameters = {String.valueOf(id)};

        int rows = database.delete("books", "_id=?", parameters);

        if (rows == 0) {
            Log.d("DB", "No se actualizó ningún registro");
        }
    }
}