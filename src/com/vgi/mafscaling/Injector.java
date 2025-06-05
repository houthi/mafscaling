/*
* Open-Source tuning tools
*/
package com.vgi.mafscaling;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Injector extends JTabbedPane implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JTable latencyTable;
    private JTable scaleTable;
    private JButton loadButton;

    public Injector(int tabPlacement) {
        super(tabPlacement);
        createDataTab();
        createUsageTab();
    }

    private void createDataTab() {
        JPanel panel = new JPanel();
        add(panel, "<html><div style='text-align: center;'>D<br>a<br>t<br>a</div></html>");
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[]{0};
        gbl.rowHeights = new int[]{0,0,0};
        gbl.columnWeights = new double[]{1.0};
        gbl.rowWeights = new double[]{0.0,0.0,1.0};
        panel.setLayout(gbl);

        loadButton = new JButton("Load Log");
        loadButton.addActionListener(this);
        GridBagConstraints gbc_btn = new GridBagConstraints();
        gbc_btn.insets = new Insets(3,3,3,3);
        gbc_btn.anchor = GridBagConstraints.WEST;
        gbc_btn.gridx = 0;
        gbc_btn.gridy = 0;
        panel.add(loadButton, gbc_btn);

        latencyTable = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"Voltage","Latency Corr"}));
        JScrollPane sp1 = new JScrollPane(latencyTable);
        GridBagConstraints gbc_tbl = new GridBagConstraints();
        gbc_tbl.insets = new Insets(3,3,3,3);
        gbc_tbl.fill = GridBagConstraints.BOTH;
        gbc_tbl.gridx = 0;
        gbc_tbl.gridy = 1;
        panel.add(sp1, gbc_tbl);

        scaleTable = new JTable(new DefaultTableModel(new Object[][]{{""}}, new String[]{"Injector Scale"}));
        JScrollPane sp2 = new JScrollPane(scaleTable);
        GridBagConstraints gbc_tbl2 = new GridBagConstraints();
        gbc_tbl2.insets = new Insets(3,3,3,3);
        gbc_tbl2.fill = GridBagConstraints.HORIZONTAL;
        gbc_tbl2.gridx = 0;
        gbc_tbl2.gridy = 2;
        panel.add(sp2, gbc_tbl2);
    }

    private void createUsageTab() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.vgi.mafscaling.injector");
        javax.swing.JTextPane usageText = new javax.swing.JTextPane();
        usageText.setContentType("text/html");
        usageText.setText(bundle.getString("usage"));
        usageText.setEditable(false);
        usageText.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(usageText);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(sp, "<html><div style='text-align: center;'>U<br>s<br>a<br>g<br>e</div></html>");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadButton)
            loadLogFile();
    }

    private void loadLogFile() {
        if (JFileChooser.APPROVE_OPTION != FCTabbedPane.fileChooser.showOpenDialog(this))
            return;
        File file = FCTabbedPane.fileChooser.getSelectedFile();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Config.getEncoding()));
            String header = br.readLine();
            if (header == null)
                return;
            String[] cols = header.split(Utils.fileFieldSplitter, -1);
            InjectorColumnsFiltersSelection sel = new InjectorColumnsFiltersSelection();
            if (!sel.getUserSettings(cols))
                return;
            Map<Integer,List<Double>> voltErr = new HashMap<Integer,List<Double>>();
            double sumErr = 0; int cnt = 0;
            String line;
            int rpmIdx = java.util.Arrays.asList(cols).indexOf(Config.getRpmColumnName());
            int loadIdx = java.util.Arrays.asList(cols).indexOf(Config.getLoadColumnName());
            int stftIdx = java.util.Arrays.asList(cols).indexOf(Config.getAfCorrectionColumnName());
            int ltftIdx = java.util.Arrays.asList(cols).indexOf(Config.getAfLearningColumnName());
            int voltIdx = java.util.Arrays.asList(cols).indexOf(Config.getBatteryVoltageColumnName());
            int pwIdx = java.util.Arrays.asList(cols).indexOf(Config.getInjectorPulseWidthColumnName());
            while ((line = br.readLine()) != null) {
                String[] flds = line.split(Utils.fileFieldSplitter, -1);
                if (flds.length <= Math.max(pwIdx, voltIdx))
                    continue;
                double rpm = Utils.parseValue(flds[rpmIdx]);
                double load = Utils.parseValue(flds[loadIdx]);
                if (rpm < Config.getRPMMinimumValue() || rpm > Config.getRPMMaximumValue())
                    continue;
                if (load < Config.getLoadMinimumValue())
                    continue;
                double stft = Utils.parseValue(flds[stftIdx]);
                double ltft = Utils.parseValue(flds[ltftIdx]);
                double err = stft + ltft;
                double volt = Utils.parseValue(flds[voltIdx]);
                int vbin = (int)Math.round(volt);
                List<Double> l = voltErr.get(vbin);
                if (l == null) {
                    l = new ArrayList<Double>();
                    voltErr.put(vbin, l);
                }
                l.add(err);
                sumErr += err;
                cnt++;
            }
            DefaultTableModel model = (DefaultTableModel)latencyTable.getModel();
            model.setRowCount(0);
            for (Integer v: voltErr.keySet()) {
                List<Double> vals = voltErr.get(v);
                double mean = 0;
                for (Double d: vals) mean += d;
                mean /= vals.size();
                model.addRow(new Object[]{v.doubleValue(), String.format("%.3f", -mean/100.0)});
            }
            if (cnt > 0) {
                double mean = sumErr / cnt;
                ((DefaultTableModel)scaleTable.getModel()).setValueAt(String.format("%.5f", 1 + mean/100.0),0,0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (br != null)
                try { br.close(); } catch (IOException e) {}
        }
    }
}
