// DatabaseHelper.java
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "billing_db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY, name TEXT, description TEXT, price REAL, quantity INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

    // CRUD operations for products
    public boolean addProduct(String name, String description, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        values.put("quantity", quantity);
        long result = db.insert("products", null, values);
        return result != -1;
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("products", null, null, null, null, null, null);
    }

    public double getTotalValue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(price * quantity) AS total_value FROM products", null);
        double totalValue = 0;
        if (cursor.moveToFirst()) {
            totalValue = cursor.getDouble(cursor.getColumnIndex("total_value"));
        }
        cursor.close();
        return totalValue;
    }

    public Cursor getMinPriceProduct() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("products", null, null, null, null, null, "price ASC", "1");
    }

    public Cursor getMaxPriceProduct() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("products", null, null, null, null, null, "price DESC", "1");
    }
}



// BillingActivity.java

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class BillingActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private ListView productListView;
    private TextView totalValueTextView, minPriceTextView, maxPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        databaseHelper = new DatabaseHelper(this);

        productListView = findViewById(R.id.product_list_view);
        totalValueTextView = findViewById(R.id.total_value_text_view);
        minPriceTextView = findViewById(R.id.min_price_text_view);
        maxPriceTextView = findViewById(R.id.max_price_text_view);

        loadProducts();
        updateTotalValue();
        updateMinMaxPriceProducts();

        findViewById(R.id.add_product_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement add product functionality
            }
        });
    }

    private void loadProducts() {
        Cursor cursor = databaseHelper.getAllProducts();
        ArrayList<String> productNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            String productName = cursor.getString(cursor.getColumnIndex("name"));
            productNames.add(productName);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productNames);
        productListView.setAdapter(adapter);
    }

    private void updateTotalValue() {
        double totalValue = databaseHelper.getTotalValue();
        totalValueTextView.setText(String.format("Total Value: $%.2f", totalValue));
    }

    private void updateMinMaxPriceProducts() {
        Cursor minPriceCursor = databaseHelper.getMinPriceProduct();
        if (minPriceCursor.moveToFirst()) {
            String minPriceProduct = minPriceCursor.getString(minPriceCursor.getColumnIndex("name"));
            double minPrice = minPriceCursor.getDouble(minPriceCursor.getColumnIndex("price"));
            minPriceTextView.setText(String.format("Minimum Priced Product: %s ($.2f)", minPriceProduct, minPrice));
        }
        minPriceCursor.close();

        Cursor maxPriceCursor = databaseHelper.getMaxPriceProduct();
        if (maxPriceCursor.moveToFirst()) {
            String maxPriceProduct = maxPriceCursor.getString(maxPriceCursor.getColumnIndex("name"));
            double maxPrice = maxPriceCursor.getDouble(maxPriceCursor.getColumnIndex("price"));
            maxPriceTextView.setText(String.format("Maximum Priced Product: %s ($.2f)", maxPriceProduct, maxPrice));
        }
        maxPriceCursor.close();
    }
}
