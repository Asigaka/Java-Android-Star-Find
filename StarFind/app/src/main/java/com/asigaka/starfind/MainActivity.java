package com.asigaka.starfind;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button firstButton;
    private Button secondButton;
    private Button thirdButton;
    private Button fourthButton;

    private String url = "https://bodysize.org/ru/top100/";
    private ArrayList<String> allNames;
    private ArrayList<String> possibleForSetNames;
    private ArrayList<String> imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeContent();
        updateContent();
    }

    private void initializeContent() {
        imageView = findViewById(R.id.imageView);
        firstButton = findViewById(R.id.firstButton);
        secondButton = findViewById(R.id.secondButton);
        thirdButton = findViewById(R.id.thirdButton);
        fourthButton = findViewById(R.id.fourthButton);

        allNames = new ArrayList<>();
        possibleForSetNames = new ArrayList<>();
        imageUrl = new ArrayList<>();

        loadNamesFromWeb();
        loadImageUrlFromWeb();
    }

    private void updateContent() {
        int randNum = (int)(Math.random() * imageUrl.size());

        setButtonsValues(randNum);
        setImage(randNum);
    }

    private void loadNamesFromWeb() {
        downloadStringFromWeb downloadStringFromWeb = new downloadStringFromWeb();
        String result = null;
        try {
            result = downloadStringFromWeb.execute(url).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        Pattern patternName = Pattern.compile("target=\"_blank\">(.*?)</a>");
        Matcher matcherName = patternName.matcher(result);
        while (matcherName.find()) {
            allNames.add(matcherName.group(1));
        }

        possibleForSetNames = allNames;
    }

    private void loadImageUrlFromWeb() {
        downloadStringFromWeb downloadStringFromWeb = new downloadStringFromWeb();
        String result = null;
        try {
            result = downloadStringFromWeb.execute(url).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        Pattern patternName = Pattern.compile("photo\" style=\"background-image: url(.*?);");
        Matcher matcherName = patternName.matcher(result);
        while (matcherName.find()) {
            imageUrl.add(matcherName.group(1).substring(1, matcherName.group(1).toCharArray().length - 1));
        }
    }

    private void setImage(int trueIndex) {
        downloadImageFromWeb task = new downloadImageFromWeb();
        Bitmap bitmap = null;
        try {
            bitmap = task.execute(imageUrl.get(trueIndex)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }

    private void setButtonsValues(int trueIndex) {
        int[] indexArr = new int[3];
        indexArr[(int)(Math.random() * indexArr.length)] = trueIndex;
        for (int i = 0; i < indexArr.length; i++) {
            if (indexArr[i] != trueIndex) {
                int randNum = (int)(Math.random() * imageUrl.size());
                indexArr[i] = randNum;
                possibleForSetNames.remove(randNum);
            }
        }

        System.out.println(trueIndex);
        for (int item : indexArr) {
            System.out.println(item);
        }
    }

    private static class downloadStringFromWeb extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream input = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            Log.i("URL", strings[0]);
            return result.toString();
        }
    }

    private static class downloadImageFromWeb extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection = null;
            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return null;
        }
    }
}