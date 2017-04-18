package com.example.hackintosh.lexicon;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.widget.Toast;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hackintosh on 2/17/17.
 */

public class Translate {

    private Context context;
    private Retrofit retrofit;
    private static RetrofitResponse retrofitResponse;
    private String key = "trnsl.1.1.20170216T192327Z.43d5fffcfb263a64.2def5a8f4d343ce8908b7d46a63e08417b3579b7";
    private String translated;
    private String translateFrom;
    private String translateTo;
    private MainActivity mainActivity;

    public Translate(Context context) {

        this.context = context;

        retrofit = new Retrofit.Builder()
                .baseUrl("https://translate.yandex.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitResponse = retrofit.create(RetrofitResponse.class);

        mainActivity = (MainActivity) context;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void translate(final String text, final String language) {
        getLanguage(text);
        retrofitResponse.getTranslation(key,language,text).enqueue(new Callback<DataModel>() {
            @Override
            public void onResponse(Call<DataModel> call, retrofit2.Response<DataModel> response) {
                String translation = response.body().getText().get(0);
                Log.v("Translation",translation);
                if(text.equals(translation)) {
                    translate_specificLanguage(text,language);
                }
                else {
                    translated = translation;
                    mainActivity.getLexiconDB().addElement(translateFrom, text, translated);
                    mainActivity.updateLexicon(text, translated);
                }
            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Toast.makeText(context, "An error occurred during networking", Toast.LENGTH_SHORT).show();
                Log.e("error","" + t);
            }
        });
    }

    public void translate_specificLanguage(final String text, final String language) {
        String specific_language = mainActivity.getTranslateFrom();
        translateFrom = specific_language;
        specific_language = specific_language + "-" + language;
        retrofitResponse.getTranslation(key,specific_language,text).enqueue(new Callback<DataModel>() {
            @Override
            public void onResponse(Call<DataModel> call, retrofit2.Response<DataModel> response) {
                String translation = response.body().getText().get(0);
                Log.v("Translation",translation);
                if(!text.equals(translation)) {
                    translated = translation;
                    mainActivity.getLexiconDB().addElement(translateFrom, text, translated);
                    mainActivity.updateLexicon(text,translated);
                }
                else {
                    Toast.makeText(context, "Text can't be translated\nCheck word or select another language", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Toast.makeText(context, "An error occurred during networking", Toast.LENGTH_SHORT).show();
                Log.e("error","" + t);
            }
        });
    }

//    public void getLanguage(String text) {
//        retrofitResponse.getLanguage(key,text).enqueue(new Callback<GetLanguage>() {
//            @Override
//            public void onResponse(Call<GetLanguage> call, Response<GetLanguage> response) {
//                translateFrom = response.body().getLanguage();
//                mainActivity.setTranslateFrom(translateFrom);
//                Log.v("Translate_From",translateFrom);
//            }
//
//            @Override
//            public void onFailure(Call<GetLanguage> call, Throwable t) {
//                Toast.makeText(context, "An error occurred during networking", Toast.LENGTH_SHORT).show();
//                Log.e("error","" + t);
//            }
//        });
//    }

    public void getLanguage(String text) {
        Call<GetLanguage> call = retrofitResponse.getLanguage(key,text);
        try {
            Response<GetLanguage> response = call.execute();
            translateFrom = response.body().getLanguage();
            mainActivity.setTranslateFrom(translateFrom);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTranslated() { return this.translated; }
}
