package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
        // nextBtn.addActionListener(e -> nextBtnClicked());
        nextBtn.setHorizontalAlignment(SwingConstants.CENTER);
        nextBtn.setVerticalAlignment(SwingConstants.CENTER);
        nextBtn.setEnabled(false);
    }

    private JButton createStateBtn(int stateNum) {
        JButton selectStateBtn = new JButton("Select state " + stateNum);
        // selectStateBtn.addActionListener(e -> createStateDialog(stateNum));
        selectStateBtn.setHorizontalAlignment(SwingConstants.CENTER);
        selectStateBtn.setVerticalAlignment(SwingConstants.CENTER);
        selectStateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return selectStateBtn;
    }
}
