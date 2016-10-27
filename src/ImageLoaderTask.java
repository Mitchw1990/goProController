import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by Mitch on 8/3/2016.
 */


public class ImageLoaderTask extends Task<ObservableList<ObjectProperty<Image>>> {
    private final ObservableList<ObjectProperty<Image>> imageList = FXCollections.observableArrayList();
    private final ObjectProperty<Image> mainImage = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> img1 = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> img2 = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> img3 = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> img4 = new SimpleObjectProperty<>(null);

    private IntegerProperty totalFiles = new SimpleIntegerProperty(0);


    File directory;
    File[] files;

    public ImageLoaderTask(String dirPath){
        directory  = new File(dirPath);
        files = null;
        Path path = directory.toPath();
        imageList.add(mainImage);
        imageList.add(img1);
        imageList.add(img2);
        imageList.add(img3);
        imageList.add(img4);
//
    }

    public int getTotalFiles() {
        return totalFiles.get();
    }

    public IntegerProperty totalFilesProperty() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles.set(totalFiles);
    }

    public Image getMainImage() {
        return mainImage.get();
    }

    public ObjectProperty<Image> mainImageProperty() {
        return mainImage;
    }

    public void setMainImage(Image mainImage) {
        this.mainImage.set(mainImage);
    }

    public Image getImg1() {
        return img1.get();
    }

    public ObjectProperty<Image> img1Property() {
        return img1;
    }

    public void setImg1(Image img1) {
        this.img1.set(img1);
    }

    public Image getImg2() {
        return img2.get();
    }

    public ObjectProperty<Image> img2Property() {
        return img2;
    }

    public void setImg2(Image img2) {
        this.img2.set(img2);
    }

    public Image getImg3() {
        return img3.get();
    }

    public ObjectProperty<Image> img3Property() {
        return img3;
    }

    public void setImg3(Image img3) {
        this.img3.set(img3);
    }

    public Image getImg4() {
        return img4.get();
    }

    public ObjectProperty<Image> img4Property() {
        return img4;
    }

    public void setImg4(Image img4) {
        this.img4.set(img4);
    }

    @Override
    protected ObservableList<ObjectProperty<Image>> call() {
        System.out.println("Scanner started.");
        while(true) {
            if (isCancelled()) {
                break;
            }
            updateTotalFiles();
            loadImages();
        }
        System.out.println("Scanner stopped.");
        return null;
    }

        public void loadImages(){
        files = directory.listFiles();
        updateTotalFiles();
        Iterator<ObjectProperty<Image>> iterator = imageList.iterator();
        int index = files.length - 1;
        while (iterator.hasNext()) {
            if(isCancelled()){
                break;
            }
            if (index < 0)
                break;
            ObjectProperty image = iterator.next();
            String newImagePath = files[index].toURI().toString();
            image.setValue(new Image(newImagePath));
            index--;
        }
    }

    private void updateTotalFiles(){
        if(files != null)
            Platform.runLater(() -> totalFilesProperty().set(files.length));
    }


}

//            try {
//                watchKey = scanner.take();
//            } catch (InterruptedException e) {
//                System.out.println("Scanner interrupted.");
//            }
//
//            for (WatchEvent<?> event : watchKey.pollEvents()) {
//                WatchEvent.Kind<?> kind = event.kind();
//
//                System.out.println("Change detected");
//
//                if (kind == OVERFLOW) {
//                    continue;
//                }

//            }
//            if (!watchKey.reset()) {
//                break;
//            }
//        }

//                    try {
//            scanner = FileSystems.getDefault().newWatchService();
//            watchKey = path.register(scanner, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }




