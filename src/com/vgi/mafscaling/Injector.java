package com.vgi.mafscaling;

import java.awt.Color;
import java.awt.Cursor;
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
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.vgi.mafscaling.NumberFormatRenderer;

public class Injector extends ACompCalc {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Injector.class);

    private static final String voltAxisName = "Voltage";
    private static final String errAxisName = "Err %";

    private int minCellHitCount = Config.getCLMinCellHitCount();
    private int logBattColIdx = -1;
    private int logPwColIdx = -1;
    private int logLtftColIdx = -1;
    private int logStftColIdx = -1;
    private int logWbColIdx = -1;
    private int logCmdAfrColIdx = -1;

    private String[] logColumns = new String[] {"Voltage","PW","Err"};

    private HashMap<Double,List<Double>> errData = null;
    private HashMap<Double,List<Double>> pwData = null;
    private ArrayList<Double> allErr = new ArrayList<Double>();

    private JTable scaleTable = null;

    public Injector(int tabPlacement) {
        super(tabPlacement);
        origTableName = "Fuel Injector Latency";
        newTableName = "New Injector Latency";
        corrTableName = "Latency Error %";
        corrCountTableName = "Latency Count";
        x3dAxisName = "Voltage";
        y3dAxisName = "Pulse Width";
        z3dAxisName = "Error %";
        initialize(logColumns);
    }

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
        gbl_cntlPanel.columnWidths = new int[]{0,0,0,0};
        gbl_cntlPanel.rowHeights = new int[]{0};
        gbl_cntlPanel.columnWeights = new double[]{0.0,0.0,0.0,1.0};
        gbl_cntlPanel.rowWeights = new double[]{0};
        cntlPanel.setLayout(gbl_cntlPanel);

        addButton(cntlPanel,0,"Load Log","loadlog",GridBagConstraints.WEST);
        addButton(cntlPanel,1,"Clear Data","clearlog",GridBagConstraints.WEST);
        addCheckBox(cntlPanel,2,"Hide Log Table","hidelogtable");
        addButton(cntlPanel,3,"GO","go",GridBagConstraints.EAST);
    }

    protected void formatTable(JTable table) {
        Format[][] formatMatrix = { { new DecimalFormat("0.0"), new DecimalFormat("0.00") } };
        NumberFormatRenderer renderer = (NumberFormatRenderer)table.getDefaultRenderer(Object.class);
        renderer.setFormats(formatMatrix);
    }

    protected void createGraphTab() {
        rbGroup = new ButtonGroup();
        JPanel plotPanel = new JPanel();
        add(plotPanel, "<html><div style='text-align: center;'>C<br>h<br>a<br>r<br>t</div></html>");
        GridBagLayout gbl_plotPanel = new GridBagLayout();
        gbl_plotPanel.columnWidths = new int[]{0};
        gbl_plotPanel.rowHeights = new int[]{0};
        gbl_plotPanel.columnWeights = new double[]{1.0};
        gbl_plotPanel.rowWeights = new double[]{1.0};
        plotPanel.setLayout(gbl_plotPanel);

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Error");
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot("Injector Error", voltAxisName, errAxisName, dataset, PlotOrientation.VERTICAL, false, false, false);
        XYPlot plot = (XYPlot)chart.getPlot();
        XYDotRenderer dot = new XYDotRenderer();
        dot.setDotHeight(3);
        dot.setDotWidth(3);
        plot.setRenderer(dot);
        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        chartPanel = new ChartPanel(chart);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = insets3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        plotPanel.add(chartPanel, gbc);
    }

    protected void loadLogFile() {
        boolean displayDialog = true;
        File[] files = fileChooser.getSelectedFiles();
        for (File file : files) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsoluteFile()), Config.getEncoding()));
                String line = null;
                String[] elements = null;
                while ((line = br.readLine()) != null && (elements = line.trim().split(Utils.fileFieldSplitter, -1)) != null && elements.length < 2)
                    continue;
                getColumnsFilters(elements);
                boolean resetColumns = false;
                if (logBattColIdx >= 0) {
                    if (displayDialog) {
                        int rc = JOptionPane.showOptionDialog(null, "Would you like to reset column names or filter values?", "Columns/Filters Reset", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionButtons, optionButtons[0]);
                        if (rc == 0)
                            resetColumns = true;
                        else if (rc == 2)
                            displayDialog = false;
                    }
                }
                if (resetColumns || logBattColIdx < 0 || logPwColIdx < 0 || logLtftColIdx < 0 || logStftColIdx < 0) {
                    ColumnsFiltersSelection selectionWindow = new InjectorColumnsFiltersSelection();
                    if (!selectionWindow.getUserSettings(elements) || !getColumnsFilters(elements))
                        return;
                }

                String[] flds;
                int row = getLogTableEmptyRow();
                clearRunTables();
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try {
                    while ((line = br.readLine()) != null) {
                        flds = line.trim().split(Utils.fileFieldSplitter, -1);
                        double batt = Double.valueOf(flds[logBattColIdx]);
                        double pw = Double.valueOf(flds[logPwColIdx]);
                        double err;
                        if (logWbColIdx >= 0 && logCmdAfrColIdx >= 0) {
                            double wb = Double.valueOf(flds[logWbColIdx]);
                            double cmd = Double.valueOf(flds[logCmdAfrColIdx]);
                            if (cmd == 0)
                                continue;
                            err = (wb - cmd) / cmd * 100.0;
                        } else {
                            double stft = Double.valueOf(flds[logStftColIdx]);
                            double ltft = Double.valueOf(flds[logLtftColIdx]);
                            err = stft + ltft;
                        }
                        Utils.ensureRowCount(row + 1, logDataTable);
                        logDataTable.setValueAt(batt, row, 0);
                        logDataTable.setValueAt(pw, row, 1);
                        logDataTable.setValueAt(err, row, 2);
                        row++;
                    }
                } finally {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            } catch (Exception e) {
                logger.error(e);
                JOptionPane.showMessageDialog(null, e, "Error loading data", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (br != null) {
                    try { br.close(); } catch (IOException e) { }
                }
            }
        }
    }

    private boolean getColumnsFilters(String[] elements) {
        boolean ret = true;
        List<String> columns = Arrays.asList(elements);
        logBattColIdx = columns.indexOf(Config.getBatteryVoltageColumnName());
        logPwColIdx = columns.indexOf(Config.getInjectorPulseWidthColumnName());
        logLtftColIdx = columns.indexOf(Config.getAfLearningColumnName());
        logStftColIdx = columns.indexOf(Config.getAfCorrectionColumnName());
        logWbColIdx = columns.indexOf(Config.getWidebandAfrColumnName());
        logCmdAfrColIdx = columns.indexOf(Config.getCommandedAfrColumnName());
        if (logBattColIdx == -1) { Config.setBatteryVoltageColumnName(Config.NO_NAME); ret = false; }
        if (logPwColIdx == -1)   { Config.setInjectorPulseWidthColumnName(Config.NO_NAME); ret = false; }
        if (logLtftColIdx == -1)  { Config.setAfLearningColumnName(Config.NO_NAME); ret = false; }
        if (logStftColIdx == -1)  { Config.setAfCorrectionColumnName(Config.NO_NAME); ret = false; }
        return ret;
    }

    protected boolean processLog() {
        errData = new HashMap<Double,List<Double>>();
        pwData = new HashMap<Double,List<Double>>();
        allErr.clear();
        try {
            for (int i = 0; i < logDataTable.getRowCount(); ++i) {
                Object vObj = logDataTable.getValueAt(i,0);
                Object pwObj = logDataTable.getValueAt(i,1);
                Object errObj = logDataTable.getValueAt(i,2);
                if (vObj == null || pwObj == null || errObj == null)
                    continue;
                String vStr = vObj.toString();
                String pwStr = pwObj.toString();
                String errStr = errObj.toString();
                if (vStr.isEmpty() || pwStr.isEmpty() || errStr.isEmpty())
                    continue;
                double volt = Double.valueOf(vStr);
                double pw = Double.valueOf(pwStr);
                double err = Double.valueOf(errStr);
                double key = xAxisArray.get(Utils.closestValueIndex(volt, xAxisArray));
                errData.computeIfAbsent(key,k->new ArrayList<Double>()).add(err);
                pwData.computeIfAbsent(key,k->new ArrayList<Double>()).add(pw);
                allErr.add(err);
            }
            return true;
        } catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error processing data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    protected boolean displayData() {
        try {
            XYSeries series = new XYSeries("Error");
            for (int i = 1; i < xAxisArray.size() + 1; ++i) {
                newTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                corrTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                corrCountTable.setValueAt(origTable.getValueAt(0, i), 0, i);
                double volt = xAxisArray.get(i-1);
                List<Double> errs = errData.get(volt);
                List<Double> pws = pwData.get(volt);
                double oldLat = 0;
                if (origTable.getRowCount() > 1)
                    oldLat = Double.valueOf(origTable.getValueAt(1, i).toString());
                if (errs != null && pws != null && errs.size() >= minCellHitCount) {
                    double meanErr = Utils.mean(errs);
                    double meanPw = Utils.mean(pws);
                    double delta = -(meanErr/100.0)*(meanPw - oldLat);
                    double newLat = oldLat + delta;
                    newTable.setValueAt(newLat, 1, i);
                    corrTable.setValueAt(meanErr,1,i);
                    corrCountTable.setValueAt(errs.size(),1,i);
                    series.add(volt, meanErr);
                } else {
                    newTable.setValueAt(oldLat,1,i);
                    if (errs != null) {
                        corrTable.setValueAt(Utils.mean(errs),1,i);
                        corrCountTable.setValueAt(errs.size(),1,i);
                    }
                }
            }
            chartPanel.getChart().getXYPlot().setDataset(new XYSeriesCollection(series));
            if (!allErr.isEmpty() && scaleTable != null) {
                double meanErr = Utils.mean(allErr);
                double scaleOld = Double.valueOf(scaleTable.getValueAt(0,0).toString());
                double scaleNew = scaleOld * (1 + meanErr/100.0);
                scaleTable.setValueAt(scaleNew,0,0);
            }
            Utils.colorTable(newTable);
            return true;
        } catch (Exception e) {
            logger.error(e);
            JOptionPane.showMessageDialog(null, e, "Error displaying data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    protected String usage() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.vgi.mafscaling.injector");
        return bundle.getString("usage");
    }

    public void actionPerformed(ActionEvent e) {
        if (checkActionPerformed(e))
            return;
    }

    protected void createDataTables(JPanel panel) {
        origTable = createDataTable(panel, origTableName, 0, true);
        newTable = createDataTable(panel, newTableName, 2, false);
        corrTable = createDataTable(panel, corrTableName, 4, false);
        corrCountTable = createDataTable(panel, corrCountTableName, 6, false);
        scaleTable = createDataTable(panel, "Fuel Injector Scale",1,1,8,0,false,true,true);
    }
}
