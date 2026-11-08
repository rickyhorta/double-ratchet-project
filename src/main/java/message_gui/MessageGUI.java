package message_gui;

import messaging.MessageHandler;
import metrics.PerformanceMetrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageGUI extends JFrame {
    private final MessageHandler messageHandler;
    private final PerformanceMetrics metrics;
    private final String userName;
    private final String remoteUserName;
    
    private JPanel chatContainer;
    private JTextField messageField;
    private JButton sendButton;
    private JButton statsButton;
    private JButton clearButton;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    // Modern Dark Theme Color Scheme
    private static final Color DARK_BG = new Color(18, 18, 18);
    private static final Color CARD_BG = new Color(28, 28, 30);
    private static final Color ACCENT_BLUE = new Color(0, 122, 255);
    private static final Color ACCENT_GREEN = new Color(52, 199, 89);
    private static final Color ACCENT_RED = new Color(255, 69, 58);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(152, 152, 157);
    private static final Color BORDER_COLOR = new Color(58, 58, 60);
    
    // Message bubble colors
    private static final Color SENT_BUBBLE = new Color(0, 122, 255);
    private static final Color RECEIVED_BUBBLE = new Color(44, 44, 46);
    private static final Color SYSTEM_BUBBLE = new Color(255, 149, 0);
    
    public MessageGUI(MessageHandler messageHandler, PerformanceMetrics metrics, String userName, String remoteUserName) {
        this.messageHandler = messageHandler;
        this.metrics = metrics;
        this.userName = userName;
        this.remoteUserName = remoteUserName;
        
        setLookAndFeel();
        initializeGUI();
        setupEventHandlers();
        
        setLocationRelativeTo(null);
        setVisible(true);
        
        System.out.println("Modern Chat GUI initialized successfully");
    }
    
    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", DARK_BG);
            UIManager.put("OptionPane.background", CARD_BG);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        } catch (Exception e) {
            System.out.println("Using default look and feel");
        }
    }
    
    private void initializeGUI() {
        setTitle("Secure Chat - " + userName + " & " + remoteUserName);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 750);
        setMinimumSize(new Dimension(700, 600));
        
        JPanel mainContainer = createMainContainer();
        
        JPanel headerPanel = createModernHeader();
        createModernChatArea();
        JPanel inputPanel = createModernInputPanel();
        JPanel statusPanel = createModernStatusPanel();
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 5));
        bottomPanel.setOpaque(false);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainContainer);
        
        SwingUtilities.invokeLater(() -> {
            addSystemMessage("Secure messaging session initialized");
            addSystemMessage("End-to-end encryption active");
            addSystemMessage("Connected to " + remoteUserName);
        });
    }
    
    private JPanel createMainContainer() {
        JPanel container = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, DARK_BG,
                    0, getHeight(), new Color(25, 25, 27)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        container.setBorder(new EmptyBorder(15, 15, 15, 15));
        return container;
    }
    
    private JPanel createModernHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                
                g2d.setColor(BORDER_COLOR);
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15));
                g2d.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        header.setPreferredSize(new Dimension(0, 80));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("SECURE CHAT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(userName + " ↔ " + remoteUserName);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        connectionLabel = new JLabel("CONNECTED");
        connectionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        connectionLabel.setForeground(ACCENT_GREEN);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        statsButton = createModernButton("STATS", ACCENT_BLUE);
        clearButton = createModernButton("CLEAR", ACCENT_RED);
        
        buttonPanel.add(statsButton);
        buttonPanel.add(clearButton);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(connectionLabel, BorderLayout.CENTER);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private void createModernChatArea() {
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setOpaque(false);
        chatContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        scrollPane = new JScrollPane(chatContainer) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
            }
        };
        
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }
    
    private JPanel createModernInputPanel() {
        JPanel inputPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
            }
        };
        
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BorderLayout(15, 0));
        inputPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        inputPanel.setPreferredSize(new Dimension(0, 70));
        
        messageField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(DARK_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                
                if (hasFocus()) {
                    g2d.setColor(ACCENT_BLUE);
                } else {
                    g2d.setColor(BORDER_COLOR);
                }
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2d.dispose();
                
                super.paintComponent(g);
            }
        };
        
        messageField.setOpaque(false);
        messageField.setBorder(new EmptyBorder(12, 15, 12, 15));
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setForeground(TEXT_PRIMARY);
        messageField.setCaretColor(ACCENT_BLUE);
        
        messageField.setText("Type your message to " + remoteUserName + "...");
        messageField.setForeground(TEXT_SECONDARY);
        messageField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (messageField.getText().startsWith("Type your message")) {
                    messageField.setText("");
                    messageField.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText("Type your message to " + remoteUserName + "...");
                    messageField.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        sendButton = createSendButton();
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    private JButton createSendButton() {
        JButton button = new JButton("SEND") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color startColor = ACCENT_BLUE;
                Color endColor = ACCENT_BLUE.darker();
                
                if (getModel().isPressed()) {
                    startColor = ACCENT_BLUE.darker().darker();
                    endColor = ACCENT_BLUE.darker();
                } else if (getModel().isRollover()) {
                    startColor = ACCENT_BLUE.brighter();
                    endColor = ACCENT_BLUE;
                }
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                
                // Add subtle shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.draw(new RoundRectangle2D.Float(0, 1, getWidth()-1, getHeight()-1, 12, 12));
                
                g2d.dispose();
                
                // Draw text manually for better visibility
                FontMetrics fm = g.getFontMetrics(getFont());
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                g.drawString(getText(), textX, textY);
            }
        };
        
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 40));
        button.setContentAreaFilled(false);
        
        return button;
    }
    
    private JPanel createModernStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
        
        statusLabel = new JLabel("End-to-end encrypted • Connected to " + remoteUserName);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        JLabel encryptionLabel = new JLabel("Double Ratchet Protocol Active");
        encryptionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        encryptionLabel.setForeground(ACCENT_GREEN);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(encryptionLabel, BorderLayout.EAST);
        
        return statusPanel;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = color;
                if (getModel().isPressed()) {
                    bgColor = color.darker().darker();
                } else if (getModel().isRollover()) {
                    bgColor = color.brighter();
                }
                
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                // Add subtle shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.draw(new RoundRectangle2D.Float(0, 1, getWidth()-1, getHeight()-1, 8, 8));
                
                g2d.dispose();
                
                // Draw text manually for better visibility
                FontMetrics fm = g.getFontMetrics(getFont());
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                g.drawString(getText(), textX, textY);
            }
        };
        
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        
        return button;
    }
    
    private void setupEventHandlers() {
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        statsButton.addActionListener(e -> showModernStatsDialog());
        clearButton.addActionListener(e -> clearChat());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showExitDialog();
            }
        });
        
        SwingUtilities.invokeLater(() -> messageField.requestFocus());
    }
    
    private void sendMessage() {
        String messageText = messageField.getText().trim();
        
        if (messageText.isEmpty() || messageText.startsWith("Type your message")) {
            return;
        }
        
        try {
            messageHandler.sendMessage(messageText);
            addSentMessage(messageText);
            
            messageField.setText("");
            messageField.setForeground(TEXT_PRIMARY);
            messageField.requestFocus();
            
        } catch (Exception e) {
            addSystemMessage("Failed to send message: " + e.getMessage());
        }
    }
    
    public void displayReceivedMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            addReceivedMessage(message);
        });
    }
    
    private void addSentMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = createMessageBubble(message, userName, SENT_BUBBLE, true);
            chatContainer.add(messagePanel);
            chatContainer.add(Box.createVerticalStrut(10));
            refreshChat();
        });
    }
    
    private void addReceivedMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = createMessageBubble(message, remoteUserName, RECEIVED_BUBBLE, false);
            chatContainer.add(messagePanel);
            chatContainer.add(Box.createVerticalStrut(10));
            refreshChat();
        });
    }
    
    private void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = createSystemBubble(message);
            chatContainer.add(messagePanel);
            chatContainer.add(Box.createVerticalStrut(10));
            refreshChat();
        });
    }
    
    private JPanel createMessageBubble(String message, String sender, Color bubbleColor, boolean isRight) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw bubble shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth()-2, getHeight()-2, 18, 18));
                
                // Draw bubble
                g2d.setColor(bubbleColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-2, 18, 18));
                
                g2d.dispose();
            }
        };
        
        bubble.setOpaque(false);
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(12, 15, 12, 15));
        
        // Create message area with proper sizing
        JTextArea messageArea = new JTextArea(message);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        messageArea.setForeground(Color.WHITE);
        
        // Calculate proper dimensions for the message
        FontMetrics fm = messageArea.getFontMetrics(messageArea.getFont());
        int maxBubbleWidth = (int) (scrollPane.getWidth() * 0.6); // 60% of chat width
        int minBubbleWidth = 100;
        
        // Calculate text dimensions
        String[] words = message.split(" ");
        int longestWordWidth = 0;
        for (String word : words) {
            longestWordWidth = Math.max(longestWordWidth, fm.stringWidth(word));
        }
        
        // Calculate required width based on text
        int textWidth = fm.stringWidth(message);
        int bubbleWidth;
        
        if (textWidth + 30 <= maxBubbleWidth && message.length() < 50) {
            // Short message - use actual text width
            bubbleWidth = Math.max(minBubbleWidth, textWidth + 30);
            messageArea.setColumns(0); // Let it be natural width
        } else {
            // Long message - use maximum width and wrap
            bubbleWidth = Math.max(minBubbleWidth, Math.min(maxBubbleWidth, longestWordWidth + 30));
        }
        
        // Set the text area width for wrapping calculation
        messageArea.setSize(bubbleWidth - 30, Integer.MAX_VALUE);
        
        // Calculate the height needed
        int textHeight = messageArea.getPreferredSize().height;
        
        // Timestamp
        JLabel timeLabel = new JLabel(timeFormat.format(new Date()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(255, 255, 255, 180));
        timeLabel.setHorizontalAlignment(isRight ? SwingConstants.RIGHT : SwingConstants.LEFT);
        
        bubble.add(messageArea, BorderLayout.CENTER);
        bubble.add(timeLabel, BorderLayout.SOUTH);
        
        // Set bubble size properly
        bubble.setPreferredSize(new Dimension(bubbleWidth, textHeight + 35)); // +35 for padding and timestamp
        bubble.setMaximumSize(new Dimension(bubbleWidth, textHeight + 35));
        bubble.setMinimumSize(new Dimension(Math.min(bubbleWidth, minBubbleWidth), textHeight + 35));
        
        // Align bubble
        if (isRight) {
            container.add(Box.createHorizontalGlue(), BorderLayout.WEST);
            container.add(bubble, BorderLayout.EAST);
        } else {
            container.add(bubble, BorderLayout.WEST);
            container.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        }
        
        return container;
    }
    
    private JPanel createSystemBubble(String message) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fill(new RoundRectangle2D.Float(1, 1, getWidth()-1, getHeight()-1, 15, 15));
                
                g2d.setColor(SYSTEM_BUBBLE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15));
                
                g2d.dispose();
            }
        };
        
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubble.setLayout(new BorderLayout());
        
        // Create text area for system messages to handle long text
        JTextArea messageArea = new JTextArea("[SYSTEM] " + message);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Arial", Font.ITALIC, 12));
        messageArea.setForeground(Color.WHITE);
        
        // Calculate size for system bubble
        FontMetrics fm = messageArea.getFontMetrics(messageArea.getFont());
        int maxWidth = (int) (scrollPane.getWidth() * 0.5); // 50% for system messages
        int textWidth = fm.stringWidth("[SYSTEM] " + message);
        
        if (textWidth + 24 <= maxWidth) {
            // Short system message
            messageArea.setPreferredSize(new Dimension(textWidth + 24, fm.getHeight()));
        } else {
            // Long system message
            messageArea.setSize(maxWidth - 24, Integer.MAX_VALUE);
            int height = messageArea.getPreferredSize().height;
            messageArea.setPreferredSize(new Dimension(maxWidth - 24, height));
        }
        
        bubble.add(messageArea, BorderLayout.CENTER);
        
        container.add(Box.createHorizontalGlue());
        container.add(bubble);
        container.add(Box.createHorizontalGlue());
        
        return container;
    }
    
    private void refreshChat() {
        // Force recalculation of sizes
        chatContainer.invalidate();
        chatContainer.revalidate();
        chatContainer.repaint();
        
        SwingUtilities.invokeLater(() -> {
            // Scroll to bottom
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
            
            // Force viewport update
            scrollPane.getViewport().revalidate();
        });
    }
    
    private void clearChat() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Clear all messages from this conversation?",
            "Clear Chat",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            chatContainer.removeAll();
            addSystemMessage("Chat history cleared");
        }
    }
    
    private void showModernStatsDialog() {
        JDialog statsDialog = new JDialog(this, "Performance Statistics", true);
        statsDialog.setSize(650, 500);
        statsDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea statsArea = new JTextArea(metrics.generateReport());
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setBackground(DARK_BG);
        statsArea.setForeground(TEXT_PRIMARY);
        statsArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = createModernButton("CLOSE", ACCENT_BLUE);
        closeButton.addActionListener(e -> statsDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        statsDialog.add(contentPanel);
        statsDialog.setVisible(true);
    }
    
    private void showExitDialog() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?\nThis will end your secure session with " + remoteUserName + ".",
            "Exit Secure Chat",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    public void showSystemMessage(String message) {
        addSystemMessage(message);
    }
    
    public void showErrorMessage(String message) {
        addSystemMessage("ERROR: " + message);
    }
    
    // Custom ScrollBar UI for modern look
    private class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = BORDER_COLOR;
            trackColor = DARK_BG;
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fill(new RoundRectangle2D.Float(thumbBounds.x + 2, thumbBounds.y, 
                     thumbBounds.width - 4, thumbBounds.height, 6, 6));
            g2d.dispose();
        }
    }
}