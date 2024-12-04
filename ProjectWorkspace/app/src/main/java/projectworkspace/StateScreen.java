package projectworkspace;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class StateScreen extends JPanel {
    private final App app;

    public StateScreen(App app) {
        super(new BorderLayout());
        this.app = app;

        JLabel descLabel = createDescLabel();
        JButton backButton = createBackButton();

        this.add(backButton, BorderLayout.NORTH);
        this.add(descLabel, BorderLayout.CENTER);
    }

    private JLabel createDescLabel() {
        JLabel descLabel = new JLabel("This is the StateScreen.");
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        return descLabel;
    }

    private JButton createBackButton() {
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(app);
        backButton.setHorizontalAlignment(SwingConstants.CENTER);
        backButton.setVerticalAlignment(SwingConstants.CENTER);
        return backButton;
    }
}
