package cz.jacktech.gradlepleasestudio;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.NonProjectScopeDisablerEP;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import cz.jacktech.gradlepleasestudio.net.NetManager;
import cz.jacktech.gradlepleasestudio.net.data.JCenterPackageFile;
import org.jetbrains.annotations.NotNull;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.util.*;

/**
 * Created by toor on 27.6.15.
 */
public class AddGradleDependency extends AnAction implements DumbAware {

    private JBList myList;
    private JBPopup searchPopup;
    private SearchTextField myPopupField;
    private JBPopup listPopup;
    private PackageListModel listModel;
    private NetManager netManager;

    public void actionPerformed(AnActionEvent event) {
        netManager = new NetManager();
        Project project = event.getData(PlatformDataKeys.PROJECT);

        PsiFile[] fileList = FilenameIndex.getFilesByName(project, "build.gradle", GlobalSearchScope.projectScope(project));
        if(fileList != null && fileList.length > 0) {
            StringBuilder data = new StringBuilder();
            String[] fileNames = new String[fileList.length];
            int i = 0;
            for (PsiFile f : fileList) {
                data.append(f.getVirtualFile().getPath()).append(", ");
                fileNames[i] = f.getVirtualFile().getCanonicalPath();
                i++;
            }
            Utils.showNotification(project, MessageType.INFO, data.toString());
            showSearchDialog(project, fileList);
        } else {
            Utils.showNotification(project, MessageType.ERROR, "No build.gradle found!");
        }
    }

    public void showSearchDialog(Project project, PsiFile[] fileList) {
        /*SearchDependencyPanel panel = new SearchDependencyPanel(project, fileList);
        JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null)
            .setRequestFocus(true)
            .setTitle("Search for gradle dependency")
            .set
            .createPopup()
                .showCenteredInCurrentWindow(project);*/
        myPopupField = new SearchTextField();
        myPopupField.getTextEditor().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    myList.repaint();
                } else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    netManager.api().searchPackage(myPopupField.getText(), new Callback<java.util.List<JCenterPackageFile>>() {
                        @Override
                        public void success(java.util.List<JCenterPackageFile> jCenterPackageFiles, Response response) {
                            parsePackageListToPackages(jCenterPackageFiles);
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            retrofitError.printStackTrace();
                            //Utils.showNotification(mProject, SearchDependencyPanel.this, MessageType.ERROR, "Package search failed");
                        }
                    });
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    myList.repaint();
                }
            }
        });
        initSearchField(myPopupField);
        myPopupField.setOpaque(false);
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel title = new JLabel(" Search Gradle dependency (package):       ");
        final JPanel topPanel = new NonOpaquePanel(new BorderLayout());
        topPanel.add(title, BorderLayout.WEST);
        final JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        final JLabel settings = new JLabel(AllIcons.General.SearchEverywhereGear);
        new ClickListener(){
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                //showSettings();
                return true;
            }
        }.installOn(settings);
        controls.add(settings, BorderLayout.EAST);
        topPanel.add(controls, BorderLayout.EAST);
        panel.add(myPopupField, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.setBorder(IdeBorderFactory.createEmptyBorder(3, 5, 4, 5));
        searchPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, myPopupField.getTextEditor())
                .setCancelOnClickOutside(true)
                .setModalContext(false)
                .setRequestFocus(true)
                .setCancelCallback(new Computable<Boolean>() {
                    @Override
                    public Boolean compute() {
                        return true;
                    }
                })
                .createPopup();
        searchPopup.showCenteredInCurrentWindow(project);

        myList = new JBList(listModel = new PackageListModel());
        JBScrollPane content = new JBScrollPane(myList) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                Dimension listSize = myList.getPreferredSize();
                if (size.height > listSize.height || myList.getModel().getSize() == 0) {
                    size.height = Math.max(JBUI.scale(30), listSize.height);
                }

                if (size.width < searchPopup.getSize().width) {
                    size.width = searchPopup.getSize().width;
                }

                return size;
            }
        };

        final ComponentPopupBuilder builder = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(content, null);
        listPopup = builder
                .setRequestFocus(false)
                .setCancelKeyEnabled(false)
                .setCancelCallback(new Computable<Boolean>() {
                    @Override
                    public Boolean compute() {
                        return false;
                    }
                })
                .setShowShadow(false)
                .setShowBorder(false)
                .createPopup();
        listPopup.getContent().setBorder(null);
        listPopup.show(new RelativePoint(myPopupField.getParent(), new Point(0, myPopupField.getParent().getHeight())));

        IdeFocusManager focusManager = IdeFocusManager.getInstance(project);
        focusManager.requestFocus(myPopupField.getTextEditor(), true);
    }

    private void initSearchField(final SearchTextField search) {
        final JTextField editor = search.getTextEditor();

        editor.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                final String pattern = editor.getText();
                if (editor.hasFocus()) {
                    //rebuildList(pattern);
                }
            }
        });
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                /*if (mySkipFocusGain) {
                    mySkipFocusGain = false;
                    return;
                }
                String text = "";
                if (myEditor != null) {
                    text = myEditor.getSelectionModel().getSelectedText();
                    text = text == null ? "" : text.trim();
                }

                search.setText(text);
                search.getTextEditor().setForeground(UIUtil.getLabelForeground());
                //titleIndex = new TitleIndexes();
                editor.setColumns(SEARCH_FIELD_COLUMNS);
                myFocusComponent = e.getOppositeComponent();
                //noinspection SSBasedInspection
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final JComponent parent = (JComponent)editor.getParent();
                        parent.revalidate();
                        parent.repaint();
                    }
                });
                //if (myPopup != null && myPopup.isVisible()) {
                //  myPopup.cancel();
                //  myPopup = null;
                //}
                rebuildList(text);*/
            }

            @Override
            public void focusLost(FocusEvent e) {
                /*if ( myPopup instanceof AbstractPopup && myPopup.isVisible()
                        && ((myList == e.getOppositeComponent()) || ((AbstractPopup)myPopup).getPopupWindow() == e.getOppositeComponent())) {
                    return;
                }
                if (myNonProjectCheckBox == e.getOppositeComponent()) {
                    mySkipFocusGain = true;
                    editor.requestFocus();
                    return;
                }
                onFocusLost();*/
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static class PackageListModel extends DefaultListModel {
    }

    private void parsePackageListToPackages(java.util.List<JCenterPackageFile> jCenterPackageFiles) {
        listModel.removeAllElements();
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
            listModel.addElement(p.name+":"+p.versionString);
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

}
