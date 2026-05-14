package com.niconator.farlandsofpain.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {
    @Test
    void acceptsValidFormulaWithSupportedVariablesAndFunctions() {
        assertTrue(FormulaSupport.isValidFormula("clamp((dist_from_spawn / 750) + max(y_coord, 1), 0.75, 10)"));
    }

    @Test
    void acceptsNewFormulaVariablesAndCurveFunctions() {
        assertTrue(FormulaSupport.isValidFormula("lerp(1, 8, smoothstep(0, 10000, distance_from_world_origin)) + local_difficulty + nearby_player_count + entity_max_health"));
    }

    @Test
    void acceptsConditionalFormulaHelpers() {
        assertTrue(FormulaSupport.isValidFormula("select(gt(vertical_depth, 64), pow(2, 3), logistic(current_day, 20, 0.2, 5))"));
    }

    @Test
    void rejectsUnknownVariables() {
        assertFalse(FormulaSupport.isValidFormula("missing_variable + 1"));
    }

    @Test
    void rejectsNonFiniteResults() {
        assertFalse(FormulaSupport.isValidFormula("1 / 0"));
    }

    @Test
    void rejectsBlankFormula() {
        assertFalse(FormulaSupport.isValidFormula("   "));
    }

    @Test
    void scalesExperienceByFlooringMultipliedValue() {
        assertEquals(17, FormulaSupport.scaleExperience(5, 3.5));
    }

    @Test
    void scalesExperienceToZeroForInvalidOrNegativeValues() {
        assertEquals(0, FormulaSupport.scaleExperience(5, -1.0));
        assertEquals(0, FormulaSupport.scaleExperience(5, Double.NaN));
        assertEquals(0, FormulaSupport.scaleExperience(0, 10.0));
    }

    @Test
    void capsExperienceAtIntegerMaximum() {
        assertEquals(Integer.MAX_VALUE, FormulaSupport.scaleExperience(Integer.MAX_VALUE, 2.0));
    }
}
