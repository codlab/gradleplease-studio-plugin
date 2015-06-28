package cz.jacktech.gradlepleasestudio.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SearchHeader extends JPanel {

    protected JLabel mSearch;
    protected JTextField mSearchField;

    private String mSearchQuery = null;
    private SearchStringUpdatedListener mListener;

    public SearchHeader(SearchStringUpdatedListener listener) {
        this.mListener = listener;
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        mSearch = new JLabel("Search:");
        mSearch.setPreferredSize(new Dimension(80, 26));
        mSearch.setFont(new Font(mSearch.getFont().getFontName(), Font.BOLD, mSearch.getFont().getSize()));
        add(mSearch);

        mSearchField = new JTextField();
        mSearchField.setMaximumSize(new Dimension(420, 26));
        mSearchField.setPreferredSize(new Dimension(420, 26));
        mSearchField.setFont(new Font(mSearchField.getFont().getFontName(), Font.PLAIN, mSearchField.getFont().getSize()));
        mSearchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    mSearchQuery = mSearchField.getText();
                    if (mListener != null)
                        mListener.searchStringUpdated(mSearchQuery);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        add(mSearchField);
    }

    protected interface SearchStringUpdatedListener {
        public void searchStringUpdated(String searchText);
    }
}