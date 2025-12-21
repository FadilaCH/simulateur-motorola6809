package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


 // Panel contenant les boutons de contrôle du simulateur.
 
public class ControlPanel extends JPanel {

    public ControlPanel(ActionListener step,
                        ActionListener run,
                        ActionListener stop,
                        ActionListener load,
                        ActionListener reset) {

        // Disposition horizontale des boutons
        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(button("Step", step));
        add(button("Run", run));
        add(button("Stop", stop));
        add(button("Load .bin", load));
        add(button("Reset", reset));
    }

    
     // Crée un bouton et lui associe une action.
     
    private JButton button(String text, ActionListener action) {

        JButton button = new JButton(text);
        button.addActionListener(action);

        return button;
    }
}
