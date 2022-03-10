package com.v7878.avm;

import static com.v7878.avm.NodeParser.parseNodes;
import com.v7878.avm.exceptions.ParseException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        Display.init();
        String data;
        File nodes = new File("nodes.txt");
        System.out.println("from file: " + nodes.isFile());
        InputStream in;
        if (nodes.isFile()) {
            in = new FileInputStream(nodes);
        } else {
            in = Main.class.getResourceAsStream("Samples.txt");
        }
        byte[] bytes = new byte[in.available()];
        new DataInputStream(in).readFully(bytes);
        data = new String(bytes);
        in.close();
        parseNodes(data);
        Machine m = Machine.get();
        m.invoke(m.findNode("main.main"), null);
    }

    public static final class Display extends JFrame {

        private static Display display;

        private BufferedImage img1, img2;
        private final int w, h;

        private final JComponent drawer = new JComponent() {
            @Override
            public void paint(Graphics g2) {
                Graphics2D g = (Graphics2D) g2;
                g.drawImage(img1, (getWidth() - w) / 2, (getHeight() - h) / 2, null);
            }
        };

        private Display(int w, int h) {
            this.w = w;
            this.h = h;
            img1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            img2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            drawer.setPreferredSize(new Dimension(w, h));
            add(drawer, BorderLayout.CENTER);
            pack();
            setResizable(false);
            setLocationByPlatform(true);
            setVisible(true);
        }

        public static void init() {
            Machine m = Machine.get();
            Node n = m.newNode((NodeHandler2) (node, data)
                    -> initDisplay(data.getInt(), data.getInt()),
                    8, 0);
            m.setNodeName(n, "display.init(II)");
            n = m.newNode((NodeHandler2) (node, data)
                    -> swap(),
                    0, 0);
            m.setNodeName(n, "display.swap()");
            n = m.newNode((NodeHandler2) (node, data)
                    -> pixel(data.getInt(), data.getInt(), data.getInt()),
                    12, 0);
            m.setNodeName(n, "display.pixel(III)");
            n = m.newNode((NodeHandler2) (node, data)
                    -> fill(data.getInt()),
                    4, 0);
            m.setNodeName(n, "display.fill(I)");
            n = m.newNode((NodeHandler2) (node, data) -> {
                data.putInt(getColor(data.getInt(), data.getInt()));
            }, 8, 4);
            m.setNodeName(n, "display.getColor(II)");
        }

        private static void pixel(int x, int y, int color) {
            display.img2.setRGB(x, y, color);
        }

        private static int getColor(int x, int y) {
            return display.img1.getRGB(x, y);
        }

        private static void fill(int color) {
            Graphics g = display.img2.getGraphics();
            g.setColor(new Color(color, true));
            g.fillRect(0, 0, display.w, display.h);
        }

        private static void initDisplay(int w, int h) {
            if (display != null) {
                throw new IllegalStateException("display already initialized");
            }
            display = new Display(w, h);
        }

        private static void swap() {
            display.swapBuffers();
        }

        private void swapBuffers() {
            BufferedImage tmp = img1;
            img1 = img2;
            img2 = tmp;
            drawer.repaint();
        }
    }
}
