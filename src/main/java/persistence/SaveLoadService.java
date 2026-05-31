package persistence;

import java.io.File;
import java.io.IOException;

import engine.GameStateImpl;

public interface SaveLoadService {

    /**
     * Nyimpan state permainan ke file
     * parameter "state" artinya state permainan yang bakal disimpen"
     * parameter "file" artinya file tujuan penyimpanan
     * bisa lempar IOException kalau gagal nulis file
     */
    void save(GameStateImpl state, File file) throws IOException;


    /**
     * Load state permainan dari file
     * parameter "file" artinya file save yang akan dimuat
     * return SaveData yang berisi semua data tersimpan
     * bisa lempar IOException kalau gagal baca file atau SaveCorruptedException kalau format file ga valid
    */
    SaveData load(File file) throws IOException, SaveCorruptedException;

    boolean isValidSave(File file);
    
    String getFileExtension();
}
