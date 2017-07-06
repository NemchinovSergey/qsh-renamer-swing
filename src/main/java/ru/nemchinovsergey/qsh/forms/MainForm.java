package ru.nemchinovsergey.qsh.forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;


/**
 * Created by Sergey on 08.04.2017.
 */
public class MainForm extends JFrame {
    private static final String CONFIG_FILE_NAME = "app.properties";
    private static final String DIRECTORY = "directory";

    private JTextField textDirectory;
    private JButton startButton;
    private JProgressBar progressBar1;
    private JButton browseButton;
    private JPanel rootPanel;
    private JTextArea textArea1;
    private Properties config;


    public MainForm() throws HeadlessException {
        super("ZERICH QSH-files Renamer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        config = loadConfig();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfig();
            }
        });

        startButton.addActionListener(action -> {
            String path = textDirectory.getText();

            if (path == null || path.isEmpty()) {
                JOptionPane.showMessageDialog(MainForm.this, "Укажите директорию!");
                return;
            }

            if (JOptionPane.showConfirmDialog(MainForm.this, String.format("Обработать директорию '%s'?", path)) == JOptionPane.YES_OPTION) {
                new Thread(new ProcessorThread(path)).start();
            }
        });

        browseButton.addActionListener(action -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(textDirectory.getText()));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String dir = fileChooser.getSelectedFile().getAbsolutePath();
                textDirectory.setText(dir);
                config.put(DIRECTORY, dir);
            }
        });

        textDirectory.setText(config.getProperty(DIRECTORY, "Укажите директорию!"));
    }


    private Properties loadConfig() {
        Properties properties = new Properties();

        try (Reader reader = new FileReader(CONFIG_FILE_NAME)) {
            properties.load(reader);
        }
        catch (FileNotFoundException e) {
            //JOptionPane.showMessageDialog(MainForm.this, e.getMessage());
        }
        catch (Exception e) {
            // do nothing
        }
        return properties;
    }

    private void saveConfig() {
        File configFile = new File(CONFIG_FILE_NAME);
        try (FileWriter writer = new FileWriter(configFile)) {
            config.store(writer, "Application settings");
        }
        catch (FileNotFoundException e) {
            // file does not exist
            JOptionPane.showMessageDialog(MainForm.this, e.getMessage());
        }
        catch (Exception e) {
            // do nothing
        }
    }

    private enum ProcessResult {
        SKIPPED,
        RENAMED,
        DELETED,
        WARNING,
        ERROR
    }

    private class ProcessorThread implements Runnable {
        private String path;

        public ProcessorThread(String path) {
            this.path = path;
        }

        private ProcessResult ProcessFile(File sourceFile) {
            String name = sourceFile.getName();
            String path = sourceFile.getParent() + "/";

            // check sourceFile name for "OrdLog.Eu-12.14.2014-10-22.qsh" sample
            if (name.toLowerCase().matches("ordlog\\..+\\.qsh")) {

                String targetName = name.substring(7, name.length() - 4) + ".OrdLog.qsh";
                File targetFile = new File(path + targetName);

                if (targetFile.exists()) {
                    if (sourceFile.length() == targetFile.length()) {
                        if (sourceFile.delete())
                            return ProcessResult.DELETED;
                        else
                            return ProcessResult.ERROR;
                    } else {
                        return ProcessResult.WARNING;
                    }
                } else {
                    if (sourceFile.renameTo(targetFile))
                        return ProcessResult.RENAMED;
                    else
                        return ProcessResult.ERROR;
                }
            }
            return ProcessResult.SKIPPED;
        }

        private void ProcessDirectory(String path) throws InterruptedException {
            File root = new File(path);
            if (path.isEmpty() || !root.isDirectory()) {
                JOptionPane.showMessageDialog(MainForm.this, "'%s' не директория!", MainForm.this.getTitle(), JOptionPane.ERROR_MESSAGE);
            }

            synchronized (MainForm.this) {
                progressBar1.setMinimum(0);
                progressBar1.setMaximum(100);
                textArea1.setText(null);
            }

            List<File> files = new ArrayList<>();

            try {
                Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        files.add(file.toFile());

                        if (Thread.currentThread().isInterrupted()) return FileVisitResult.TERMINATE;
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            synchronized (MainForm.this) {
                textArea1.append(String.format("Всего файлов: %d\n", files.size()));
                progressBar1.setMinimum(0);
                progressBar1.setMaximum(files.size());
            }

            for (int i = 0; i < files.size() && !Thread.currentThread().isInterrupted(); i++) {
                ProcessResult result = ProcessFile(files.get(i));
                String action = null;
                switch (result) {
                    case SKIPPED:
                        action = "Пропущен";
                        break;
                    case RENAMED:
                        action = "Переименован";
                        break;
                    case DELETED:
                        action = "Удалён";
                        break;
                    case WARNING:
                        action = "Требует внимания";
                        break;
                    case ERROR:
                        action = "Ошибка переименования/удаления";
                        break;
                }

                synchronized (MainForm.this) {
                    if (result != ProcessResult.SKIPPED) {
                        textArea1.append(files.get(i).getAbsolutePath() + " ---> " + action + "\n");
                    }
                    progressBar1.setValue(i + 1);
                }

                //Thread.sleep(10);
            }
        }

        @Override
        public void run() {
            try {
                browseButton.setEnabled(false);
                startButton.setEnabled(false);
                MainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                ProcessDirectory(path);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                browseButton.setEnabled(true);
                startButton.setEnabled(true);
                MainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

}