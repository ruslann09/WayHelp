package com.way.help.wayhelp.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.way.help.wayhelp.R;

import java.util.ArrayList;

public class VoiceAssistantActivity extends AppCompatActivity {

    private Button speakButton;
    private ListView mList;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_assistant);

        speakButton = (Button) findViewById(R.id.button);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeak();
            }
        });
        mList = (ListView) findViewById(R.id.listView);
    }

    public void startSpeak() {
        Intent intent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // намерение для вызова формы обработки речи (ОР)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // сюда он слушает и запоминает
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What can you tell me?");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE); // вызываем активность ОР

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            ArrayList commandList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, commandList));

            // для лучшего распознавания английского языка, поставьте в настройках англ. яз как язык системы
            // хотя все то же самое можно проделать и с русскими словами
            if (commandList.contains("red")){
                speakButton.setText("red");
                speakButton.setBackgroundColor(Color.RED);
            }

            if (commandList.contains("blue")){
                speakButton.setText("blue");
                speakButton.setBackgroundColor(Color.BLUE);
            }

            if (commandList.contains("green")){
                speakButton.setText("green");
                speakButton.setBackgroundColor(Color.GREEN);
            }

            if (commandList.contains("yellow")){
                speakButton.setText("yellow");
                speakButton.setBackgroundColor(Color.YELLOW);
            }

            if (commandList.contains("white")){
                speakButton.setText("white");
                speakButton.setBackgroundColor(Color.WHITE);
            }

            if (commandList.contains("black")){
                speakButton.setText("black");
                speakButton.setBackgroundColor(Color.BLACK);
            }

            // выйти
            if (commandList.contains("finish")){
                finish();
            }
            // попробуем открыть гугловские карты
            if (commandList.contains("Maps")){
                Intent i = new Intent();
                PackageManager manager = getPackageManager();
                i = manager.getLaunchIntentForPackage("com.google.android.apps.maps");
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(i);
            }

            //попросим открыть некоторые сайты

            if (commandList.contains("google")){
                finish();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                startActivity(browserIntent);
            }

            if (commandList.contains("facebook")){
                finish();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com"));
                startActivity(browserIntent);
            }

            // если у Вас есть права суперпользователя
            // Можно как-то и добавив  "android.permission.REBOOT", но я не стал на этом заморачиваться (пока)
            if (commandList.contains("reboot")){
                try {
                    Process proc = Runtime.getRuntime()
                            .exec(new String[]{ "su", "-c", "reboot -p" });
                    proc.waitFor();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}
