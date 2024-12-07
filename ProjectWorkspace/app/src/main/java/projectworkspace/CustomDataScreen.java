package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

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
 * This class creates and handles all operations and components within the CustomDataScreen.
 */
public class CustomDataScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private JPanel actionPanel;
    private ArrayList<JPanel> labelPanels;
    private JButton nextBtn;
    private ArrayList<ArrayList<String>> datasets;

    /**
     * Constructs a CustomDataScreen object that belongs to a specified App.
     *
     * @param app the App object that this CustomDataScreen object belongs to.
     */
    public CustomDataScreen(App app) {
        // create this object as a child of JPanel with a BorderLayout
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);
        datasets = new ArrayList<>();
        labelPanels = new ArrayList<>();

        // create components
        JLabel descLabel = componentFactory.createDescLabel("This is the CustomDataScreen.");
        JButton backBtn = componentFactory.createBackButton();
        JButton resetBtn = createResetBtn();
        JButton addDatasetBtn = createAddDatasetBtn();
        createNextBtn();

        // add components to actionPanel
        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(addDatasetBtn);

        // add actionPanel to contentPanel
        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        // add contentPanel to scrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // add all components to parent JPanel (this)
        this.add(backBtn, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(nextBtn, BorderLayout.SOUTH);

        // add default of two custom datasets (user can add more)
        addDataset();
        addDataset();
    }

    /**
     * Adds another custom dataset that the user can add areas to.
     */
    private void addDataset() {
        // add new arraylist to datasets array list
        int datasetNum = datasets.size() + 1;
        ArrayList<String> dataset = new ArrayList<>();
        datasets.add(dataset);

        // create new labelPanel and add region/state buttons for new dataset
        JPanel labelPanel = createLabelPanel();
        labelPanels.add(labelPanel);
        addToLabelPanel(datasetNum, "Selected Regions/States for Dataset " + datasetNum + ":");
        JButton addStateBtn = createAddStateBtn(datasetNum);
        JButton addRegionBtn = createAddRegionBtn(datasetNum);

        // add components to actionPanel
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(labelPanel);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(addStateBtn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(addRegionBtn);

        // new dataset will be empty so disable next button
        nextBtn.setEnabled(false);

        // recreate actionpanel after adding components to it
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Creates a JButton "Add another dataset" which adds another empty custom dataset for the user
     * to fill upon clicking.
     *
     * @return a JButton allowing user to add another custom dataset
     */
    private JButton createAddDatasetBtn() {
        JButton addDatasetBtn = new JButton("Add another dataset");
        addDatasetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addDatasetBtn.addActionListener(e -> addDataset());
        return addDatasetBtn;
    }

    /**
     * Creates the JButton "Compare Custom Datasets" which brings the user to the
     * CustomDataVisScreen. It is only ever enabled  when the user has at least one area in each
     * of their datasets.
     */
    private void createNextBtn() {
        nextBtn = new JButton("Compare Custom Datasets -->");
        nextBtn.addActionListener(e -> app.refreshCustomDataVisScreen(datasets));
        nextBtn.setHorizontalAlignment(SwingConstants.CENTER);
        nextBtn.setVerticalAlignment(SwingConstants.CENTER);
        nextBtn.setEnabled(false);
    }

    /**
     * Creates a JButton "add region to dataset" which prompts the user for a region to add to the
     * given dataset and adds it.
     *
     * @param datasetNum an int containing the number of the dataset that this button will belong
     *                   to
     * @return a JButton allowing user to add a region to a given dataset
     */
    private JButton createAddRegionBtn(int datasetNum) {
        JButton addRegionBtn = new JButton("add region to dataset " + datasetNum);
        addRegionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addRegionBtn.addActionListener(e -> addRegion(datasetNum));
        return addRegionBtn;
    }

    /**
     * Creates a JButton "add state to dataset" which prompts the user for a state to add to the
     * given dataset and adds it.
     *
     * @param datasetNum an int containing the number of the dataset that this button will belong
     *                   to
     * @return a JButton allowing user to add a state to a given dataset
     */
    private JButton createAddStateBtn(int datasetNum) {
        JButton addStateBtn = new JButton("add state to dataset " + datasetNum);
        addStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addStateBtn.addActionListener(e -> addState(datasetNum));
        return addStateBtn;
    }

    /**
     * Called when an addRegionBtn is clicked. First prompts user for a state where the desired
     * region is located, then prompts the user for the desired region in that state to add to the
     * given dataset.
     *
     * @param datasetNum an int containing the number of the dataset to add the region to
     */
    private void addRegion(int datasetNum) {
        // addToLabelPanel(datasetNum, "region");
        JDialog dialog = new JDialog(app.getFrame(), "Select state for region to add");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // retrieve array of states from database
        String[] states = app.getDataReader().getStates();

        // create button for each state and add button to panel
        for (String state : states) {
            JButton stateBtn = new JButton(state);
            stateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateBtn.addActionListener(e -> {
                dialog.dispose();
                selectRegion(state, datasetNum);
            });
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
     * Called when a state is selected from a dialog to add a region from the state to a specific
     * dataset. Prompts user for a region to add from the selected state.
     *
     * @param state a string containing the selected state with which to show regions from in the
     *              dialog
     * @param datasetNum the specific number of the dataset to add a region to
     */
    private void selectRegion(String state, int datasetNum) {
        // String array to store all regions for selected state
        String[] regions = app.getDataReader().getRegionsFromState(state);

        // create dialog prompt for region
        JDialog dialog = new JDialog(app.getFrame(), "Select a region to add to dataset " + datasetNum);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store region options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add regionBtn for each region in selected state to dialog
        for (String region : regions) {
            JButton regionBtn = new JButton(region);
            regionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            regionBtn.addActionListener(e -> regionBtnClicked(region, state, datasetNum, dialog));
            panel.add(regionBtn);
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
     * Called when a region button from the 'select region to add' dialog prompt is clicked. Adds
     * the user selected region to the given dataset only if there are no duplicates in any
     * dataset.
     *
     * @param region String containing the user-selected region
     * @param state String containing the user-selected region's state
     * @param datasetNum int indicating the number of the given dataset to potentially add
     *                   user-selected region to
     * @param dialog JDialog that the selectRegionBtn belonged to
     */
    private void regionBtnClicked(String region, String state, int datasetNum, JDialog dialog) {
        // determine if region already in any datasets
        boolean regionInDataset = false;
        for (ArrayList<String> stateRegionList : datasets) {
            if (stateRegionList.contains(region + ", " + state)) {
                regionInDataset = true;
                break;
            }
        }

        // if it does already exist, display warning and don't add
        if (regionInDataset) {
            duplicateStateRegionWarning();
        // else (selected region isn't in any dataset)
        } else {
            // try to find dataset to add region to in datasets
            try {
                // if it exists, add region to existing dataset
                ArrayList<String> dataset = datasets.get(datasetNum - 1);
                dataset.add(region + ", " + state);
            } catch (IndexOutOfBoundsException ex) {
                // if doesn't exist, add region to new dataset and add dataset to datasets
                ArrayList<String> stateRegionList = new ArrayList<>();
                stateRegionList.add(region + ", " + state);
                datasets.add(datasetNum - 1, stateRegionList);
            }

            // add region to labelpanel for given dataset with incorporated status
            if (app.getDataReader().getIncorporatedStatus(state, region)) {
                addToLabelPanel(datasetNum, region + ", " + state + " (Incorporated)");
            } else {
                addToLabelPanel(datasetNum, region + ", " + state + " (Unincorporated)");
            }
        }

        // close dialog
        dialog.dispose();

        // if all datasets have at least one area in them, enable the next button
        boolean atLeastOneListEmpty = false;
        for (ArrayList<String> dataset : datasets) {
            if (dataset.isEmpty()) {
                atLeastOneListEmpty = true;
                break;
            }
        }
        if (!atLeastOneListEmpty) {
            nextBtn.setEnabled(true);
        }
    }

    /**
     * Called when an addRegionBtn is clicked. Prompts the user for the desired state to add to the
     * given dataset.
     * @param datasetNum int indicating the number of the dataset to add the state to
     */
    private void addState(int datasetNum) {
        // addToLabelPanel(datasetNum, "state");
        JDialog dialog = new JDialog(app.getFrame(), "Select state to add to set " + datasetNum);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // String array to hold states from database
        String[] states = app.getDataReader().getStates();

        // add a stateBtn to panel for each state
        for (String state : states) {
            JButton stateBtn = new JButton(state);
            stateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateBtn.addActionListener(e -> stateBtnClicked(datasetNum, state, dialog));
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
     * Called when a stateBtn is clicked from the dialog prompt 'select state to add to set'. Checks if
     *
     * @param datasetNum
     * @param state
     * @param dialog
     */
    private void stateBtnClicked(int datasetNum, String state, JDialog dialog) {
        // determine if state already in any datasets
        boolean stateInDataset = false;
        for (ArrayList<String> dataset : datasets) {
            if (dataset.contains(state)) {
                stateInDataset = true;
                break;
            }
        }

        // if it is, display warning and don't add
        if (stateInDataset) {
            duplicateStateRegionWarning();
        // if it isn't, add to dataset.
        } else {
            // if no dataset exists for the given dataset number, create one and add it
            try {
                ArrayList<String> stateRegionList = datasets.get(datasetNum - 1);
                stateRegionList.add(state);
            } catch (IndexOutOfBoundsException ex) {
                ArrayList<String> stateRegionList = new ArrayList<>();
                stateRegionList.add(state);
                datasets.add(datasetNum - 1, stateRegionList);
            }

            // add new state to labelPanel for given dataset
            addToLabelPanel(datasetNum, state);
        }

        // close dialog
        dialog.dispose();

        // if all lists have at least one area in them, enable nextBtn
        boolean atLeastOneListEmpty = false;
        for (ArrayList<String> stateRegionList : datasets) {
            if (stateRegionList.isEmpty()) {
                atLeastOneListEmpty = true;
                break;
            }
        }
        if (!atLeastOneListEmpty) {
            nextBtn.setEnabled(true);
        }
    }

    /**
     * Called whenever a user selects a region or a state from a dialog to add to a given dataset,
     * and conditions for adding have been satisfied. Adds a specified string as a JLabel to the
     * labelPanel for a given dataset.
     * <p>
     * Precondition: data set with number corresponding to panelNum must have already been added to
     * dataset.
     *
     * @param panelNum int indicating the number of the given panel number to add to (corresponds
     *                 to the number of the given dataset)
     * @param text String containing the text to add to the panel with the given panel number
     */
    private void addToLabelPanel(int panelNum, String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel labelPanel = labelPanels.get(panelNum - 1);
        labelPanel.add(label);
        labelPanel.revalidate();
        labelPanel.repaint();
    }

    /**
     * Called from addDataset() whenever a dataset is added by the user. Creates a new labelPanel
     * as a JPanel to display areas from a given dataset.
     *
     * @return JPanel with a vertical box layout
     */
    private JPanel createLabelPanel() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        return labelPanel;
    }

    /**
     * Called from the Constructor whenever this screen is refreshed. Creates a 'Reset all inputs'
     * button which refreshes this screen so all user input is reset.
     *
     * @return a JButton 'Reset all inputs' which refreshes this screen
     */
    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshCustomDataScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    /**
     * Called whenever a user attempts to add a state or region to any given dataset when the state
     * or region already exists in a dataset. Displays an error message to the user informing them
     * that their selection has not been added.
     */
    private void duplicateStateRegionWarning() {
        JOptionPane.showMessageDialog(
            app.getFrame(),
            "Your custom datasets my not have duplicate values.",
            "Error",
            JOptionPane.WARNING_MESSAGE
        );
    }
}