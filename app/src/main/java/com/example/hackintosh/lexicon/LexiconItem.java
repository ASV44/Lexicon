package com.example.hackintosh.lexicon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LexiconItem extends AppCompatActivity {
    private Retrofit retrofit = null;
    private RetrofitResponse retrofitResponse = null;
    private String key = "4692032-42e921d271ca11655533d787c";
    private List<ImageDataModel.ImageDescription> imageData;
    private String message;
    private String translation;
    public String translateFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lexicon_item);
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
        Intent intent = getIntent();
        if(intent.getExtras().getSerializable("message") != null) {
            int index = ((NotificationLexicon) intent.getExtras().getSerializable("message")).getTime();
            message = ((NotificationLexicon) intent.getExtras().getSerializable("message")).getLexicon().get(index)[0];
            translation = ((NotificationLexicon) intent.getExtras().getSerializable("message")).getLexicon().get(index)[1];
            translateFrom = ((NotificationLexicon) intent.getExtras().getSerializable("message")).getLanguageFrom();
        }
        TextView textView_initial = (TextView) findViewById(R.id.initial_message);
        TextView textView_translated = (TextView) findViewById(R.id.translated_message);
        textView_initial.setText(message);
        textView_translated.setText(translation);
        getImage(message);
    }

    public void getImage(String imageRequest) {
        if(retrofit == null & retrofitResponse == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://pixabay.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            retrofitResponse = retrofit.create(RetrofitResponse.class);
        }
        retrofitResponse.getImage(key,imageRequest,translateFrom).enqueue(new Callback<ImageDataModel>() {
            @Override
            public void onResponse(Call<ImageDataModel> call, Response<ImageDataModel> response) {
                imageData = response.body().getHits();
                setImages();
            }

            @Override
            public void onFailure(Call<ImageDataModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error occurred during networking", Toast.LENGTH_SHORT).show();
                Log.e("error","" + t);
            }
        });
    }

    public void setImages() {
        int id;
        String viewName = "image";
        ImageView imageView;
        int iteration;
        if(imageData.size() < 3) {
            iteration = imageData.size();
        }
        else {
            iteration = 3;
        }
        for(int i = 0; i < iteration; i ++) {
            id = getResources().getIdentifier(viewName + String.valueOf(i + 1), "id", getPackageName());
            imageView = (ImageView) findViewById(id);
            Picasso.with(getApplicationContext()).load(imageData.get(i).getWebformatURL()).into(imageView);
        }
    }

}
