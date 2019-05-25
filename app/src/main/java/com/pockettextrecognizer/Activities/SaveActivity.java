package com.pockettextrecognizer.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.Button;

import com.pockettextrecognizer.Classes.DatabaseHelper;
import com.pockettextrecognizer.R;


public class SaveActivity extends AppCompatActivity {


    EditText editID,editName,editCapturedText;
    Button saveButton, updateButton, viewButton, deleteButton, resetButton;
    DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);


        /**
         * SQLite
         * Calling the constructor and creating a database instance myDB
         */
        myDB = new DatabaseHelper(this);

        /**
         * Casting views
         */
        editID = findViewById(R.id.edit_id);
        editName = findViewById(R.id.edit_name);
        editCapturedText = findViewById(R.id.edit_text);

        /**
         * Getting the captured text from mainActivity
         */
        editCapturedText.setText(getIntent().getStringExtra("capturedText"));


        /**
         * Casting buttons to layout
         */
        saveButton = findViewById(R.id.save_button);
        updateButton = findViewById(R.id.update_button);
        viewButton = findViewById(R.id.view_button);
        deleteButton = findViewById(R.id.delete_button);
        resetButton = findViewById(R.id.reset_button);


        SaveData();
        UpdateDataByID();
        ViewData();
        DeleteDataByID();
        Reset();

    }

    //
    public void SaveData() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                boolean isInserted = myDB.insertData(editName.getText().toString(),
                        editCapturedText.getText().toString());

                if(isInserted == true)
                    Toast.makeText(SaveActivity.this,R.string.saveSuccessful,Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(SaveActivity.this,R.string.saveUnsuccessful,Toast.LENGTH_LONG).show();
            }
        });
    }

    public void ViewData() {
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Cursor result = myDB.getAllData();

                if((result.getCount() == 0)){
                    showMessage("Error","No Data Found");

                    return;
                }
                StringBuilder builder = new StringBuilder();

                /**
                 * Building the message block
                 */
                while (result.moveToNext()) {
                    builder.append("Id : " + result.getString(0) + "\n");
                    builder.append("Name : " + result.getString(1) + "\n");
                    builder.append("Text : " + result.getString(2) + "\n\n");
                }

                /**
                 * Showing all data
                 */
                showMessage("Data",builder.toString());
            }

        });
    }


    public void showMessage(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        /**
        * Setting title and message via builder.
        * Set to be cancelable for returning to Activity.
        */
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void UpdateDataByID() {
        updateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //check if rows are updated
                        Integer updatedRows = myDB.updateData(editID.getText().toString(),
                                editName.getText().toString(),
                                editCapturedText.getText().toString());

                        if (updatedRows > 0)
                            Toast.makeText(SaveActivity.this, R.string.updateSuccessful, Toast.LENGTH_LONG).show();
                        else {
                            Toast.makeText(SaveActivity.this, R.string.updateUnsuccessful, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public void DeleteDataByID() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer deletedRows = myDB.deleteData(editID.getText().toString());

                if (deletedRows > 0)
                    Toast.makeText(SaveActivity.this, R.string.deleteSuccessful, Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(SaveActivity.this, R.string.deleteUnsuccessful, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void Reset() {

        resetButton.setOnClickListener(
                new View.OnClickListener() {

                    public void onClick (View v) {

                        Thread restartThread = new Thread() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        };
                        // Launching LoadingActivity.
                        restartThread.start();
                    }
                });
    }

}
