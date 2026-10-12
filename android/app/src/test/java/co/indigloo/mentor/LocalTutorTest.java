package co.indigloo.mentor;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.*;
public class LocalTutorTest {
    private LocalTutor tutor() throws Exception {return new LocalTutor(new String(Files.readAllBytes(Paths.get("src/main/assets/lessons.json")),"UTF-8"));}
    @Test public void unrelatedDoesNotInherit() throws Exception {LocalTutor t=tutor();assertEquals("green-leaves",t.reply("Why are leaves green?","nova","en","answer").getString("lesson_id"));assertEquals("green-leaves",t.reply("explain again","nova","en","example").getString("lesson_id"));assertFalse(t.reply("Who won the 2011 cricket world cup?","nova","en","answer").has("lesson_id"));}
    @Test public void hindiAndBand() throws Exception {LocalTutor t=tutor();assertTrue(t.reply("क्लोरोफिल","nova","hi","answer").has("lesson_id"));assertFalse(t.reply("quadratic discriminant","koko","en","answer").has("lesson_id"));}
}
