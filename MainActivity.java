package com.jarvis.ai;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    TextView status, output;
    EditText input;
    Button mic;
    SpeechRecognizer recognizer;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        output = findViewById(R.id.output);
        input = findViewById(R.id.input);
        mic = findViewById(R.id.micButton);
        Button access = findViewById(R.id.accessButton);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US);
        });

        access.setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        mic.setOnClickListener(v -> listen());
        input.setOnEditorActionListener((v, actionId, event) -> {
            runCommand(input.getText().toString());
            input.setText("");
            return true;
        });

        requestPermissionsIfNeeded();
        speak("JARVIS online.");
    }

    void requestPermissionsIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> p = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                p.add(Manifest.permission.RECORD_AUDIO);
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                p.add(Manifest.permission.CALL_PHONE);
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                p.add(Manifest.permission.READ_CONTACTS);
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                p.add(Manifest.permission.SEND_SMS);
            if (!p.isEmpty()) requestPermissions(p.toArray(new String[0]), 20);
        }
    }

    void listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition is not available on this phone.");
            return;
        }
        if (recognizer != null) recognizer.destroy();
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle b) { status.setText("LISTENING"); }
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float v) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() { status.setText("PROCESSING"); }
            public void onError(int e) { status.setText("STANDBY"); }
            public void onEvent(int t, Bundle b) {}
            public void onPartialResults(Bundle b) {}
            public void onResults(Bundle b) {
                ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (r != null && !r.isEmpty()) runCommand(r.get(0));
                status.setText("STANDBY");
            }
        });
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizer.startListening(i);
    }

    void runCommand(String raw) {
        if (raw == null) return;
        String c = raw.trim();
        String x = c.toLowerCase(Locale.ROOT);
        output.setText("› " + c);

        if (x.matches(".*\\b(hello|hi)\\b.*")) {
            speak("Hello. How can I help?");
            return;
        }

        if (x.contains("what time") || x.equals("time") || x.contains("current time")) {
            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            speak("The time is " + time);
            return;
        }

        if (x.contains("open settings")) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            speak("Opening settings.");
            return;
        }

        if (x.contains("open notifications")) {
            try {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } catch (Exception e) { speak("I cannot open that screen on this phone."); }
            return;
        }

        if (x.startsWith("search youtube for ") || x.startsWith("search youtube ")) {
            String q = c.replaceFirst("(?i)^search youtube (for )?", "").trim();
            Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(q)));
            startActivity(i);
            speak("Opening YouTube and searching for " + q);
            return;
        }

        if (x.contains("open youtube")) {
            openAppByName("youtube", "com.google.android.youtube");
            return;
        }

        if (x.contains("open whatsapp")) {
            openAppByName("whatsapp", "com.whatsapp");
            return;
        }

        if (x.contains("open instagram")) {
            openAppByName("instagram", "com.instagram.android");
            return;
        }

        if (x.contains("open chrome")) {
            openAppByName("chrome", "com.android.chrome");
            return;
        }

        if (x.contains("open camera")) {
            startActivity(new Intent("android.media.action.IMAGE_CAPTURE"));
            speak("Opening camera.");
            return;
        }

        if (x.contains("open calendar")) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_APP_CALENDAR);
            try { startActivity(i); speak("Opening calendar."); }
            catch (Exception e) { speak("Calendar app not found."); }
            return;
        }

        if (x.startsWith("call ")) {
            String number = c.substring(5).trim();
            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                startActivity(i);
            else requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 21);
            return;
        }

        if (x.startsWith("message ") || x.startsWith("sms ")) {
            String body = c.replaceFirst("(?i)^(message|sms)\\s+", "").trim();
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
            i.putExtra("sms_body", body);
            startActivity(i);
            speak("Opening messages.");
            return;
        }

        if (x.startsWith("set alarm")) {
            int h = new java.util.Calendar.Builder().setInstant(java.time.Instant.now()).build()
                    .get(java.util.Calendar.HOUR_OF_DAY);
            int m = new java.util.Calendar.Builder().setInstant(java.time.Instant.now()).build()
                    .get(java.util.Calendar.MINUTE) + 1;
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, h);
            i.putExtra(AlarmClock.EXTRA_MINUTES, m % 60);
            i.putExtra(AlarmClock.EXTRA_MESSAGE, "JARVIS alarm");
            startActivity(i);
            speak("Opening the alarm screen.");
            return;
        }

        if (x.startsWith("timer ")) {
            String n = x.replaceFirst("^timer\\s+", "").replaceAll("[^0-9]", "");
            if (!n.isEmpty()) {
                Intent i = new Intent(AlarmClock.ACTION_SET_TIMER);
                i.putExtra(AlarmClock.EXTRA_LENGTH, Integer.parseInt(n));
                i.putExtra(AlarmClock.EXTRA_MESSAGE, "JARVIS timer");
                startActivity(i);
                speak("Setting the timer.");
                return;
            }
        }

        // Generic app launcher: resolves installed launcher apps by label.
        if (x.startsWith("open ")) {
            String name = c.substring(5).trim();
            if (openInstalledApp(name)) return;
        }

        if (x.startsWith("go to ")) {
            String name = c.substring(6).trim();
            if (openInstalledApp(name)) return;
        }

        speak("I heard " + c + ". That action is not installed yet.");
    }

    boolean openInstalledApp(String wanted) {
        PackageManager pm = getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN, null);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> apps = pm.queryIntentActivities(launcher, 0);
        for (android.content.pm.ResolveInfo r : apps) {
            CharSequence label = r.loadLabel(pm);
            if (label != null && label.toString().toLowerCase(Locale.ROOT)
                    .contains(wanted.toLowerCase(Locale.ROOT))) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                i.setPackage(r.activityInfo.packageName);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(i);
                    speak("Opening " + label + ".");
                    return true;
                } catch (Exception ignored) {}
            }
        }
        speak("I could not find " + wanted + ".");
        return false;
    }

    void openAppByName(String label, String fallbackPackage) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(fallbackPackage);
            if (i != null) {
                startActivity(i);
                speak("Opening " + label + ".");
                return;
            }
        } catch (Exception ignored) {}
        openInstalledApp(label);
    }

    void speak(String s) {
        output.setText(s);
        if (tts != null) tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "jarvis");
    }

    @Override protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
