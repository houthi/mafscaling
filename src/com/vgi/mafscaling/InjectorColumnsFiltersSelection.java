/*
 * Injector tuning column selection
 */
package com.vgi.mafscaling;

import java.awt.event.ActionEvent;

public class InjectorColumnsFiltersSelection extends ColumnsFiltersSelection {
    protected void addColSelection() {
        addRPMColSelection();
        addLoadColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addWidebandAFRColSelection();
        addCommandedAFRColSelection(true);
        addBatteryVoltageColSelection();
        addInjectorPulseWidthColSelection();
        addTimeColSelection();
    }

    protected void addFilterSelection() {
        addCellHitCountMinimumFilter();
    }

    protected boolean validate(StringBuffer error) {
        boolean ret = true;
        String value;
        String colName;

        value = rpmName.getText().trim();
        colName = rpmLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setRpmColumnName(value);

        value = loadName.getText().trim();
        colName = loadLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setLoadColumnName(value);

        value = afLearningName.getText().trim();
        colName = afLearningLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setAfLearningColumnName(value);

        value = afCorrectionName.getText().trim();
        colName = afCorrectionLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setAfCorrectionColumnName(value);

        Config.setWidebandAfrColumnName(wbAfrName.getText().trim());
        Config.setCommandedAfrColumnName(commAfrName.getText().trim());

        value = battVoltName.getText().trim();
        colName = battVoltLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setBatteryVoltageColumnName(value);

        value = injPwName.getText().trim();
        colName = injPwLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setInjectorPulseWidthColumnName(value);

        value = timeName.getText().trim();
        colName = timeLabelText;
        if (value.isEmpty()) { ret = false; error.append("\"").append(colName).append("\" column must be specified\n"); }
        else Config.setTimeColumnName(value);

        Config.setCLMinCellHitCount(Integer.valueOf(minCellHitCountFilter.getText()));

        return ret;
    }

    protected boolean processDefaultButton(ActionEvent e) {
        return false;
    }
}
