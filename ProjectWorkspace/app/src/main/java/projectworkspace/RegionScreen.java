package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * This class creates all components and handles all operations within the RegionScreen, where the
 * user selects 2 regions to compare.
 *
 * @author Thomas England
 */
public class RegionScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private JButton nextBtn;
    private JButton selectState1Btn;
    private JButton selectState2Btn;
    private JButton selectRegion1Btn;
    private JButton selectRegion2Btn;
    private JPanel actionPanel;
    private String selectedState1;
    private String selectedState2;
    private String selectedRegion1;
    private String selectedRegion2;

    /**
     * Constructor for the RegionScreen class. Creates a RegionScreen as a JPanel belonging to a
     * specified App.
     *
     * @param app App object referencing the App that this JPanel belongs to.
     */
    public RegionScreen(App app) {
        // create as JPanel with BorderLayout
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);

        selectedState1 = null;
        selectedState2 = null;
        selectedRegion1 = null;
        selectedRegion2 = null;

        // create description and instruction labels, back button, next button
        JLabel descLabel = componentFactory.createDescLabel("To compare two regions, begin by selecting the two states which the regions belong to.");
        JLabel descLabel2 = componentFactory.createDescLabel("Once you have selected your 2 regions, click the \"Compare Regions -->\" button at the bottom.");
        JLabel descLabel3 = componentFactory.createDescLabel("Please note that you cannot select two of the same regions.");
        JLabel descLabel4 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs.");
        JButton resetBtn = createResetBtn();
        JButton backButton = componentFactory.createBackButton();
        nextBtn = createNextBtn();

        // create select state buttons (select region buttons added after user selects states)
        selectState1Btn = createStateButton(1);
        selectState2Btn = createStateButton(2);

        // JPanel actionPanel to handle panel with all components including dynamic ones
        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel2);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel3);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel4);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(40));
        actionPanel.add(selectState1Btn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectState2Btn);

        // contentPanel to hold all screen components (except back and next button)
        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        // add all components to parent panel (this)
        this.add(backButton, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
        this.add(nextBtn, BorderLayout.SOUTH);
    }

    /**
     * Called from the constructor to create JButton 'Reset all inputs' to refresh the screen
     * which resets all user input.
     *
     * @return JButton to refresh screen and reset all user input
     */
    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshRegionScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    /**
     * Called from the constructor to create a JButton 'Compare Regions -->' which is enabled when
     * the user has selected two different regions. Brings the user to the RegionDataVisScreen.
     *
     * @return JButton to take user to the next screen once selections have been made
     */
    private JButton createNextBtn() {
        JButton nextButton = new JButton("Compare Regions -->");
        nextButton.addActionListener(e -> nextBtnClicked());
        nextButton.setHorizontalAlignment(SwingConstants.CENTER);
        nextButton.setVerticalAlignment(SwingConstants.CENTER);
        nextButton.setEnabled(false);
        return nextButton;
    }

    /**
     * Called from the constructor to create JButton 'Select state [1 or 2]' for each state the user
     * must select to choose regions from. Calls a method to create dialog prompt for user to
     * choose a state.
     *
     * @param stateNum int indicating the number of the state selection that this button belongs to
     * @return JButton to prompt user to select a state to choose for a region
     */
    private JButton createStateButton(int stateNum) {
        JButton selectStateBtn = new JButton("Select state " + stateNum);
        selectStateBtn.addActionListener(e -> createStateDialog(stateNum));
        selectStateBtn.setHorizontalAlignment(SwingConstants.CENTER);
        selectStateBtn.setVerticalAlignment(SwingConstants.CENTER);
        selectStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return selectStateBtn;
    }

    /**
     * Called when the user clicks a selectStateBtn. Prompts user for a state to choose for the
     * given stateNum.
     *
     * @param stateNum int indicating the number of the state that the user is currently choosing
     */
    public void createStateDialog(int stateNum) {
        // create dialog
        JDialog dialog = new JDialog(app.getFrame(), "Select State for Region " + Integer.toString(stateNum), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // retrieve all states
        String[] states = app.getDataReader().getStates();

        // add states to panel
        for (String state : states) {
            JButton stateBtn = new JButton(state);
            stateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateBtn.addActionListener(e ->  stateBtnClicked(stateNum, stateBtn, dialog));
            panel.add(stateBtn);
            panel.add(Box.createVerticalStrut(10));
        }

        // wrap panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        // add scroll pane to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);

        // show dialog
        // center relative to parent frame
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setVisible(true);
    }

    /**
     * Called when the user selects a state from the dialog created by the createStateDialog()
     * method when the user chooses to select a state. Changes the text of the respective
     * selectStateBtn to indicate that a state has been selected, checks if user has already
     * selected both states and calls method to show selectRegionBtns if so.
     *
     * @param stateNum int indicating the number of the region the user just selected a state for
     * @param stateBtn JButton referencing the stateBtn that the user clicked to choose
     * @param dialog JDialog containing the stateBtn that the user chose (to close)
     */
    private void stateBtnClicked(int stateNum, JButton stateBtn, JDialog dialog) {
        if (stateNum == 1) {
            selectState1Btn.setText("State 1: " + stateBtn.getText());
            selectedState1 = stateBtn.getText();
        } else if (stateNum == 2) {
            selectState2Btn.setText("State 2: " + stateBtn.getText());
            selectedState2 = stateBtn.getText();
        }

        dialog.dispose();

        // call statesSelected() to display selectRegionBtns if both states chosen
        if (selectedState1 != null && selectedState2 != null) {
            statesSelected();
        }
    }

    /**
     * Called when the user has selected states for both regions. Disables the selectStateBtns and
     * creates 2 selectRegionBtns and adds them to the actionPanel.
     */
    private void statesSelected() {
        selectState1Btn.setEnabled(false);
        selectState2Btn.setEnabled(false);

        selectRegion1Btn = createRegionButton(1);
        selectRegion2Btn = createRegionButton(2);

        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectRegion1Btn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectRegion2Btn);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Called by statesSelected() method when user has chosen states for both regions. Creates a
     * selectRegionBtn which creates a dialog prompt for the user to choose a region for the given
     * state.
     *
     * @param regionNum int indicating the number of the region to select
     * @return JButton selectRegionBtn that prompts user for region to select
     */
    private JButton createRegionButton(int regionNum) {
        JButton selectRegionBtn = new JButton();
        if (regionNum == 1) {
            selectRegionBtn.setText("Select region " + regionNum + " for " + selectedState1);
        } else {
            selectRegionBtn.setText("Select region " + regionNum + " for " + selectedState2);
        }
        selectRegionBtn.addActionListener(e -> createRegionDialog(regionNum));
        selectRegionBtn.setHorizontalAlignment(SwingConstants.CENTER);
        selectRegionBtn.setVerticalAlignment(SwingConstants.CENTER);
        selectRegionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return selectRegionBtn;
    }

    /**
     * Called when the user clicks on a selectRegionBtn to select a region for a given state.
     * Creates a dialog prompt for the user to choose a region for the given state.
     *
     * @param regionNum int indicating the number of the region to choose
     */
    private void createRegionDialog(int regionNum) {
        // String array to store all regions for given state
        String[] regions = null;

        // retrieve all regions for selected state
        if (regionNum == 1) {
            regions = app.getDataReader().getRegionsFromState(selectedState1);
        } else if (regionNum == 2) {
            regions = app.getDataReader().getRegionsFromState(selectedState2);
        }

        // create dialog
        JDialog dialog = new JDialog(app.getFrame(), "Select Region " + Integer.toString(regionNum), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store region options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add regions to panel
        if (regions != null) {
            for (String region : regions) {
                JButton regionBtn = new JButton(region);
                regionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                regionBtn.addActionListener(e ->  regionBtnClicked(regionNum, regionBtn, dialog));
                panel.add(regionBtn);
                panel.add(Box.createVerticalStrut(10));
            }
        }

        // wrap panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        // add scroll pane to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);

        // show dialog
        // center relative to parent frame
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setVisible(true);
    }

    /**
     * Called when the user selects a regionBtn from the select region dialog prompt. Changes text
     * of the respective selectRegionBtn to indicate a region has been selected, and enables the
     * nextBtn if both regions have been selected.
     *
     * @param regionNum int indicating the number of the region user just selected
     * @param regionBtn JButton referencing the regionBtn containing the region that user selected
     * @param dialog JDialog containing the regionBtn that user selected
     */
    private void regionBtnClicked(int regionNum, JButton regionBtn, JDialog dialog) {
        if (regionNum == 1) {
            selectRegion1Btn.setText("Region 1: " + regionBtn.getText() + ", " + selectedState1);
            selectedRegion1 = regionBtn.getText();
        } else if (regionNum == 2) {
            selectRegion2Btn.setText("Region 2: " + regionBtn.getText() + ", " + selectedState2);
            selectedRegion2 = regionBtn.getText();
        }

        dialog.dispose();

        // enable nextBtn and disable selectRegionBtns if user has chosen 2 regions to compare
        if (selectedRegion1 != null && selectedRegion2 != null) {
            nextBtn.setEnabled(true);
            selectRegion1Btn.setEnabled(false);
            selectRegion2Btn.setEnabled(false);
        }
    }

    /**
     * Called when the user clicks the nextBtn to go to the RegionDataVisScreen. Displays an error
     * message and refreshes this screen if the user selected two of the same regions, or brings
     * the user to the RegionDataVisScreen for their selected regions otherwise.
     */
    private void nextBtnClicked() {
        if (selectedState1.equals(selectedState2) && selectedRegion1.equals(selectedRegion2)) {
            JOptionPane.showMessageDialog(
                    app.getFrame(),
                    "The selected regions must be unique in order to compare.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE
            );
            app.refreshRegionScreen();
        } else {
            app.refreshRegionDataVisScreen(selectedState1, selectedState2, selectedRegion1, selectedRegion2);
        }
    }
}