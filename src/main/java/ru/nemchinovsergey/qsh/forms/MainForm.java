package ru.nemchinovsergey.qsh.forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by Sergey on 08.04.2017.
 */
public class MainForm extends JFrame {
    private JTextField textField1;
    private JButton startButton;
    private JProgressBar progressBar1;
    private JList list1;
    private JButton browseButton;
    private JPanel rootPanel;
    private JTextArea textArea1;


    public MainForm() throws HeadlessException {
        super("ZERICH QSH-files Renamer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        startButton.addActionListener(new StartAction());

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(""));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    textField1.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
    }

    private class StartAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String path = textField1.getText();

            if (JOptionPane.showConfirmDialog(MainForm.this, String.format("Обработать директорию '%s'?", path)) == JOptionPane.YES_OPTION) {
                ProcessDirectory(path);
            }
        }
    }

    public int ProcessDirectory(String path) {
        File root = new File(path);
        if (!root.isDirectory()) {
            JOptionPane.showMessageDialog(MainForm.this, "'%s' не директория!", MainForm.this.getTitle(), JOptionPane.ERROR_MESSAGE);
            return 0;
        }

        for (File file : root.listFiles()) {
            textArea1.append(file.getAbsolutePath() + "\n");
        }

        return 0;
    }
}
