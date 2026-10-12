package co.indigloo.mentor;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
@Dao public interface PracticeDao {
    @Insert void insert(Practice p);
    @Query("SELECT * FROM Practice ORDER BY id DESC LIMIT 50") List<Practice> recent();
    @Query("DELETE FROM Practice") void erase();
    @Query("DELETE FROM Practice WHERE id NOT IN (SELECT id FROM Practice ORDER BY id DESC LIMIT 500)") void trim();
}
