package ru.nemchinovsergey.qsh;

import ru.nemchinovsergey.qsh.forms.MainForm;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Sergey on 08.04.2017.
 */
public class MainClass {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {

        try {
            // Set cross-platform Java L&F (also called "Metal")
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }

        SwingUtilities.invokeAndWait(() -> new MainForm());
    }
}
