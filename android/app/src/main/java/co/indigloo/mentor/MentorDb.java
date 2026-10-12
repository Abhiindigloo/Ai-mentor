package co.indigloo.mentor;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {Practice.class}, version = 1, exportSchema = false)
public abstract class MentorDb extends RoomDatabase { public abstract PracticeDao practice(); }
