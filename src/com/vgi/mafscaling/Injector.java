/*
* Open-Source tuning tools
*/
package com.vgi.mafscaling;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import org.apache.log4j.Logger;

/**
 * Simple injector tuning tab. Allows pasting current injector latency table,
 * loading log files and calculating latency correction per voltage as well as
 * overall injector scale factor.
 */
public class Injector extends ACompCalc {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Injector.class);

    private static final String voltAxisName = "Voltage";
    private static final String errAxisName = "Fueling Error %";

    private int logRpmIdx = -1;
    private int logLoadIdx = -1;
    private int logStftIdx = -1;
    private int logLtftIdx = -1;
    private int logVoltIdx = -1;
    private int logPwIdx = -1;

    private double rpmMin = Config.getRPMMinimumValue();
    private double rpmMax = Config.getRPMMaximumValue();
    private double loadMin = Config.getLoadMinimumValue();

    private String[] logColumns = new String[] { voltAxisName, "PW", errAxisName };
    private Map<Double, List<Double>> voltErr = null;
    private Map<Double, List<Double>> voltPw = null;
    private ArrayList<Double> voltList = new ArrayList<Double>();
    private ArrayList<Double> errList = new ArrayList<Double>();
    private double meanErr = 0;

    public Injector(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Current Injector Latency";
        newTableName = "New Injector Latency";
        corrTableName = "Latency Correction";
        corrCountTableName = "Scale Factor";
        x3dAxisName = voltAxisName;
        y3dAxisName = errAxisName;
        z3dAxisName = "";
        initialize(logColumns);
    }

    ////////////////////////////////////////////////////////////////////////////
    // DATA TAB
    ////////////////////////////////////////////////////////////////////////////

    protected void createControlPanel(JPanel dataPanel) {
        JPanel cntlPanel = new JPanel();
        GridBagConstraints gbl_ctrlPanel = new GridBagConstraints();
        gbl_ctrlPanel.insets = insets3;
        gbl_ctrlPanel.anchor = GridBagConstraints.NORTH;
        gbl_ctrlPanel.fill = GridBagConstraints.HORIZONTAL;
        gbl_ctrlPanel.gridx = 0;
        gbl_ctrlPanel.gridy = 0;
        gbl_ctrlPanel.weightx = 1.0;
        gbl_ctrlPanel.gridwidth = 2;
        dataPanel.add(cntlPanel, gbl_ctrlPanel);

        GridBagLayout gbl_cntlPanel = new GridBagLayout();
        gbl_cntlPanel.columnWidths = new int[]{0,0,0,0,0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0,0.0,0.0,0.0,1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addButton(cntlPanel, 0, "Load Log", "loadlog", GridBagConstraints.WEST);
        addButton(cntlPanel, 1, "Clear Run Data", "clearlog", GridBagConstraints.WEST);
        addButton(cntlPanel, 2, "Clear All", "clearall", GridBagConstraints.WEST);
        addCheckBox(cntlPanel, 3, "Hide Log Table", "hidelogtable");
        addButton(cntlPanel, 4, "GO", "go", GridBagConstraints.EAST);
    }

    protected void createDataTables(JPanel panel) {
        origTable = createDataTable(panel, origTableName, 12, 2, 0, 0, true, true, true);
        newTable = createDataTable(panel, newTableName, 12, 2, 2, 0, false, true, true);
        corrTable = createDataTable(panel, corrTableName, 12, 2, 4, 0, false, true, true);
        corrCountTable = createDataTable(panel, corrCountTableName, 1, 2, 6, 0, false, false, false);
    }

    protected void formatTable(JTable table) {
        Format[][] formatMatrix = { { new DecimalFormat("0.00"), new DecimalFormat("0.000") } };
        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
    }

    ////////////////////////////////////////////////////////////////////////////
    // CHART TAB
    ////////////////////////////////////////////////////////////////////////////

    protected void createGraphTab() {
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[] {0};
        gbl_plotPanel.rowHeights = new int[] {0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{1.0};
        plotPanel.setLayout(gbl_plotPanel);
        createChart(plotPanel, voltAxisName, errAxisName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // USAGE TAB
    ////////////////////////////////////////////////////////////////////////////

    protected String usage() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.vgi.mafscaling.injector");
        return bundle.getString("usage");
    }

    ////////////////////////////////////////////////////////////////////////////
    // LOG LOADING AND PROCESSING
    ////////////////////////////////////////////////////////////////////////////

    protected void loadLogFile() {
        fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this))
            return;
        File[] files = fileChooser.getSelectedFiles();
        clearLogDataTables();
        for (File file : files) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
                String header = br.readLine();
                if (header == null)
                    continue;
                String[] cols = header.trim().split(Utils.fileFieldSplitter, -1);
                InjectorColumnsFiltersSelection sel = new InjectorColumnsFiltersSelection();
                if (!sel.getUserSettings(cols))
                    return;
                List<String> colList = Arrays.asList(cols);
                logRpmIdx = colList.indexOf(Config.getRpmColumnName());
                logLoadIdx = colList.indexOf(Config.getLoadColumnName());
                logStftIdx = colList.indexOf(Config.getAfCorrectionColumnName());
                logLtftIdx = colList.indexOf(Config.getAfLearningColumnName());
                logVoltIdx = colList.indexOf(Config.getBatteryVoltageColumnName());
                logPwIdx = colList.indexOf(Config.getInjectorPulseWidthColumnName());
                String line;
                int row = getLogTableEmptyRow();
                while ((line = br.readLine()) != null) {
                    String[] flds = line.trim().split(Utils.fileFieldSplitter, -1);
                    if (flds.length <= Math.max(logPwIdx, logVoltIdx))
                        continue;
                    double rpm = Utils.parseValue(flds[logRpmIdx]);
                    if (rpm < rpmMin || rpm > rpmMax)
                        continue;
                    double load = Utils.parseValue(flds[logLoadIdx]);
                    if (load < loadMin)
                        continue;
                    double stft = Utils.parseValue(flds[logStftIdx]);
                    double ltft = Utils.parseValue(flds[logLtftIdx]);
                    double err = stft + ltft;
                    double volt = Utils.parseValue(flds[logVoltIdx]);
                    double pw = Utils.parseValue(flds[logPwIdx]);
                    Utils.ensureRowCount(row + 1, logDataTable);
                    logDataTable.setValueAt(volt, row, 0);
                    logDataTable.setValueAt(pw, row, 1);
                    logDataTable.setValueAt(err, row, 2);
                    row += 1;
                }
            }
            catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            finally {
                if (br != null) {
                    try { br.close(); } catch (IOException e) { logger.error(e); }
                }
            }
        }
    }

    protected boolean processLog() {
        try {
            voltErr = new HashMap<Double, List<Double>>();
            voltPw = new HashMap<Double, List<Double>>();
            voltList.clear();
            errList.clear();
            meanErr = 0;
            int cnt = 0;
            for (int i = 0; i < logDataTable.getRowCount(); ++i) {
                Object vObj = logDataTable.getValueAt(i, 0);
                Object pwObj = logDataTable.getValueAt(i, 1);
                Object eObj = logDataTable.getValueAt(i, 2);
                if (vObj == null || pwObj == null || eObj == null)
                    continue;
                String vStr = vObj.toString();
                String pwStr = pwObj.toString();
                String eStr = eObj.toString();
                if (vStr.isEmpty() || pwStr.isEmpty() || eStr.isEmpty())
                    continue;
                double v = Double.valueOf(vStr);
                double pw = Double.valueOf(pwStr);
                double err = Double.valueOf(eStr);
                voltList.add(v);
                errList.add(err);
                double bin = xAxisArray.get(Utils.closestValueIndex(v, xAxisArray));
                List<Double> l = voltErr.get(bin);
                if (l == null) { l = new ArrayList<Double>(); voltErr.put(bin, l); }
                l.add(err);
                l = voltPw.get(bin);
                if (l == null) { l = new ArrayList<Double>(); voltPw.put(bin, l); }
                l.add(pw);
                meanErr += err;
                cnt++;
            }
            if (cnt > 0)
                meanErr /= cnt;
            return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    protected boolean displayData() {
        try {
            for (int i = 1; i < origTable.getColumnCount(); ++i) {
                newTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                corrTable.setValueAt(origTable.getValueAt(0, i), 0, i);
            }
            for (int j = 1; j < origTable.getRowCount(); ++j) {
                newTable.setValueAt(origTable.getValueAt(j, 0), j, 0);
                corrTable.setValueAt(origTable.getValueAt(j, 0), j, 0);
            }
            for (int i = 1; i < xAxisArray.size() + 1; ++i) {
                double volt = xAxisArray.get(i - 1);
                String valStr = origTable.getValueAt(1, i).toString();
                double lat = valStr.isEmpty() ? 0 : Double.valueOf(valStr);
                List<Double> errs = voltErr.get(volt);
                List<Double> pws = voltPw.get(volt);
                if (errs != null && pws != null && errs.size() > 0) {
                    double err = Utils.mean(errs);
                    double pw = Utils.mean(pws);
                    double delta = -(err / 100.0) * pw;
                    newTable.setValueAt(String.format("%.3f", lat + delta), 1, i);
                    corrTable.setValueAt(String.format("%.3f", delta), 1, i);
                } else {
                    newTable.setValueAt(valStr, 1, i);
                    corrTable.setValueAt("", 1, i);
                }
            }
            Utils.colorTable(newTable);
            corrCountTable.setValueAt(String.format("%.5f", 1 + meanErr / 100.0), 1, 0);
            plotRel2dChartData(voltAxisName, voltList, errAxisName, errList);
            return true;
        }
        catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
            return;
        if ("corr".equals(e.getActionCommand())) {
            JRadioButton radioButton = (JRadioButton)e.getSource();
            if (radioButton.isSelected())
                plotRel2dChartData(voltAxisName, voltList, errAxisName, errList);
            else
                clear2dChartData();
        }
    }
}
