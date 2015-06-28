package cz.jacktech.gradlepleasestudio.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import cz.jacktech.gradlepleasestudio.net.NetManager;
import cz.jacktech.gradlepleasestudio.net.data.JCenterPackageFile;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public class SearchDependencyPanel extends JPanel implements SearchHeader.SearchStringUpdatedListener {

    private final NetManager mNetManager;
    protected Project mProject;
    protected JButton mConfirm;
    protected JButton mCancel;
    private JBList mPackagesList;
    private SearchHeader mHeader;
    private DefaultListModel mListModel;
    private ArrayList<String> mGradlePackageList;

    public SearchDependencyPanel(Project project) {
        mProject = project;
        mNetManager = new NetManager();

        setPreferredSize(new Dimension(500, 250));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        initializeList();
        addButtons();
    }

    protected void initializeList() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(mHeader = new SearchHeader(this));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        mListModel = new DefaultListModel();
        mPackagesList = new JBList(mListModel);
        mPackagesList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        mPackagesList.setLayoutOrientation(JList.VERTICAL);
        mPackagesList.setSelectedIndex(0);

        JBScrollPane scrollPane = new JBScrollPane(mPackagesList);
        contentPanel.add(scrollPane);

        add(contentPanel, BorderLayout.CENTER);
        refresh();
    }

    protected void addButtons() {
        mCancel = new JButton();
        mCancel.setAction(new CancelAction());
        mCancel.setMinimumSize(new Dimension(120, 32));
        mCancel.setPreferredSize(new Dimension(120, 32));
        mCancel.setText("Cancel");
        mCancel.setVisible(true);

        mConfirm = new JButton();
        mConfirm.setAction(new ConfirmAction());
        mConfirm.setMinimumSize(new Dimension(120, 32));
        mConfirm.setPreferredSize(new Dimension(120, 32));
        mConfirm.setText("Add");
        mConfirm.setVisible(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(mCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(mConfirm);

        add(buttonPanel, BorderLayout.PAGE_END);
        refresh();
    }

    protected void refresh() {
        revalidate();

        if (mConfirm != null) {
        }
    }

    protected boolean checkValidity() {
        boolean valid = true;


        return valid;
    }

    public JButton getConfirmButton() {
        return mConfirm;
    }

    @Override
    public void searchStringUpdated(String searchText) {
        mNetManager.api().searchPackage(searchText, new Callback<java.util.List<JCenterPackageFile>>() {
            @Override
            public void success(List<JCenterPackageFile> jCenterPackageFiles, Response response) {
                parsePackageListToPackages(jCenterPackageFiles);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                retrofitError.printStackTrace();
                //Utils.showNotification(mProject, SearchDependencyPanel.this, MessageType.ERROR, "Package search failed");
            }
        });
    }

    private void parsePackageListToPackages(List<JCenterPackageFile> jCenterPackageFiles) {
        mListModel.removeAllElements();
        mGradlePackageList = new ArrayList<>();
        ArrayList<Package> packages = new ArrayList<>();
        for(JCenterPackageFile file : jCenterPackageFiles) {
            Package current = new Package(file.packageName, file.version);
            Package p;
            if ((p = containsPackage(packages, file)) != null) {
                if(current.isNewerThan(p)) {
                    packages.remove(p);
                    packages.add(current);
                } else {
                    continue;
                }
            } else {
                packages.add(current);
            }
        }

        for(Package p : packages) {
            mListModel.addElement(p.name+":"+p.versionString);
        }
    }

    private Package containsPackage(ArrayList<Package> packages, JCenterPackageFile file) {
        for(Package p : packages)
            if(p.name.equals(file.packageName))
                return p;
        return null;
    }
    // classes

    private class Package {
        public String name;
        public int[] version;
        public String versionString;

        public Package(String name, String version) {
            this.name = name;
            this.versionString = version;
            try {
                this.version = parseToIntArray(version.split("\\."));
            } catch (Exception e) {
                this.version = new int[]{0,0,0};
            }
        }

        private int[] parseToIntArray(String[] array) {
            int[] intArray = new int[array.length];
            for(int i = 0;i < array.length;i++)
                intArray[i] = Integer.parseInt(array[i]);
            return intArray;
        }

        public boolean isNewerThan(Package aPackage) {
            for(int i = 0;i < (version.length >= aPackage.version.length ? version.length : aPackage.version.length);i++) {
                if(i >= version.length)
                    return false;
                if(i >= aPackage.version.length)
                    return true;

                if(version[i] > aPackage.version[i]) {
                    return true;
                } else if (version[i] < aPackage.version[i]) {
                    return false;
                } else {
                    continue;
                }
            }
            return false;
        }
    }

    protected class ConfirmAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            boolean valid = checkValidity();


        }
    }

    protected class CancelAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {

        }
    }


}