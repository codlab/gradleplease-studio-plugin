package cz.jacktech.gradlepleasestudio;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.awt.RelativePoint;
import cz.jacktech.gradlepleasestudio.ui.SearchDependencyPanel;

import javax.swing.*;

/**
 * Created by toor on 27.6.15.
 */
public class AddGradleDependency extends AnAction {

    private JFrame mDialog;

    public void actionPerformed(AnActionEvent e) {
        PsiFile[] fileList = FilenameIndex.getFilesByName(e.getProject(), "build.gradle", GlobalSearchScope.projectScope(e.getProject()));
        StringBuilder data = new StringBuilder();
        String[] fileNames = new String[fileList.length];
        int i = 0;
        for(PsiFile f : fileList) {
            data.append(f.getVirtualFile().getPath()).append(", ");
            fileNames[i] = f.getVirtualFile().getCanonicalPath();
            i++;
        }
        Utils.showNotification(e.getProject(), MessageType.INFO, data.toString());
        showSearchDialog(e.getProject());
    }

    public void showSearchDialog(Project project) {
        SearchDependencyPanel panel = new SearchDependencyPanel(project);
        mDialog = new JFrame();
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
        mDialog.getContentPane().add(panel);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
        mDialog.requestFocus();
    }



}
