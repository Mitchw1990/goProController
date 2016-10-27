import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

/**
 * Created by Mitch on 8/3/2016.
 */
public class GoProUI extends Application {

    public final String goProFolderPath = System.getProperty("user.home") +
            File.separatorChar + "Pictures" +
            File.separatorChar + "gopro" +
            File.separatorChar;

    private final String PHOTO_CAPTURE_URL = "http://10.5.5.9:8080/gp/gpControl/command/shutter?p=";

    GridPane grid = new GridPane();
    ImageView mainImg = new ImageView();
    ImageView img1 = new ImageView();
    ImageView img2 = new ImageView();
    ImageView img3 = new ImageView();
    ImageView img4 = new ImageView();

    Label statusLabel = new Label();
    Label addresLabel = new Label();
    Label fileCountLabel = new Label();

    BorderPane largePhotoPane = new BorderPane(mainImg);
    FlowPane multiPhotoPane = new FlowPane();
    ImageView[] imageViews = {img1, img2, img3, img4};
    FlowPane buttonBox = new FlowPane();
    ArrayList<Property> propertyList = new ArrayList<>();
    HashSet<String> downloadedFiles = new HashSet<>();

    Button triggerButton = new Button("Capture");
    Button nextButton =  new Button("Next");
    Button previousButton = new Button("Previous");

    ExecutorService downloaderTaskExecutor;
    ImageLoaderTask imageLoader;
    GoProDL downloader;

    private int currentIndex = 0;
    private BooleanProperty activeCapture = new SimpleBooleanProperty(false);


    public boolean isActiveCapture() {
        return activeCapture.get();
    }

    public BooleanProperty activeCaptureProperty() {
        return activeCapture;
    }

    public void setActiveCapture(boolean activeCapture) {
        this.activeCapture.set(activeCapture);
    }

