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

public class CustomDataScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private JPanel actionPanel;
    private ArrayList<JPanel> labelPanels;
    private JButton nextBtn;
    // TODO: Implement functionality for multiple datasets
    private ArrayList<ArrayList<String>> stateRegions;

    public CustomDataScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);
        // stateRegions1 = new ArrayList<>();
        // stateRegions2 = new ArrayList<>();
        stateRegions = new ArrayList<>();
        labelPanels = new ArrayList<>();
        ArrayList<String> stateRegions1 = new ArrayList<>();
        ArrayList<String> stateRegions2 = new ArrayList<>();
        stateRegions.add(stateRegions1);
        stateRegions.add(stateRegions2);

        JLabel descLabel = componentFactory.createDescLabel("This is the CustomDataScreen.");
        JButton backBtn = componentFactory.createBackButton();
        JButton resetBtn = createResetBtn();
        JPanel labelPanel1 = createLabelPanel();
        JPanel labelPanel2 = createLabelPanel();
        labelPanels.add(labelPanel1);
        labelPanels.add(labelPanel2);
        addToLabelPanel(1, "Selected Regions/States for Dataset 1:");
        addToLabelPanel(2, "Selected Regions/States for Dataset 2:");
        JButton addStateBtn1 = createAddStateBtn(1);
        JButton addStateBtn2 = createAddStateBtn(2);
        JButton addRegionBtn1 = createAddRegionBtn(1);
        JButton addRegionBtn2 = createAddRegionBtn(2);
        createNextBtn();

        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(labelPanel1);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(addStateBtn1);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(addRegionBtn1);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(labelPanel2);
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(addStateBtn2);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(addRegionBtn2);

        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.add(backBtn, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(nextBtn, BorderLayout.SOUTH);
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
                for (ArrayList<String> stateRegionList : stateRegions) {
                    if (stateRegionList.contains(region + ", " + state)) {
                        stateRegionInDataset = true;
                        break;
                    }
                }

                if (stateRegionInDataset) {
                    duplicateStateRegionWarning();
                } else {
                    try {
                        ArrayList<String> stateRegionList = stateRegions.get(datasetNum - 1);
                        stateRegionList.add(region + ", " + state);
                    } catch (IndexOutOfBoundsException ex) {
                        ArrayList<String> stateRegionList = new ArrayList<>();
                        stateRegionList.add(region + ", " + state);
                        stateRegions.add(datasetNum - 1, stateRegionList);
                    }

                    if (app.getDataReader().getIncorporatedStatus(state, region) == true) {
                        addToLabelPanel(datasetNum, region + ", " + state + " (Incorporated)");
                    } else {
                        addToLabelPanel(datasetNum, region + ", " + state + " (Unincorporated)");
                    }
                }

                dialog.dispose();

                boolean atLeastOneListEmpty = false;
                for (ArrayList<String> stateRegionList : stateRegions) {
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
                for (ArrayList<String> stateRegionList : stateRegions) {
                    if (stateRegionList.contains(state)) {
                        stateRegionInDataset = true;
                        break;
                    }
                }

                if (stateRegionInDataset) {
                    duplicateStateRegionWarning();
                } else {
                    try {
                        ArrayList<String> stateRegionList = stateRegions.get(datasetNum - 1);
                        stateRegionList.add(state);
                    } catch (IndexOutOfBoundsException ex) {
                        ArrayList<String> stateRegionList = new ArrayList<>();
                        stateRegionList.add(state);
                        stateRegions.add(datasetNum - 1, stateRegionList);
                    }

                    addToLabelPanel(datasetNum, state);
                }

                dialog.dispose();

                boolean atLeastOneListEmpty = false;
                for (ArrayList<String> stateRegionList : stateRegions) {
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
        // TODO: replace with app.refreshCustomDataVisScreen(stateRegions1, stateRegions2);
        app.refreshCustomDataScreen();

        int i = 1;
        for (ArrayList<String> stateRegionList : stateRegions) {
            System.out.println(i + ":");
            for (String stateRegion : stateRegionList) {
                System.out.println(stateRegion);
            }
            i++;
        }
    }
}