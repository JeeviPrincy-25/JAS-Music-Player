package miniProj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.File;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class UserDashboardUI1 extends JFrame {
    private JButton exitBtn;
    private MediaPlayer mediaPlayer;
    private File currentlyPlayingFile = null;
    private int userId;
    private String username;
    private JPanel cardPanel;

    public UserDashboardUI1(int userId, String username) {
        this.userId = userId;
        this.username = username;
        new JFXPanel(); 

        setTitle("User Dashboard - " + username);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        JButton playlistBtn = new JButton("Playlist");
        JButton exitBtn = new JButton("Exit");

        playlistBtn.addActionListener(e -> showPlaylist());

        exitBtn.addActionListener(e -> {
            dispose(); 
            new MainDashboard(); 
        });

        topPanel.add(welcomeLabel);
        topPanel.add(playlistBtn);
        topPanel.add(exitBtn);
        add(topPanel, BorderLayout.SOUTH);

        cardPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        JScrollPane scrollPane = new JScrollPane(cardPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadSongs();
        setVisible(true);
    }
    private void showPlaylist() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/playlist_db", "root", "your-sql-password");

            String query = "SELECT s.song_id, s.title, s.artist, s.album_art_path, s.file_path " +
                           "FROM songs s JOIN user_favorites uf ON s.song_id = uf.song_id " +
                           "WHERE uf.user_id = ?";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            JPanel playlistPanel = new JPanel(new GridLayout(0, 4, 10, 10));
            while (rs.next()) {
                int songId = rs.getInt("song_id");
                String title = rs.getString("title");
                String artist = rs.getString("artist");
                String albumArtPath = rs.getString("album_art_path");
                String songPath = rs.getString("file_path");

                boolean isFavorite = isFavorite(songId);

                JPanel card = new JPanel(new BorderLayout());
                card.setPreferredSize(new Dimension(220, 220));
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                JLabel titleLabel = new JLabel("<html><center><b>" + title + "</b><br>" + artist + "</center></html>", SwingConstants.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

                JLabel imageLabel;
                if (albumArtPath != null && new File(albumArtPath).exists()) {
                    ImageIcon icon = new ImageIcon(albumArtPath);
                    Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
                    imageLabel = new JLabel(new ImageIcon(img));
                } else {
                    ImageIcon icon = new ImageIcon("default_bg.jpg");
                    Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
                    imageLabel = new JLabel(new ImageIcon(img));
                }

                JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
                JButton playBtn = new JButton(" ▶ ");
                JButton pauseBtn = new JButton(" ⏸ ");
                JButton resumeBtn = new JButton(" ⏯ ");
                JButton stopBtn = new JButton(" ■ ");

                JButton heartBtn = new JButton(isFavorite ? " ❤ " : " 🤍 ");
                heartBtn.addActionListener(e -> toggleFavorite(songId, heartBtn));

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
                buttonPanel.add(heartBtn);

                card.add(imageLabel, BorderLayout.CENTER);
                card.add(titleLabel, BorderLayout.NORTH);
                card.add(buttonPanel, BorderLayout.SOUTH);
                playlistPanel.add(card);
            }

            rs.close();
            ps.close();
            conn.close();

            JScrollPane scrollPane = new JScrollPane(playlistPanel);
            JFrame popup = new JFrame("Your Playlist");
            popup.setSize(800, 600);
            popup.add(scrollPane);
            popup.setLocationRelativeTo(null);
            popup.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load playlist.");
        }
    }
    private void loadSongs() {
        try (Connection conn = connect()) {
            String sql = "SELECT * FROM songs";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int songId = rs.getInt("song_id");
                String title = rs.getString("title");
                String image = rs.getString("album_art_path");
                String path = rs.getString("file_path");

                boolean isFavorite = isFavorite(songId);
                JPanel card = createSongCard(title, image, path, songId, isFavorite);
                cardPanel.add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFavorite(int songId) {
        try (Connection conn = connect()) {
            String sql = "SELECT * FROM user_favorites WHERE user_id = ? AND song_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private JPanel createSongCard(String title, String imagePath, String songPath, int songId, boolean isFavorite) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 220));
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

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        JButton playBtn = new JButton("▶");
        JButton pauseBtn = new JButton("⏸");
        JButton resumeBtn = new JButton("⏯");
        JButton stopBtn = new JButton("■");

        JButton heartBtn = new JButton(isFavorite ? "❤" : "🤍");
        heartBtn.addActionListener(e -> toggleFavorite(songId, heartBtn));

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
        buttonPanel.add(heartBtn);

        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void playSong(String path) {
        stopSong();
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

    private void toggleFavorite(int songId, JButton heartBtn) {
        try (Connection conn = connect()) {
            if (isFavorite(songId)) {
                String sql = "DELETE FROM user_favorites WHERE user_id = ? AND song_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, songId);
                ps.executeUpdate();
                heartBtn.setText("🤍");
            } else {
                String sql = "INSERT INTO user_favorites (user_id, song_id) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, songId);
                ps.executeUpdate();
                heartBtn.setText("❤");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/playlist_db";
        String user = "root";
        String pass = "$Jesus25";
        return DriverManager.getConnection(url, user, pass);
    }

    private void closeWindow() {
        this.dispose();
        new UserLogin1(); // Back to login
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserLogin1());
    }

}
