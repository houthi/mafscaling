/*
* Open-Source tuning tools
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.vgi.mafscaling;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class VEColumnsFiltersSelection extends ColumnsFiltersSelection {
    private JScrollPane pane = null;
    JPanel selectionPanel = null;
    private String[] columns = null;
    private boolean isFullOl = false;
    private boolean mafMode = true;
    private JFormattedTextField maxAfrWbFilter = null;
    private JFormattedTextField minAfrWbFilter = null;
    private JFormattedTextField maxAfrClFilter = null;
    private JFormattedTextField minAfrClFilter = null;
    private JSpinner clStatusFilter = null;
    private JSpinner olStatusFilter = null;
    private JFormattedTextField olThrtlMinimumFilter = null;

    public VEColumnsFiltersSelection() {
        this(true);
    }

    public VEColumnsFiltersSelection(boolean mafMode) {
        this.mafMode = mafMode;
    }
    
    public boolean getUserSettings(String[] cols) {
        columns = cols;
        createScrollPane();
        final JComboBox<String> modeSelection = new JComboBox<String>(new String [] { "CL/OL", "Full Time OL" });
        modeSelection.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                isFullOl = (modeSelection.getSelectedIndex() == 1);
                selectionPanel.remove(columnsPanel);
                selectionPanel.remove(filtersPanel);
                createColumnsPanel(columns);
                createFiltersPanel();
                selectionPanel.add(columnsPanel);
                selectionPanel.add(filtersPanel);
                selectionPanel.revalidate();
                selectionPanel.repaint();
                pane.setPreferredSize(new Dimension(windowWidth, windowHeight));
                SwingUtilities.invokeLater(() -> pane.getVerticalScrollBar().setValue(0));
            }
        });

        JComponent[] inputs = new JComponent[] {modeSelection, pane};
        // bring scroll pane to the start
        SwingUtilities.invokeLater(new Runnable() { public void run() { pane.getVerticalScrollBar().setValue(0); } });
        
        do {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, inputs, "Columns / Filters Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
                return false;
        }
        while (!validate());
        
        return true;
    }
    
    protected void createScrollPane() {
        createColumnsPanel(columns);
        createFiltersPanel();
        
        selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.add(columnsPanel);
        selectionPanel.add(filtersPanel);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        pane = new JScrollPane(selectionPanel);
        pane.setPreferredSize(new Dimension(windowWidth, windowHeight));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    protected void addColSelection() {
        addWidebandAFRColSelection();
        addStockAFRColSelection();
        if (!isFullOl) {
            addAFCorrectionColSelection();
            addAFLearningColSelection();
        }
        addRPMColSelection();
        addIATColSelection();
        addThrottleAngleColSelection();
        addManifoldPressureColSelection();
        addFFBColSelection();
        if (!isFullOl)
            addClOlStatusColSelection();
        if (mafMode)
            addMAFColSelection();
        addVEFlowColSelection();
    }
    
    protected void addFilterSelection() {
        addThrottleChangeMaximumFilter();
        thrtlChangeMaxFilter.setValue(Config.getVEThrottleChangeMaxValue());
        addThrottleMinimumFilter();
        thrtlMinimumFilter.setValue(Config.getVEThrottleMinimumValue());
        if (!isFullOl) {
            addOlThrottleMinimumFilter();
            olThrtlMinimumFilter.setText(String.valueOf(Config.getVEOlThrottleMinimumValue()));
        }
        addWbAFRMaximumFilter();
        maxAfrWbFilter.setText(String.valueOf(Config.getVEOlAfrMaximumValue()));
        addWbAFRMinimumFilter();
        minAfrWbFilter.setText(String.valueOf(Config.getVEOlAfrMinimumValue()));
        if (isFullOl) {
            addStockAFRMaximumFilter();
            maxAfrClFilter.setText(String.valueOf(Config.getVEClAfrMaximumValue()));
            addStockAFRMinimumFilter();
            minAfrClFilter.setText(String.valueOf(Config.getVEClAfrMinimumValue()));
            addAfrMpSwitchFilter();
            afrMpSwitchFilter.setText(String.valueOf(Config.getVEWbAfrMpSwitch()));
            addAfrRpmSwitchFilter();
            afrRpmSwitchFilter.setText(String.valueOf(Config.getVEWbAfrRpmSwitch()));
            addAfrSmoothFilter();
            afrSmoothFilter.setText(String.valueOf(Config.getVEWbAfrSmooth()));
        } else {
            addClStatusFilter();
            clStatusFilter.setValue(Config.getVEClStatusValue());
            addOlStatusFilter();
            olStatusFilter.setValue(Config.getVEOlStatusValue());
        }
        addIATMaximumFilter();
        maxIatFilter.setText(String.valueOf(Config.getVEIatMaximumValue()));
        addRPMMinimumFilter();
        minRPMFilter.setText(String.valueOf(Config.getVERPMMinimumValue()));
        addManifoldPressureMinimumFilter();
        minMPFilter.setText(String.valueOf(Config.getVEMPMinimumValue()));
        addFFBMaximumFilter();
        maxFFBFilter.setText(String.valueOf(Config.getFFBMaximumValue()));
        addFFBMinimumFilter();
        minFFBFilter.setText(String.valueOf(Config.getFFBMinimumValue()));
        addWideBandAFRRowOffsetFilter();
        wbo2RowOffsetField.setText(String.valueOf(Config.getWBO2RowOffset()));
        addCellHitCountMinimumFilter();
        minCellHitCountFilter.setText(String.valueOf(Config.getVEMinCellHitCount()));
        addCorrectionAppliedValue();
        correctionAppliedValue.setValue(Config.getVECorrectionAppliedValue());
        
        for (Component c : filtersPanel.getComponents()) {
            if (c instanceof JEditorPane) {
                JEditorPane label = (JEditorPane)c;
                if (label.getText().startsWith("Remove data where Throttle Input is below"))
                    label.setText(label.getText() + " (*** works with AFR Maximum filter)");
                else if (label.getText().startsWith("Remove data where AFR is above"))
                    label.setText(label.getText() + "(*** works with Throttle Input Minimum filter)");
                else if (label.getText().startsWith("Remove data where RPM is below"))
                    label.setText(label.getText() + " (hint: check min RPM in SD table (y-axis)");
                else if (label.getText().startsWith("Remove data where Manifold Pressure is below"))
                    label.setText(label.getText() + " (hint: check min MP in SD table (x-axis)");
            }
        }
    }
    
    protected boolean validate(StringBuffer error) {
        boolean ret = true;
        String value;
        String colName;
        
        // Wideband AFR
        value = wbAfrName.getText().trim();
        colName = wbAfrLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setWidebandAfrColumnName(value);

        // Stock AFR
        value = stockAfrName.getText().trim();
        colName = stockAfrLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setAfrColumnName(value);

        if (!isFullOl) {
            // AFR Learning
            value = afLearningName.getText().trim();
            colName = afLearningLabelText;
            if (value.isEmpty()) {
                ret = false;
                error.append("\"").append(colName).append("\" column must be specified\n");
            }
            else
                Config.setAfLearningColumnName(value);

            // AFR Correction
            value = afCorrectionName.getText().trim();
            colName = afCorrectionLabelText;
            if (value.isEmpty()) {
                ret = false;
                error.append("\"").append(colName).append("\" column must be specified\n");
            }
            else
                Config.setAfCorrectionColumnName(value);
        }
        
        // Engine Speed
        value = rpmName.getText().trim();
        colName = rpmLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setRpmColumnName(value);
        
        // Intake Air Temperature
        value = iatName.getText().trim();
        colName = iatLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setIatColumnName(value);
        
        // Throttle Angle
        value = thrtlAngleName.getText().trim();
        colName = thrtlAngleLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setThrottleAngleColumnName(value);

        // MP
        value = mpName.getText().trim();
        colName = mpLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setMpColumnName(value);

        // FFB
        value = ffbName.getText().trim();
        colName = ffbLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setFinalFuelingBaseColumnName(value);

        if (!isFullOl) {
            // CL/OL Status
            value = clolStatusName.getText().trim();
            colName = clolStatusLabelText;
            if (value.isEmpty()) {
                ret = false;
                error.append("\"").append(colName).append("\" column must be specified\n");
            }
            else
                Config.setClOlStatusColumnName(value);
        }

        if (mafMode) {
            // MAF
            value = mafName.getText().trim();
            colName = mafLabelText;
            if (value.isEmpty()) {
                ret = false;
                error.append("\"").append(colName).append("\" column must be specified\n");
            }
            else
                Config.setMassAirflowColumnName(value);
        } else {
            Config.setMassAirflowColumnName(Config.NO_NAME);
        }

        // VE Flow
        value = veFlowName.getText().trim();
        colName = veFlowLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setVEFlowColumnName(value);
        
        // Throttle Change % Maximum
        Config.setVEThrottleChangeMaxValue(Integer.valueOf(thrtlChangeMaxFilter.getValue().toString()));

        // Throttle Minimum Input
        Config.setVEThrottleMinimumValue(Integer.valueOf(thrtlMinimumFilter.getText()));
        if (!isFullOl)
            Config.setVEOlThrottleMinimumValue(Integer.valueOf(olThrtlMinimumFilter.getText()));

        // AFR filters
        Config.setVEOlAfrMaximumValue(Double.valueOf(maxAfrWbFilter.getText()));
        Config.setVEOlAfrMinimumValue(Double.valueOf(minAfrWbFilter.getText()));
        if (isFullOl) {
            Config.setVEClAfrMaximumValue(Double.valueOf(maxAfrClFilter.getText()));
            Config.setVEClAfrMinimumValue(Double.valueOf(minAfrClFilter.getText()));
        }

        // WBO2 Row Offset
        Config.setWBO2RowOffset(Integer.valueOf(wbo2RowOffsetField.getText()));

        if (isFullOl) {
            Config.setVEWbAfrMpSwitch(Double.valueOf(afrMpSwitchFilter.getText()));
            Config.setVEWbAfrRpmSwitch(Integer.valueOf(afrRpmSwitchFilter.getText()));
            Config.setVEWbAfrSmooth(Double.valueOf(afrSmoothFilter.getText()));
        } else {
            Config.setVEClStatusValue(Integer.valueOf(clStatusFilter.getValue().toString()));
            Config.setVEOlStatusValue(Integer.valueOf(olStatusFilter.getValue().toString()));
        }
        
        // IAT filter
        Config.setVEIatMaximumValue(Double.valueOf(maxIatFilter.getText()));
        
        // RPM Minimum
        Config.setVERPMMinimumValue(Integer.valueOf(minRPMFilter.getText()));
        
        // MP Minimum
        Config.setVEMPMinimumValue(Double.valueOf(minMPFilter.getText()));
        
        // FFB filters
        Config.setFFBMaximumValue(Double.valueOf(maxFFBFilter.getText()));
        Config.setFFBMinimumValue(Double.valueOf(minFFBFilter.getText()));
        
        // Minimum Cell Hit Count Filter
        Config.setVEMinCellHitCount(Integer.valueOf(minCellHitCountFilter.getText()));
        
        // Correction applied
        Config.setVECorrectionAppliedValue(Integer.valueOf(correctionAppliedValue.getValue().toString()));

        Config.veFullTimeOl(isFullOl);

        return ret;
    }

    private void addWbAFRMaximumFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Remove data where WB AFR is above the specified maximum");
        addLabel(filtersPanel, ++filtrow, "WB AFR Maximum");
        maxAfrWbFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "maxafrwb");
    }

    private void addWbAFRMinimumFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Remove data where WB AFR is below the specified minimum");
        addLabel(filtersPanel, ++filtrow, "WB AFR Minimum");
        minAfrWbFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minafrwb");
    }

    private void addStockAFRMaximumFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Remove data where Stock AFR is above the specified maximum");
        addLabel(filtersPanel, ++filtrow, "Stock AFR Maximum");
        maxAfrClFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "maxafrstock");
    }

    private void addStockAFRMinimumFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Remove data where Stock AFR is below the specified minimum");
        addLabel(filtersPanel, ++filtrow, "Stock AFR Minimum");
        minAfrClFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minafrstock");
    }

    private void addClStatusFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Logged value indicating Closed Loop");
        addLabel(filtersPanel, ++filtrow, "CL Status Value");
        clStatusFilter = addSpinnerFilter(filtrow, Config.getVEClStatusValue(), -1, 10, 1);
        addDefaultButton(filtrow, "clstatus");
    }

    private void addOlStatusFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Logged value indicating Open Loop");
        addLabel(filtersPanel, ++filtrow, "OL Status Value");
        olStatusFilter = addSpinnerFilter(filtrow, Config.getVEOlStatusValue(), -1, 10, 1);
        addDefaultButton(filtrow, "olstatus");
    }

    private void addOlThrottleMinimumFilter() {
        addNote(filtersPanel, ++filtrow, 3, "Remove OL data where Throttle Input is below the specified minimum");
        addLabel(filtersPanel, ++filtrow, "OL Throttle Minimum");
        olThrtlMinimumFilter = addTextFilter(filtrow, doubleFmt);
        addDefaultButton(filtrow, "minolthrtl");
    }
    
    protected boolean processDefaultButton(ActionEvent e) {
        if ("thrtlchange".equals(e.getActionCommand()))
            thrtlChangeMaxFilter.setValue(Integer.valueOf(Config.DefaultVEThrottleChangeMax));
        else if ("minrpm".equals(e.getActionCommand()))
            minRPMFilter.setText(Config.DefaultRPMMinimum);
        else if ("minmp".equals(e.getActionCommand()))
            minMPFilter.setText(Config.DefaultMPMinimum);
        else if ("maxiat".equals(e.getActionCommand()))
            maxIatFilter.setText(Config.DefaultVEIATMaximum);
        else if ("maxffb".equals(e.getActionCommand()))
            maxFFBFilter.setText(Config.DefaultFFBMaximum);
        else if ("minffb".equals(e.getActionCommand()))
            minFFBFilter.setText(Config.DefaultFFBMinimum);
        else if ("clstatus".equals(e.getActionCommand()))
            clStatusFilter.setValue(Integer.valueOf(Config.DefaultVEClStatusValue));
        else if ("olstatus".equals(e.getActionCommand()))
            olStatusFilter.setValue(Integer.valueOf(Config.DefaultVEOlStatusValue));
        else if ("minolthrtl".equals(e.getActionCommand()))
            olThrtlMinimumFilter.setValue(Integer.valueOf(Config.DefaultVEOlThrottleMinimum));
        else if ("minthrtl".equals(e.getActionCommand()))
            thrtlMinimumFilter.setValue(Integer.valueOf(Config.DefaultVEThrottleMinimum));
        else if ("maxafrwb".equals(e.getActionCommand()))
            maxAfrWbFilter.setText(Config.DefaultVEOlAfrMaximum);
        else if ("minafrwb".equals(e.getActionCommand()))
            minAfrWbFilter.setText(Config.DefaultVEOlAfrMinimum);
        else if ("maxafrstock".equals(e.getActionCommand()))
            maxAfrClFilter.setText(Config.DefaultVEClAfrMaximum);
        else if ("minafrstock".equals(e.getActionCommand()))
            minAfrClFilter.setText(Config.DefaultVEClAfrMinimum);
        else if ("wbo2offset".equals(e.getActionCommand()))
            wbo2RowOffsetField.setText(Config.DefaultWBO2RowOffset);
        else if ("wbswitchmp".equals(e.getActionCommand()))
            afrMpSwitchFilter.setText(Config.DefaultVEWbAfrMpSwitch);
        else if ("wbswitchrpm".equals(e.getActionCommand()))
            afrRpmSwitchFilter.setText(Config.DefaultVEWbAfrRpmSwitch);
        else if ("wbsmooth".equals(e.getActionCommand()))
            afrSmoothFilter.setText(Config.DefaultVEWbAfrSmooth);
        else if ("minhitcnt".equals(e.getActionCommand()))
            minCellHitCountFilter.setText(Config.DefaultVEMinCellHitCount);
        else if ("corrapply".equals(e.getActionCommand()))
            correctionAppliedValue.setValue(Integer.valueOf(Config.DefaultCorrectionAppliedValue));
        else
            return false;
        return true;
    }
}
