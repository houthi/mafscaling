package com.vgi.mafscaling;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JTable;

public class VEPlus extends VECalc {
    private static final long serialVersionUID = 6485063587743412779L;
    private JTable ectIatTable = null;
    private JTable iatCompTable = null;

    public VEPlus(int tabPlacement) {
        super(tabPlacement);
        setTitleAt(0, "<html><div style='text-align: center;'>V<br>E</div></html>");
        createCompTab();
    }

    private void createCompTab() {
        JPanel compPanel = new JPanel();
        insertTab("<html><div style='text-align: center;'>C<br>o<br>m<br>p<br>s</div></html>", null, compPanel, null, 1);

        GridBagLayout gbl_compPanel = new GridBagLayout();
        gbl_compPanel.columnWidths = new int[]{0, 0};
        gbl_compPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
        gbl_compPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_compPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        compPanel.setLayout(gbl_compPanel);

        ectIatTable = createDataTable(compPanel, "Intake Port Temperature ECT/IAT Estimation Factor", TableRowCount, 2, 0, 0, true, false, true);
        iatCompTable = createDataTable(compPanel, "VE IAT compensation", TableRowCount, 2, 0, 2, true, false, true);
    }
}
