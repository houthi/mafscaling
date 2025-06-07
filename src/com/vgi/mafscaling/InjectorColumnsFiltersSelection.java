/*
* Open-Source tuning tools
*/
package com.vgi.mafscaling;

import java.awt.event.ActionEvent;

public class InjectorColumnsFiltersSelection extends ColumnsFiltersSelection {
    protected void addColSelection() {
        addRPMColSelection();
        addLoadColSelection();
        addThrottleAngleColSelection();
        addTimeColSelection();
        addMAFVoltageColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addBatteryVoltageColSelection();
        addInjectorPulseWidthColSelection();
    }

    protected void addFilterSelection() {
        addRPMMaximumFilter();
        maxRPMFilter.setText(String.valueOf(Config.getRPMMaximumValue()));
        addRPMMinimumFilter();
        minRPMFilter.setText(String.valueOf(Config.getRPMMinimumValue()));
        addEngineLoadMinimumFilter();
        minEngineLoadFilter.setText(String.valueOf(Config.getLoadMinimumValue()));
        addDvDtMaximumFilter();
        maxDvdtFilter.setText(String.valueOf(Config.getDvDtMaximumValue()));
        addThrottleChangeMaximumFilter();
        thrtlChangeMaxFilter.setValue(Config.getThrottleChangeMaxValue());
    }

    protected boolean validate(StringBuffer error) {
        boolean ret = true;
        String value;
        value = rpmName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+rpmLabelText+"\" column must be specified\n"); } else Config.setRpmColumnName(value);
        value = loadName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+loadLabelText+"\" column must be specified\n"); } else Config.setLoadColumnName(value);
        value = thrtlAngleName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+thrtlAngleLabelText+"\" column must be specified\n"); } else Config.setThrottleAngleColumnName(value);
        value = timeName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+timeLabelText+"\" column must be specified\n"); } else Config.setTimeColumnName(value);
        value = mafVName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+mafVLabelText+"\" column must be specified\n"); } else Config.setMafVoltageColumnName(value);
        value = afLearningName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+afLearningLabelText+"\" column must be specified\n"); } else Config.setAfLearningColumnName(value);
        value = afCorrectionName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+afCorrectionLabelText+"\" column must be specified\n"); } else Config.setAfCorrectionColumnName(value);
        value = batteryVoltageName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+batteryVoltageLabelText+"\" column must be specified\n"); } else Config.setBatteryVoltageColumnName(value);
        value = injectorPWName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+injectorPWLabelText+"\" column must be specified\n"); } else Config.setInjectorPulseWidthColumnName(value);

        Config.setRPMMaximumValue(Integer.valueOf(maxRPMFilter.getText()));
        Config.setRPMMinimumValue(Integer.valueOf(minRPMFilter.getText()));
        Config.setLoadMinimumValue(Double.valueOf(minEngineLoadFilter.getText()));
        Config.setDvDtMaximumValue(Double.valueOf(maxDvdtFilter.getText()));
        Config.setThrottleChangeMaxValue(Integer.valueOf(thrtlChangeMaxFilter.getValue().toString()));
        return ret;
    }

    protected boolean processDefaultButton(ActionEvent e) {
        if ("maxdvdt".equals(e.getActionCommand()))
            maxDvdtFilter.setText(Config.DefaultDvDtMaximum);
        else if ("thrtlchange".equals(e.getActionCommand()))
            thrtlChangeMaxFilter.setValue(Integer.valueOf(Config.DefaultThrottleChangeMax));
        else
            return false;
        return true;
    }
}
