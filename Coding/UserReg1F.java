package miniProj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserReg1 extends JFrame {
    private JTextField usernameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerBtn, backBtn;
    private JButton showPassBtn1, showPassBtn2;
    private boolean isPasswordVisible1 = false, isPasswordVisible2 = false;

    public UserReg1() {
        setTitle("User Registration");
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 20, 100, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 20, 200, 25);
        add(usernameField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 60, 100, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(150, 60, 200, 25);
        add(emailField);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 100, 100, 25);
        add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(150, 100, 200, 25);
        add(phoneField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 140, 100, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 140, 200, 25);
        add(passwordField);

        showPassBtn1 = new JButton("👁");
        showPassBtn1.setBounds(360, 140, 50, 25);
        showPassBtn1.setFocusable(false);
        showPassBtn1.addActionListener(e -> togglePasswordVisibility(passwordField, 1));
        add(showPassBtn1);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setBounds(30, 180, 120, 25);
        add(confirmPasswordLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(150, 180, 200, 25);
        add(confirmPasswordField);

        showPassBtn2 = new JButton("👁");
        showPassBtn2.setBounds(360, 180, 50, 25);
        showPassBtn2.setFocusable(false);
        showPassBtn2.addActionListener(e -> togglePasswordVisibility(confirmPasswordField, 2));
        add(showPassBtn2);

        registerBtn = new JButton("Register");
        registerBtn.setBounds(80, 230, 120, 30);
        registerBtn.addActionListener(e -> registerUser());
        add(registerBtn);

        backBtn = new JButton("Back to Login");
        backBtn.setBounds(220, 230, 150, 30);
        backBtn.addActionListener(e -> {
            dispose();
            new UserLogin1(); 
        });
        add(backBtn);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void togglePasswordVisibility(JPasswordField passField, int which) {
        if (which == 1) {
            isPasswordVisible1 = !isPasswordVisible1;
            passField.setEchoChar(isPasswordVisible1 ? (char) 0 : '•');
        } else {
            isPasswordVisible2 = !isPasswordVisible2;
            passField.setEchoChar(isPasswordVisible2 ? (char) 0 : '•');
        }
    }

    private boolean isValidEmail(String email) {
        return email.endsWith("@gmail.com") || email.endsWith("@yahoo.com")|| email.endsWith("@outlook.in");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d{10}$");
    }

    private void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Only gmail.com or yahoo.com or outlook.in emails are allowed!");
            return;
        }

        if (!isValidPhoneNumber(phone)) {
            JOptionPane.showMessageDialog(this, "Enter a valid 10-digit phone number!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        try (Connection conn = connect()) {
            String sql = "INSERT INTO users (username, email, phone, password) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration Successful!");
            dispose();
            new UserLogin1();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/playlist_db", "root", "your-sql-password");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserRegistration::new);
    }

}
