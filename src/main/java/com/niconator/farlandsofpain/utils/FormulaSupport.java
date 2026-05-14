package com.niconator.farlandsofpain.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public final class FormulaSupport {
    public static final String[] VARIABLES = {
            "x_coord",
            "y_coord",
            "z_coord",
            "dist_from_spawn",
            "horizontal_distance_from_spawn",
            "distance_from_world_origin",
            "vertical_depth",
            "nearest_player_level",
            "nearest_player_health_percent",
            "nearby_player_count",
            "nearby_mob_count",
            "entity_health",
            "entity_max_health",
            "current_day",
            "moon_phase",
            "world_difficulty",
            "local_difficulty"
    };

    private static final Function MIN = new Function("min", 2) {
        @Override
        public double apply(double... args) {
            return Math.min(args[0], args[1]);
        }
    };
    private static final Function MAX = new Function("max", 2) {
        @Override
        public double apply(double... args) {
            return Math.max(args[0], args[1]);
        }
    };
    private static final Function CLAMP = new Function("clamp", 3) {
        @Override
        public double apply(double... args) {
            return clamp(args[0], args[1], args[2]);
        }
    };
    private static final Function ROUND = new Function("round", 1) {
        @Override
        public double apply(double... args) {
            return Math.round(args[0]);
        }
    };
    private static final Function POW = new Function("pow", 2) {
        @Override
        public double apply(double... args) {
            return Math.pow(args[0], args[1]);
        }
    };
    private static final Function LERP = new Function("lerp", 3) {
        @Override
        public double apply(double... args) {
            return args[0] + (args[1] - args[0]) * args[2];
        }
    };
    private static final Function SMOOTHSTEP = new Function("smoothstep", 3) {
        @Override
        public double apply(double... args) {
            if (Double.compare(args[0], args[1]) == 0) {
                return args[2] < args[0] ? 0.0 : 1.0;
            }
            double t = clamp((args[2] - args[0]) / (args[1] - args[0]), 0.0, 1.0);
            return t * t * (3.0 - 2.0 * t);
        }
    };
    private static final Function LOGISTIC = new Function("logistic", 4) {
        @Override
        public double apply(double... args) {
            return args[3] / (1.0 + Math.exp(-args[2] * (args[0] - args[1])));
        }
    };
    private static final Function SELECT = new Function("select", 3) {
        @Override
        public double apply(double... args) {
            return args[0] != 0.0 ? args[1] : args[2];
        }
    };
    private static final Function GT = new Function("gt", 2) {
        @Override
        public double apply(double... args) {
            return args[0] > args[1] ? 1.0 : 0.0;
        }
    };
    private static final Function GTE = new Function("gte", 2) {
        @Override
        public double apply(double... args) {
            return args[0] >= args[1] ? 1.0 : 0.0;
        }
    };
    private static final Function LT = new Function("lt", 2) {
        @Override
        public double apply(double... args) {
            return args[0] < args[1] ? 1.0 : 0.0;
        }
    };
    private static final Function LTE = new Function("lte", 2) {
        @Override
        public double apply(double... args) {
            return args[0] <= args[1] ? 1.0 : 0.0;
        }
    };
    private static final Function EQ = new Function("eq", 2) {
        @Override
        public double apply(double... args) {
            return Double.compare(args[0], args[1]) == 0 ? 1.0 : 0.0;
        }
    };

    private FormulaSupport() {
    }

    public static boolean isValidFormula(String formula) {
        if (formula == null || formula.isBlank()) {
            return false;
        }

        try {
            Expression expression = buildExpression(formula);
            for (String variable : VARIABLES) {
                expression.setVariable(variable, 1.0);
            }
            return Double.isFinite(expression.evaluate());
        } catch (Exception e) {
            return false;
        }
    }

    public static Expression buildExpression(String formula) {
        return new ExpressionBuilder(formula)
                .functions(MIN, MAX, CLAMP, ROUND, POW, LERP, SMOOTHSTEP, LOGISTIC, SELECT, GT, GTE, LT, LTE, EQ)
                .variables(VARIABLES)
                .build();
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int scaleExperience(int originalExperience, double multiplier) {
        if (originalExperience <= 0 || multiplier <= 0.0 || !Double.isFinite(multiplier)) {
            return 0;
        }

        double scaledExperience = Math.floor(originalExperience * multiplier);
        if (scaledExperience >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) scaledExperience;
    }
}
