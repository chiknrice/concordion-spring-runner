package org.chiknrice.test.spec.nested;

import org.chiknrice.test.spec.BaseFixture;
import org.chiknrice.test.spec.autoscan.ScannedClass;
import org.springframework.beans.factory.annotation.Autowired;

public class NestedSpecFixture extends BaseFixture {

    @Autowired
    private ScannedClass scannedClass;

    public ScannedClass getScannedClass() {
        return scannedClass;
    }

}
