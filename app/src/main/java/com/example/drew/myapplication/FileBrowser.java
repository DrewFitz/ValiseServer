package com.example.drew.myapplication;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Paul on 11/9/2015.
 */
public class FileBrowser extends ListActivity {

    private ArrayList<String> pathComponents;

    private String currentPath() {
        String s = "";
        for (String component : pathComponents) {
            s += component;
        }
        return s;
    }

    @Override
    public void onBackPressed() {
        if (pathComponents.size() > 1) {
            pathComponents.remove(pathComponents.size()-1);
            rebuildList();
        } else {
            Intent intent = this.getIntent();
            this.setResult(0, intent);
            finish(); //ending the activity
        }
    }

    private void rebuildList() {
        File currentDirectory = new File(currentPath());
        ArrayList<String> filenames = new ArrayList<>();

        if (true || currentDirectory.canRead()) {
            File[] children = currentDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (true || !child.isHidden() && child.canRead()) {
                        if (child.isDirectory()) {
                            filenames.add(child.getName() + "/");
                        } else {
                            filenames.add(child.getName());
                        }
                    }
                }
            }
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, filenames); //the middle parameter can be replaced with a custom xml file. as of this writing it uses a default
        setListAdapter(adapter);
        registerForContextMenu(getListView()); //this allows us to differentiate between single clicks and "long clicks".

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_browser); //the xml file that controls the layout

        pathComponents = new ArrayList<>();
        String path = "/";

        pathComponents.add(path);

        rebuildList();

    }

    private void fileSelected(String filename) {
        // do a thing when selected
//        File file = new File(currentPath() + filename);
        Intent intent = this.getIntent(); //getting our current intent
//        FileSystem myServ = new FileSystem(file.getAbsolutePath()); //instantiating the fileserver object that we will use
//        intent.putExtra("Result", myServ); // adding the fileserver to our result
        intent.putExtra("Path", currentPath() + filename);
        this.setResult(0,intent); //returning the fileserver to the parent activity
        finish(); //ending the activity
    }

    @Override
    protected void onListItemClick(ListView myList, View myView, int position, long id){

        final String filename;

        filename = (String) getListAdapter().getItem(position);

        File file = new File(currentPath() + filename);

        if (file.isDirectory()) {
            pathComponents.add(filename);
            rebuildList();
        } else {
            new AlertDialog.Builder(this) //a dialog box, too short to necessitate its own file
                    .setTitle("[" + file.getName() + "]") //the title of the alert dialog is the filename
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() { //pressing this means the user wishes to use this file
                        @Override //
                        public void onClick(DialogInterface dialog, int which) {
                            fileSelected(filename);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).show();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View myView, ContextMenu.ContextMenuInfo menuInfo){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fb_context_menu, menu); //pulling up our context menu. This happens if a user "long clicks" on an object

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(item.getItemId()== R.id.negative){
            return true;

        }

        if(item.getItemId() == R.id.positive) {
            String filename = (String) getListAdapter().getItem(info.position);
            fileSelected(filename);
            return true;
        }

        return true;
    }
}
