import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by Mitch on 8/3/2016.
 */
public class DirectoryScannerTask extends Task<String> {

    WatchService scanner;
    WatchKey watchKey;
    Path path;

    public DirectoryScannerTask(String path) {
        this.path = new File(path).toPath();
        try {
            watchKey = this.path.register(scanner, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected String call() {
        return "";
    }

}

