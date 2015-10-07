package ostromich.lev.gmail.com.plainofnotes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.Date;


public class MainActivity extends ActionBarActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int EDITOR_REQUEST_CODE = 100;
    private CursorAdapter cursorAdapter;
   // @TargetApi(Build.VERSION_CODES.JELLY_BEAN)


    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create DB
//        DBOpenHelper helper = new DBOpenHelper(this);
//        SQLiteDatabase database = helper.getWritableDatabase();

        // No new note need here  - it is in testing menu now
        //String newNote = "New Note: " + new Date();
        //insertNote(newNote);

        // Moved to Loader - no need cursor in Loader
        // Cursor cursor = getContentResolver().query(NotesProvider.CONTENT_URI, DBOpenHelper.ALL_COLUMNS,
        //        null, null, null, null);
        // String of columns
        String [] from = {DBOpenHelper.NOTE_TEXT};
        // int []  to = {android.R.id.text1}; - Replace with another Id
        int []  to = {R.id.tvNote};

        cursorAdapter = new SimpleCursorAdapter(this,
        //        android.R.layout.simple_expandable_list_item_1, null, from, to, 0);  // set up cursor to null
                // replace above layout with new layout
                R.layout.note_list_item, null, from, to, 0);
        // Get reference to the list
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
        //
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // editor activity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE , uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
        // init loader
        getLoaderManager().initLoader(0,null, this);

    }

    private void insertNote(String newNote) {
        // Insert new note Method
        ContentValues values = new ContentValues();

        values.put(DBOpenHelper.NOTE_TEXT, newNote);
        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        // Log to know tha tit worked
        String noteText = "New Note: " + new Date() + " " + noteUri.getLastPathSegment();

        String newNoteLog = noteText  + " " + noteUri.getLastPathSegment();
        //Log.d("MainActivity", "Inserted new note: " + newNoteLog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        // Check the selection
        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();

    }

    private void insertSampleData() {
        insertNote("One Note");
        insertNote("Multi-line \n note");
        insertNote("Very Long Note again note again Note again some " +
                "text know know study know note");
        // re-read the data
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    public void openEditorForNewNote(View view) {
        Intent intent = new  Intent(this, EditorActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }
}
