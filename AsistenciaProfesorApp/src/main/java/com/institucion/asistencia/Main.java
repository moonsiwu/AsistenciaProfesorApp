package com.institucion.asistencia;

import com.institucion.asistencia.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Habilitar renderizado de texto nítido (antialiasing)
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Intentar usar FlatLaf Light (Look & Feel moderno estilo Dashboard Web)
        boolean lookAndFeelCargado = false;
        try {
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            Object flatLafInstance = flatLafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel((LookAndFeel) flatLafInstance);
            
            // Personalizaciones globales de FlatLaf (bordes redondeados, fuentes limpias)
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("TableHeader.separatorColor", new Color(0xE2, 0xE8, 0xF0));
            lookAndFeelCargado = true;
        } catch (Throwable t) {
            // Fallback a Nimbus o Look & Feel del sistema
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        lookAndFeelCargado = true;
                        break;
                    }
                }
                if (!lookAndFeelCargado) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } catch (Exception ignorada) {
            }
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
