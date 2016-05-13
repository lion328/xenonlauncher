package com.lion328.xenonlauncher.launcher.ui.swing;

import com.lion328.xenonlauncher.launcher.Launcher;
import com.lion328.xenonlauncher.launcher.ui.LauncherUI;
import com.lion328.xenonlauncher.launcher.ui.swing.data.Button;
import com.lion328.xenonlauncher.launcher.ui.swing.data.Component;
import com.lion328.xenonlauncher.launcher.ui.swing.data.Font;
import com.lion328.xenonlauncher.launcher.ui.swing.data.Frame;
import com.lion328.xenonlauncher.launcher.ui.swing.data.Label;
import com.lion328.xenonlauncher.launcher.ui.swing.data.Panel;
import com.lion328.xenonlauncher.launcher.ui.swing.data.PasswordField;
import com.lion328.xenonlauncher.launcher.ui.swing.data.ProgressBar;
import com.lion328.xenonlauncher.launcher.ui.swing.data.TextField;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SwingLauncherUI implements LauncherUI
{

    private Frame frame;
    private Map<String, java.awt.Component> components;
    private Map<URL, Image> images;

    private JFrame jFrame;

    public SwingLauncherUI(Frame frame) throws IOException
    {
        this.frame = frame;

        components = new HashMap<>();
        images = new HashMap<>();

        generateJFrame();
    }

    private void generateJFrame() throws IOException
    {
        jFrame = new JFrame();
        jFrame.setSize(frame.getWidth(), frame.getHeight());
        jFrame.setUndecorated(!frame.isDecorated());

        if (frame.getX() < 0 || frame.getY() < 0)
        {
            jFrame.setLocationRelativeTo(null);
        }
        else
        {
            jFrame.setLocation(frame.getX(), frame.getY());
        }

        components.put(jFrame.getName(), jFrame);

        if (frame.getPanel() == null)
        {
            return;
        }

        JPanel jPanel = parseJPanel(frame.getPanel());

        jFrame.setContentPane(jPanel);
    }

    private JPanel parseJPanel(Panel panel) throws IOException
    {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(null);

        Font defaultFont = frame.getPanel().getFont();

        for (Component component : frame.getPanel().getChildren())
        {
            java.awt.Component swingComponent = null;

            if (component instanceof Button)
            {
                Button button = (Button) component;

                URL imageURL = button.getImageURL();
                URL hoverURL = button.getImageHoverURL();

                if (button.getImageHoverURL() != null)
                {
                    images.put(imageURL, ImageIO.read(imageURL));
                }

                if (button.getImageHoverURL() != null)
                {
                    try
                    {
                        images.put(imageURL, ImageIO.read(imageURL));
                    }
                    catch (IOException unused)
                    {
                        hoverURL = imageURL;
                    }
                }
                else
                {
                    hoverURL = imageURL;
                }

                final Icon imageIcon = new ImageIcon(images.get(imageURL));
                final Icon hoverIcon = new ImageIcon(images.get(hoverURL));

                final JLabel buttonLabel = new JLabel();
                buttonLabel.setIcon(imageIcon);
                buttonLabel.setBounds(button.getX(), button.getY(), button.getWidth(), button.getHeight());

                buttonLabel.addMouseListener(new MouseAdapter()
                {

                    @Override
                    public void mouseEntered(MouseEvent e)
                    {
                        super.mouseEntered(e);
                        buttonLabel.setIcon(hoverIcon);
                    }

                    @Override
                    public void mouseExited(MouseEvent e)
                    {
                        super.mouseExited(e);
                        buttonLabel.setIcon(imageIcon);
                    }
                });

                swingComponent = buttonLabel;
            }
            else if (component instanceof Label)
            {
                Label label = (Label) component;

                Font font;

                if (label.getFont() != null)
                {
                    font = label.getFont();
                }
                else
                {
                    font = defaultFont;
                }

                JLabel jLabel = new JLabel();
                jLabel.setText(label.getDefaultText());
                jLabel.setBounds(label.getX(), label.getY(), label.getWidth(), label.getHeight());
                jLabel.setFont(new java.awt.Font(font.getFamily(), java.awt.Font.PLAIN, font.getSize()));
                jLabel.setText(label.getDefaultText());

                swingComponent = jLabel;
            }
            else if (component instanceof Panel)
            {
                swingComponent = parseJPanel((Panel) component);
            }
            else if (component instanceof TextField)
            {
                TextField textField = (TextField) component;

                JTextField jTextField;

                switch (component.getType())
                {
                    case PasswordField.TYPE:
                        jTextField = new JPasswordField();
                        break;
                    default:
                        jTextField = new JTextField();
                }

                jTextField.setBounds(textField.getX(), textField.getY(), textField.getWidth(), textField.getHeight());
                jTextField.setText(textField.getDefaultText());

                swingComponent = jTextField;
            }
            else if (component instanceof ProgressBar)
            {
                ProgressBar progressBar = (ProgressBar) component;

                JProgressBar jProgressBar = new JProgressBar();
                jProgressBar.setBounds(progressBar.getX(), progressBar.getY(), progressBar.getWidth(), progressBar.getHeight());

                swingComponent = jProgressBar;
            }

            if (swingComponent != null)
            {
                components.put(component.getId(), swingComponent);
                jPanel.add(swingComponent);
            }
        }

        components.put(panel.getId(), jPanel);

        return jPanel;
    }

    @Override
    public void start()
    {

    }

    @Override
    public Launcher getLauncher()
    {
        return null;
    }

    @Override
    public void setLauncher(Launcher launcher)
    {

    }

    @Override
    public boolean isVisible()
    {
        return false;
    }

    @Override
    public void setVisible(boolean visible)
    {

    }

    @Override
    public void displayError(String message)
    {

    }

    @Override
    public void onPercentageChange(File file, int overallPercentage, long fileSize, long fileDownloaded)
    {

    }
}
