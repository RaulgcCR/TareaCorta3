package com.example.raul.reproductormusica;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.drawable.ic_media_play;
import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    SeekBar advanceSeekBar;
    TextView textView;
    int cancionActual;
    final ArrayList<Integer> listaCancionesId = new ArrayList<Integer>();
    final ArrayList<String> listaCanciones = new ArrayList<String>();


    public void playClicked(View view){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            Button button = (Button) findViewById(R.id.playButton);
            button.setBackgroundResource(R.drawable.play_circle);
            textView.animate().translationYBy(0f).setDuration(1000);

        }else{
            mediaPlayer.start();
            Button button = (Button) findViewById(R.id.playButton);
            button.setBackgroundResource(R.drawable.pause_circle);
            textView.animate().translationYBy(-1000f).setDuration(50000);
        }

    }

    public void nextSongClicked(View view){
        if(listaCancionesId.size() == (cancionActual + 1)){
            mediaPlayer.stop();
            cancionActual = 0;
        }else{
            mediaPlayer.stop();
            cancionActual += 1;
        }
        mediaPlayer = MediaPlayer.create(MainActivity.this, listaCancionesId.get(cancionActual));
        advanceSeekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        Button button = (Button) findViewById(R.id.playButton);
        button.setBackgroundResource(R.drawable.pause_circle);
    }


    public void backSongClicked(View view){
        if( 0 == cancionActual){
            mediaPlayer.stop();
            cancionActual = listaCancionesId.size() - 1;
        }else{
            mediaPlayer.stop();
            cancionActual -= 1;
        }
        mediaPlayer = MediaPlayer.create(MainActivity.this, listaCancionesId.get(cancionActual));
        advanceSeekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        Button button = (Button) findViewById(R.id.playButton);
        button.setBackgroundResource(R.drawable.pause_circle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cancionActual = 0;

        final Field[] drawables = R.raw.class.getFields();
        for (Field f : drawables) {
            if(f.getName().contains("lrc") == false){
                listaCancionesId.add(getResources().getIdentifier(f.getName(), "raw", getPackageName()));
                listaCanciones.add(f.getName());
            }
            //Log.i("Cancion: ",f.getName());
        }
        mediaPlayer = MediaPlayer.create(this, listaCancionesId.get(cancionActual));
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        textView = (TextView) findViewById(R.id.lyricsTextView);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        //mainLayout.setTranslationY(-2000f);


        StringBuilder lyrics = new StringBuilder();
        String linea;
        try {

            InputStream fraw = getResources().openRawResource(R.raw.fight_musiclrc);
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(fraw));

            //Log.i("Hola:", bufferedReader.readLine());
            while ((linea = bufferedReader.readLine()) != null){
                lyrics.append(linea).append("\n");
                //Log.i("Lyrics aquí:", bufferedReader.readLine());
            }
            Log.i("Lyrics aquí:", "");
            textView.setText(lyrics);

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.i("Error aquí:", e.toString());
        }


        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        SeekBar volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setMax(maxVolume);

        //Manejo de Volumen
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("volume", Integer.toString(progress));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        advanceSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);
        int duration = mediaPlayer.getDuration();
        int progress = mediaPlayer.getCurrentPosition();
        advanceSeekBar.setMax(duration);
        advanceSeekBar.setProgress(progress);


        advanceSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        advanceSeekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        volumeSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        volumeSeekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        int var = 100;
        advanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    Log.d("volume", Integer.toString(progress));
                    mediaPlayer.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                advanceSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                if(mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()){
                    cancionActual += 1;
                    mediaPlayer.stop();
                    mediaPlayer = MediaPlayer.create(MainActivity.this, listaCancionesId.get(cancionActual));
                    advanceSeekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                }/*
                if(mediaPlayer.isPlaying()){
                    textView.animate().translationYBy(-5);
                    textView.setHeight(textView.getHeight() + 5);
                }*/
            }
        }, 0, 1000);


        ListView listView = (ListView) findViewById(R.id.songsListView);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.lista_canciones, listaCanciones);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                mediaPlayer.stop();
                mediaPlayer = MediaPlayer.create(MainActivity.this, listaCancionesId.get(i));
                advanceSeekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                cancionActual = i;
                Button button = (Button) findViewById(R.id.playButton);
                button.setBackgroundResource(R.drawable.pause_circle);
                //Toast.makeText(getApplicationContext(), listaCanciones.get(i), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
