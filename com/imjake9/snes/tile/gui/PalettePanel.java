package com.imjake9.snes.tile.gui;

import com.imjake9.snes.tile.PreferencesManager;
import com.imjake9.snes.tile.PreferencesManager.PrefKey;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


public class PalettePanel extends JPanel implements MouseListener, Scrollable {
    
    private DrawingPanel drawingPanel;
    private Color[][] palettes;
    private int currentPalette;
    private byte currentColor;
    
    public PalettePanel() {
        setPalettesData(PreferencesManager.getByteArray(PrefKey.DEFAULT_PALETTES));
        addMouseListener(this);
    }
    
    public void setDrawingPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }
    
    public void setCurrentColor(byte index) {
        currentColor = index;
        repaint();
    }
    
    public byte getCurrentColor() {
        return currentColor;
    }
    
    public void loadPalette(File f) throws IOException {
        String filetype = FilenameUtils.getExtension(f.getName());
        byte[] data = FileUtils.readFileToByteArray(f);
        if (filetype.equalsIgnoreCase("pal")) {
            setPalettesData(data);
        }
        currentPalette = 0;
        repaint();
        drawingPanel.repaintAll();
    }
    
    public void savePaletteAsDefault() {
        PreferencesManager.set(PrefKey.DEFAULT_PALETTES, getPalettesData());
    }
    
    @Override
    public void paintComponent(Graphics g) {
        recalculatePreferredSize();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (palettes == null) {
            return;
        }
        
        int squareSize = getWidth() / 16;
        for (int i = 0; i < palettes.length; i++) {
            for (int j = 0; j < 16; j++) {
                g.setColor(palettes[i][j]);
                g.fillRect(j * squareSize, i * squareSize, squareSize, squareSize);
            }
        }
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.drawRect(0, currentPalette * squareSize, 16*squareSize - 1, squareSize - 1);
        g.fillOval(squareSize*currentColor + squareSize/2 - 4, squareSize*currentPalette + squareSize/2 - 4, 7, 7);
        g.setColor(Color.BLACK);
        g.drawOval(squareSize*currentColor + squareSize/2 - 5, squareSize*currentPalette + squareSize/2 - 5, 8, 8);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    private Dimension recalculatePreferredSize() {
        if (palettes == null) {
            return new Dimension(0, 0);
        }
        Dimension size = new Dimension(getWidth(), (getWidth() / 16) * palettes.length);
        setPreferredSize(size);
        JViewport viewport = (JViewport) getParent();
        viewport.doLayout();
        return size;
    }
    
    public Color getColor(byte index) {
        if (palettes == null) {
            return Color.getHSBColor(index / 16f, 1, 1);
        }
        return palettes[currentPalette][index];
    }
    
    public Color[] getPalette() {
        return palettes[currentPalette];
    }
    
    private void setPalettesData(byte[] data) {
        if (data == null) {
            palettes = null;
            return;
        }
        palettes = new Color[data.length / (3 * 16)][16];
        for (int i = 0; i < palettes.length; i++) {
            for (int j = 0; j < 16; j++) {
                palettes[i][j] = new Color((int) data[i*3*16 + j*3] & 0xFF, (int) data[i*3*16 + j*3 + 1] & 0xFF, (int) data[i*3*16 + j*3 + 2] & 0xFF);
            }
        }
    }
    
    private byte[] getPalettesData() {
        if (palettes == null) {
            return null;
        }
        byte[] data = new byte[palettes.length * 16 * 3];
        for (int i = 0; i < palettes.length; i++) {
            for (int j = 0; j < 16; j++) {
                data[i*3*16 + j*3] = (byte) palettes[i][j].getRed();
                data[i*3*16 + j*3 + 1] = (byte) palettes[i][j].getGreen();
                data[i*3*16 + j*3 + 2] = (byte) palettes[i][j].getBlue();
            }
        }
        return data;
    }
    
    
    @Override
    public void mouseClicked(MouseEvent me) {
        int squareSize = getWidth() / 16;
        int oldPalette = currentPalette;
        currentPalette = me.getY() / squareSize;
        if (currentPalette > palettes.length) {
            currentPalette = palettes.length - 1;
        }
        currentColor = (byte) (me.getX() / squareSize);
        if (currentColor > 0xF) {
            currentColor = 0xF;
        }
        repaint();
        if (oldPalette != currentPalette) {
            drawingPanel.repaintAll();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle rctngl, int i, int i1) {
        return getWidth() / 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle rctngl, int i, int i1) {
        return getWidth() / 16;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
}
