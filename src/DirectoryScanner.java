import org.apache.commons.lang3.time.StopWatch;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Mitch on 7/15/2016.
 */
public class DirectoryScanner extends Thread {

    private final int MAX_SECONDS_TO_POLL_DIR = 25;
    private ArrayList<JLabel> labelList;
    private volatile JLabel counterLabel;
    private int totalFiles;
    private volatile Boolean suspended;
    private volatile String currentDirectory;
    private volatile boolean loop;
    private StopWatch timer;

    public DirectoryScanner(ArrayList<JLabel> labelList, JLabel counterLabel){
        this.labelList = labelList;
        this.counterLabel = counterLabel;
        totalFiles = 0;
        suspended = false;
        currentDirectory = null;
        loop = true;
        timer = new StopWatch();
    }

    public void run() {
        while (loop) {
            if (currentDirectory != null) {
                if (timer.getNanoTime() == 0) {
                    timer.start();
                    System.out.println("Timer started.");
                }
                File directory = new File(currentDirectory);
                File[] fileList = directory.listFiles();

                if(fileList == null || fileList.length == 0) {
                    counterLabel.setText("Files: 0");
                }

                if (fileList != null) {
                    if (totalFiles != fileList.length) {
                        System.out.println(currentDirectory);
                        totalFiles = fileList.length;
                        if (totalFiles == 0) {
                           clearLabels();
                            counterLabel.setText("Files: " + fileList.length);
                        }else{
                            timer.reset();
                            timer.start();

                            System.out.println(timer.getTime());

                            System.out.println("Updating files.");

                            int index = 0;
                            for (int i = fileList.length - 1; i > fileList.length - 6; i--) {
                                if (i < 0 || suspended) {
                                    break;
                                }
                                JLabel label = labelList.get(index);

                                if (!(fileList[i].toString().endsWith("JPG") || fileList[i].toString().endsWith("jpg"))) {
                                    System.out.println("Non-jpeg file found in directory" + currentDirectory + ".");
                                    break;
                                }
                                BufferedImage image = null;

                                System.out.println("Adding GUI image: " + fileList[i].toString());

                                try {
                                    image = ImageIO.read(fileList[i]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (image != null) {

                                    Image scaledImage = null;

                                    if(index == 0){
                                        scaledImage = image.getScaledInstance(1080, 810, Image.SCALE_SMOOTH);
                                    }else
                                    {
                                        scaledImage = image.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
                                    }

                                    ImageIcon icon = new ImageIcon(scaledImage);

                                    if(!suspended) {
                                        label.setIcon(icon);
                                        label.revalidate();
                                        label.repaint();
                                            index++;
                                            counterLabel.setText("Files: " + fileList.length);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (timer.getTime() >= MAX_SECONDS_TO_POLL_DIR * 1000) {
                suspended = true;
                timer.reset();
                System.out.println("Scanner paused.");
            }

            if (suspended) {
                totalFiles = 0;
            }
            while (suspended) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Scanner thread killed.");
    }

    public void setCurrentDirectory(String currentDirectory){
        this.currentDirectory = currentDirectory;
    }

    public int getTotalFiles(){
        return totalFiles;
    }

    public void suspendScanner(){
        suspended = true;
    }

    public void resumeScanner(){
        suspended = false;
    }

    public void clearLabels(){
        for (JLabel label : labelList) {
            label.setIcon(null);
            label.repaint();
        }
    }


    public void kill(){
        loop = false;
    }
}