    public void start(Stage stage){

        try {
            propertyList = importSpreadsheet();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadExistingFilePaths();
        bindComponentsSizing();

        ColumnConstraints col0 = new ColumnConstraints();
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();
        ColumnConstraints col5 = new ColumnConstraints();
        ColumnConstraints col6 = new ColumnConstraints();

        col0.setPercentWidth(1);
        col1.setPercentWidth(47);
        col2.setPercentWidth(1);
        col3.setPercentWidth(50/3.0);
        col4.setPercentWidth(50/3.0);
        col5.setPercentWidth(50/3.0);
        col6.setPercentWidth(1);

        RowConstraints headerRow = new RowConstraints();
        RowConstraints centerRow = new RowConstraints();
        RowConstraints buttonRow = new RowConstraints();

        headerRow.setPercentHeight(20);
        centerRow.setPercentHeight(67);
        buttonRow.setPercentHeight(13);

        grid.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5, col6);
        grid.getRowConstraints().addAll(headerRow, centerRow, buttonRow);

        multiPhotoPane.setOrientation(Orientation.HORIZONTAL);
        multiPhotoPane.getChildren().addAll(img1, img2, img3, img4);
        multiPhotoPane.getStyleClass().add("border-pane");
        multiPhotoPane.setAlignment(Pos.CENTER);
        grid.add(multiPhotoPane, 3, 1, 3, 1);
        grid.add(largePhotoPane, 1, 1, 1, 1);

        triggerButton.setOnAction(e -> triggerCapture());

        nextButton.setOnAction(e -> moveNext());

        previousButton.setOnAction(e -> movePrevious());

        buttonBox.getChildren().addAll(previousButton, triggerButton, nextButton);

        nextButton.prefWidthProperty().bind(buttonBox.widthProperty().divide(3));
        triggerButton.prefWidthProperty().bind(buttonBox.widthProperty().divide(3));
        previousButton.prefWidthProperty().bind(buttonBox.widthProperty().divide(3));

        Label status = new Label("Status:");
        Label files = new Label("Files:");
        VBox labelBox1 = new VBox(status, files);
        VBox labelBox2 = new VBox(statusLabel, fileCountLabel);

        status.prefHeightProperty().bind(labelBox1.heightProperty().divide(2));
        status.prefWidthProperty().bind(labelBox1.widthProperty());
        files.prefHeightProperty().bind(labelBox1.heightProperty().divide(2));
        files.prefWidthProperty().bind(labelBox1.widthProperty());

        statusLabel.prefHeightProperty().bind(labelBox2.heightProperty().divide(2));
        statusLabel.prefWidthProperty().bind(labelBox2.widthProperty());
        statusLabel.getStyleClass().add("label-custom");
        fileCountLabel.prefHeightProperty().bind(labelBox2.heightProperty().divide(2));
        fileCountLabel.prefWidthProperty().bind(labelBox2.widthProperty());
        fileCountLabel.getStyleClass().add("label-custom");

        HBox addressBox = new HBox(addresLabel);
        addresLabel.prefWidthProperty().bind(addressBox.widthProperty());
        addresLabel.prefHeightProperty().bind(addressBox.heightProperty());

        grid.add(labelBox1, 3, 0);
        grid.add(labelBox2, 4, 0, 2, 1);
        grid.add(addressBox, 1, 0);

        statusLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals("TIMEOUT"))
                statusLabel.styleProperty().setValue("-fx-text-fill: red;");
            else
                statusLabel.styleProperty().setValue("-fx-text-fill: black;");
        });

        grid.add(buttonBox, 3, 2, 3, 1);

        Scene scene = new Scene(grid);
        scene.getStylesheets().add("style.css");


        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if(code == KeyCode.valueOf("SPACE")){
                triggerCapture();
                event.consume();
            }
            else if(code == KeyCode.LEFT){
                movePrevious();
                event.consume();
            }
            else if(code == KeyCode.RIGHT){
                moveNext();
                event.consume();
            }
        });

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(visualBounds.getWidth());
        stage.setHeight(visualBounds.getHeight());
        stage.setX(visualBounds.getMaxX());
        stage.setY(visualBounds.getMaxY());

        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();

        String currentPathName = goProFolderPath + File.separatorChar +
                propertyList.get(currentIndex).getPropertyId();

        createDirIfNotExists(currentPathName);
        addresLabel.textProperty().bind(propertyList.get(currentIndex).propertyAddressProperty());

        imageLoader = new ImageLoaderTask(currentPathName);
        bindComponentsToImageLoader(imageLoader);
        Thread imageLoaderThread = new Thread(imageLoader);
        imageLoaderThread.start();

        downloader = new GoProDL(goProFolderPath, downloadedFiles);
        downloader.setCurrentDirName(propertyList.get(currentIndex).getPropertyId());
        statusLabel.textProperty().bind(downloader.connectionStatusProperty());
        Thread downloaderThread = new Thread(downloader);
        downloaderThread.start();
    }

    private void launchNewImageLoaderTask(){
        String currentPathName = goProFolderPath + File.separatorChar +
                propertyList.get(currentIndex).getPropertyId();
        imageLoader.cancel();
        imageLoader = new ImageLoaderTask(currentPathName);
        bindComponentsToImageLoader(imageLoader);
        Thread imageLoaderThread = new Thread(imageLoader);
        imageLoaderThread.start();
    }

