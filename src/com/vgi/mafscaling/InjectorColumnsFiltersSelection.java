/*
* Open-Source tuning tools
*/
package com.vgi.mafscaling;

import java.awt.event.ActionEvent;

public class InjectorColumnsFiltersSelection extends ColumnsFiltersSelection {
    private static final String batteryVoltageLabelText = "Battery Voltage";
    private static final String injectorPWLabelText = "Injector Pulse Width";


    protected void addColSelection() {
        addRPMColSelection();
        addLoadColSelection();
        addAFLearningColSelection();
        addAFCorrectionColSelection();
        addWidebandAFRColSelection();
        addCommandedAFRColSelection(true);
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
    }

    protected boolean validate(StringBuffer error) {
        boolean ret = true;
        String value;
        value = rpmName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+rpmLabelText+"\" column must be specified\n"); } else Config.setRpmColumnName(value);
        value = loadName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+loadLabelText+"\" column must be specified\n"); } else Config.setLoadColumnName(value);
        value = afLearningName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+afLearningLabelText+"\" column must be specified\n"); } else Config.setAfLearningColumnName(value);
        value = afCorrectionName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+afCorrectionLabelText+"\" column must be specified\n"); } else Config.setAfCorrectionColumnName(value);
        value = wbAfrName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+wbAfrLabelText+"\" column must be specified\n"); } else Config.setWidebandAfrColumnName(value);
        Config.setCommandedAfrColumnName(commAfrName.getText().trim());
        value = batteryVoltageName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+batteryVoltageLabelText+"\" column must be specified\n"); } else Config.setBatteryVoltageColumnName(value);
        value = injectorPWName.getText().trim();
        if (value.isEmpty()) { ret = false; error.append("\""+injectorPWLabelText+"\" column must be specified\n"); } else Config.setInjectorPulseWidthColumnName(value);

        Config.setRPMMaximumValue(Integer.valueOf(maxRPMFilter.getText()));
        Config.setRPMMinimumValue(Integer.valueOf(minRPMFilter.getText()));
        Config.setLoadMinimumValue(Double.valueOf(minEngineLoadFilter.getText()));
        return ret;
    }

    protected boolean processDefaultButton(ActionEvent e) {
        return false;
    }
}
