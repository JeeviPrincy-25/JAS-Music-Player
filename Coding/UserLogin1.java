package miniProj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserLogin1 extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public UserLogin1() {
        setTitle("User Login - JAS Music");
        setSize(350, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");

        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setMaximumSize(usernameField.getPreferredSize());
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setMaximumSize(passwordField.getPreferredSize());
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(loginBtn);

        loginBtn.addActionListener(e -> validateLogin());

        add(panel);
        setVisible(true);
    }

    private void validateLogin() {
        String username = usernameField.getText();
        String password = String.valueOf(passwordField.getPassword());

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/playlist_db", "root", "your-sql-password")) {

            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");
                dispose();
                new UserDashboardUI1(rs.getInt("id"), username);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
