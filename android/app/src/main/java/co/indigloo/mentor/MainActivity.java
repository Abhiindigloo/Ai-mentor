package co.indigloo.mentor;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputFilter;
import android.view.*;
import android.widget.*;
import androidx.room.Room;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final ApiClient api=new ApiClient();
    private LocalTutor tutor; private MentorDb db; private TextToSpeech tts;
    private LinearLayout root,log; private ScrollView scroll; private EditText question;
    private Spinner band,language; private CheckBox cloud,model; private TextView status;
    private final List<View> controls=new ArrayList<>(); private boolean busy=false,ttsReady=false;
    private String lastAnswer="";private TextRecognizer recognizer;
    private String band(){return new String[]{"koko","nova","principal"}[band.getSelectedItemPosition()];}
    private String lang(){return language.getSelectedItemPosition()==1?"hi":"en";}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.rgb(25,63,55));v.setPadding(dp(8),dp(7),dp(8),dp(7));return v;}
    private Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setOnClickListener(v->action.run());controls.add(b);return b;}
    private Spinner spinner(String[] items){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items));controls.add(s);return s;}
    private void message(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void busy(boolean on){busy=on;for(View v:controls)v.setEnabled(!on);question.setEnabled(!on);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        db=Room.databaseBuilder(getApplicationContext(),MentorDb.class,"mentor-practice").build();
        try(InputStream in=getAssets().open("lessons.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);tutor=new LocalTutor(out.toString("UTF-8"));}catch(Exception e){throw new IllegalStateException("Bundled lessons missing",e);}
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(8));root.setBackgroundColor(Color.rgb(244,246,238));setContentView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{root.setPadding(dp(12),insets.getSystemWindowInsetTop()+dp(8),dp(12),insets.getSystemWindowInsetBottom()+dp(8));return insets;});
        root.addView(text("INDIGLOO  ·  AI Mentor",24));root.addView(text("Your pace. Your way.",15));
        LinearLayout selectors=new LinearLayout(this);band=spinner(new String[]{"Koko · 1–4","Nova · 5–8","Principal · 9–12"});band.setSelection(1);language=spinner(new String[]{"English","हिन्दी"});selectors.addView(band,new LinearLayout.LayoutParams(0,dp(48),1));selectors.addView(language,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(selectors);
        cloud=new CheckBox(this);cloud.setText("Connected account (saves questions to server)");model=new CheckBox(this);model.setText("Use server AI · answers may contain mistakes");controls.add(cloud);controls.add(model);root.addView(cloud);root.addView(model);
        status=text("Local sample lessons · no model required",12);root.addView(status);
        LinearLayout toolbar=new LinearLayout(this);toolbar.addView(button("Account",this::account),new LinearLayout.LayoutParams(0,dp(48),1));toolbar.addView(button("Lessons",this::lessons),new LinearLayout.LayoutParams(0,dp(48),1));toolbar.addView(button("Practice",this::history),new LinearLayout.LayoutParams(0,dp(48),1));root.addView(toolbar);
        scroll=new ScrollView(this);log=new LinearLayout(this);log.setOrientation(LinearLayout.VERTICAL);scroll.addView(log);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout ways=new LinearLayout(this);for(String mode:new String[]{"hint","example","steps","answer"})ways.addView(button(mode,()->ask(lang().equals("hi")?"फिर से":"explain again",mode)),new LinearLayout.LayoutParams(0,dp(48),1));root.addView(ways);
        question=new EditText(this);question.setHint("Ask a question / प्रश्न लिखो");question.setMaxLines(3);question.setFilters(new InputFilter[]{new InputFilter.LengthFilter(600)});root.addView(question);
        LinearLayout actions=new LinearLayout(this);actions.addView(button("Photo",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,11);}),new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(button("Listen",this::listen),new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(button("Ask",()->ask(question.getText().toString(),"hint")),new LinearLayout.LayoutParams(0,dp(48),1));root.addView(actions);
        AdapterView.OnItemSelectedListener change=new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){reset();}public void onNothingSelected(AdapterView<?> p){}};band.setOnItemSelectedListener(change);language.setOnItemSelectedListener(change);cloud.setOnCheckedChangeListener((b,on)->{reset();if(on&&api.token.isEmpty()){cloud.setChecked(false);account();}});
        tts=new TextToSpeech(this,s->{ttsReady=s==TextToSpeech.SUCCESS;});
        reset();
    }
    private void reset(){api.session="";tutor.previous=null;lastAnswer="";log.removeAllViews();bubble(lang().equals("hi")?"नमस्ते! कोई पाठ चुनो या उससे जुड़ा प्रश्न पूछो।":"Hello! Choose a sample lesson, then ask as many questions as you like.",false);}
    private void bubble(String s,boolean student){TextView v=text((student?"You\n":"Mentor\n")+s,16);v.setTextIsSelectable(true);v.setPadding(dp(14),dp(12),dp(14),dp(12));v.setBackgroundColor(student?Color.rgb(224,235,215):Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(student?dp(25):0,dp(6),student?0:dp(15),dp(6));log.addView(v,p);scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    private void lessons(){try{List<String>titles=tutor.titles(band());new AlertDialog.Builder(this).setTitle("Illustrative sample lessons").setItems(titles.toArray(new String[0]),(d,i)->ask(titles.get(i),"hint")).show();}catch(Exception e){message(e.getMessage());}}
    private void ask(String q,String mode){
        if(busy||q.trim().isEmpty())return;final String b=band(),l=lang();final boolean connected=cloud.isChecked(),useModel=model.isChecked();busy(true);question.setText("");bubble(q,true);
        worker.execute(()->{try{JSONObject r=connected?api.chat(q,b,l,mode,useModel):tutor.reply(q,b,l,mode);String a=r.getString("answer");String source=r.has("source")&&!r.isNull("source")?r.getJSONObject("source").optString("title"):"Outside sample pack";String engine=r.optString("engine","sample-pack");Practice p=new Practice();p.question=q;p.answer=a;p.lesson=r.optString("lesson_id","");p.created=System.currentTimeMillis();db.practice().insert(p);db.practice().trim();runOnUiThread(()->{if(isFinishing()||isDestroyed())return;lastAnswer=a;bubble(a+"\n\n"+source+" · "+engine+"\n"+r.optString("notice",""),false);status.setText(connected?"Connected · "+r.optString("model_status"):"Offline sample explanations · saved on this device");busy(false);});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;message(e.getMessage());status.setText("Request failed. Switch off connected mode to use local lessons.");busy(false);});}});
    }
    private void account(){
        String[] options=api.token.isEmpty()?new String[]{"Sign in / register","Erase local practice"}:new String[]{"Sign in / register","Erase local practice","Sign out","Delete server account"};
        new AlertDialog.Builder(this).setTitle("Account & privacy").setItems(options,(d,i)->{if(i==0)login();else if(i==1)new AlertDialog.Builder(this).setMessage("Erase all practice saved on this phone?").setPositiveButton("Erase",(x,y)->worker.execute(()->{db.practice().erase();runOnUiThread(()->{reset();message("Local practice erased");});})).setNegativeButton("Cancel",null).show();else if(i==2){busy(true);worker.execute(()->{try{api.request("/v1/auth/logout","POST",new JSONObject());}catch(Exception ignored){}api.token="";api.session="";runOnUiThread(()->{cloud.setChecked(false);reset();busy(false);});});}else new AlertDialog.Builder(this).setMessage("Delete your server account and its conversations and progress? Local practice is erased separately.").setPositiveButton("Delete",(x,y)->{busy(true);worker.execute(()->{try{api.request("/v1/me","DELETE",null);api.token="";runOnUiThread(()->{cloud.setChecked(false);reset();busy(false);message("Server account deleted");});}catch(Exception e){runOnUiThread(()->{busy(false);message(e.getMessage());});}});}).setNegativeButton("Cancel",null).show();}).show();
    }
    private void login(){
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(20),0,dp(20),0);EditText url=new EditText(this);url.setHint("https://your-mentor-server");url.setText(api.base);EditText user=new EditText(this);user.setHint("Nickname");EditText pass=new EditText(this);pass.setHint("Password · 12+ characters");pass.setInputType(129);form.addView(url);form.addView(user);form.addView(pass);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Connect to your Mentor API").setMessage("Use a nickname. Connected chats are saved on the server. No API keys belong here.").setView(form).setPositiveButton("Sign in",null).setNeutralButton("Register",null).setNegativeButton("Cancel",null).create();dialog.setOnShowListener(v->{for(int which:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEUTRAL})dialog.getButton(which).setOnClickListener(w->{String base=url.getText().toString().trim().replaceAll("/+$","");Uri uri=Uri.parse(base);boolean emulator=BuildConfig.DEBUG&&"http".equals(uri.getScheme())&&"10.0.2.2".equals(uri.getHost());if((!"https".equals(uri.getScheme())&&!emulator)||uri.getHost()==null||uri.getUserInfo()!=null){message("Use HTTPS, or http://10.0.2.2:8000 in a debug emulator.");return;}String username=user.getText().toString(),password=pass.getText().toString();dialog.dismiss();busy(true);worker.execute(()->{try{ApiClient candidate=new ApiClient();candidate.base=base;JSONObject r=candidate.request(which==AlertDialog.BUTTON_POSITIVE?"/v1/auth/login":"/v1/auth/register","POST",new JSONObject().put("username",username).put("password",password));api.base=base;api.token=r.getString("access_token");api.session="";runOnUiThread(()->{busy(false);cloud.setChecked(true);reset();message("Connected for this app session");});}catch(Exception e){runOnUiThread(()->{busy(false);message(e.getMessage());});}});});});dialog.show();
    }
    private void history(){busy(true);worker.execute(()->{List<Practice> rows=db.practice().recent();StringBuilder b=new StringBuilder();for(Practice p:rows)b.append(p.question).append("\n").append(p.answer).append("\n\n");runOnUiThread(()->{busy(false);TextView t=text(b.length()==0?"Practice will appear here.":b.toString(),15);t.setTextIsSelectable(true);ScrollView s=new ScrollView(this);s.addView(t);new AlertDialog.Builder(this).setTitle("Practice saved on this phone").setView(s).setPositiveButton("Done",null).show();});});}
    private void listen(){if(!ttsReady||lastAnswer.isEmpty()){message("Speech is not ready yet.");return;}int result=tts.setLanguage(Locale.forLanguageTag(lang().equals("hi")?"hi-IN":"en-IN"));if(result<0){message("Install a voice for this language in Android settings.");return;}tts.speak(lastAnswer,TextToSpeech.QUEUE_FLUSH,null,"mentor");}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=11||result!=RESULT_OK||data==null||data.getData()==null)return;busy(true);try{InputImage image=InputImage.fromFilePath(this,data.getData());recognizer=lang().equals("hi")?TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build()):TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);recognizer.process(image).addOnSuccessListener(t->{question.setText(t.getText());message("Review the recognised text before sending.");}).addOnFailureListener(e->message("Photo could not be read. Try a clearer photo.")).addOnCompleteListener(t->{recognizer.close();recognizer=null;busy(false);});}catch(Exception e){busy(false);message("Unable to open that image.");}}
    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();if(recognizer!=null)recognizer.close();worker.execute(()->db.close());worker.shutdown();super.onDestroy();}
}
