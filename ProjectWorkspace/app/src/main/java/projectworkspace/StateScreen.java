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

public class StateScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private String selectedState1;
    private String selectedState2;
    private JPanel actionPanel;
    private JButton nextBtn;
    private JButton selectState1Btn;
    private JButton selectState2Btn;

    public StateScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);

        selectedState1 = null;
        selectedState2 = null;

        JLabel descLabel = componentFactory.createDescLabel("To compare two states, begin by selecting the two states below.");
        JLabel descLabel2 = componentFactory.createDescLabel("Once you have selected your 2 states, click the \"Compare States -->\" button at the bottom.");
        JLabel descLabel3 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs.");
        JButton backBtn = componentFactory.createBackButton();
        JButton resetBtn = createResetBtn();
        createNextBtn();
        selectState1Btn = createStateBtn(1);
        selectState2Btn = createStateBtn(2);

        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel2);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel3);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(40));
        actionPanel.add(selectState1Btn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(selectState2Btn);

        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        this.add(backBtn, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
        this.add(nextBtn, BorderLayout.SOUTH);
    }

    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshStateScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    private void createNextBtn() {
        nextBtn = new JButton("Compare States -->");
        nextBtn.addActionListener(e -> nextBtnClicked());
        nextBtn.setHorizontalAlignment(SwingConstants.CENTER);
        nextBtn.setVerticalAlignment(SwingConstants.CENTER);
        nextBtn.setEnabled(false);
    }

    private JButton createStateBtn(int stateNum) {
        JButton selectStateBtn = new JButton("Select state " + stateNum);
        selectStateBtn.addActionListener(e -> createStateDialog(stateNum));
        selectStateBtn.setHorizontalAlignment(SwingConstants.CENTER);
        selectStateBtn.setVerticalAlignment(SwingConstants.CENTER);
        selectStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return selectStateBtn;
    }

    private void createStateDialog(int stateNum) {
        // create dialog
        JDialog dialog = new JDialog(app.getFrame(), "Select State " + stateNum);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] states = app.getDataReader().getStates();

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

    private void statesSelected() {
        selectState1Btn.setEnabled(false);
        selectState2Btn.setEnabled(false);
        nextBtn.setEnabled(true);
    }

    private void duplicateStateWarning() {
        JOptionPane.showMessageDialog(
            app.getFrame(),
            "The selected states must be unique in order to compare.",
            "Error",
            JOptionPane.WARNING_MESSAGE
        );
        app.refreshStateScreen();
    }

    private void nextBtnClicked() {
        if (selectedState1.equals(selectedState2)) {
            duplicateStateWarning();
        } else {
            //TODO: replace with refreshStateDataVisScreen(selectedState1, selectedState2);
            app.refreshStateScreen();
        }
    }
}
