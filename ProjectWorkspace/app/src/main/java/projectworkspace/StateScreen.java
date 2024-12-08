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
 * This class creates all components and handles all operations in the StateScreen. Allows the
 * user to select 2 states to retrieve data from and brings the user to the StateDataVisScreen
 * to view this data.
 *
 * @author Thomas England
 */
public class StateScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private String selectedState1;
    private String selectedState2;
    private JPanel actionPanel;
    private JButton nextBtn;
    private JButton selectState1Btn;
    private JButton selectState2Btn;

    /**
     * Constructor for the StateScreen. Constructs the StateScreen as a JPanel with a BorderLayout
     * belonging to a specified App. Adds all components to StateScreen.
     *
     * @param app App object referencing the App that this JPanel belongs to.
     */
    public StateScreen(App app) {
        // create as JPanel with BorderLayout
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);

        selectedState1 = null;
        selectedState2 = null;

        // create description labels, back button, reset button, next button and both select state
        // buttons
        JLabel descLabel = componentFactory.createDescLabel("To compare two states, begin by selecting the two states below.");
        JLabel descLabel2 = componentFactory.createDescLabel("Once you have selected your 2 states, click the \"Compare States -->\" button at the bottom.");
        JLabel descLabel3 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs.");
        JButton backBtn = componentFactory.createBackButton();
        JButton resetBtn = createResetBtn();
        createNextBtn();
        selectState1Btn = createStateBtn(1);
        selectState2Btn = createStateBtn(2);

        // create actionPanel and add all components
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

        // create contentPanel to store actionPanel
        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        // add all components to parent JPanel (this)
        this.add(backBtn, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
        this.add(nextBtn, BorderLayout.SOUTH);
    }

    /**
     * Called by the constructor to create a 'reset all inputs' JButton which refreshes the screen
     * to reset all user input.
     *
     * @return JButton to refresh screen and reset all user input.
     */
    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshStateScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    /**
     * Called by the constructor to create a 'Compare States -->' button to bring the user to the
     * StateDataVisScreen. Only enabled after user has selected 2 states to compare.
     */
    private void createNextBtn() {
        nextBtn = new JButton("Compare States -->");
        nextBtn.addActionListener(e -> nextBtnClicked());
        nextBtn.setHorizontalAlignment(SwingConstants.CENTER);
        nextBtn.setVerticalAlignment(SwingConstants.CENTER);
        nextBtn.setEnabled(false);
    }

    /**
     * Called by the constructor to create a 'select state [stateNumber]' which creates a dialog
     * prompt asking user for a state to select.
     *
     * @param stateNum int indicating which state option the button will be created for
     * @return JButton creating a dialog prompt asking user for a state to select
     */
    private JButton createStateBtn(int stateNum) {
        JButton selectStateBtn = new JButton("Select state " + stateNum);
        selectStateBtn.addActionListener(e -> createStateDialog(stateNum));
        selectStateBtn.setHorizontalAlignment(SwingConstants.CENTER);
        selectStateBtn.setVerticalAlignment(SwingConstants.CENTER);
        selectStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return selectStateBtn;
    }

    /**
     * Called when the user clicks a 'select state' button. Creates a dialog prompt asking user to
     * select a state for the given state option number stateNum
     *
     * @param stateNum int indicating the state option number
     */
    private void createStateDialog(int stateNum) {
        // create dialog
        JDialog dialog = new JDialog(app.getFrame(), "Select State " + stateNum);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store state options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // retrieve all US states from DataReader
        String[] states = app.getDataReader().getStates();

        // add a stateBtn to panel for each US state
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
     * Called whenever the user clicks a stateBtn to select from the dialog created in
     * createStateDialog() after choosing to select a state. Changes the text of the respective
     * selectStateBtn to indicate that state has been selected and closes the dialog. If both
     * states selected, enable the next button.
     *
     * @param stateNum int indicating the state option number
     * @param stateBtn JButton referencing the button that was selected (containing the state name
     *                 that was selected)
     * @param dialog JDialog containing the stateBtn (to close)
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

        if (selectedState1 != null && selectedState2 != null) {
            selectState1Btn.setEnabled(false);
            selectState2Btn.setEnabled(false);
            nextBtn.setEnabled(true);
        }
    }

    /**
     * Called whenever the user clicks the next button. nextBtn is only enabled after the user has
     * selected two states. If both states are equal, displays a warning message, or brings the
     * user to the StateDataVisScreen for the selected states.
     */
    private void nextBtnClicked() {
        if (selectedState1.equals(selectedState2)) {
            JOptionPane.showMessageDialog(
                    app.getFrame(),
                    "The selected states must be unique in order to compare.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE
            );
            app.refreshStateScreen();
        } else {
            app.refreshStateDataVisScreen(selectedState1, selectedState2);
        }
    }
}
