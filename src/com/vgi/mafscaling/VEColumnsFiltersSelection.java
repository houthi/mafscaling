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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class VEColumnsFiltersSelection extends ColumnsFiltersSelection {
    private JScrollPane pane = null;
    JPanel selectionPanel = null;
    private String[] columns = null;
    
    public boolean getUserSettings(String[] cols) {
        columns = cols;
        createScrollPane();

        JComponent[] inputs = new JComponent[] {pane};
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
        addAFCorrectionColSelection();
        addAFLearningColSelection();
        addRPMColSelection();
        addIATColSelection();
        addThrottleAngleColSelection();
        addManifoldPressureColSelection();
        addFFBColSelection();
        addClOlStatusColSelection();
        addMAFColSelection();
        addVEFlowColSelection();
    }
    
    protected void addFilterSelection() {
        addThrottleChangeMaximumFilter();
        thrtlChangeMaxFilter.setValue(Config.getVEThrottleChangeMaxValue());
        addThrottleMinimumFilter();
        thrtlMinimumFilter.setValue(Config.getVEThrottleMinimumValue());
        addAFRMaximumFilter();
        maxAfrFilter.setText(String.valueOf(Config.getVEOlAfrMaximumValue()));
        addAFRMinimumFilter();
        minAfrFilter.setText(String.valueOf(Config.getVEAfrMinimumValue()));
        addCLOLStatusFilter();
        clolStatusFilter.setValue(Config.getVEClOlStatusValue());
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
                else if (label.getText().equals(clolStatusLabelText))
                    label.setText(clolStatusLabelText + " *");
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

        // CL/OL Status
        value = clolStatusName.getText().trim();
        colName = clolStatusLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setClOlStatusColumnName(value);

        // MAF
        value = mafName.getText().trim();
        colName = mafLabelText;
        if (value.isEmpty()) {
            ret = false;
            error.append("\"").append(colName).append("\" column must be specified\n");
        }
        else
            Config.setMassAirflowColumnName(value);

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

        // AFR Maximum
        Config.setVEOlAfrMaximumValue(Double.valueOf(maxAfrFilter.getText()));

        // AFR Minimum
        Config.setVEAfrMinimumValue(Double.valueOf(minAfrFilter.getText()));

        // WBO2 Row Offset
        Config.setWBO2RowOffset(Integer.valueOf(wbo2RowOffsetField.getText()));
        
        // CL/OL Status
        Config.setVEClOlStatusValue(Integer.valueOf(clolStatusFilter.getValue().toString()));
        
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
        
        return ret;
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
        else if ("clolstatus".equals(e.getActionCommand()))
            clolStatusFilter.setValue(Integer.valueOf(Config.DefaultClOlStatusValue));
        else if ("minthrtl".equals(e.getActionCommand()))
            thrtlMinimumFilter.setValue(Integer.valueOf(Config.DefaultVEThrottleMinimum));
        else if ("maxafr".equals(e.getActionCommand()))
            maxAfrFilter.setText(Config.DefaultVEOlAfrMaximum);
        else if ("minafr".equals(e.getActionCommand()))
            minAfrFilter.setText(Config.DefaultVEAfrMinimum);
        else if ("wbo2offset".equals(e.getActionCommand()))
            wbo2RowOffsetField.setText(Config.DefaultWBO2RowOffset);
        else if ("minhitcnt".equals(e.getActionCommand()))
            minCellHitCountFilter.setText(Config.DefaultVEMinCellHitCount);
        else if ("corrapply".equals(e.getActionCommand()))
            correctionAppliedValue.setValue(Integer.valueOf(Config.DefaultCorrectionAppliedValue));
        else
            return false;
        return true;
    }
}
