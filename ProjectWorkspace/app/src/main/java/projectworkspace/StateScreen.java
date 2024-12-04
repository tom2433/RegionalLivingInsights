package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StateScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private String selectedState1;
    private String selectedState2;
    private JPanel actionPanel;

    public StateScreen(App app) {
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);

        selectedState1 = null;
        selectedState2 = null;

        JLabel descLabel = componentFactory.createDescLabel("This is the StateScreen.");
        JButton backBtn = componentFactory.createBackButton();
        JButton resetBtn = createResetBtn();

        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);

        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        this.add(backBtn, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshStateScreen());
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }
}
