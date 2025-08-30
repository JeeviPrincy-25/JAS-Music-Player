package miniProj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicManagerUI extends JFrame {
    private JTextField titleField, artistField;
    private JButton chooseFileBtn, chooseImageBtn, uploadBtn,exitBtn;
    private File songFile = null, imageFile = null;
    private JPanel cardPanel;
    private MediaPlayer mediaPlayer;
    private File currentlyPlayingFile = null;

    public MusicManagerUI() {
        new JFXPanel(); // initialize JavaFX runtime

        setTitle("Music Manager");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel for upload
        JPanel topPanel = new JPanel(new GridLayout(5,5, 9, 5));
        titleField = new JTextField();
        artistField = new JTextField();
        chooseFileBtn = new JButton("Choose Song File");
        chooseImageBtn = new JButton("Choose Album cover");
        uploadBtn = new JButton("Upload Song");

        topPanel.add(new JLabel("                Song Title :"));
        topPanel.add(titleField);
        topPanel.add(new JLabel("                    Artist :"));
        topPanel.add(artistField);
        topPanel.add(chooseFileBtn);
        topPanel.add(chooseImageBtn);
        add(topPanel, BorderLayout.NORTH);

        cardPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        JScrollPane scrollPane = new JScrollPane(cardPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        uploadBtn.addActionListener(e -> uploadSong());
        exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> closeWindow());  // Close action
        bottomPanel.add(uploadBtn);
        bottomPanel.add(exitBtn);
        add(bottomPanel, BorderLayout.SOUTH);



        chooseFileBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                songFile = fc.getSelectedFile();
            }
        });

        chooseImageBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imageFile = fc.getSelectedFile();
            }
        });

        uploadBtn.addActionListener(e -> uploadSong());

        loadSongs();

        setVisible(true);
    }

    private void uploadSong() {
        if (titleField.getText().isEmpty() || artistField.getText().isEmpty() || songFile == null) {
            JOptionPane.showMessageDialog(this, "Fill all fields and choose a song!");
            return;
        }

        try (Connection conn = connect()) {
            String sql = "INSERT INTO songs (title, artist, file_path, album_art_path) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, titleField.getText());
            ps.setString(2, artistField.getText());
            ps.setString(3, songFile.getAbsolutePath());
            ps.setString(4, (imageFile != null) ? imageFile.getAbsolutePath() : null);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Song uploaded!");
            titleField.setText("");
            artistField.setText("");
            songFile = null;
            imageFile = null;

            cardPanel.removeAll();
            loadSongs();
            cardPanel.revalidate();
            cardPanel.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Upload error: " + ex.getMessage());
        }
    }

    private void loadSongs() {
        try (Connection conn = connect()) {
            String sql = "SELECT * FROM songs";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("song_id"); // added id for delete/edit
                String title = rs.getString("title");
                String image = rs.getString("album_art_path");
                String path = rs.getString("file_path");

                JPanel card = createSongCard(id, title, image, path);
                cardPanel.add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createSongCard(int songId, String title, String imagePath, String songPath) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 240));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel imageLabel;
        if (imagePath != null && new File(imagePath).exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(img));
        } else {
            ImageIcon icon = new ImageIcon("default_bg.jpg");
            Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(img));
        }

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5)); // increased to 5 slots

        JButton playBtn = new JButton("▶");
        JButton pauseBtn = new JButton("⏸");
        JButton resumeBtn = new JButton("⏯");
        JButton stopBtn = new JButton("■");

        // Three dots button
        JButton optionsBtn = new JButton("⋮");

        playBtn.addActionListener(e -> playSong(songPath));
        pauseBtn.addActionListener(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });
        resumeBtn.addActionListener(e -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });
        stopBtn.addActionListener(e -> stopSong());

        buttonPanel.add(playBtn);
        buttonPanel.add(pauseBtn);
        buttonPanel.add(resumeBtn);
        buttonPanel.add(stopBtn);
        buttonPanel.add(optionsBtn);

        // Popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");

        popupMenu.add(editItem);
        popupMenu.add(deleteItem);

        optionsBtn.addActionListener(e -> popupMenu.show(optionsBtn, 0, optionsBtn.getHeight()));

        editItem.addActionListener(e -> {
            String newTitle = JOptionPane.showInputDialog(this, "Enter new title:", title);
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                updateSongTitle(songId, newTitle);
            }
        });

        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteSong(songId);
            }
        });

        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateSongTitle(int songId, String newTitle) {
        try (Connection conn = connect()) {
            String sql = "UPDATE songs SET title=? WHERE song_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newTitle);
            ps.setInt(2, songId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Title updated!");
            reloadCards();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
        }
    }

    private void deleteSong(int songId) {
        try (Connection conn = connect()) {
            String sql = "DELETE FROM songs WHERE song_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, songId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Song deleted!");
            reloadCards();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Deletion failed: " + e.getMessage());
        }
    }

    private void reloadCards() {
        cardPanel.removeAll();
        loadSongs();
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void playSong(String path) {
        stopSong(); // Stop current if playing
        try {
            Media hit = new Media(new File(path).toURI().toString());
            mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
            currentlyPlayingFile = new File(path);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error playing song: " + ex.getMessage());
        }
    }

    private void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/playlist_db";
        String user = "root";
        String pass = "your-sql-password";
        return DriverManager.getConnection(url, user, pass);
    }
        private void closeWindow() {
            this.dispose();
            new MainDashboard(); 
        }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicManagerUI::new);
    }

}
