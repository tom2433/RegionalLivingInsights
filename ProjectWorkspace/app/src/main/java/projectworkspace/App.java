package projectworkspace;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
Buttons in main screen:
    - compare regions
    - compare states
    - compare custom datasets with regions and/or states
 */
public class App implements ActionListener {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JFrame frame;
    private final DataReader dataReader;

    public App() {
        dataReader = new DataReader();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
    }

    private void windowManager() {
        frame = new JFrame("Regional Living Insights");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel.add(new MainMenu(this), "MainMenu");
        mainPanel.add(new RegionScreen(this), "RegionScreen");
        mainPanel.add(new RegionDataVisScreen(this), "RegionDataVisScreen");
        mainPanel.add(new StateScreen(this), "StateScreen");
        mainPanel.add(new StateDataVisScreen(this), "StateDataVisScreen");
        mainPanel.add(new CustomDataScreen(this), "CustomDataScreen");
        mainPanel.add(new CustomDataVisScreen(this), "CustomDataVisScreen");

        frame.add(mainPanel);
        frame.setSize(750, 750);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        System.out.println("Button clicked: " + action);

        switch (action) {
            case "Compare Regions" -> refreshRegionScreen();
            case "Compare States" -> refreshStateScreen();
            case "Compare Custom Datasets with Regions and/or States" -> refreshCustomDataScreen();
            case "Back to Main Menu" -> refreshMainMenu();
        }
    }

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

    public void refreshRegionScreen() {
        removeScreen("RegionScreen");
        mainPanel.add(new RegionScreen(this), "RegionScreen");
        cardLayout.show(mainPanel, "RegionScreen");
    }

    public void refreshStateScreen() {
        removeScreen("StateScreen");
        mainPanel.add(new StateScreen(this), "StateScreen");
        cardLayout.show(mainPanel, "StateScreen");
    }

    public void refreshCustomDataScreen() {
        removeScreen("CustomDataScreen");
        mainPanel.add(new CustomDataScreen(this), "CustomDataScreen");
        cardLayout.show(mainPanel, "CustomDataScreen");
    }

    private void refreshMainMenu() {
        removeScreen("MainMenu");
        mainPanel.add(new MainMenu(this), "MainMenu");
        cardLayout.show(mainPanel, "MainMenu");
    }

    public void refreshRegionDataVisScreen(String state1, String state2, String region1, String region2) {
        removeScreen("RegionDataVisScreen");
        mainPanel.add(new RegionDataVisScreen(this, state1, state2, region1, region2), "RegionDataVisScreen");
        cardLayout.show(mainPanel, "RegionDataVisScreen");
    }

    public void refreshStateDataVisScreen(String state1, String state2) {
        removeScreen("StateDataVisScreen");
        mainPanel.add(new StateDataVisScreen(this, state1, state2), "StateDataVisScreen");
        cardLayout.show(mainPanel, "StateDataVisScreen");
    }

    public void refreshCustomDataVisScreen(ArrayList<ArrayList<String>> datasets) {
        removeScreen("CustomDataVisScreen");
        mainPanel.add(new CustomDataVisScreen(this, datasets), "CustomDataVisScreen");
        cardLayout.show(mainPanel, "CustomDataVisScreen");
    }

    public JFrame getFrame() {
        return frame;
    }

    public DataReader getDataReader() {
        return dataReader;
    }

    public static void main(String[] args) {
        App app = new App();
        app.windowManager();
    }
}
