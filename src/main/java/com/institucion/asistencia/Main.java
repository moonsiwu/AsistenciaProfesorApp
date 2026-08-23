package com.institucion.asistencia;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.institucion.asistencia.ui.LoginFrame;

public class Main {

    public static void main(String[] args) {
        // Se usa Nimbus porque viene incluido en el JDK y se ve más
        // moderno que el look&feel por defecto de Swing.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorada) {
            // Si Nimbus no está disponible, se sigue con el look&feel por defecto.
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
