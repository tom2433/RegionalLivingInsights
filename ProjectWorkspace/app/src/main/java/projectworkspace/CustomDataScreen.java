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
     *
     */
    private void addDataset() {
        int datasetNum = datasets.size() + 1;
        ArrayList<String> dataset = new ArrayList<>();
        datasets.add(dataset);

        JPanel labelPanel = createLabelPanel();
        labelPanels.add(labelPanel);
        addToLabelPanel(datasetNum, "Selected Regions/States for Dataset " + datasetNum + ":");
        JButton addStateBtn = createAddStateBtn(datasetNum);
        JButton addRegionBtn = createAddRegionBtn(datasetNum);

        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(labelPanel);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(addStateBtn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(addRegionBtn);

        nextBtn.setEnabled(false);

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private JButton createAddDatasetBtn() {
        JButton addDatasetBtn = new JButton("Add another dataset");
        addDatasetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addDatasetBtn.addActionListener(e -> addDataset());
        return addDatasetBtn;
    }

    private void createNextBtn() {
        nextBtn = new JButton("Compare Custom Datasets -->");
        nextBtn.addActionListener(e -> nextBtnClicked());
        nextBtn.setHorizontalAlignment(SwingConstants.CENTER);
        nextBtn.setVerticalAlignment(SwingConstants.CENTER);
        nextBtn.setEnabled(false);
    }

    private JButton createAddRegionBtn(int datasetNum) {
        JButton addRegionBtn = new JButton("add region to dataset " + datasetNum);
        addRegionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addRegionBtn.addActionListener(e -> addRegion(datasetNum));
        return addRegionBtn;
    }

    private JButton createAddStateBtn(int datasetNum) {
        JButton addStateBtn = new JButton("add state to dataset " + datasetNum);
        addStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addStateBtn.addActionListener(e -> addState(datasetNum));
        return addStateBtn;
    }

    private void addRegion(int datasetNum) {
        // addToLabelPanel(datasetNum, "region");
        JDialog dialog = new JDialog(app.getFrame(), "Select state for region to add");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] states = app.getDataReader().getStates();

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

    private void selectRegion(String state, int datasetNum) {
        String[] regions;

        regions = app.getDataReader().getRegionsFromState(state);

        JDialog dialog = new JDialog(app.getFrame(), "Select a region to add to dataset " + datasetNum);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store region options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (String region : regions) {
            JButton regionBtn = new JButton(region);
            regionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            regionBtn.addActionListener(e -> {
                boolean stateRegionInDataset = false;
                for (ArrayList<String> stateRegionList : datasets) {
                    if (stateRegionList.contains(region + ", " + state)) {
                        stateRegionInDataset = true;
                        break;
                    }
                }

                if (stateRegionInDataset) {
                    duplicateStateRegionWarning();
                } else {
                    try {
                        ArrayList<String> stateRegionList = datasets.get(datasetNum - 1);
                        stateRegionList.add(region + ", " + state);
                    } catch (IndexOutOfBoundsException ex) {
                        ArrayList<String> stateRegionList = new ArrayList<>();
                        stateRegionList.add(region + ", " + state);
                        datasets.add(datasetNum - 1, stateRegionList);
                    }

                    if (app.getDataReader().getIncorporatedStatus(state, region) == true) {
                        addToLabelPanel(datasetNum, region + ", " + state + " (Incorporated)");
                    } else {
                        addToLabelPanel(datasetNum, region + ", " + state + " (Unincorporated)");
                    }
                }

                dialog.dispose();

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
            });
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

    private void addState(int datasetNum) {
        // addToLabelPanel(datasetNum, "state");
        JDialog dialog = new JDialog(app.getFrame(), "Select state to add to set " + datasetNum);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] states = app.getDataReader().getStates();

        for (String state : states) {
            JButton stateBtn = new JButton(state);
            stateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateBtn.addActionListener(e -> {
                boolean stateRegionInDataset = false;
                for (ArrayList<String> stateRegionList : datasets) {
                    if (stateRegionList.contains(state)) {
                        stateRegionInDataset = true;
                        break;
                    }
                }

                if (stateRegionInDataset) {
                    duplicateStateRegionWarning();
                } else {
                    try {
                        ArrayList<String> stateRegionList = datasets.get(datasetNum - 1);
                        stateRegionList.add(state);
                    } catch (IndexOutOfBoundsException ex) {
                        ArrayList<String> stateRegionList = new ArrayList<>();
                        stateRegionList.add(state);
                        datasets.add(datasetNum - 1, stateRegionList);
                    }

                    addToLabelPanel(datasetNum, state);
                }

                dialog.dispose();

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

    private void addToLabelPanel(int panelNum, String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel labelPanel = labelPanels.get(panelNum - 1);
        labelPanel.add(label);
        labelPanel.revalidate();
        labelPanel.repaint();
    }

    private JPanel createLabelPanel() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        return labelPanel;
    }

    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshCustomDataScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    private void duplicateStateRegionWarning() {
        JOptionPane.showMessageDialog(
            app.getFrame(),
            "Your custom datasets my not have duplicate values.",
            "Error",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private void nextBtnClicked() {
        app.refreshCustomDataVisScreen(datasets);
    }
}