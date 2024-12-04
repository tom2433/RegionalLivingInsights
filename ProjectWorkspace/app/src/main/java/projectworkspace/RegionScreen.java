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

    public RegionScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);

        selectedState1 = null;
        selectedState2 = null;
        selectedRegion1 = null;
        selectedRegion2 = null;

        JLabel descLabel = componentFactory.createDescLabel("To compare two regions, begin by selecting the two states which the regions belong to.");
        JLabel descLabel2 = componentFactory.createDescLabel("Once you have selected your 2 regions, click the \"Compare Regions -->\" button at the bottom.");
        JLabel descLabel3 = componentFactory.createDescLabel("Please note that you cannot select two of the same regions.");
        JLabel descLabel4 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs.");
        JButton resetBtn = createResetBtn();
        JButton backButton = componentFactory.createBackButton();
        nextBtn = createNextBtn();

        selectState1Btn = createStateButton(1);
        selectState2Btn = createStateButton(2);

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
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectState1Btn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectState2Btn);

        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        this.add(backButton, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
        this.add(nextBtn, BorderLayout.SOUTH);
    }

    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshRegionScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    private JButton createStateButton(int stateNum) {
        JButton selectStateBtn = new JButton("Select state " + stateNum);
        selectStateBtn.addActionListener(e -> createStateDialog(stateNum));
        selectStateBtn.setHorizontalAlignment(SwingConstants.CENTER);
        selectStateBtn.setVerticalAlignment(SwingConstants.CENTER);
        selectStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return selectStateBtn;
    }

    private JButton createNextBtn() {
        JButton nextButton = new JButton("Compare Regions -->");
        nextButton.addActionListener(e -> nextBtnClicked());
        nextButton.setHorizontalAlignment(SwingConstants.CENTER);
        nextButton.setVerticalAlignment(SwingConstants.CENTER);
        nextButton.setEnabled(false);
        return nextButton;
    }

    public void createStateDialog(int stateNum) {
        // create dialog
        JDialog dialog = new JDialog(app.getFrame(), "Select State for Region " + Integer.toString(stateNum), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] states = app.getDataReader().getStates();

        // add states to panel
        for (String state : states) {
            JButton stateBtn = new JButton(state);
            stateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateBtn.addActionListener(e ->  {
                if (stateNum == 1) {
                    selectState1Btn.setText("State 1: " + stateBtn.getText());
                    selectedState1 = stateBtn.getText();
                } else if (stateNum == 2) {
                    selectState2Btn.setText("State 2: " + stateBtn.getText());
                    selectedState2 = stateBtn.getText();
                }

                dialog.dispose();

                if (selectedState1 != null && selectedState2 != null) {
                    statesSelected();
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

    private void createRegionDialog(int regionNum) {
        String[] regions = null;

        if (regionNum == 1) {
            regions = app.getDataReader().getRegionsFromState(selectedState1);
        } else if (regionNum == 2) {
            regions = app.getDataReader().getRegionsFromState(selectedState2);
        }

        // create dialog
        JDialog dialog = new JDialog(app.getFrame(), "Select Region " + Integer.toString(regionNum), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add states to panel
        if (regions != null) {
            for (String region : regions) {
                JButton regionBtn = new JButton(region);
                regionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                regionBtn.addActionListener(e ->  {
                    if (regionNum == 1) {
                        selectRegion1Btn.setText("Region 1: " + regionBtn.getText() + ", " + selectedState1);
                        selectedRegion1 = regionBtn.getText();
                    } else if (regionNum == 2) {
                        selectRegion2Btn.setText("Region 2: " + regionBtn.getText() + ", " + selectedState2);
                        selectedRegion2 = regionBtn.getText();
                    }

                    dialog.dispose();

                    if (selectedRegion1 != null && selectedRegion2 != null) {
                        regionsSelected();
                    }
                });
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

    private void statesSelected() {
        selectState1Btn.setEnabled(false);
        selectState2Btn.setEnabled(false);

        selectRegion1Btn = createRegionButton(1);
        selectRegion2Btn = createRegionButton(2);

        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectRegion1Btn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectRegion2Btn);
    }

    private void regionsSelected() {
        nextBtn.setEnabled(true);
        selectRegion1Btn.setEnabled(false);
        selectRegion2Btn.setEnabled(false);
    }

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

    private void duplicateRegionWarning() {
        JOptionPane.showMessageDialog(
            null,
            "The selected regions must be unique in order to compare.",
            "Error",
            JOptionPane.WARNING_MESSAGE
        );
        app.refreshRegionScreen();
    }

    private void nextBtnClicked() {
        if (selectedState1.equals(selectedState2) && selectedRegion1.equals(selectedRegion2)) {
            duplicateRegionWarning();
        } else {
            app.refreshRegionDataVisScreen(selectedState1, selectedState2, selectedRegion1, selectedRegion2);
        }
    }
}