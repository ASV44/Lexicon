package com.example.hackintosh.lexicon;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Translate translate;
    private String message;
    private String translateTo = "ro";
    private String translateTo_id = "2";
    private String translateFrom = "NONE";
    private EditText editText;
    private List<String[]> lexicon;
    private ArrayAdapter<String[]> adapter = null;
    private PopupMenu selectLanguage = null;
    private LexiconDataModel mDbHelper = new LexiconDataModel(this);
    private LexiconDataBase lexiconDB;
    private Map<String,List<String[]>> lexicons = new HashMap<>();
    private Map<String,ArrayAdapter<String[]>> lexiconsAdapters = new HashMap<>();
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        lexiconDB = new LexiconDataBase(this);
        lexiconDB.getCurrentLanguages();

        final ListView lexiconList = (ListView) findViewById(R.id.lexiconList);
        lexiconList.post(new Runnable() {
          @Override
          public void run() {
              lexiconList.smoothScrollToPosition(0);
            }
          });

        editText = (EditText) findViewById(R.id.edit_message);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    translate_text();
                    return true;
                }
                return false;
            }
        });

        translate = new Translate(this);
        Button translateButton = (Button) findViewById(R.id.translate);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translate_text();
            }
        });
        translateButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d("Long Press","active");
                showSelectLanguage(view);
                return true;
            }
        });
        uploadLexicon();
        updateLexiconList();
        onSwipeListChange();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //getMenuInflater().inflate(R.menu.activity_main_drawer,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    public void translate_text() {
        message = editText.getText().toString();
        Log.d("Message", "" + message);
        Log.d("Translate To",translateTo);
        if(!message.equals("")) {
            translate.translate(message, translateTo);
        }
        editText.setText("");
    }

    public void updateLexicon(String text, String translation) {
        lexicon.add(0,new String[] {text, translation});
        updateLexiconList();
    }

    public void updateLexiconList() {
        ListView lexiconList = (ListView)findViewById(R.id.lexiconList);
        if(!lexiconsAdapters.containsKey(translateFrom)) {
            adapter = new ArrayAdapter<String[]>(this, R.layout.lexicon_list_item, lexicon) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    String[] currentTranslation = getItem(position);

                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext())
                                .inflate(R.layout.lexicon_list_item, null);
                    }

                    ((TextView) convertView.findViewById(R.id.initial_text))
                            .setText(currentTranslation[0]);
                    ((TextView) convertView.findViewById(R.id.translated_text))
                            .setText(currentTranslation[1]);
                    return convertView;

                }
            };
            lexiconList.setAdapter(adapter);
            lexiconsAdapters.put(translateFrom,adapter);
        }
        else {
            if(!lexiconsAdapters.get(translateFrom).equals(adapter)) {
                adapter = lexiconsAdapters.get(translateFrom);
                lexiconList.setAdapter(adapter);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void showSelectLanguage(View v) {
        if(selectLanguage == null) {
            selectLanguage = new PopupMenu(this, v);
            selectLanguage.inflate(R.menu.language_select);
            selectLanguage.getMenu().getItem(Integer.parseInt(translateTo_id)).setChecked(true);
            selectLanguage
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.english:
                                    translateTo = "en";
                                    translateTo_id = "0";
                                    selectLanguage.getMenu().getItem(0).setChecked(true);
                                    lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                                    return true;
                                case R.id.german:
                                    translateTo = "de";
                                    translateTo_id = "1";
                                    selectLanguage.getMenu().getItem(1).setChecked(true);
                                    lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                                    return true;
                                case R.id.romanian:
                                    translateTo = "ro";
                                    translateTo_id = "2";
                                    selectLanguage.getMenu().getItem(2).setChecked(true);
                                    lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                                    return true;
                                case R.id.russian:
                                    translateTo = "ru";
                                    translateTo_id = "3";
                                    selectLanguage.getMenu().getItem(3).setChecked(true);
                                    lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                                    return true;
                                case R.id.french:
                                    selectLanguage.getMenu().getItem(4).setChecked(true);
                                    translateTo_id = "4";
                                    translateTo = "fr";
                                    lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
        }

        selectLanguage.show();
    }

    public void uploadLexicon() {
        Log.d("translate_FROM",translateFrom);
        if(!translateFrom.equals("NONE")) {
            lexicon = lexiconDB.getTable(translateFrom);
        }
        if(lexicon == null) {
            lexicon = new ArrayList<String[]>();
        }
    }

    public LexiconDataBase getLexiconDB() { return lexiconDB; }

    public void setTranslateFrom(String translateFrom) {
        Log.d("translateFromMAIN",this.translateFrom);
        Log.d("translateFrom",translateFrom);
        if(!translateFrom.equals(this.translateFrom)) {
            lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
            changeCurrentLexicon(translateFrom);
        }
        this.translateFrom = translateFrom;
    }

    public void changeCurrentLexicon(String translateFrom) {
        Log.d("lexicons_keys","" + lexicons.keySet());
        if(!lexicons.containsKey(translateFrom)) {
            lexicon = lexiconDB.getTable(translateFrom);
            if(lexicon == null) {
                lexicon = new ArrayList<String[]>();

            }
            lexicons.put(translateFrom, lexicon);
        }
        else {
            lexicon = lexicons.get(translateFrom);
        }
    }

    public void onSwipeListChange() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.content_main);
        GestureListener gestureListener =new GestureListener(new GestureListener.swipeMotion() {
            @Override
            public void onLeft() {
                List<String> languages = lexiconDB.getDBlanguages();
                int index = languages.indexOf(translateFrom) + 1;
                if(index == languages.size()) { index = 0; }
                translateFrom = languages.get(index);
                lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                if(!lexicons.containsKey(translateFrom)) {
                    lexicon = lexiconDB.getTable(translateFrom);
                    lexicons.put(translateFrom,lexicon);
                }
                else {
                    lexicon = lexicons.get(translateFrom);
                }
                updateLexiconList();
            }

            @Override
            public void onRight() {
                List<String> languages = lexiconDB.getDBlanguages();
                int index = languages.indexOf(translateFrom) - 1;
                if(index == -1) { index = languages.size() -1; }
                translateFrom = languages.get(index);
                lexiconDB.setCurrentLanguage(translateFrom,"0",translateTo,translateTo_id);
                if(!lexicons.containsKey(translateFrom)) {
                    lexicon = lexiconDB.getTable(translateFrom);
                    lexicons.put(translateFrom,lexicon);
                }
                else {
                    lexicon = lexicons.get(translateFrom);
                }
                updateLexiconList();
            }
        });
        gestureDetector = new GestureDetector(MainActivity.this, gestureListener);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view,MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    public void setTranslateTo(String translateTo) { this.translateTo = translateTo; }

    public void setTranslateTo_id(String translateTo_id) { this.translateTo_id = translateTo_id; }
}
