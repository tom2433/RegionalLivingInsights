package projectworkspace;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This class holds the main JFrame of the application, and it contains a JPanel with a CardLayout
 * containing all Screens (classes extending JPanels). The main purpose of this class is to manage
 * the different screens (when to show what screen) , hold the main JFrame, and store an instance
 * of the DataReader class which retrieves data from the database to display in the DataVisScreens.
 */
public class App implements ActionListener {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JFrame frame;
    private final DataReader dataReader;

    /**
     * Constructs an instance of App. Only one instance of App will exist throughout the entire
     * execution of the program.
     */
    public App() {
        dataReader = new DataReader();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
    }

    /**
     * Creates all screens and adds them to the CardLayout of the mainPanel, and adds mainPanel to
     * the frame (window). Also makes the window visible.
     */
    private void windowManager() {
        // instantiate JFrame
        frame = new JFrame("Regional Living Insights");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add all screens to mainPanel
        mainPanel.add(new MainMenu(this), "MainMenu");
        mainPanel.add(new RegionScreen(this), "RegionScreen");
        mainPanel.add(new RegionDataVisScreen(this), "RegionDataVisScreen");
        mainPanel.add(new StateScreen(this), "StateScreen");
        mainPanel.add(new StateDataVisScreen(this), "StateDataVisScreen");
        mainPanel.add(new CustomDataScreen(this), "CustomDataScreen");
        mainPanel.add(new CustomDataVisScreen(this), "CustomDataVisScreen");

        // add mainPanel to frame and set visible
        frame.add(mainPanel);
        frame.setSize(750, 750);
        frame.setVisible(true);
    }

    /**
     * Catches an ActionEvent when one occurs (a button is pressed). The only buttons with action
     * listeners added to this class are buttons associated with changing screens and returning to
     * the main menu. This method handles all of those operations.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        // call method based on ActionEvent command (go to RegionScreen, StateScreen,
        // CustomDataScreen, and MainMenu)
        switch (action) {
            case "Compare Regions" -> refreshRegionScreen();
            case "Compare States" -> refreshStateScreen();
            case "Compare Custom Datasets with Regions and/or States" -> refreshCustomDataScreen();
            case "Back to Main Menu" -> refreshMainMenu();
        }
    }

    /**
     * Removes a screen from the mainPanel CardLayout.
     *
     * @param constraintName a string containing the constraintName of the specific screen to
     *                       remove
     */
    private void removeScreen(String constraintName) {
        for (Component comp : mainPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(constraintName)) {
                mainPanel.remove(comp);
                // refresh layout
                mainPanel.revalidate();
                // repaint all components
                mainPanel.repaint();
                break;
            }
        }
    }

    /**
     * Refreshes the RegionScreen by removing it from the mainPanel CardLayout, re-creating it,
     * adding it back to the CardLayout, and displaying it.
     */
    public void refreshRegionScreen() {
        removeScreen("RegionScreen");
        mainPanel.add(new RegionScreen(this), "RegionScreen");
        cardLayout.show(mainPanel, "RegionScreen");
    }

    /**
     * Refreshes the StateScreen by removing it from the mainPanel CardLayout, re-creating it,
     * adding it back to the CardLayout, and displaying it.
     */
    public void refreshStateScreen() {
        removeScreen("StateScreen");
        mainPanel.add(new StateScreen(this), "StateScreen");
        cardLayout.show(mainPanel, "StateScreen");
    }

    /**
     * Refreshes the CustomDataScreen by removing it from the mainPanel CardLayout, re-creating it,
     * adding it back to the CardLayout, and displaying it.
     */
    public void refreshCustomDataScreen() {
        removeScreen("CustomDataScreen");
        mainPanel.add(new CustomDataScreen(this), "CustomDataScreen");
        cardLayout.show(mainPanel, "CustomDataScreen");
    }

    /**
     * Refreshes the MainMenu by removing it from the mainPanel CardLayout, re-creating it,
     * adding it back to the CardLayout, and displaying it.
     */
    private void refreshMainMenu() {
        removeScreen("MainMenu");
        mainPanel.add(new MainMenu(this), "MainMenu");
        cardLayout.show(mainPanel, "MainMenu");
    }

    /**
     * Refreshes the RegionDataVisScreen by removing it from the mainPanel CardLayout, re-creating
     * it, adding it back to the CardLayout, and displaying it.
     */
    public void refreshRegionDataVisScreen(String state1, String state2, String region1, String region2) {
        removeScreen("RegionDataVisScreen");
        mainPanel.add(new RegionDataVisScreen(this, state1, state2, region1, region2), "RegionDataVisScreen");
        cardLayout.show(mainPanel, "RegionDataVisScreen");
    }

    /**
     * Refreshes the StateDataVisScreen by removing it from the mainPanel CardLayout, re-creating
     * it, adding it back to the CardLayout, and displaying it.
     */
    public void refreshStateDataVisScreen(String state1, String state2) {
        removeScreen("StateDataVisScreen");
        mainPanel.add(new StateDataVisScreen(this, state1, state2), "StateDataVisScreen");
        cardLayout.show(mainPanel, "StateDataVisScreen");
    }

    /**
     * Refreshes the CustomDataVisScreen by removing it from the mainPanel CardLayout, re-creating it,
     * adding it back to the CardLayout, and displaying it.
     */
    public void refreshCustomDataVisScreen(ArrayList<ArrayList<String>> datasets) {
        removeScreen("CustomDataVisScreen");
        mainPanel.add(new CustomDataVisScreen(this, datasets), "CustomDataVisScreen");
        cardLayout.show(mainPanel, "CustomDataVisScreen");
    }

    /**
     * Retrieves the JFrame of the application
     *
     * @return a JFrame containing the main JFrame of the application
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Retrieves the DataReader object of the application
     *
     * @return a DataReader object containing the DataReader for the application
     */
    public DataReader getDataReader() {
        return dataReader;
    }

    /**
     * Main method.
     *
     * @param args String array containing compiler arguments
     */
    public static void main(String[] args) {
        App app = new App();
        app.windowManager();
    }
}