//    private void launchNewDownloadTask() {
//        String currentPathName = goProFolderPath + File.separatorChar +
//                propertyList.get(currentIndex).getPropertyId();
//        downloader.cancel();
//        downloader = new GoProDL(currentPathName, downloadedFiles);
//        Thread downloaderThread = new Thread(downloader);
//        downloaderThread.start();
////        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
////        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
//    }
    private void bindComponentsSizing() {
        for (ImageView i : imageViews) {
            i.setPreserveRatio(true);
            i.fitWidthProperty().bind(multiPhotoPane.widthProperty().divide(2).subtract(5));
        }
        mainImg.setPreserveRatio(true);
        mainImg.fitWidthProperty().bind(largePhotoPane.widthProperty());
    }

    private void bindComponentsToImageLoader(ImageLoaderTask loaderTask){
        Platform.runLater(() -> {
            mainImg.imageProperty().unbind();
            img1.imageProperty().unbind();
            img2.imageProperty().unbind();
            img3.imageProperty().unbind();
            img4.imageProperty().unbind();
            fileCountLabel.textProperty().unbind();
            fileCountLabel.textProperty().bind(loaderTask.totalFilesProperty().asString());
            mainImg.imageProperty().bind(loaderTask.mainImageProperty());
            img1.imageProperty().bind(loaderTask.img1Property());
            img2.imageProperty().bind(loaderTask.img2Property());
            img3.imageProperty().bind(loaderTask.img3Property());
            img4.imageProperty().bind(loaderTask.img4Property());
        });
    }

    private void loadExistingFilePaths(){
        createDirIfNotExists(goProFolderPath);

        File directory = new File(goProFolderPath);
        File[] dirList = directory.listFiles();

        for(File dir : dirList) {
            if (dir.isDirectory()) {
                File subDir = new File(dir.toString());
                File[] fileList = subDir.listFiles();
                for (File file : fileList) {
                    if (file.isFile()) {
                        String name = file.toString().split("\\\\")[6];
                        downloadedFiles.add(name);
                    }
                }
            }
        }
    }

    private void moveNext(){
        if(currentIndex < propertyList.size() - 1){
            currentIndex++;
            performInspectionChangeTasks();
        }
    }

    private void movePrevious(){
        if(currentIndex > 0){
            currentIndex--;
            performInspectionChangeTasks();
        }
    }

    private void performInspectionChangeTasks(){
        String currentFolderPath = goProFolderPath + File.separatorChar +
                propertyList.get(currentIndex).getPropertyId();
        addresLabel.textProperty().unbind();
        addresLabel.textProperty().bind(propertyList.get(currentIndex).propertyAddressProperty());
        createDirIfNotExists(currentFolderPath);
        downloader.setCurrentDirName(propertyList.get(currentIndex).getPropertyId());
        launchNewImageLoaderTask();
    }

    private void createDirIfNotExists(String path){
        File dir = new File(path);

        if (!dir.exists()) {
            if (!(dir.mkdirs())) {
                throw new RuntimeException("Unable to ensure folder exists:" + path);
            }
            System.out.println("Created download folder:" + path);
        }
    }
    public  ArrayList<Property> importSpreadsheet() throws IOException {
        ArrayList<Property> propertyList = new ArrayList<>();
        String spreadsheetPath = System.getProperty("user.dir") + File.separatorChar + "gpControlSheet.xlsx";
        FileInputStream inputStream = new FileInputStream(new File(spreadsheetPath));

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet spreadsheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = spreadsheet.iterator();
        rowIterator.next();
        XSSFRow currentRow = null;

        while (rowIterator.hasNext()){
            currentRow = (XSSFRow)rowIterator.next();

            String propertyID = Integer.toString((int) Double.parseDouble(currentRow.getCell(0).toString()));
            String address = currentRow.getCell(1).toString();
            propertyList.add(new Property(propertyID, address));
        }
        inputStream.close();

        System.out.println("Imported address list successfully:\n");
        for(Property p : propertyList){
            System.out.println(p);
        }

        return propertyList;
    }

    private void triggerCapture(){

        activeCaptureProperty().setValue(!isActiveCapture());
        downloader.setSuspended(false);
        downloader.resetTimer();
        Task<String> fire = new Task<String>() {
            @Override
            protected String call() throws Exception {

                String url = null;

                if (isActiveCapture()) {
                    url = PHOTO_CAPTURE_URL +"1";
                    bindComponentsToImageLoader(imageLoader);
                    triggerButton.styleProperty().setValue("-fx-background-color: red;");
                }else{
                   url = PHOTO_CAPTURE_URL + "0";
                    triggerButton.styleProperty().setValue("-fx-background-color: #b0b0b0");
                }
                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet request = new HttpGet(url);
                    httpclient.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        Thread capture = new Thread(fire);
        capture.start();
    }

    public static void main(String[] args) {Application.launch(args);}

}
