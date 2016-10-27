import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashSet;

public class GoProDL extends Task {
    private final HashSet<String> downloadedFiles;
    private final String DCIM_URL = "http://10.5.5.9:8080/videos/DCIM/";
    private transient StringProperty currentDirName = new SimpleStringProperty(null);
    private volatile StopWatch timer;
    private final int MAX_SECONDS_TO_POLL_GOPRO = 20;
    private StringProperty connectionStatus = new SimpleStringProperty("Connecting...");
    private transient BooleanProperty suspended = new SimpleBooleanProperty(false);
    private transient BooleanProperty alive = new SimpleBooleanProperty(true);
    private final String goProFolderPath;


    public GoProDL(String goProFolderPath, HashSet<String> downloadedFiles){
        this.goProFolderPath = goProFolderPath;
        this.downloadedFiles = downloadedFiles;
        timer = new StopWatch();
    }

    public void resetTimer(){
        timer.reset();
        timer.start();
    }


    public String call() {
        System.out.println("New downloader task started.");
        while(isAlive()){
            if(timer.getNanoTime() == 0) {
                Platform.runLater(() -> connectionStatusProperty().setValue("Connecting..."));
                timer.start();
            }
            while (!isSuspended()) {
                if (timer.getTime() >= (MAX_SECONDS_TO_POLL_GOPRO * 1000)) {
                    setSuspended(true);
                    break;
                }

                String currentFolderPath = goProFolderPath + getCurrentDirName() + File.separatorChar;
                Document currentPage = null;

                try {
                    currentPage = getCurrentDirPage();
                } catch (final IOException ex) {
                    System.err.println("Error\t" + ex.getMessage() + " on " + currentFolderPath);
                    Platform.runLater(() -> connectionStatusProperty().setValue("TIMEOUT"));
                    System.out.println("Timeout");
                    break;
                }

                if (currentPage != null) {
                    Platform.runLater(() -> connectionStatusProperty().setValue("CONNECTED"));
                }

                for (final Element pictureLink : currentPage.select("a")) {
                    if (isSuspended())
                        break;
                    final String linkFileName = pictureLink.attr("href");
                    if (linkFileName.endsWith("JPG")) {
                        final File destinationFile = new File(currentFolderPath + linkFileName);
                        if (isSuspended())
                            break;
                        if (!((destinationFile.exists() && destinationFile.length() > 1_024) || (downloadedFiles.contains(linkFileName)))) {
                            System.out.println("Starting download of\t" + destinationFile);
                            try (final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile), 1024 * 1024 * 5)) {
                                Connection.Response resultImageResponse = Jsoup.connect(pictureLink.absUrl("href")).maxBodySize(0).execute();
                                outputStream.write(resultImageResponse.bodyAsBytes());
                                outputStream.flush();
                                outputStream.close();
                                downloadedFiles.add(linkFileName);
                                timer.reset();
                                timer.start();
                            } catch (Exception e) {
                                Platform.runLater(() -> connectionStatusProperty().setValue("TIMEOUT"));
                                break;
                            }
                            System.out.println("Downloaded\t" + linkFileName);
                        }
                    }
                }
            }

            if(isSuspended()) {
                Platform.runLater(() -> connectionStatusProperty().setValue("SUSPENDED"));
                timer.reset();
            }

            while(isSuspended()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        Platform.runLater(() -> connectionStatusProperty().setValue("STOPPED"));
        System.out.println("Downloader task ended.");
        return "done";
    }

    private Document getCurrentDirPage() throws IOException {
        if(!isSuspended()) {
            Document page = Jsoup.connect(DCIM_URL)
                    .timeout(15 * 1_000)
                    .maxBodySize(0)
                    .get();
            Elements directoryLinkList = page.select("a");
            for (Element e : directoryLinkList) {
                String element = e.attr("href");
                if (!element.contains("GOPRO")) {
                    directoryLinkList.remove(element);
                }
            }
            Element currentDirectoryLink = directoryLinkList.get(directoryLinkList.size() - 1);
            page = Jsoup.connect(DCIM_URL + currentDirectoryLink.attr("href"))
                    .timeout(10 * 1_000)
                    .maxBodySize(0)
                    .get();
            return page;
        }
            return null;
    }

    public String getConnectionStatus() {
        return connectionStatus.get();
    }

    public StringProperty connectionStatusProperty() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus.set(connectionStatus);
    }

    public boolean isSuspended() {
        return suspended.get();
    }

    public BooleanProperty suspendedProperty() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended.set(suspended);
    }

    public boolean isAlive() {
        return alive.get();
    }

    public BooleanProperty aliveProperty() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive.set(alive);
    }

    public void kill(){
        setAlive(false);
    }

    public String getCurrentDirName() {
        return currentDirName.get();
    }

    public StringProperty currentDirNameProperty() {
        return currentDirName;
    }

    public void setCurrentDirName(String currentDirName) {
        this.currentDirName.set(currentDirName);
    }
}