package net.simplifiedcoding.sqlitedbcode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditServer extends ActionBarActivity implements View.OnClickListener{
    private EditText editTextIP;
    private EditText editTextName;
    private EditText editTextPort;
    private EditText editTextCommand;
    private EditText editTextStatus;
    private TextView serverID;
    private Button btnPrev;
    private Button btnNext;
    private Button btnSave;
    private Button btnDelete;

    private static final String SELECT_SQL = "SELECT * FROM servers";

    private SQLiteDatabase db;

    private Cursor cursor;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initializeViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            position = extras.getInt("SERVER", position);
        }

        try {
            openDatabase();
            cursor = db.rawQuery(SELECT_SQL, null);
            cursor.moveToPosition(position);
            showRecords();
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessage("No records found.");
                }
            });
        }
    }

    private void initializeViews() {
        serverID = (TextView) findViewById(R.id.serverID);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextIP = (EditText) findViewById(R.id.editTextIP);
        editTextPort = (EditText) findViewById(R.id.editTextPort);
        editTextCommand = (EditText) findViewById(R.id.editTextCommand);
        editTextStatus = (EditText) findViewById(R.id.editTextStatus);

        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    protected void openDatabase() {
        db = openOrCreateDatabase("ESPServerDB", Context.MODE_PRIVATE, null);
    }

    protected void showRecords() {
        String id = cursor.getString(cursor.getColumnIndex("id"));
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String ip = cursor.getString(cursor.getColumnIndex("ip"));
        int port = cursor.getInt(cursor.getColumnIndex("port"));
        String command = cursor.getString(cursor.getColumnIndex("command"));
        int status = cursor.getInt(cursor.getColumnIndex("status"));
        serverID.setText(id);
        editTextName.setText(name);
        editTextIP.setText(ip);
        editTextCommand.setText(command);
        if(port != 0)
            editTextPort.setText(Integer.toString(port));
        else
            editTextPort.setText("");
        String switchStatus =  (status == 1) ? "ON" : "OFF";
        editTextStatus.setText(switchStatus);
    }

    protected void moveNext() {
        if (!cursor.isLast()) {
            cursor.moveToNext();
            showRecords();
        }
        else
            showMessage("No next records found.");
    }

    protected void movePrev() {
        if (!cursor.isFirst()) {
            cursor.moveToPrevious();
            showRecords();
        }
        else
            showMessage("No previous records found.");

    }


    protected void saveRecord() {
        String id = serverID.getText().toString().trim();
        String ip = editTextIP.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String strPort = editTextPort.getText().toString().trim();
        int port = strPort.isEmpty() ? 0 : Integer.parseInt(strPort);
        String command = editTextCommand.getText().toString().trim();
        String status = editTextStatus.getText().toString().trim().equals("ON") ? "1" : "0";

        String sql = "UPDATE servers SET name='" + name + "',ip='" + ip + "', port='" + port + "', command='" + command + "', status='" + status + "' WHERE id=" + id + ";";

        if (name.equals("") || ip.equals("")) {
            showMessage("You cannot save blank values.");
            return;
        }
        try {
            db.execSQL(sql);
            showMessage("Records Saved Successfully.");
        }
        catch (Exception e){
            showMessage("Record with this name already exist.");
        }
        cursor = db.rawQuery(SELECT_SQL, null);
        cursor.moveToPosition(Integer.parseInt(id));
    }

    private void deleteRecord() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want delete this server configuration?");

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String id = serverID.getText().toString().trim();

                        String sql = "DELETE FROM servers WHERE id=" + id + ";";
                        db.execSQL(sql);
                        showMessage("Record Deleted.");
                        cursor = db.rawQuery(SELECT_SQL, null);
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View view) {
        if (view == btnNext) {
            moveNext();
        }

        if (view == btnPrev) {
            movePrev();
        }

        if (view == btnSave) {
            saveRecord();
        }

        if (view == btnDelete) {
            deleteRecord();
        }
    }

    private void showMessage(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.toast_background_color);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}