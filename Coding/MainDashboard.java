package miniProj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainDashboard extends JFrame {

    public MainDashboard() {
        setTitle("JAS Music - Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load and scale background image
        ImageIcon rawIcon = new ImageIcon("music_bg.jpg"); // Place your background image here
        Image bgImage = rawIcon.getImage().getScaledInstance(1000, 650, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(bgImage));
        background.setLayout(null);
        setContentPane(background);

        // Add logo to the top-left (with some margin)
        ImageIcon logoIcon = new ImageIcon("jas_logo2.jpg"); // Place your logo image here
        Image logoImg = logoIcon.getImage().getScaledInstance(180, 140, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
        logoLabel.setBounds(100, 90, 180, 100); // Top-left with margin
        background.add(logoLabel);

        // User Login Button
        JButton loginBtn = new JButton("User Login");
        loginBtn.setBounds(400, 150, 200, 40);
        background.add(loginBtn);
        loginBtn.addActionListener(e -> new UserLogin1());
        
        // User Registration Button
        JButton regBtn = new JButton("Register");
        regBtn.setBounds(400, 220, 200, 40);
        background.add(regBtn);
        regBtn.addActionListener(e -> new UserReg1());

     // Admin Login Button
        JButton adminBtn = new JButton("Admin Login");
        adminBtn.setBounds(400, 290, 200, 40);
        background.add(adminBtn);
        adminBtn.addActionListener(e -> new AdminLogin());

        // Exit Button
        JButton exitBtn = new JButton("Exit");
        exitBtn.setBounds(400, 360, 200, 40);
        background.add(exitBtn);

        // Exit Action
        exitBtn.addActionListener(e -> System.exit(0));

        // TODO: Add action listeners for adminBtn, regBtn, loginBtn
        setVisible(true);
    }

    public static void main(String[] args) {
        new MainDashboard();
    }
}