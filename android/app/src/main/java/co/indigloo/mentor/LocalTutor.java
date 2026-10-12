package co.indigloo.mentor;
import org.json.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
public class LocalTutor {
    private final JSONArray pack;
    public String previous;
    private static final Set<String> STOP = new HashSet<>(Arrays.asList("a an the is are was were why what how do does in to of for and i me please explain tell about can you this that it with from का की के क्या कैसे क्यों है हैं में से को और मुझे समझाओ बताओ".split(" ")));
    private static final Pattern FOLLOW=Pattern.compile("^(why|how|again|simpler|explain again|i (?:do not|don't) understand|give (?:me )?an example|show (?:the )?answer|समझ नहीं आया|फिर से|आसान भाषा में|उदाहरण दो|जवाब दिखाओ)[?!.।\\s]*$",Pattern.CASE_INSENSITIVE);
    public LocalTutor(String json) throws JSONException { pack=new JSONArray(json); }
    private Set<String> words(String text){Set<String>s=new HashSet<>();for(String w:Normalizer.normalize(text,Normalizer.Form.NFC).toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{M}\\p{N}]+"))if(w.length()>1&&!STOP.contains(w))s.add(w);return s;}
    public List<String> titles(String band) throws JSONException {List<String>r=new ArrayList<>();for(int i=0;i<pack.length();i++){JSONObject d=pack.getJSONObject(i);if(d.getString("band").equals(band))r.add(d.getString("title"));}return r;}
    public JSONObject reply(String q,String band,String lang,String mode) throws JSONException {
        JSONObject found=null;double best=0;int bestOverlap=0,bestTitle=0;Set<String>query=words(q);
        for(int i=0;i<pack.length();i++){
            JSONObject d=pack.getJSONObject(i);if(!d.getString("band").equals(band))continue;
            if(previous!=null&&FOLLOW.matcher(q.trim()).matches()&&d.getString("id").equals(previous)){found=d;break;}
            Set<String>v=words(d.getString("title")+" "+d.getString("kw")+" "+d.getString("text")+" "+d.getJSONObject("answer").getString("hi"));int n=0;for(String w:query)if(v.contains(w))n++;
            int titleOverlap=0;for(String w:query)if(words(d.getString("title")).contains(w))titleOverlap++; double score=(double)n/Math.max(1,query.size());if(n>0&&score>=.5&&(score>best||(score==best&&(titleOverlap>bestTitle||(titleOverlap==bestTitle&&n>bestOverlap))))){best=score;bestOverlap=n;bestTitle=titleOverlap;found=d;}
        }
        JSONObject out=new JSONObject();out.put("engine","Local sample pack");
        if(found==null){previous=null;out.put("answer",lang.equals("hi")?"यह प्रश्न नमूना पाठ में नहीं मिला। अपने शिक्षक से पूछो।":"I cannot find that question in this sample pack. Try a lesson or ask your teacher.");return out;}
        previous=found.getString("id");JSONArray hints=found.getJSONObject("hints").getJSONArray(lang);String text;
        if(mode.equals("hint"))text=hints.getString(0);
        else if(mode.equals("example"))text=found.getJSONObject("example").getString(lang);
        else if(mode.equals("steps")){StringBuilder b=new StringBuilder();for(int i=0;i<hints.length();i++)b.append(i+1).append(". ").append(hints.getString(i)).append("\n");text=b.toString();}
        else text=found.getJSONObject("answer").getString(lang)+"\n\n"+found.getJSONObject("example").getString(lang);
        return out.put("answer",text).put("lesson_id",previous).put("source",new JSONObject().put("title",found.getString("title")));
    }
}
