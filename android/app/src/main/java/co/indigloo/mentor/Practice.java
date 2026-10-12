package co.indigloo.mentor;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity public class Practice {
    @PrimaryKey(autoGenerate = true) public long id;
    public String question;
    public String answer;
    public String lesson;
    public long created;
}
