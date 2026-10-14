package tn.esprit.autoloc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void isNotEmpty_returnsFalseForNullEmptyAndBlank() {
        assertFalse(Utils.isNotEmpty(null));
        assertFalse(Utils.isNotEmpty(""));
        assertFalse(Utils.isNotEmpty("   "));
    }

    @Test
    void isNotEmpty_returnsTrueForRealText() {
        assertTrue(Utils.isNotEmpty("AutoLoc"));
        assertTrue(Utils.isNotEmpty("  a  "));
    }
}
