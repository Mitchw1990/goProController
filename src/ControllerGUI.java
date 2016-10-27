import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Mitch on 7/12/2016.
 */
public class ControllerGUI {

    JFrame mainFrame;
    JLabel addressLabel;
    JLabel fileCountLabel;
    JButton nextButton;
    JButton prevButton;
    JButton captureButton;
    JButton markDupButton;
    JButton markNotDupButton;
    ArrayList<JLabel>  labelList;
    JPanel buttonPanel;
    

    public void buildGUI() {

        JLabel imageLabel1 = new JLabel();
        JLabel imageLabel2 = new JLabel();
        JLabel imageLabel3 = new JLabel();
        JLabel imageLabel4 = new JLabel();
        JLabel imageLabel5 = new JLabel();

        labelList = new ArrayList<>();

        labelList.add(imageLabel1);
        labelList.add(imageLabel2);
        labelList.add(imageLabel3);
        labelList.add(imageLabel4);
        labelList.add(imageLabel5);

//        for(JLabel l : labelList){
//            l.setPreferredSize(new Dimension(640,480));
//        }

        JPanel pictureGrid = new JPanel(new GridLayout(2,2));
        JPanel largePicture = new JPanel(new GridLayout(1,1));
        JPanel pictureContainer = new JPanel(new GridLayout(1,2));
        largePicture.setBackground(Color.black);
        largePicture.setOpaque(true);
        pictureGrid.setBackground(Color.black);
        pictureContainer.setOpaque(true);
        pictureContainer.add(largePicture);
        pictureContainer.add(pictureGrid);

        largePicture.add(imageLabel1);
        pictureGrid.add(imageLabel2);
        pictureGrid.add(imageLabel3);
        pictureGrid.add(imageLabel4);
        pictureGrid.add(imageLabel5);

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getScreenDevices()[0];
        mainFrame = new JFrame("GoPro Photo Downloader");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setUndecorated(false);

        addressLabel = new JLabel();
        fileCountLabel = new JLabel();
        setLabelProperties(fileCountLabel, addressLabel);
        addressLabel.setFont(addressLabel.getFont().deriveFont(70.0f));
        fileCountLabel.setFont(fileCountLabel.getFont().deriveFont(70.0f));

        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        captureButton = new JButton("Trigger");
        markDupButton = new JButton("Duplicate");
        markNotDupButton = new JButton("Not Duplicate");

        JPanel dupButtonPanel = new JPanel(new GridLayout(2,1));
        dupButtonPanel.add(markDupButton);
        dupButtonPanel.add(markNotDupButton);

        buttonPanel = new JPanel(new GridLayout(1,4));
        buttonPanel.setPreferredSize(new Dimension(mainFrame.getWidth(),50));
        buttonPanel.add(prevButton);
        buttonPanel.add(captureButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(dupButtonPanel);

        JPanel headerPanel = new JPanel(new GridLayout(1,2));
        headerPanel.add(addressLabel);
        headerPanel.add(fileCountLabel);

        mainFrame.getContentPane().add(BorderLayout.NORTH, headerPanel);
        mainFrame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);
        mainFrame.getContentPane().add(BorderLayout.CENTER, pictureContainer);

        mainFrame.pack();
        device.setFullScreenWindow(mainFrame);
        mainFrame.setVisible(true);
    }

    public JPanel getButtonPanel(){
        return buttonPanel;
    }


    public void setAddressLabel(String text){
        addressLabel.setText(text);
    }

    public void setFileCountLabel(String text){
        fileCountLabel.setText(text);
    }

    public JLabel getFileCountLabel(){
        return fileCountLabel;
    }

    public JButton getNextButton(){
        return nextButton;
    }

    public JButton getPrevButton(){
        return prevButton;
    }

    public JButton getCaptureButton(){ return captureButton;}


    public static void setLabelProperties(JLabel... label){
        for(JLabel jlabel : label) {
            jlabel.setHorizontalAlignment(JLabel.CENTER);
            jlabel.setVerticalAlignment(JLabel.NORTH);
            jlabel.setOpaque(true);
            jlabel.setBackground(Color.black);
            jlabel.setForeground(Color.white);
        }
    }

    public ArrayList<JLabel> getLabelList(){
        return labelList;
    }

    public void setLabelWhite(){
        addressLabel.setForeground(Color.white);
    }

    public void setLabelRed(){
        addressLabel.setForeground(Color.red);
    }

    public JButton getMarkDupButton(){
        return markDupButton;
    }

    public JButton getMarkNotDupButton(){
        return markNotDupButton;
    }

    public static void main(String[] args) {
        new ControllerGUI().buildGUI();
    }

}
