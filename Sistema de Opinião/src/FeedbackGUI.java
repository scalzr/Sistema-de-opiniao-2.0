import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Classe abstrata FeedbackButton
abstract class FeedbackButton extends JButton implements ActionListener {
    protected FeedbackGUI feedbackGUI;
    protected JLabel countLabel;
    protected String label;
    protected String emoji;
    protected Color color;

    public FeedbackButton(FeedbackGUI feedbackGUI, JLabel countLabel, String label, String emoji, Color color) {
        this.feedbackGUI = feedbackGUI;
        this.countLabel = countLabel;
        this.label = label;
        this.emoji = emoji;
        this.color = color;
        setupButton();
    }

    private void setupButton() {
        setFont(new Font("Segoe UI Emoji", Font.BOLD, 30));
        setBackground(color);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setPreferredSize(new Dimension(400, 300));
        setOpaque(true);
        setBorderPainted(false);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Adicionando o emoji
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel labelEmoji = new JLabel(emoji, SwingConstants.CENTER);
        labelEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        labelEmoji.setForeground(Color.WHITE);
        add(labelEmoji, gbc);

        // Adicionando o texto
        gbc.gridy = 1;
        gbc.weighty = 0.6;
        JLabel labelText = new JLabel(label, SwingConstants.CENTER);
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 40));
        labelText.setForeground(Color.WHITE);
        add(labelText, gbc);

        addActionListener(this);
    }

    protected void updateCount() {
        feedbackGUI.incrementCount(label);
        feedbackGUI.updateBackgroundColor();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String feedbackComment = JOptionPane.showInputDialog(feedbackGUI, "Nos forneça um feedback aqui: ", label.toUpperCase());
            if (feedbackComment != null) {
                feedbackGUI.addFeedbackComment(feedbackComment);
                updateCount();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

// Classes para os botões específicos
class BomButton extends FeedbackButton {
    public BomButton(FeedbackGUI feedbackGUI, JLabel countLabel) {
        super(feedbackGUI, countLabel, "Bom", "😊", Color.decode("#4CAF50"));
    }
}

class MedioButton extends FeedbackButton {
    public MedioButton(FeedbackGUI feedbackGUI, JLabel countLabel) {
        super(feedbackGUI, countLabel, "Médio", "😐", Color.decode("#FFC107"));
    }
}

class RuimButton extends FeedbackButton {
    public RuimButton(FeedbackGUI feedbackGUI, JLabel countLabel) {
        super(feedbackGUI, countLabel, "Ruim", "😞", Color.decode("#F44336"));
    }
}

// Classe para contagens de feedback
class FeedbackCounts implements Serializable {
    private static final long serialVersionUID = 1L;
    private int bomCount;
    private int medioCount;
    private int ruimCount;

    public FeedbackCounts() {
        this.bomCount = 0;
        this.medioCount = 0;
        this.ruimCount = 0;
    }

    public int getBomCount() {
        return bomCount;
    }

    public void setBomCount(int bomCount) {
        this.bomCount = bomCount;
    }

    public int getMedioCount() {
        return medioCount;
    }

    public void setMedioCount(int medioCount) {
        this.medioCount = medioCount;
    }

    public int getRuimCount() {
        return ruimCount;
    }

    public void setRuimCount(int ruimCount) {
        this.ruimCount = ruimCount;
    }
}

// Classe para comentários de feedback
class FeedbackComments implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> comments;

    public FeedbackComments() {
        this.comments = new ArrayList<>();
    }

    public List<String> getComments() {
        return comments;
    }

    public void addComment(String comment) {
        comments.add(comment);
    }
}

// Classe principal FeedbackGUI
public class FeedbackGUI extends JFrame {
    private JLabel bomLabel;
    private JLabel medioLabel;
    private JLabel ruimLabel;
    private int bomCount;
    private int medioCount;
    private int ruimCount;
    private JPanel panel;
    private DefaultListModel<String> historyModel;
    private FeedbackCounts feedbackCounts;
    private FeedbackComments feedbackComments;

    public FeedbackGUI() {
        setTitle("Sistema de Feedback");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        bomLabel = new JLabel("Bom: " + bomCount, SwingConstants.CENTER);
        medioLabel = new JLabel("Médio: " + medioCount, SwingConstants.CENTER);
        ruimLabel = new JLabel("Ruim: " + ruimCount, SwingConstants.CENTER);

        bomLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        medioLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        ruimLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));

        BomButton bomButton = new BomButton(this, bomLabel);
        MedioButton medioButton = new MedioButton(this, medioLabel);
        RuimButton ruimButton = new RuimButton(this, ruimLabel);

        panel.add(bomButton);
        panel.add(medioButton);
        panel.add(ruimButton);
        panel.add(bomLabel);
        panel.add(medioLabel);
        panel.add(ruimLabel);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton qrButton = new JButton("Mostrar QR Code");
        qrButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        qrButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showQRCode();
            }
        });
        buttonPanel.add(qrButton);

        // Adicionando botão de exportação
        JButton exportButton = new JButton("Exportar Feedbacks para CSV");
        exportButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportFeedbacksToCSV();
            }
        });
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Adicionando o painel de histórico de feedbacks
        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Histórico de Feedbacks"));
        add(scrollPane, BorderLayout.EAST);

        // Carregar histórico de feedbacks
        feedbackComments = loadFeedbackComments();
        loadFeedbackHistory();

        // Carregar contagens de feedbacks
        feedbackCounts = loadFeedbackCounts();
        bomCount = feedbackCounts.getBomCount();
        medioCount = feedbackCounts.getMedioCount();
        ruimCount = feedbackCounts.getRuimCount();

        bomLabel.setText("Bom: " + bomCount);
        medioLabel.setText("Médio: " + medioCount);
        ruimLabel.setText("Ruim: " + ruimCount);

        updateBackgroundColor();
    }

    public void incrementCount(String feedbackType) {
        if (feedbackType.equals("Bom")) {
            bomCount++;
            feedbackCounts.setBomCount(bomCount);
            bomLabel.setText("Bom: " + bomCount);
        } else if (feedbackType.equals("Médio")) {
            medioCount++;
            feedbackCounts.setMedioCount(medioCount);
            medioLabel.setText("Médio: " + medioCount);
        } else if (feedbackType.equals("Ruim")) {
            ruimCount++;
            feedbackCounts.setRuimCount(ruimCount);
            ruimLabel.setText("Ruim: " + ruimCount);
        }
        saveFeedbackCounts();
    }

    public void updateBackgroundColor() {
        if (bomCount > 10 && bomCount > medioCount && bomCount > ruimCount) {
            panel.setBackground(Color.decode("#C8E6C9")); // Verde claro
        } else if (medioCount > 10 && medioCount > bomCount && medioCount > ruimCount) {
            panel.setBackground(Color.decode("#FFF9C4")); // Amarelo claro
        } else if (ruimCount > 10 && ruimCount > bomCount && ruimCount > medioCount) {
            panel.setBackground(Color.decode("#FFCDD2")); // Vermelho claro
        } else {
            panel.setBackground(Color.WHITE); // Branco
        }
    }

    public void addFeedbackComment(String comment) {
        feedbackComments.addComment(comment);
        saveFeedbackComments();
        addFeedbackToHistory(comment);
    }

    public void loadFeedbackHistory() {
        if (feedbackComments != null) {
            for (String comment : feedbackComments.getComments()) {
                historyModel.addElement(comment);
            }
        }
    }

    public void exportFeedbacksToCSV() {
        try (PrintWriter writer = new PrintWriter(new File("feedbacks.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Tipo");
            sb.append(',');
            sb.append("Quantidade");
            sb.append('\n');

            sb.append("Bom");
            sb.append(',');
            sb.append(bomCount);
            sb.append('\n');

            sb.append("Médio");
            sb.append(',');
            sb.append(medioCount);
            sb.append('\n');

            sb.append("Ruim");
            sb.append(',');
            sb.append(ruimCount);
            sb.append('\n');

            writer.write(sb.toString());
            JOptionPane.showMessageDialog(this, "Feedbacks exportados com sucesso!");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar feedbacks!");
            e.printStackTrace();
        }
    }

    public void showQRCode() {
        try {
            BufferedImage originalImage = ImageIO.read(getClass().getResource("qrcode.png"));
            int maxWidth = 200;
            int maxHeight = 200;
            int newWidth = originalImage.getWidth();
            int newHeight = originalImage.getHeight();
            if (newWidth > maxWidth || newHeight > maxHeight) {
                double aspectRatio = (double) newWidth / newHeight;
                if (newWidth > maxWidth) {
                    newWidth = maxWidth;
                    newHeight = (int) (newWidth / aspectRatio);
                }
                if (newHeight > maxHeight) {
                    newHeight = maxHeight;
                    newWidth = (int) (newHeight * aspectRatio);
                }
            }
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaledImage);
            Font font = new Font("Arial", Font.BOLD, 18);
            UIManager.put("OptionPane.messageFont", font);
            UIManager.put("OptionPane.buttonFont", font);
            JOptionPane.showMessageDialog(this, "Nos forneça um feedback construtivo em nosso site", "QR CODE", JOptionPane.INFORMATION_MESSAGE, icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public FeedbackCounts loadFeedbackCounts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("feedback_counts.dat"))) {
            return (FeedbackCounts) ois.readObject();
        } catch (Exception e) {
            return new FeedbackCounts();
        }
    }

    public void saveFeedbackCounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("feedback_counts.dat"))) {
            oos.writeObject(feedbackCounts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FeedbackComments loadFeedbackComments() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("feedback_comments.dat"))) {
            return (FeedbackComments) ois.readObject();
        } catch (Exception e) {
            return new FeedbackComments();
        }
    }

    public void saveFeedbackComments() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("feedback_comments.dat"))) {
            oos.writeObject(feedbackComments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFeedbackToHistory(String feedback) {
        historyModel.addElement(feedback + " - " + java.time.LocalDateTime.now());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new FeedbackGUI().setVisible(true);
            }
        });
    }
}