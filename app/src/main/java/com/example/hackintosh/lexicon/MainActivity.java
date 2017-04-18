package com.example.hackintosh.lexicon;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.app.NotificationCompat;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private GestureDetector listGestureDetector;
    private GestureDetector layoutGestureDetector;
    private NotificationCompat.Builder mBuilder;
    private Intent notificationIntent;
    private NotificationLexicon notificationLexicon;
    private int time = 0;
    private BroadcastReceiver receiver;
    private PopupMenu editItem = null;

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
        lexiconList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("OnItemLongClick","detected at position" + i);
                showEditItem(view, i);
                return true;
            }
        });
        lexiconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Item onClick","position " + i);
                Intent intent = new Intent(MainActivity.this,LexiconItem.class);
                NotificationLexicon notificationLexicon = new NotificationLexicon(lexicon, i, translateFrom);
                intent.putExtra("message",notificationLexicon);
                startActivity(intent);
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
        //time = lexiconDB.getNotificationTime();
        uploadLexicon();
        updateLexiconList();
        onSwipeListChange();
        //setNotificationBuilder();
        if(!isMyServiceRunning(AppService.class)) {
            startAppService();
        }
        else {
            Log.d("Service","running");
            restartService();
        }

        receiver  = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","Works");
//                time = intent.getExtras().getInt("time");
//                lexiconDB.setNotificationTIme(time);
                updateAppService();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("TIME"));
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

    public void uploadLexicon() {
        Log.d("translate_FROM",translateFrom);
        if(!translateFrom.equals("NONE")) {
            lexicon = lexiconDB.getTable(translateFrom);
            lexicons.put(translateFrom,lexicon);
        }
        if(lexicon == null) {
            lexicon = new ArrayList<String[]>();
        }
    }

    public void translate_text() {
        message = editText.getText().toString();
        Log.d("Message", "" + message);
        Log.d("Translate To",translateTo);
        if(!message.equals("") && message.trim().length() > 0) {
            translate.translate(message, translateTo);
        }
        editText.setText("");
    }

    public void updateLexicon(String text, String translation) {
        lexicon.add(0,new String[] {text, translation});
        if(notificationIntent != null) {
            stopService(notificationIntent);
        }
        else {
            startAppService();
        }
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
            //selectLanguage.getMenu().getItem(Integer.parseInt(translateTo_id)).setChecked(true);
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
        selectLanguage.getMenu().getItem(Integer.parseInt(translateTo_id)).setChecked(true);
        selectLanguage.show();
    }

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
        if(!lexicons.containsKey(this.translateFrom)) {
            lexicons.put(this.translateFrom,lexicon);
        }
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
        ListView lexiconList = (ListView) findViewById(R.id.lexiconList);
        listGestureDetector = gestureDetector();
        layoutGestureDetector = gestureDetector();
        lexiconList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view,MotionEvent event) {
                listGestureDetector.onTouchEvent(event);
                return false;
            }
        });
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                layoutGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    public GestureDetector gestureDetector() {
        GestureDetector gestureDetector;
        GestureListener gestureListener =new GestureListener(new GestureListener.swipeMotion() {
            @Override
            public void onLeft() {
                moveLeft();
            }

            @Override
            public void onRight() {
                moveRight();
            }
        });
        gestureDetector = new GestureDetector(MainActivity.this, gestureListener);

        return gestureDetector;
    }

    public void moveLeft() {
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
        stopService(notificationIntent);
        languages.clear();
    }

    public void moveRight() {
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
        stopService(notificationIntent);
        languages.clear();
    }

    public void startAppService() {
        if(lexicon != null && lexicon.size() != 0) {
            notificationIntent = new Intent(this, AppService.class);
            notificationLexicon = new NotificationLexicon(lexicon, time, translateFrom);
            notificationIntent.putExtra("lexicon", notificationLexicon);
            startService(notificationIntent);
        }
    }

    public void updateAppService() {
        if(notificationIntent == null && notificationLexicon == null) {
            notificationIntent = new Intent(this, AppService.class);
            notificationLexicon = new NotificationLexicon(lexicon, time, translateFrom);
        }
        notificationLexicon.setTime(time);
        notificationLexicon.setLexicon(lexicon);
        notificationIntent.putExtra("lexicon",notificationLexicon);
        startService(notificationIntent);
    }

    public void restartService() {
        notificationIntent = new Intent(this, AppService.class);
        notificationLexicon = new NotificationLexicon(lexicon, time, translateFrom);
        notificationIntent.putExtra("lexicon", notificationLexicon);
        stopService(notificationIntent);
        if(!isMyServiceRunning(AppService.class)) {
            Log.d("RestartService","service_stop");
            //startService(notificationIntent);
        }

    }

    public void showEditItem(final View view, final int i) {
        if(editItem == null) {
            editItem = new PopupMenu(this,view);
            editItem.inflate(R.menu.list_edit);
        }
        editItem.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_item:
                        editText.setText(getListItemText(view,0));
                        lexiconDB.deleteElement(translateFrom,getListItemText(view,0));
                        lexicon.remove(i);
                        updateLexiconList();
                        stopService(notificationIntent);
                        return true;
                    case R.id.delete:
                        lexiconDB.deleteElement(translateFrom,getListItemText(view,0));
                        Log.d("Delete index","" + i);
                        lexicon.remove(i);
                        updateLexiconList();
                        if(lexicon.size() == 0) {
                            deleteLexicon();
                        }
                        stopService(notificationIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });
        editItem.show();
    }

    public void deleteLexicon() {
        String previous_translateFrom = translateFrom;
        lexicon.clear();
        lexicons.remove(translateFrom);
        moveLeft();
        lexiconDB.deleteTable(previous_translateFrom);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public String getListItemText(View view, int index) {
        return ((TextView) ((LinearLayout) view).getChildAt(index)).getText().toString();
    }

    public LexiconDataBase getLexiconDB() { return lexiconDB; }

    public void setTranslateTo(String translateTo) {
        Log.d("TranslateTo","" + translateTo);
        this.translateTo = translateTo; }

    public void setTranslateTo_id(String translateTo_id) {
        Log.d("TranslateTo_id","" + translateTo_id);
        this.translateTo_id = translateTo_id; }

    public void setTime(int time) { this.time = time; }

    public String getTranslateFrom() { return this.translateFrom; }
}
